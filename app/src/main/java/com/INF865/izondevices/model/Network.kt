package com.INF865.izondevices.model

data class Network(
    val networkAddress: String,
    val networkName: String?,
    val devices: List<NetworkDevice>
)
