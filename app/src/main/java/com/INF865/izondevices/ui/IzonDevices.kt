package com.INF865.izondevices.ui

import android.content.Context
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.INF865.izondevices.model.NetworkDevice
import com.INF865.izondevices.ui.screens.MainMenuScreen
import com.INF865.izondevices.ui.screens.DeviceInfoScreen
import com.INF865.izondevices.ui.screens.CVEScreen
import com.INF865.izondevices.ui.screens.ParametresScreen
import com.INF865.izondevices.ui.screens.HistoriqueScreen
import com.INF865.izondevices.ui.screens.ScanScreen
import com.INF865.izondevices.ui.theme.*

@Composable
fun IzonDevicesApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Shared state for the latest scan results, persisted in SharedPreferences
    var latestScanResult by remember { mutableStateOf(loadLatestScan(context)) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CoralRedAppBackground,
        bottomBar = {
            if (currentRoute == NavScreen.MainMenu.route || 
                currentRoute?.startsWith("device_info") == true || 
                currentRoute == NavScreen.CVE.route ||
                currentRoute == NavScreen.Parametres.route ||
                currentRoute == NavScreen.Historique.route) {
                IzonBottomNavigation(
                    currentRoute = currentRoute,
                    onScreenSelected = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavScreen.MainMenu.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable(NavScreen.MainMenu.route) {
                MainMenuScreen(
                    devices = latestScanResult,
                    onDeviceClick = { device -> 
                        navController.navigate(NavScreen.DeviceInfo.createRoute(device.ipAddress)) 
                    },
                    onScanClick = { navController.navigate(NavScreen.Scan.route) }
                )
            }
            composable(NavScreen.DeviceInfo.route) { backStackEntry ->
                val ip = backStackEntry.arguments?.getString("ip")
                val device = latestScanResult.find { it.ipAddress == ip }
                if (device != null) {
                    DeviceInfoScreen(
                        device = device,
                        onBack = { navController.popBackStack() },
                        onVulnerabilityClick = { navController.navigate(NavScreen.CVE.route) }
                    )
                }
            }
            composable(NavScreen.CVE.route) {
                CVEScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavScreen.Parametres.route) {
                ParametresScreen()
            }
            composable(NavScreen.Historique.route) {
                HistoriqueScreen()
            }
            composable(NavScreen.Scan.route) {
                ScanScreen(
                    onCancel = { navController.popBackStack() },
                    onScanFinished = { network ->
                        latestScanResult = network.devices
                        saveLatestScan(context, network.devices)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun IzonBottomNavigation(
    modifier: Modifier = Modifier,
    currentRoute: String?,
    onScreenSelected: (NavScreen) -> Unit
) {
    NavigationBar(
        modifier = modifier,
        containerColor = GreyBarBackground,
        tonalElevation = elevation_none
    ) {
        NavigationBarItem(
            selected = currentRoute == NavScreen.MainMenu.route || 
                       currentRoute?.startsWith("device_info") == true || 
                       currentRoute == NavScreen.CVE.route,
            onClick = { onScreenSelected(NavScreen.MainMenu) },
            icon = { BottomNavIconPlaceholder(shape = "triangle_up") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = CoralRedSelectedBackground
            )
        )
        NavigationBarItem(
            selected = currentRoute == NavScreen.Parametres.route,
            onClick = { onScreenSelected(NavScreen.Parametres) },
            icon = { BottomNavIconPlaceholder(shape = "square_x") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = CoralRedSelectedBackground
            )
        )
        NavigationBarItem(
            selected = currentRoute == NavScreen.Historique.route,
            onClick = { onScreenSelected(NavScreen.Historique) },
            icon = { BottomNavIconPlaceholder(shape = "triangle_down") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = CoralRedSelectedBackground
            )
        )
    }
}

@Composable
fun BottomNavIconPlaceholder(modifier: Modifier = Modifier, shape: String) {
    Box(
        modifier = modifier
            .size(huge_space)
            .border(border_thickness, CoralRed40),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (shape) {
                "triangle_up" -> "▲"
                "square_x" -> "⊠"
                "triangle_down" -> "▼"
                else -> ""
            },
            color = CoralRed80,
            fontSize = big_text
        )
    }
}

private fun saveLatestScan(context: Context, devices: List<NetworkDevice>) {
    val prefs = context.getSharedPreferences("izon_prefs", Context.MODE_PRIVATE)
    val data = devices.joinToString(";") { "${it.ipAddress},${it.macAddress ?: ""},${it.hostname ?: ""}" }
    prefs.edit().putString("latest_scan", data).apply()
}

private fun loadLatestScan(context: Context): List<NetworkDevice> {
    val prefs = context.getSharedPreferences("izon_prefs", Context.MODE_PRIVATE)
    val data = prefs.getString("latest_scan", null) ?: return emptyList()
    return data.split(";").filter { it.isNotEmpty() }.map {
        val parts = it.split(",")
        NetworkDevice(parts[0], parts.getOrNull(1)?.takeIf { it.isNotEmpty() }, parts.getOrNull(2)?.takeIf { it.isNotEmpty() })
    }
}

@Preview(showBackground = true)
@Composable
fun MainMenuPreview() {
    IzondevicesTheme {
        MainMenuScreen(devices = listOf(
            NetworkDevice(ipAddress = "192.168.1.10", macAddress = "AA:BB:CC:DD:EE:01", hostname = "Printer"),
            NetworkDevice(ipAddress = "192.168.1.20", macAddress = "AA:BB:CC:DD:EE:02", hostname = "Laptop")
        ))//emptyList())
    }
}

@Preview(showBackground = true)
@Composable
fun CVEPreview() {
    IzondevicesTheme {
        CVEScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ParametresPreview() {
    IzondevicesTheme {
        ParametresScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun HistoriquePreview() {
    IzondevicesTheme {
        HistoriqueScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    IzondevicesTheme {
        IzonDevicesApp()
    }
}
