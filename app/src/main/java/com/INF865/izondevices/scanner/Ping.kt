package com.INF865.izondevices.scanner

import com.INF865.izondevices.model.NetworkDevice
import java.net.InetAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun pingDevice(device: NetworkDevice): Boolean {
    return withContext(Dispatchers.IO) {
        runCatching {
            InetAddress.getByName(device.ipAddress).isReachable(PROBE_TIMEOUT_MS)
        }.getOrDefault(false)
    }
}