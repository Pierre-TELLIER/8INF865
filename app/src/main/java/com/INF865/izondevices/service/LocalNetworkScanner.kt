package com.INF865.izondevices.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.LinkAddress
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.IBinder
import com.INF865.izondevices.R
import com.INF865.izondevices.model.Network
import com.INF865.izondevices.model.NetworkDevice
import com.INF865.izondevices.scanner.*
import java.io.Closeable
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class LocalNetworkScanner(private val context: Context) : Closeable {
    private val coordinatorExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val probeExecutor: ExecutorService = Executors.newFixedThreadPool(PROBE_THREADS)
    private val ouiLines: List<String> by lazy { loadOuiLines() }

    fun scanNetwork(): CompletableFuture<Network?> {
        return CompletableFuture.supplyAsync(
            {
                // get subnet
                val subnet = getActiveSubnetInfo() ?: return@supplyAsync null
                // ip of the phone
                val ownIpAddress = subnet.address.hostAddress.orEmpty()

                // create list of IPs to scan from network phone's address and number of bits for network address
                val candidates = buildCandidateIps(subnet.address, subnet.prefixLength)
                    .asSequence()
                    // remove phone's ip address
                    .filter { it != ownIpAddress }
                    // take a subset of IPs
                    .take(MAX_HOSTS)
                    .toList()

                // create task to check reachable IPs
                val probeTasks = candidates.map { hostIp ->
                    probeExecutor.submit<Pair<String, Pair<Boolean, String?>>> {
                        // create inet instance
                        val inet = InetAddress.getByName(hostIp)
                        // capture and throw away an error
                        hostIp to runCatching {
                            // check if the hostIp is reachable
                            // under PROBE_TIMEOUT_MS milliseconds
                            if (inet.isReachable(PROBE_TIMEOUT_MS)) {
                                // if it is return Pair<true, hostname>
                                true to inet.hostName
                            } else {
                                // or Pair<false, null>
                                false to null
                            }
                        }.getOrDefault(false to null) // any error case
                    }
                }

                val reachableDevices = mutableListOf<NetworkDevice>()

                // execute tasks defined earlier
                probeTasks.forEach { task ->
                    runCatching {
                        // execute task and kill it if it takes more
                        // than twice the time to check if a host is
                        // reachable or not
                        val (ipAddress, data) =
                            task.get(PROBE_TIMEOUT_MS.toLong() * 2, TimeUnit.MILLISECONDS)
                        // deconstruct data
                        val (reachable, hostName) = data
                        // add host to a Map
                        if (reachable) {
                            reachableDevices += NetworkDevice(ipAddress, hostName = hostName)
                        }
                    }
                }

                // after every request, read ARP table
                val arpByIp = readArpTable()

                // for every reachable IP, get MAC @ from ARP table
                for (device in reachableDevices) {
                    val mac = arpByIp[device.ipAddress]
                    device.macAddress = mac
                }

                val macInfoTasks = reachableDevices.map { device ->
                    probeExecutor.submit<Unit> {
                        try {
                            if ((device.macAddress ?: INVALID_MAC) == INVALID_MAC) {
                                return@submit
                            }
                            device.constructor = findOuiLineForMac(device.macAddress)
                        } catch (e: TimeoutException) {
                            e.printStackTrace()
                        }
                    }
                }

                macInfoTasks.forEach {
                    it.get(
                        PROBE_TIMEOUT_MS.toLong() * 4,
                        TimeUnit.MILLISECONDS
                    )
                }

                val ownMacAddress = getOwnMacAddress(ownIpAddress)
                reachableDevices += NetworkDevice(
                    ipAddress = ownIpAddress,
                    macAddress = ownMacAddress,
                    hostName = getHostnameFromNetwork(),
                    constructor = findOuiLineForMac(ownMacAddress)
                )

                Network(
                    networkAddress = subnet.address.hostAddress.orEmpty(),
                    networkName = this.getWifiSSID(),
                    devices = reachableDevices.sortedBy { ipAsSortableNumber(it.ipAddress) }
                )
            },
            coordinatorExecutor
        )
    }

    override fun close() {
        probeExecutor.shutdownNow()
        coordinatorExecutor.shutdownNow()
        runCatching {
            probeExecutor.awaitTermination(TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            coordinatorExecutor.awaitTermination(TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        }
    }

    private fun getOwnMacAddress(ip: String): String? {
        return runCatching {
            execCommand("ifconfig | grep -B 1 ${ip} | head -n 1 | awk -F' ' '{ print \$5 }'")
                .firstOrNull()
        }.getOrNull()
    }

    private fun getWifiSSID(): String? {
        return runCatching {
                val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                return wifiManager.connectionInfo?.ssid?.replace("\"", "")
        }.getOrNull()
    }

    private fun getActiveSubnetInfo(): SubnetInfo? {
        // get connectivity service (wifi, etc)
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // get the active network
        val activeNetwork = runCatching { connectivityManager.activeNetwork }.getOrNull()
            ?: return fallbackSubnetInfo()
        // get properties of the active network
        val linkProperties =
            runCatching { connectivityManager.getLinkProperties(activeNetwork) }.getOrNull()
                ?: return fallbackSubnetInfo()

        // get the link, information of the phone's interface
        val linkAddress = linkProperties.linkAddresses.firstOrNull { isUsableIpv4LinkAddress(it) }
            ?: return fallbackSubnetInfo()

        return SubnetInfo(
            address = linkAddress.address as Inet4Address,
            prefixLength = linkAddress.prefixLength
        )
    }

    private fun fallbackSubnetInfo(): SubnetInfo? {
        return NetworkInterface.getNetworkInterfaces()
            ?.toList()
            ?.flatMap { it.inetAddresses.toList() }
            ?.firstOrNull { it is Inet4Address && !it.isLoopbackAddress }
            ?.let {
                SubnetInfo(
                    address = it as Inet4Address,
                    prefixLength = DEFAULT_PREFIX_LENGTH
                )
            }
    }

    private fun buildCandidateIps(localAddress: Inet4Address, prefixLength: Int): List<String> {
        // weird case, no subnet ?!
        if (prefixLength !in 1..30) {
            return emptyList()
        }

        // transform local pointed decimal ip to int value
        val addressInt = inetAddressToInt(localAddress)
        // construct mask from prefix
        // 255.255.255.0 is the prefix for /24 (example :))
        // used by network 192.168.1.0 fore xample
        val mask = (-1 shl (32 - prefixLength))
        // so address & mask is network address
        // let a device with address 192.168.1.204
        // 192.168.1.204 & 255.255.255.0 = 192.168.1.0
        val network = addressInt and mask
        // broadcast is network address | ~mask
        // ~mask = 0.0.0.255, therefore broadcast is
        // 192.168.1.255
        val broadcast = network or mask.inv()

        val addresses = mutableListOf<String>()
        for (host in (network + 1) until broadcast) {
            // retransform int value to pointed decimal ip
            addresses.add(intToIpv4(host))
        }
        return addresses
    }

    private fun resolveOwnMacAddress(localAddress: Inet4Address): String? {
        return NetworkInterface.getByInetAddress(localAddress)?.hardwareAddress?.joinToString(":") {
            "%02X".format(it)
        }
    }

    fun getHostnameFromNetwork(): String? {
        return runCatching {
            NetworkInterface.getNetworkInterfaces()
                .toList()
                .flatMap { it.inetAddresses.toList() }
                .firstOrNull { !it.isLoopbackAddress && it is java.net.Inet4Address }
                ?.hostName
        }.getOrNull()
    }

    /**
     * Root function to get entries from ARP table to resolve MAC addresses
     *
     * @return Map of IP addresses to MAC addresses
     */
    private fun readArpTable(): Map<String, String> {
        return runCatching {
            // if rooted this can work
            execCommand("cat /proc/net/arp")
                // drop first line
                .drop(1)
                // split column separated by at least one space
                .map { it.trim().split(Regex("\\s+")) }
                // get only lines that have more than 4 chars
                .filter { it.size >= 4 }
                // get value from first and fourth column
                // IP address and MAC address
                // check if MAC address is valid or not
                .mapNotNull { columns ->
                    val ip = columns[0]
                    val mac = columns[3]
                    if (isValidMac(mac)) ip to mac.uppercase() else null
                }
                .toMap()
        }.getOrDefault(emptyMap())
    }

    private fun findOuiLineForMac(macAddress: String?): String? {
        val ouiPrefix = macAddress
            ?.replace(":", "")
            ?.uppercase()
            ?.take(6)
            ?: return null

        return ouiLines.firstOrNull { line ->
            val firstSpace = line.indexOf(' ')
            if (firstSpace <= 0) {
                false
            } else {
                val ouiPart = line.substring(0, firstSpace)
                ouiPart.split(',').any { it.trim().uppercase() == ouiPrefix }
            }
        }?.split(" ")[1]
    }

    private fun loadOuiLines(): List<String> {
        return context.resources.openRawResource(R.raw.oui).bufferedReader()
            .use { it.readLines().filterNotNull() }
    }

    private fun isUsableIpv4LinkAddress(linkAddress: LinkAddress): Boolean {
        val address = linkAddress.address
        return address is Inet4Address && !address.isLoopbackAddress
    }

    private fun isValidMac(macAddress: String): Boolean {
        return macAddress.matches(Regex("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$")) &&
                macAddress != INVALID_MAC
    }

    private fun inetAddressToInt(address: Inet4Address): Int {
        val bytes = address.address
        return ((bytes[0].toInt() and 0xFF) shl 24) or
                ((bytes[1].toInt() and 0xFF) shl 16) or
                ((bytes[2].toInt() and 0xFF) shl 8) or
                (bytes[3].toInt() and 0xFF)
    }

    private fun intToIpv4(value: Int): String {
        return listOf(
            value ushr 24 and 0xFF,
            value ushr 16 and 0xFF,
            value ushr 8 and 0xFF,
            value and 0xFF
        ).joinToString(".")
    }

    private fun ipAsSortableNumber(ipAddress: String): Long {
        val parts = ipAddress.split(".")
        if (parts.size != 4) return Long.MAX_VALUE

        var result = 0L
        for (part in parts) {
            val octet = part.toIntOrNull() ?: return Long.MAX_VALUE
            result = (result shl 8) + octet
        }
        return result
    }

    private data class SubnetInfo(
        val address: Inet4Address,
        val prefixLength: Int
    )

}

class NetworkScanService : Service() {
    private val binder = LocalBinder()
    private lateinit var scanner: LocalNetworkScanner

    override fun onCreate() {
        super.onCreate()
        scanner = LocalNetworkScanner(applicationContext)
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onDestroy() {
        scanner.close()
        super.onDestroy()
    }

    fun scanNetwork(): CompletableFuture<Network?> {
        return scanner.scanNetwork()
    }

    inner class LocalBinder : Binder() {
        fun getService(): NetworkScanService = this@NetworkScanService
    }
}
