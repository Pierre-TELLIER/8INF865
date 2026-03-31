package com.INF865.izondevices.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.INF865.izondevices.R
import com.INF865.izondevices.model.Network
import com.INF865.izondevices.model.NetworkDevice
import com.INF865.izondevices.model.Scan
import com.INF865.izondevices.ui.theme.CoralRed40
import com.INF865.izondevices.ui.theme.CoralRed80Background
import com.INF865.izondevices.ui.theme.CoralRedDark
import com.INF865.izondevices.ui.theme.CoralRedLight
import com.INF865.izondevices.ui.theme.border_thickness
import com.INF865.izondevices.ui.theme.divider_thickness
import com.INF865.izondevices.ui.theme.extra_large_space
import com.INF865.izondevices.ui.theme.extra_small_space
import com.INF865.izondevices.ui.theme.giga_space
import com.INF865.izondevices.ui.theme.grid_spacing
import com.INF865.izondevices.ui.theme.huge_space
import com.INF865.izondevices.ui.theme.large_space
import com.INF865.izondevices.ui.theme.large_text
import com.INF865.izondevices.ui.theme.medium_large_text
import com.INF865.izondevices.ui.theme.medium_small_text
import com.INF865.izondevices.ui.theme.medium_space
import com.INF865.izondevices.ui.theme.small_medium_space
import com.INF865.izondevices.ui.theme.small_space
import com.INF865.izondevices.ui.theme.tiny_space

@Composable
fun MainMenuScreen(
    scan: Scan?,
    modifier: Modifier = Modifier,
    onDeviceClick: (NetworkDevice) -> Unit = {},
    onScanClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = large_space, vertical = large_space),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (scan == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(text = "Aucun scan récent", color = Color.DarkGray)
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = " " + stringResource(id = R.string.reseau_label) + " " + (scan.scannedNetwork.networkName?: ""), // TODO : récupérer le nom du réseau scanné
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = medium_large_text
                    ),
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
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

        Spacer(modifier = Modifier.height(medium_space))

        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(large_space))

            Button(
                onClick = onScanClick,
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(giga_space)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(medium_space),
                border = BorderStroke(width = tiny_space, color = CoralRedLight),
                colors = ButtonDefaults.buttonColors(containerColor = CoralRed80Background)
            ) {
                Text(
                    text = stringResource(id = R.string.scan),
                    color = Color.Black,
                    fontSize = large_text,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(large_space))
    }
}

@Composable
fun DeviceItem(
    device: NetworkDevice,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(border_thickness, CoralRedDark, shape = RoundedCornerShape(extra_small_space))
            .clickable(onClick = onClick)
            .padding(medium_space),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            /*Box(
                modifier = Modifier
                    .size(huge_space)
                    .border(border_thickness, Color.Gray)
            )*/
            Image(
                painterResource(id = R.drawable.smartphone),
                contentDescription = "Smartphone icon",
            )
            Spacer(modifier = Modifier.weight(1.0f))
            Text(
                text = device.ipAddress,
                fontSize = medium_small_text,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            device.hostName?.let {
                Text(
                    text = it,
                    fontSize = medium_small_text,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}