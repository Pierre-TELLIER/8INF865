package com.INF865.izondevices

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.INF865.izondevices.ui.IzonDevicesApp
import com.INF865.izondevices.ui.theme.IzondevicesTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (!File("/proc/net/arp").canRead()) {
            println("!!!=== ARP not readable !")
        } else {
            println("!!!=== ARP IS READABLE")
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IzondevicesTheme {
                IzonDevicesApp()
            }
        }
    }
}
