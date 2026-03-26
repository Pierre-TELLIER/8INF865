package com.INF865.izondevices.model

data class NetworkDevice(
    val ipAddress: String,
    val macAddress: String? = null,
    val hostname: String? = null
)
