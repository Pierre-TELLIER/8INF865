package com.INF865.izondevices.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.INF865.izondevices.ui.theme.CoralRed40
import com.INF865.izondevices.ui.theme.CoralRed80Background
import com.INF865.izondevices.ui.theme.extra_large_space
import com.INF865.izondevices.ui.theme.extra_small_space
import com.INF865.izondevices.ui.theme.large_space
import com.INF865.izondevices.ui.theme.medium_large_text
import com.INF865.izondevices.ui.theme.medium_space
import com.INF865.izondevices.ui.theme.placeholder_height
import com.INF865.izondevices.ui.theme.small_medium_space
import com.INF865.izondevices.ui.theme.small_space
import com.INF865.izondevices.ui.theme.tiny_space

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CVEScreen(modifier: Modifier = Modifier, onBack: () -> Unit = {}) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = large_space, vertical = large_space),
        horizontalAlignment = Alignment.CenterHorizontally,) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Icon(
                painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = "Back",
                modifier = Modifier.clickable(onClick = onBack).align(Alignment.CenterStart),
                tint = CoralRed40,
            )
            Text(
                text = stringResource(id = R.string.vulnerability_title),
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

        Column(modifier = Modifier.padding(medium_space)) {
            CVESection(title = stringResource(id = R.string.description_label))
            CVESection(title = stringResource(id = R.string.remediation_label))
            CVESection(title = stringResource(id = R.string.details_label))
        }
    }
}

@Composable
fun CVESection(modifier: Modifier = Modifier, title: String) {
        Column(modifier = modifier.padding(vertical = small_space)) {
        Text(text = title, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(extra_small_space))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(placeholder_height)
                .clip(RoundedCornerShape(small_medium_space))
                .background(CoralRed80Background)
        )
    }
}
