package com.INF865.izondevices.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.INF865.izondevices.model.NetworkDevice
import com.INF865.izondevices.scanner.scanPortDevice
import com.INF865.izondevices.scanner.scanPortDeviceProgressive
import com.INF865.izondevices.scanner.PortScanProgress
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PortScanService : Service() {
    private val binder = LocalBinder()
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent): IBinder = binder

    override fun onDestroy() {
        executor.shutdownNow()
        runCatching { executor.awaitTermination(3, TimeUnit.SECONDS) }
        super.onDestroy()
    }

    fun scanPorts(device: NetworkDevice): CompletableFuture<List<Int>> {
        return CompletableFuture.supplyAsync(
            {
                runBlocking {
                    scanPortDevice(device).toList()
                }
            },
            executor
        )
    }

    fun scanPortsProgressive(
        device: NetworkDevice,
        ports: List<Int>,
        onProgress: (PortScanProgress) -> Unit
    ): CompletableFuture<List<Int>> {
        return CompletableFuture.supplyAsync(
            {
                runBlocking {
                    scanPortDeviceProgressive(device, ports) { progress ->
                        mainHandler.post { onProgress(progress) }
                    }
                }
            },
            executor
        )
    }

    inner class LocalBinder : Binder() {
        fun getService(): PortScanService = this@PortScanService
    }
}

