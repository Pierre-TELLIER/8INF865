package com.INF865.izondevices.scanner

import com.INF865.izondevices.model.NetworkDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

data class PortScanProgress(
    val openPorts: List<Int>,
    val scannedCount: Int,
    val remainingCount: Int
)

const val PROGRESS_STEP = 100
val ALL_PORTS = 1..65535
val QUICK_PORTS = listOf(21, 22, 23, 25, 53, 80, 110, 143, 443, 445, 3360, 3389)

suspend fun scanPortDevice(device: NetworkDevice): Sequence<Int> {
    return withContext(Dispatchers.IO) {
        scanAllPorts(device)
    }
}

suspend fun scanPortDeviceProgressive(
    device: NetworkDevice,
    ports: List<Int>,
    onProgress: (PortScanProgress) -> Unit
): List<Int> {
    return withContext(Dispatchers.IO) {
        val openPorts = mutableListOf<Int>()

        var count = 0
        val lastPort = ports.lastOrNull() ?: 0
        for (port in ports) {
            if (isPortOpen(device.ipAddress, port, PING_TIMEOUT_MS)) {
                openPorts.add(port)
            }

            count++
            val shouldEmit = count % PROGRESS_STEP == 0 || port == lastPort || openPorts.lastOrNull() == port
            if (shouldEmit) {
                onProgress(
                    PortScanProgress(
                        openPorts = openPorts.toList(),
                        scannedCount = count,
                        remainingCount = ports.size - count
                    )
                )
            }
        }

        openPorts
    }
}

private fun scanAllPorts(device: NetworkDevice): Sequence<Int> = sequence {
    for (port in ALL_PORTS) {
        if (isPortOpen(device.ipAddress, port, PING_TIMEOUT_MS)) {
            yield(port)
        }
    }
}

private fun isPortOpen(host: String, port: Int, timeout: Int): Boolean {
    return runCatching {
        Socket().use { socket ->
            socket.connect(InetSocketAddress(host, port), timeout)
            socket.close()
            true
        }
    }.getOrDefault(false)
}