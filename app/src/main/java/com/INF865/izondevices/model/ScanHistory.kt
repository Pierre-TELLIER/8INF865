package com.INF865.izondevices.model

import kotlinx.serialization.Serializable

@Serializable
data class ScanHistory(val scans: MutableList<Scan>) {
}