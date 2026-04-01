package com.INF865.izondevices.model.serialization

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.INF865.izondevices.model.Scan
import com.INF865.izondevices.model.ScanHistory
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object ScanHistorySerializer : Serializer<ScanHistory> {

    override val defaultValue: ScanHistory = ScanHistory(mutableListOf<Scan>())

    override suspend fun readFrom(input: InputStream): ScanHistory =
        try {
            Json.decodeFromString<ScanHistory>(
                input.readBytes().decodeToString()
            )
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read the scan history", serialization)
        }

    override suspend fun writeTo(t: ScanHistory, output: OutputStream) {
        output.write(
            Json.encodeToString(t)
                .encodeToByteArray()
        )
    }
}