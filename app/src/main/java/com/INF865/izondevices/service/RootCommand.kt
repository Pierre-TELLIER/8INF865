package com.INF865.izondevices.service

import java.io.DataOutputStream

fun execCommand(command: String): Sequence<String> {
    val suProcess = Runtime.getRuntime().exec("su")
    val data = DataOutputStream(suProcess.outputStream)

    data.writeBytes("$command\n")
    data.writeBytes("exit\n")
    data.flush()

    val output = suProcess.inputStream.bufferedReader().lineSequence()
    suProcess.waitFor()

    return output
}