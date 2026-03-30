package com.INF865.izondevices.ui

import android.content.Context
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import androidx.core.content.edit
import com.INF865.izondevices.R
import com.INF865.izondevices.model.Network
import kotlinx.serialization.json.Json

@Composable
fun IzonDevicesApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Shared state for the latest scan results, persisted in SharedPreferences
    var latestScanResult by remember { mutableStateOf(loadLatestScan(context)) }

    val prefs = context.getSharedPreferences("izon_prefs", Context.MODE_PRIVATE)
    prefs.edit { putString("theme_mode", isSystemInDarkTheme().toString()) }

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
                    network = latestScanResult,
                    onDeviceClick = { device -> 
                        navController.navigate(NavScreen.DeviceInfo.createRoute(device.ipAddress)) 
                    },
                    onScanClick = { navController.navigate(NavScreen.Scan.route) }
                )
            }
            composable(NavScreen.DeviceInfo.route) { backStackEntry ->
                val ip = backStackEntry.arguments?.getString("ip")
                val device = latestScanResult.devices.find { it.ipAddress == ip }
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
                        latestScanResult = network
                        saveLatestScan(context, network)
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
            selected = currentRoute == NavScreen.Historique.route,
            onClick = { onScreenSelected(NavScreen.Historique) },
            icon = { BottomNavIcon(menu = "history") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = CoralRedSelectedBackground
            )
        )
        NavigationBarItem(
            selected = currentRoute == NavScreen.MainMenu.route || 
                       currentRoute?.startsWith("device_info") == true || 
                       currentRoute == NavScreen.CVE.route,
            onClick = { onScreenSelected(NavScreen.MainMenu) },
            icon = { BottomNavIcon(menu = "home") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = CoralRedSelectedBackground
            )
        )
        NavigationBarItem(
            selected = currentRoute == NavScreen.Parametres.route,
            onClick = { onScreenSelected(NavScreen.Parametres) },
            icon = { BottomNavIcon(menu = "settings") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = CoralRedSelectedBackground
            )
        )
    }
}

@Composable
fun BottomNavIcon(modifier: Modifier = Modifier, menu: String) {
    val icon = when (menu) {
        "home" -> {R.drawable.ic_home}
        "history" -> {R.drawable.ic_history}
        "settings" -> {R.drawable.ic_settings}
        else -> {R.drawable.ic_question_mark}
    }
    Box(
        modifier = modifier
            .size(huge_space)
            .border(border_thickness, CoralRed40, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(icon_size_large),
            tint = CoralRed40
        )
    }
}

// awful way to save the latest scan result...
// but cheap and easy
private fun saveLatestScan(context: Context, network: Network) {
    val prefs = context.getSharedPreferences("izon_prefs", Context.MODE_PRIVATE)
    val data = Json.encodeToString(network)
    prefs.edit { putString("latest_scan", data) }
}

private fun loadLatestScan(context: Context): Network {
    val prefs = context.getSharedPreferences("izon_prefs", Context.MODE_PRIVATE)
    val invalid_network = Network("", null, emptyList())
    val data = prefs.getString("latest_scan", null) ?: return invalid_network
    return runCatching {
        Json.decodeFromString<Network>(data)
    }.getOrDefault(invalid_network)
}

@Preview(showBackground = true)
@Composable
fun MainMenuPreview() {
    IzondevicesTheme {
        MainMenuScreen(
            Network(
                networkAddress = "192.168.1.0",
                networkName = "My SSID",
                devices = listOf(
                    NetworkDevice(ipAddress = "192.168.1.10", macAddress = "AA:BB:CC:DD:EE:01", hostName = "Printer"),
                    NetworkDevice(ipAddress = "192.168.1.20", macAddress = "AA:BB:CC:DD:EE:02", hostName = "Laptop")
                )
            )
        )
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
fun DeviceInfoPreview() {
    IzondevicesTheme {
        DeviceInfoScreen(NetworkDevice(ipAddress = "192.168.1.10", macAddress = "AA:BB:CC:DD:EE:01", hostName = "Printer"))
    }
}

@Preview(showBackground = true)
@Composable
fun ScanPreview() {
    IzondevicesTheme {
        ScanScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    IzondevicesTheme {
        IzonDevicesApp()
    }
}
