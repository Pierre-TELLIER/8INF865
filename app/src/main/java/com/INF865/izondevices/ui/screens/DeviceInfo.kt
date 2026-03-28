package com.INF865.izondevices.ui.screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.INF865.izondevices.R
import com.INF865.izondevices.model.NetworkDevice
import com.INF865.izondevices.scanner.FIRST_PORT
import com.INF865.izondevices.scanner.INVALID_MAC
import com.INF865.izondevices.scanner.LAST_PORT
import com.INF865.izondevices.scanner.pingDevice
import com.INF865.izondevices.scanner.PortScanProgress
import com.INF865.izondevices.service.PortScanService
import com.INF865.izondevices.ui.theme.*
import kotlinx.coroutines.launch
import java.util.concurrent.CompletableFuture


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceInfoScreen(
    device: NetworkDevice,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onVulnerabilityClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val pingButtonColor = remember { mutableStateOf<Color?>(null) }
    val openPorts = remember { mutableStateOf<List<Int>>(emptyList()) }
    val isPortScanning = remember { mutableStateOf(false) }
    val scannedPortsCount = remember { mutableStateOf(0) }
    val remainingPortsCount = remember { mutableStateOf(0) }
    val portScanService = remember { mutableStateOf<PortScanService?>(null) }
    val activePortScan = remember { mutableStateOf<CompletableFuture<List<Int>>?>(null) }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(context) {
        var isBound = false
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as? PortScanService.LocalBinder ?: return
                portScanService.value = binder.getService()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                portScanService.value = null
                isBound = false
            }
        }

        val serviceIntent = Intent(context, PortScanService::class.java)
        isBound = context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

        onDispose {
            activePortScan.value?.cancel(true)
            if (isBound) {
                runCatching { context.unbindService(connection) }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    device.hostname ?: device.ipAddress,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = "Back",
                        tint = CoralRed40
                    )
                }
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(
                        painterResource(id = R.drawable.ic_refresh),
                        contentDescription = "Refresh",
                        tint = CoralRed40
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .padding(horizontal = medium_space)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = RedVulnerabilities, // TODO : adapt color with number of vulnerabilities found
                shape = RoundedCornerShape(extra_small_space),
                modifier = Modifier.padding(vertical = small_space).width(bar_width_giant).height(extra_large_space)
            ) {
                Text(
                    text = stringResource(id = R.string.vulnerabilities_detected),
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    modifier = Modifier.padding(
                        horizontal = medium_space,
                        vertical = extra_small_space
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = device.ipAddress,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(medium_space))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(mega_space)
                        .clip(CircleShape)
                        .background(CoralRed80Background),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_home), // Placeholder icon
                        contentDescription = null,
                        modifier = Modifier.size(extra_large_space),
                        tint = CoralRed40
                    )
                }
                Spacer(modifier = Modifier.width(medium_space))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = device.hostname ?: "Inconnu", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(extra_small_space))
                        Icon(
                            painterResource(id = R.drawable.ic_edit),
                            contentDescription = null,
                            modifier = Modifier.size(icon_size_small),
                            tint = CoralRed40
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "MAC: ${device.macAddress ?: INVALID_MAC}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(extra_small_space))
                    Text(
                        text = "More info on device",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(large_space))

            Text(
                text = stringResource(id = R.string.vulnerabilities_label),
                modifier = Modifier.align(Alignment.Start),
                style = MaterialTheme.typography.titleMedium,
                color = Color.DarkGray,
            )

            Spacer(modifier = Modifier.height(small_space))

            VulnerabilityItem(onClick = onVulnerabilityClick)
            Spacer(modifier = Modifier.height(small_space))
            VulnerabilityItem(onClick = onVulnerabilityClick)

            Spacer(modifier = Modifier.height(large_space))

            Text(
                text = stringResource(id = R.string.actions_label),
                modifier = Modifier.align(Alignment.Start),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(medium_space))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    text = stringResource(id = R.string.ping_label),
                    onClick = {
                        coroutineScope.launch {
                            val result = pingDevice(device)
                            pingButtonColor.value = if (result) GreenVulnerabilities else RedVulnerabilities
                        }
                    },
                    backgroundColor = pingButtonColor.value ?: CoralRed80Background
                )
                ActionButton(
                    text = stringResource(id = R.string.port_scan_label),
                    onClick = {
                        val service = portScanService.value ?: return@ActionButton
                        if (isPortScanning.value) return@ActionButton

                        isPortScanning.value = true
                        openPorts.value = emptyList()
                        scannedPortsCount.value = 0
                        remainingPortsCount.value = LAST_PORT - FIRST_PORT + 1

                        val future = service.scanPortsProgressive(device) { progress: PortScanProgress ->
                            openPorts.value = progress.openPorts
                            scannedPortsCount.value = progress.scannedCount
                            remainingPortsCount.value = progress.remainingCount
                        }
                        activePortScan.value = future
                        future.whenComplete { result, throwable ->
                            Handler(Looper.getMainLooper()).post {
                                isPortScanning.value = false
                                if (throwable == null && !future.isCancelled) {
                                    openPorts.value = result ?: emptyList()
                                    scannedPortsCount.value = LAST_PORT - FIRST_PORT + 1
                                    remainingPortsCount.value = 0
                                }
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(small_space))

            if (isPortScanning.value) {
                CircularProgressIndicator(color = CoralRed40)
                Spacer(modifier = Modifier.height(extra_small_space))
                Text(
                    text = "Ports scannés: ${scannedPortsCount.value}",
                    modifier = Modifier.align(Alignment.Start),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Ports restants: ${remainingPortsCount.value}",
                    modifier = Modifier.align(Alignment.Start),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (openPorts.value.isNotEmpty()) {
                Text(
                    text = "Ports ouverts: ${openPorts.value.joinToString(", ")}",
                    modifier = Modifier.align(Alignment.Start),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
fun VulnerabilityItem(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(elevation_none),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column {
            HorizontalDivider(color = CoralRed80Background)
            Row(
                modifier = Modifier
                    .padding(vertical = small_medium_space)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Nom", color = Color.DarkGray)
                    Text(
                        text = "More info on vulnerability",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Icon(painterResource(id = R.drawable.ic_chevron_right), contentDescription = null, tint = CoralRed40)
            }
        }
    }
}

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit = { },
    backgroundColor: Color = CoralRed80Background
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(small_medium_space),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        border = BorderStroke(width = border_thickness, color = CoralRedLight),
        modifier = modifier
            .height(huge_space)
    ) {
        Text(text = text, color = Color.Black)
    }
}