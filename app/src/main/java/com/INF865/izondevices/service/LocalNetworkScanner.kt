package com.INF865.izondevices.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.LinkAddress
import android.os.Binder
import android.os.IBinder
import com.INF865.izondevices.model.NetworkDevice
import java.io.Closeable
import java.io.File
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LocalNetworkScanner(private val context: Context) : Closeable {
    private val coordinatorExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val probeExecutor: ExecutorService = Executors.newFixedThreadPool(PROBE_THREADS)

    fun scanNetwork(): CompletableFuture<List<NetworkDevice>> {
        return CompletableFuture.supplyAsync(
            {
                // get subnet
                val subnet = getActiveSubnetInfo() ?: return@supplyAsync emptyList()
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

                // get reachable IPs
                val probeTasks = candidates.map { hostIp ->
                    probeExecutor.submit<Pair<String, Boolean>> {
                        hostIp to runCatching {
                            InetAddress.getByName(hostIp).isReachable(PROBE_TIMEOUT_MS)
                        }.getOrDefault(false)
                    }
                }

                val arpByIp = readArpTable()
                val reachableIps = mutableMapOf<String, String?>()

                probeTasks.forEach { task ->
                    runCatching {
                        val (ipAddress, reachable) =
                            task.get(PROBE_TIMEOUT_MS.toLong() * 2, TimeUnit.MILLISECONDS)
                        if (reachable) {
                            val macAddress = arpByIp[ipAddress]
                            reachableIps[ipAddress] = macAddress
                        }
                    }
                }


                val discoveredByIp = linkedMapOf<String, NetworkDevice>()
                discoveredByIp[ownIpAddress] = NetworkDevice(
                    ipAddress = ownIpAddress,
                    macAddress = resolveOwnMacAddress(subnet.address),
                    hostname = subnet.address.hostName
                )

                // sort by ip address
                val remoteIps = reachableIps.keys
                    .asSequence()
                    .filter { it != ownIpAddress }
                    .sortedBy { ipAsSortableNumber(it) }
                    .toList()

                // map collected information to a class
                remoteIps.forEach { ip ->
                    discoveredByIp[ip] = NetworkDevice(
                        ipAddress = ip,
                        macAddress = reachableIps[ip],
                        hostname = resolveHostName(ip)
                    )
                }

                discoveredByIp.values.toList()
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

    private fun getActiveSubnetInfo(): SubnetInfo? {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return fallbackSubnetInfo()
        val linkProperties = connectivityManager.getLinkProperties(activeNetwork) ?: return fallbackSubnetInfo()

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
            ?.asSequence()
            ?.flatMap { it.inetAddresses.toList().asSequence() }
            ?.firstOrNull { it is Inet4Address && !it.isLoopbackAddress }
            ?.let {
                SubnetInfo(
                    address = it as Inet4Address,
                    prefixLength = DEFAULT_PREFIX_LENGTH
                )
            }
    }

    private fun buildCandidateIps(localAddress: Inet4Address, prefixLength: Int): List<String> {
        if (prefixLength !in 1..30) {
            return emptyList()
        }

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
            addresses.add(intToIpv4(host))
        }
        return addresses
    }

    private fun resolveOwnMacAddress(localAddress: Inet4Address): String? {
        return NetworkInterface.getByInetAddress(localAddress)?.hardwareAddress?.joinToString(":") {
            "%02X".format(it)
        }
    }

    private fun readArpTable(): Map<String, String> {
        val arpTable = File(ARP_TABLE_PATH)
        if (!arpTable.exists() || !arpTable.canRead()) return emptyMap()

        return runCatching {
            arpTable.bufferedReader().use { reader ->
                reader.lineSequence()
                    .drop(1)
                    .map { it.trim().split(Regex("\\s+")) }
                    .filter { it.size >= 4 }
                    .mapNotNull { columns ->
                        val ip = columns[0]
                        val mac = columns[3]
                        if (isValidMac(mac)) ip to mac else null
                    }
                    .toMap()
            }
        }.getOrDefault(emptyMap())
    }

    private fun resolveHostName(ipAddress: String): String? {
        return runCatching {
            InetAddress.getByName(ipAddress).canonicalHostName
        }.getOrNull()?.takeUnless { it == ipAddress }
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

    private companion object {
        const val PROBE_TIMEOUT_MS = 250
        const val PROBE_THREADS = 32
        const val DEFAULT_PREFIX_LENGTH = 24
        const val MAX_HOSTS = 512
        const val TERMINATION_TIMEOUT_SECONDS = 1L
        const val ARP_TABLE_PATH = "/proc/net/arp"
        const val INVALID_MAC = "00:00:00:00:00:00"
    }
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

    fun scanNetwork(): CompletableFuture<List<NetworkDevice>> {
        return scanner.scanNetwork()
    }

    inner class LocalBinder : Binder() {
        fun getService(): NetworkScanService = this@NetworkScanService
    }
}
