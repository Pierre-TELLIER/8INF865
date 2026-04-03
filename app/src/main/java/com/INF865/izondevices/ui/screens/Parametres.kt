package com.INF865.izondevices.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.edit
import com.INF865.izondevices.R
import com.INF865.izondevices.ui.theme.CoralRed40
import com.INF865.izondevices.ui.theme.CoralRed80
import com.INF865.izondevices.ui.theme.CoralRed80Background
import com.INF865.izondevices.ui.theme.CoralRedSwitchOnBackground
import com.INF865.izondevices.ui.theme.extra_large_space
import com.INF865.izondevices.ui.theme.extra_small_space
import com.INF865.izondevices.ui.theme.extra_small_text
import com.INF865.izondevices.ui.theme.huge_space
import com.INF865.izondevices.ui.theme.icon_size_medium
import com.INF865.izondevices.ui.theme.icon_size_small
import com.INF865.izondevices.ui.theme.large_space
import com.INF865.izondevices.ui.theme.medium_large_text
import com.INF865.izondevices.ui.theme.medium_small_text
import com.INF865.izondevices.ui.theme.medium_space
import com.INF865.izondevices.ui.theme.small_medium_space
import com.INF865.izondevices.ui.theme.tiny_space


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParametresScreen(modifier: Modifier = Modifier, onShowRootWarning: () -> Unit = {}) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = large_space, vertical = large_space),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.parameters_title),
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

        LazyColumn(modifier = Modifier.padding(medium_space)) {
            items(7) { index ->
                val displayIndex = index + 1
                when (displayIndex) {
                    1, 2 -> {
                        ParametreItem(name = "Paramètre $index", type = "edit")
                    }

                    3 -> {
                        ParametreItem(name = "Mode sombre", type = "switch")
                    }

                    4 -> {
                        ParametreItem(
                            name = stringResource(R.string.mac_warning_setting),
                            type = "basic",
                            onClick = onShowRootWarning
                        )
                    }

                    5, 6 -> {
                        ParametreItem(name = "Paramètre $index", type = "basic")
                    }

                    else -> {
                        ParametreItem(name = "Paramètre $index", type = "test")
                    }
                }
            }
        }
    }
}

@Composable
fun ParametreItem(
    modifier: Modifier = Modifier,
    name: String,
    type: String = "basic",
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("izon_prefs", Context.MODE_PRIVATE)
    val isDarkStr = prefs.getString("theme_mode", null)
    val isDark = if (isDarkStr == "true") true else false
    var checked by remember { mutableStateOf(isDark) }
    val onChecked: (checked: Boolean) -> Unit  = { prefs.edit { putString("theme_mode", it.toString()) } }

    Column(modifier = modifier.fillMaxSize().clickable { onClick() }) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = small_medium_space),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, fontSize = medium_small_text)

            when (type) {
                "switch" -> {
                    Box(modifier = Modifier.width(huge_space), contentAlignment = Alignment.BottomEnd) {
                        Switch(
                            modifier = modifier.size(icon_size_medium).scale(0.8f),
                            checked = checked,
                            onCheckedChange = {checked = it; onChecked(checked) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CoralRed80,
                                checkedTrackColor = CoralRedSwitchOnBackground,
                                checkedBorderColor = CoralRed80,
                                uncheckedThumbColor = CoralRed80,
                                uncheckedTrackColor = CoralRed80Background,
                                uncheckedBorderColor = CoralRed80
                            ),
                            thumbContent = if (checked) {
                                {
                                    Icon(
                                        painterResource(id = R.drawable.ic_dark_mode),
                                        contentDescription = null,
                                        modifier = Modifier.size(icon_size_small),
                                        tint = Color.Black
                                    )
                                }
                            } else {
                                {
                                    Icon(
                                        painterResource(id = R.drawable.ic_light_mode),
                                        contentDescription = null,
                                        modifier = Modifier.size(icon_size_small),
                                        tint = Color.White
                                    )
                                }
                            }
                        )
                    }
                }

                "edit" -> {
                    Text(text = "Current value", fontSize = extra_small_text)
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

                "basic" -> {
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
                else -> {
                    Box(modifier = Modifier.width(huge_space), contentAlignment = Alignment.BottomEnd)
                    {
                        Icon(
                            painterResource(id = R.drawable.ic_question_mark),
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
