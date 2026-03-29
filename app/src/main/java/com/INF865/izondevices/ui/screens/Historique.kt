package com.INF865.izondevices.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.INF865.izondevices.R
import com.INF865.izondevices.ui.theme.CoralRed40
import com.INF865.izondevices.ui.theme.CoralRed80Background
import com.INF865.izondevices.ui.theme.extra_large_space
import com.INF865.izondevices.ui.theme.extra_small_space
import com.INF865.izondevices.ui.theme.icon_size_medium
import com.INF865.izondevices.ui.theme.large_space
import com.INF865.izondevices.ui.theme.medium_large_text
import com.INF865.izondevices.ui.theme.medium_small_text
import com.INF865.izondevices.ui.theme.medium_space
import com.INF865.izondevices.ui.theme.small_medium_space
import com.INF865.izondevices.ui.theme.tiny_space


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoriqueScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = large_space, vertical = large_space),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.history_scans_title),
                style = typography.titleMedium.copy(
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

        LazyColumn(modifier = Modifier.padding(medium_space)) {
            items(7) { index ->
                HistoryItem(index = 7 - index)
            }
        }
    }
}

@Composable
fun HistoryItem(modifier: Modifier = Modifier, index: Int) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = small_medium_space),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Réseau $index", fontSize = medium_small_text)
            Icon(
                painterResource(id = R.drawable.ic_chevron_right),
                contentDescription = null,
                modifier = Modifier.size(icon_size_medium),
                tint = CoralRed40
            )
        }
        HorizontalDivider(color = CoralRed80Background)
    }
}