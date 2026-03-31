package com.INF865.izondevices.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkDevice(
    val ipAddress: String,
    var macAddress: String? = null,
    val hostName: String? = null,
    var constructor: String? = null,
    var model: String? = null,
    var openPorts: List<Int> = emptyList(),
)
