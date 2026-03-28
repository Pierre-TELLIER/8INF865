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

const val FIRST_PORT = 1
const val LAST_PORT = 65535
const val TOTAL_PORTS = LAST_PORT - FIRST_PORT + 1
const val PROGRESS_STEP = 100

suspend fun scanPortDevice(device: NetworkDevice): Sequence<Int> {
    return withContext(Dispatchers.IO) {
        scanAllPorts(device)
    }
}

suspend fun scanPortDeviceProgressive(
    device: NetworkDevice,
    onProgress: (PortScanProgress) -> Unit
): List<Int> {
    return withContext(Dispatchers.IO) {
        val openPorts = mutableListOf<Int>()

        for (port in FIRST_PORT..LAST_PORT) {
            if (isPortOpen(device.ipAddress, port, PING_TIMEOUT_MS)) {
                openPorts.add(port)
            }

            val scannedCount = port - FIRST_PORT + 1
            val shouldEmit = scannedCount % PROGRESS_STEP == 0 || port == LAST_PORT || openPorts.lastOrNull() == port
            if (shouldEmit) {
                onProgress(
                    PortScanProgress(
                        openPorts = openPorts.toList(),
                        scannedCount = scannedCount,
                        remainingCount = TOTAL_PORTS - scannedCount
                    )
                )
            }
        }

        openPorts
    }
}

private fun scanAllPorts(device: NetworkDevice): Sequence<Int> = sequence {
    for (port in FIRST_PORT..LAST_PORT) {
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