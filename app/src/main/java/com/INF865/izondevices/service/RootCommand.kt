package com.INF865.izondevices.service

import java.io.DataOutputStream

/**
 * Execute a command as root
 *
 * @param command the command to execute
 * @return sequence of lines of output
 */
fun execCommand(command: String): Sequence<String> {
    // open a shell as a root user
    val suProcess = Runtime.getRuntime().exec("su")
    // get output stream (from application view)
    // so its the input of the command
    val data = DataOutputStream(suProcess.outputStream)

    // write shell command : `COMMAND\nexit\n`
    // as a human would write
    // exit is mandatory to close the shell
    data.writeBytes("$command\n")
    data.writeBytes("exit\n")
    // send all data
    data.flush()

    // get output line by line
    val output = suProcess.inputStream.bufferedReader().lineSequence()
    // wait exiting process
    suProcess.waitFor()

    return output
}