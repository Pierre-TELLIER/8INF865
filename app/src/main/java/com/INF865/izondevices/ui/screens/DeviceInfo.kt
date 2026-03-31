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
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.INF865.izondevices.model.Vulnerability
import com.INF865.izondevices.model.values.HTTP_EXPOSED
import com.INF865.izondevices.model.values.TELNET_EXPOSED
import com.INF865.izondevices.scanner.BRUTE_PORTS
import com.INF865.izondevices.scanner.FUZZY_PORTS
import com.INF865.izondevices.scanner.INVALID_MAC
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
    onVulnerabilityClick: (Vulnerability) -> Unit = {}
) {
    val context = LocalContext.current
    val pingButtonColor = remember { mutableStateOf<Color?>(null) }
    val isPortScanning = remember { mutableStateOf(false) }
    val scannedPortsCount = remember { mutableStateOf(0) }
    val remainingPortsCount = remember { mutableStateOf(0) }
    val portScanService = remember { mutableStateOf<PortScanService?>(null) }
    val activePortScan = remember { mutableStateOf<CompletableFuture<List<Int>>?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val showPortMenu = remember { mutableStateOf(false) }

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

    // TODO : sometimes all ports are not detected ?
    val startPortScan = { ports: List<Int> ->
        val service = portScanService.value
        if (service != null && !isPortScanning.value) {
            isPortScanning.value = true
            device.openPorts = emptyList()
            scannedPortsCount.value = 0
            remainingPortsCount.value = ports.size + 1

            val future = service.scanPortsProgressive(device, ports) { progress: PortScanProgress ->
                device.openPorts = progress.openPorts
                scannedPortsCount.value = progress.scannedCount
                remainingPortsCount.value = progress.remainingCount
            }
            activePortScan.value = future
            future.whenComplete { result, throwable ->
                Handler(Looper.getMainLooper()).post {
                    isPortScanning.value = false
                    if (throwable == null && !future.isCancelled) {
                        device.openPorts = result ?: emptyList()
                        scannedPortsCount.value = ports.size + 1
                        remainingPortsCount.value = 0

                        // TODO : check if not already in list
                        for (port in device.openPorts) {
                            when (port) {
                                23 -> device.vulnerabilities.add(TELNET_EXPOSED)
                                80 -> device.vulnerabilities.add(HTTP_EXPOSED)
                            }
                        }

                        // TODO : save latest device state to database
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = large_space, vertical = large_space),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Icon(
                painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = "Back",
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .align(Alignment.CenterStart),
                tint = CoralRed40,
            )
            Text(
                text = device.hostName ?: device.ipAddress,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = medium_large_text
                ),
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }
        Spacer(modifier = Modifier.height(extra_small_space))
        Box(
            modifier = Modifier
                .width(extra_large_space)
                .height(tiny_space)
                .align(Alignment.CenterHorizontally)
                .background(CoralRed40),
        )
        Spacer(modifier = Modifier.height(extra_small_space))
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = medium_space)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Surface(
                    color = if (device.vulnerabilities.isNotEmpty()) RedVulnerabilities else GreenVulnerabilities,
                    shape = RoundedCornerShape(extra_small_space),
                    modifier = Modifier
                        .padding(vertical = small_space)
                        .width(bar_width_giant)
                        .height(extra_large_space)
                ) {
                    Text(
                        text = "${device.vulnerabilities.size} vulnérabilité${if (device.vulnerabilities.size > 1) 's' else ""}",
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        modifier = Modifier.padding(
                            horizontal = medium_space,
                            vertical = extra_small_space
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                Text(
                    text = device.ipAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }

            item {
                Spacer(modifier = Modifier.height(medium_space))
            }

            item {
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
                            painter = painterResource(id = R.drawable.ic_home),
                            contentDescription = null,
                            modifier = Modifier.size(extra_large_space),
                            tint = CoralRed40
                        )
                    }
                    Spacer(modifier = Modifier.width(medium_space))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = device.hostName ?: "Inconnu", fontWeight = FontWeight.Bold)
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
                            text = device.constructor ?: "Constructeur inconnu",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(large_space))
            }

            if (device.vulnerabilities.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.vulnerabilities_label),
                        modifier = Modifier.align(Alignment.Start),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.DarkGray,
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(small_space))
                }

                items(device.vulnerabilities) { vuln ->
                    VulnerabilityItem(
                        vuln = vuln,
                        onClick = onVulnerabilityClick,
                        modifier = Modifier
                            .padding(horizontal = medium_space)
                            .fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(small_space))
                }

                item {
                    Spacer(modifier = Modifier.height(large_space))
                }
            }

            item {
                Text(
                    text = stringResource(id = R.string.actions_label),
                    modifier = Modifier.align(Alignment.Start),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                Spacer(modifier = Modifier.height(medium_space))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ActionButton(
                        text = stringResource(id = R.string.ping_label),
                        onClick = {
                            coroutineScope.launch {
                                val result = pingDevice(device)
                                pingButtonColor.value =
                                    if (result) GreenVulnerabilities else RedVulnerabilities
                            }
                        },
                        backgroundColor = pingButtonColor.value ?: CoralRed80Background
                    )
                    Box {
                        ActionButton(
                            text = stringResource(id = R.string.port_scan_label),
                            onClick = {
                                if (!isPortScanning.value) {
                                    showPortMenu.value = true
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = showPortMenu.value,
                            onDismissRequest = { showPortMenu.value = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Fuzzy ports") },
                                onClick = {
                                    showPortMenu.value = false
                                    startPortScan(FUZZY_PORTS)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Brute ports") },
                                onClick = {
                                    showPortMenu.value = false
                                    startPortScan(BRUTE_PORTS.toList())
                                }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(small_space))
            }

            if (isPortScanning.value) {
                item {
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
            }

            if (device.openPorts.isNotEmpty()) {
                item {
                    Text(
                        text = "Ports ouverts: ${device.openPorts.joinToString(", ")}",
                        modifier = Modifier.align(Alignment.Start),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(large_space))
            }
        }
    }
}

@Composable
fun VulnerabilityItem(
    vuln: Vulnerability,
    modifier: Modifier = Modifier, onClick: (Vulnerability) -> Unit = {}
) {
    Card(
        onClick = { onClick(vuln) },
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
                    Text(text = vuln.name, color = Color.DarkGray)
                    Text(
                        text = "Voir plus d'informations",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Icon(
                    painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = CoralRed40
                )
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