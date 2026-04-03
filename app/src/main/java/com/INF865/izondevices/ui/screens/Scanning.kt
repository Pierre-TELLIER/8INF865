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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.INF865.izondevices.model.Scan
import com.INF865.izondevices.service.NetworkScanService
import com.INF865.izondevices.ui.theme.CoralRed40
import com.INF865.izondevices.ui.theme.CoralRed80Background
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
import com.INF865.izondevices.ui.theme.CoralRedDark
import com.INF865.izondevices.ui.theme.CoralRedSelectedBackground
import com.INF865.izondevices.ui.theme.GreenVulnerabilities
import java.util.concurrent.CompletableFuture

@Composable
fun ScanScreen(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit = {},
    onScanFinished: (Scan) -> Unit = {}
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val requiredPermissions = remember {
        arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }
    
    var isScanning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var scan by remember { mutableStateOf<Scan?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    val activeScan = remember { mutableStateOf<CompletableFuture<Network?>?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grantResults ->
        val allGranted = requiredPermissions.all { 
            grantResults[it] == true || ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            isScanning = true
        } else {
            showPermissionDialog = true
        }
    }

    // Trigger permission check on entry
    LaunchedEffect(Unit) {
        if (!isPreview) {
            if (hasRequiredPermissions(context, requiredPermissions)) {
                isScanning = true
            } else {
                permissionLauncher.launch(requiredPermissions)
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false; onCancel() },
            title = { Text(stringResource(R.string.permission_error_title)) },
            text = { Text(stringResource(R.string.permission_error_message)) },
            confirmButton = {
                TextButton(onClick = { 
                    showPermissionDialog = false
                    permissionLauncher.launch(requiredPermissions)
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showPermissionDialog = false
                    onCancel()
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    DisposableEffect(context, isPreview, isScanning) {
        if (isPreview) {
            scan = Scan.fromNow(
                Network(
                    "192.168.1.0", "Local network", listOf(
                        NetworkDevice(
                            ipAddress = "192.168.1.10",
                            macAddress = "AA:BB:CC:DD:EE:01",
                            hostName = "Printer"
                        ),
                        NetworkDevice(
                            ipAddress = "192.168.1.20",
                            macAddress = "AA:BB:CC:DD:EE:02",
                            hostName = "Laptop"
                        )
                    )
                )
            )
            onDispose { }
        } else if (!isScanning) {
            onDispose { }
        } else {
            var isBound = false
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as? NetworkScanService.LocalBinder ?: return
                    val networkService = binder.getService()
                    errorMessage = null

                    val future = networkService.scanNetwork()
                    activeScan.value = future
                    future.whenComplete { result, throwable ->
                        Handler(Looper.getMainLooper()).post {
                            isScanning = false
                            if (throwable != null && !future.isCancelled || result == null) {
                                errorMessage = throwable.message ?: "Scan failed"
                            } else if (!future.isCancelled) {
                                scan = Scan.fromNow(result)
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
            color = if (isScanning) CoralRedDark else GreenVulnerabilities,
            shape = RoundedCornerShape(extra_small_space)
        ) {
            Text(
                text = if (isScanning) stringResource(id = R.string.scan_in_progress) else stringResource(R.string.scan_completed),
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
                CircularProgressIndicator(color = CoralRed40, strokeWidth = tiny_space)
            } else {
                Text(
                    text = "${scan?.scannedNetwork?.devices?.size ?: 0} appareils",
                    color = Color.DarkGray,
                    fontSize = large_text,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(mega_space))

        Button(
            onClick = {
                if (!isScanning && scan != null) {
                    onScanFinished(scan!!)
                } else {
                    activeScan.value?.cancel(true)
                    onCancel()
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(button_height),
            shape = RoundedCornerShape(small_medium_space),
            colors = ButtonDefaults.buttonColors(containerColor = CoralRed80Background)
        ) {
            Text(
                text = if (isScanning) stringResource(id = R.string.cancel) else stringResource(R.string.details),
                color = Color.Black,
                fontSize = medium_text,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
        ) {
            if (errorMessage != null) {
                ScanStatusItem(
                    text = errorMessage.orEmpty(),
                    backgroundColor = CoralRedSelectedBackground
                )
            } else if (scan?.scannedNetwork?.devices?.isEmpty() ?: true && !isScanning) {
                ScanStatusItem(text = "No devices found")
            } else {
                scan?.scannedNetwork?.devices?.forEach { device ->
                    val macLabel = device.macAddress ?: "Unknown MAC"
                    ScanStatusItem(text = "${device.ipAddress} - $macLabel (${device.hostName ?: "No hostname"})")
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
    backgroundColor: Color = CoralRedSelectedBackground
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(extra_small_space),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = border_thickness)
    ) {
        Text(
            text = text,
            color = Color.DarkGray,
            modifier = Modifier.padding(horizontal = medium_space, vertical = extra_small_space),
            fontSize = medium_small_text
        )
    }
}