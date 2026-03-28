package com.INF865.izondevices.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.INF865.izondevices.R
import com.INF865.izondevices.ui.theme.CoralRed40
import com.INF865.izondevices.ui.theme.CoralRed40Background
import com.INF865.izondevices.ui.theme.CoralRed80
import com.INF865.izondevices.ui.theme.CoralRed80Background
import com.INF865.izondevices.ui.theme.bar_width_large
import com.INF865.izondevices.ui.theme.grid_spacing
import com.INF865.izondevices.ui.theme.huge_space
import com.INF865.izondevices.ui.theme.icon_size_medium
import com.INF865.izondevices.ui.theme.medium_space
import com.INF865.izondevices.ui.theme.small_medium_space
import com.INF865.izondevices.ui.theme.small_space
import com.INF865.izondevices.ui.theme.small_text
import com.INF865.izondevices.ui.theme.tiny_space


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParametresScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    stringResource(id = R.string.parameters_title),
                    fontWeight = FontWeight.Bold
                )
            }
        )

        LazyColumn(modifier = Modifier.padding(medium_space)) {
            items(6) { index ->
                ParametreItem(index = index + 1)
            }
        }
    }
}

@Composable
fun ParametreItem(modifier: Modifier = Modifier, index: Int) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = small_medium_space),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Param$index", fontSize = small_text)
            Box(
                modifier = Modifier
                    .width(bar_width_large)
                    .height(small_space)
                    .background(CoralRed40Background)
            )

            when (index) {
                3 -> {
                    Box(
                        modifier = Modifier
                            .width(huge_space)
                            .height(grid_spacing)
                            .clip(CircleShape)
                            .background(CoralRed40Background),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(tiny_space)
                                .size(medium_space)
                                .clip(CircleShape)
                                .background(CoralRed80)
                        )
                    }
                }

                1, 2 -> {
                    Box(modifier = Modifier.width(huge_space), contentAlignment = Alignment.BottomEnd)
                    {
                        Icon(
                            painterResource(id = R.drawable.ic_edit),
                            contentDescription = null,
                            modifier = Modifier.size(icon_size_medium),
                            tint = CoralRed40
                        )
                    }
                }

                else -> {
                    Box(modifier = Modifier.width(huge_space), contentAlignment = Alignment.BottomEnd)
                    {
                        Icon(
                            painterResource(id = R.drawable.ic_chevron_right),
                            contentDescription = null,
                            modifier = Modifier.size(icon_size_medium),
                            tint = CoralRed40
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = CoralRed80Background)
    }
}
