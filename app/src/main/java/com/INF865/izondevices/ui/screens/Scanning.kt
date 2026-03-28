package com.INF865.izondevices.ui.screens

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import com.INF865.izondevices.R
import com.INF865.izondevices.model.Network
import com.INF865.izondevices.model.NetworkDevice
import com.INF865.izondevices.service.NetworkScanService
import com.INF865.izondevices.ui.theme.border_thickness
import com.INF865.izondevices.ui.theme.button_height
import com.INF865.izondevices.ui.theme.extra_small_space
import com.INF865.izondevices.ui.theme.giant_space
import com.INF865.izondevices.ui.theme.large_space
import com.INF865.izondevices.ui.theme.large_text
import com.INF865.izondevices.ui.theme.medium_large_text
import com.INF865.izondevices.ui.theme.medium_small_text
import com.INF865.izondevices.ui.theme.medium_space
import com.INF865.izondevices.ui.theme.medium_text
import com.INF865.izondevices.ui.theme.mega_space
import com.INF865.izondevices.ui.theme.small_medium_space
import com.INF865.izondevices.ui.theme.tiny_space
import java.util.concurrent.CompletableFuture

@Composable
fun ScanScreen(
    modifier: Modifier = Modifier, 
    onCancel: () -> Unit = {},
    onScanFinished: (Network) -> Unit = {}
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val requiredPermissions = remember {
        arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
        )
    }
    var isScanning by remember { mutableStateOf(!isPreview) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var network by remember { mutableStateOf<Network?>(null) }
    var permissionsGranted by remember {
        mutableStateOf(hasRequiredPermissions(context, requiredPermissions))
    }
    val activeScan = remember { mutableStateOf<CompletableFuture<Network?>?>(null) }

    // check if permissions have been given
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grantResults ->
        permissionsGranted = requiredPermissions.all { permission ->
            grantResults[permission] == true ||
                    ContextCompat.checkSelfPermission(
                        context,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED
        }
        if (!permissionsGranted) {
            isScanning = false
            errorMessage = "Internet and network state permissions are required"
        }
    }

    LaunchedEffect(isPreview, permissionsGranted) {
        if (!isPreview && !permissionsGranted) {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    DisposableEffect(context, isPreview, permissionsGranted) {
        if (isPreview) {
            network = Network(
                "192.168.1.0", "Local network", listOf(
                    NetworkDevice(ipAddress = "192.168.1.10", macAddress = "AA:BB:CC:DD:EE:01", hostname = "Printer"),
                    NetworkDevice(ipAddress = "192.168.1.20", macAddress = "AA:BB:CC:DD:EE:02", hostname = "Laptop")
                )
            )
            isScanning = false
            onDispose { }
        } else if (!permissionsGranted) {
            isScanning = false
            onDispose { }
        } else {
            var isBound = false
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as? NetworkScanService.LocalBinder ?: return
                    val networkService = binder.getService()
                    isScanning = true
                    errorMessage = null

                    val future = networkService.scanNetwork()
                    activeScan.value = future
                    future.whenComplete { result, throwable ->
                        Handler(Looper.getMainLooper()).post {
                            isScanning = false
                            if (throwable != null && !future.isCancelled) {
                                errorMessage = throwable.message ?: "Scan failed"
                            } else if (!future.isCancelled) {
                                network = result
                            }
                        }
                    }
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    isBound = false
                }
            }

            val serviceIntent = Intent(context, NetworkScanService::class.java)
            isBound = context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
            if (!isBound) {
                isScanning = false
                errorMessage = "Unable to bind scanner service"
            }

            onDispose {
                activeScan.value?.cancel(true)
                if (isBound) {
                    runCatching { context.unbindService(connection) }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(medium_space),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = Color.DarkGray,
            shape = RoundedCornerShape(extra_small_space)
        ) {
            Text(
                text = if (isScanning) stringResource(id = R.string.scan_in_progress) else "Scan complete",
                color = Color.White,
                modifier = Modifier.padding(
                    horizontal = large_space,
                    vertical = small_medium_space
                ),
                fontSize = medium_large_text
            )
        }

        Spacer(modifier = Modifier.height(giant_space))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (isScanning) {
                CircularProgressIndicator(color = Color.Gray, strokeWidth = tiny_space)
            } else {
                Text(
                    text = "${network?.devices?.size ?: 0} appareils",
                    color = Color.Gray,
                    fontSize = large_text,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(mega_space))

        Button(
            onClick = {
                if (!isScanning && network != null) {
                    onScanFinished(network!!)
                } else {
                    activeScan.value?.cancel(true)
                    onCancel()
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(button_height),
            shape = RoundedCornerShape(small_medium_space),
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
        ) {
            Text(
                text = if (isScanning) stringResource(id = R.string.cancel) else stringResource(R.string.details),
                color = Color.Black,
                fontSize = medium_text,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxWidth()) {
            if (errorMessage != null) {
                ScanStatusItem(text = errorMessage.orEmpty(), backgroundColor = Color(0xFF9C2F2F))
            } else if (network?.devices?.isEmpty() ?: true && !isScanning) {
                ScanStatusItem(text = "No devices found")
            } else {
                network?.devices?.forEach { device ->
                    val macLabel = device.macAddress ?: "Unknown MAC"
                    ScanStatusItem(text = "${device.ipAddress} - $macLabel (${device.hostname ?: "No hostname"})")
                }
            }
        }
    }
}

fun hasRequiredPermissions(context: Context, permissions: Array<String>): Boolean {
    return permissions.all { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun ScanStatusItem(
    modifier: Modifier = Modifier,
    text: String,
    backgroundColor: Color = Color.Gray
) {
    Surface(
        color = backgroundColor,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = border_thickness)
    ) {
        Text(
            text = text,
            color = Color.White,
            modifier = Modifier.padding(horizontal = medium_space, vertical = extra_small_space),
            fontSize = medium_small_text
        )
    }
}