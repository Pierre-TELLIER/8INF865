package com.INF865.izondevices.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.INF865.izondevices.R
import com.INF865.izondevices.model.Network
import com.INF865.izondevices.model.NetworkDevice
import com.INF865.izondevices.model.Scan
import com.INF865.izondevices.ui.theme.CoralRed40
import com.INF865.izondevices.ui.theme.extra_large_space
import com.INF865.izondevices.ui.theme.extra_small_space
import com.INF865.izondevices.ui.theme.grid_spacing
import com.INF865.izondevices.ui.theme.huge_space
import com.INF865.izondevices.ui.theme.large_space
import com.INF865.izondevices.ui.theme.medium_large_text
import com.INF865.izondevices.ui.theme.medium_space
import com.INF865.izondevices.ui.theme.tiny_space
import com.INF865.izondevices.ui.theme.IzondevicesTheme

@Composable
fun ScanHistoryScreen(
    scan: Scan,
    modifier: Modifier = Modifier,
    onDeviceClick: (NetworkDevice) -> Unit = {},
    onBack: () -> Unit = {}
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = large_space, vertical = large_space),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                    text = " " + stringResource(id = R.string.reseau_label) + " " + (scan.scannedNetwork.networkName
                        ?: ""),
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
                    .background(CoralRed40)
            )
        }

        Spacer(modifier = Modifier.height(huge_space))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(grid_spacing),
            verticalArrangement = Arrangement.spacedBy(grid_spacing),
            contentPadding = PaddingValues(bottom = medium_space)
        ) {
            items(scan.scannedNetwork.devices) { device ->
                DeviceItem(device = device, onClick = { onDeviceClick(device) })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScanHistoryScreenPreview() {
    IzondevicesTheme {
        ScanHistoryScreen(
            scan =
                Scan.fromNow(
                    Network(
                        networkAddress = "192.168.1.0",
                        networkName = "Old SSID",
                        devices = listOf(
                            NetworkDevice(ipAddress = "192.168.1.11", hostName = "Printer"),
                            NetworkDevice(ipAddress = "192.168.1.12", hostName = "Laptop")
                        )
                    )
                )
        )
    }
}

