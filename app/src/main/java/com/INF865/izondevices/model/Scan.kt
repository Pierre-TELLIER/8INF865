package com.INF865.izondevices.model

import com.INF865.izondevices.model.serialization.DateSerializer
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class Scan(
    val scannedNetwork: Network,
    val date: @Serializable(with = DateSerializer::class) Date
) {
    companion object {

        fun fromNow(network: Network): Scan {
            return Scan(network, Date())
        }

    }
}