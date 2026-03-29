package com.INF865.izondevices.model

import kotlinx.serialization.Serializable

@Serializable
data class Network(
    val networkAddress: String,
    val networkName: String?,
    val devices: List<NetworkDevice>
)
