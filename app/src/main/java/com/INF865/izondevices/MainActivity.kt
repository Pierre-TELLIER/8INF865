package com.INF865.izondevices

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.INF865.izondevices.service.execCommand
import com.INF865.izondevices.ui.IzonDevicesApp
import com.INF865.izondevices.ui.theme.IzondevicesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        execCommand("ip neigh show").forEach { println(it) }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IzondevicesTheme {
                IzonDevicesApp()
            }
        }
    }
}
