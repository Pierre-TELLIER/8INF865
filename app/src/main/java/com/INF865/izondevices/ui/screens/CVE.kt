package com.INF865.izondevices.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.INF865.izondevices.R
import com.INF865.izondevices.ui.theme.CoralRed40
import com.INF865.izondevices.ui.theme.CoralRed80Background
import com.INF865.izondevices.ui.theme.extra_small_space
import com.INF865.izondevices.ui.theme.medium_space
import com.INF865.izondevices.ui.theme.placeholder_height
import com.INF865.izondevices.ui.theme.small_medium_space
import com.INF865.izondevices.ui.theme.small_space

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CVEScreen(modifier: Modifier = Modifier, onBack: () -> Unit = {}) {
    Column(modifier = modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    stringResource(id = R.string.vulnerability_title),
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
            }
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
