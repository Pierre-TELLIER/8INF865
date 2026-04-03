package com.INF865.izondevices.ui

import android.content.Context
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.INF865.izondevices.R
import com.INF865.izondevices.model.Network
import com.INF865.izondevices.model.NetworkDevice
import com.INF865.izondevices.model.Scan
import com.INF865.izondevices.model.ScanHistory
import com.INF865.izondevices.model.Vulnerability
import com.INF865.izondevices.model.serialization.ScanHistorySerializer
import com.INF865.izondevices.ui.screens.CVEScreen
import com.INF865.izondevices.ui.screens.DeviceInfoScreen
import com.INF865.izondevices.ui.screens.HistoriqueScreen
import com.INF865.izondevices.ui.screens.MainMenuScreen
import com.INF865.izondevices.ui.screens.ParametresScreen
import com.INF865.izondevices.ui.screens.ScanHistoryScreen
import com.INF865.izondevices.ui.screens.ScanScreen
import com.INF865.izondevices.ui.theme.CoralRed40
import com.INF865.izondevices.ui.theme.CoralRedAppBackground
import com.INF865.izondevices.ui.theme.CoralRedSelectedBackground
import com.INF865.izondevices.ui.theme.GreyBarBackground
import com.INF865.izondevices.ui.theme.IzondevicesTheme
import com.INF865.izondevices.ui.theme.border_thickness
import com.INF865.izondevices.ui.theme.elevation_none
import com.INF865.izondevices.ui.theme.huge_space
import com.INF865.izondevices.ui.theme.icon_size_large
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


val Context.dataStore: DataStore<ScanHistory> by dataStore(
    fileName = "ScanHistory.json",
    serializer = ScanHistorySerializer,
)


@Composable
fun IzonDevicesApp(modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var latestScanResults by remember { mutableStateOf(mutableListOf<Scan>()) }

    // Shared state for the latest scan results, persisted in SharedPreferences
    LaunchedEffect(Unit) {
        loadLatestScansV2(context).collect({ value ->
            latestScanResults = value
        })
    }


    var latestScan = latestScanResults.lastOrNull()
    var selectedHistoryScan by remember { mutableStateOf<Scan?>(null) }
    var deviceSourceScan by remember { mutableStateOf<Scan?>(null) }
    var selectedVulnerability by remember { mutableStateOf<Vulnerability?>(null) }

    val prefs = remember { context.getSharedPreferences("izon_prefs", Context.MODE_PRIVATE) }
    
    val isDark = isSystemInDarkTheme()
    LaunchedEffect(isDark) {
        prefs.edit { putString("theme_mode", isDark.toString()) }
    }

    var showRootWarning by remember { mutableStateOf(false) }
    
    // Check for first run using LaunchedEffect to ensure it runs only once and correctly updates state
    LaunchedEffect(Unit) {
        if (prefs.getBoolean("is_first_run", true)) {
            showRootWarning = true
        }
    }

    if (showRootWarning) {
        AlertDialog(
            onDismissRequest = { 
                showRootWarning = false 
                prefs.edit { putBoolean("is_first_run", false) }
            },
            title = { Text(stringResource(R.string.root_warning_title)) },
            text = { Text(stringResource(R.string.root_warning_message)) },
            confirmButton = {
                TextButton(onClick = { 
                    showRootWarning = false 
                    prefs.edit { putBoolean("is_first_run", false) }
                }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CoralRedAppBackground,
        bottomBar = {
            if (currentRoute == NavScreen.MainMenu.route ||
                currentRoute?.startsWith("device_info") == true ||
                currentRoute?.startsWith("cve") == true ||
                currentRoute == NavScreen.Parametres.route ||
                currentRoute == NavScreen.Historique.route
            ) {
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
                    scan = latestScan,
                    onDeviceClick = { device ->
                        deviceSourceScan = latestScan
                        navController.navigate(NavScreen.DeviceInfo.createRoute(device.ipAddress))
                    },
                    onScanClick = { navController.navigate(NavScreen.Scan.route) }
                )
            }
            composable(NavScreen.DeviceInfo.route) { backStackEntry ->
                val ip = backStackEntry.arguments?.getString("ip")
                val device =
                    deviceSourceScan?.scannedNetwork?.devices?.find { it.ipAddress == ip }
                        ?: latestScan?.scannedNetwork?.devices?.find { it.ipAddress == ip }
                if (device != null) {
                    DeviceInfoScreen(
                        device = device,
                        onBack = { navController.popBackStack() },
                        onVulnerabilityClick = { vuln ->
                            selectedVulnerability = vuln
                            navController.navigate(NavScreen.CVE.route)
                        }
                    )
                }
            }
            composable(NavScreen.CVE.route) {
                selectedVulnerability?.let { vuln ->
                    CVEScreen(
                        vuln = vuln,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            composable(NavScreen.Parametres.route) {
                ParametresScreen(onShowRootWarning = { showRootWarning = true })
            }
            composable(NavScreen.Historique.route) {
                HistoriqueScreen(
                    scans = latestScanResults,
                    onScanClick = { scan ->
                        selectedHistoryScan = scan
                        navController.navigate(NavScreen.ScanHistory.route)
                    }
                )
            }
            composable(NavScreen.ScanHistory.route) {
                selectedHistoryScan?.let { scan ->
                    ScanHistoryScreen(
                        scan = scan,
                        onDeviceClick = { device ->
                            deviceSourceScan = scan
                            navController.navigate(NavScreen.DeviceInfo.createRoute(device.ipAddress))
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            composable(NavScreen.Scan.route) {
                ScanScreen(
                    onCancel = { navController.popBackStack() },
                    onScanFinished = { scan ->
                        latestScanResults += scan
                        //saveLatestScans(context, latestScanResults)
                        coroutineScope.launch {
                            saveLatestScansV2(
                                context,
                                latestScanResults
                            )
                        }
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
        "home" -> {
            R.drawable.ic_home
        }

        "history" -> {
            R.drawable.ic_history
        }

        "settings" -> {
            R.drawable.ic_settings
        }

        else -> {
            R.drawable.ic_question_mark
        }
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
private fun saveLatestScans(context: Context, scan: List<Scan>) {

    val prefs = context.getSharedPreferences("izon_prefs", Context.MODE_PRIVATE)
    val data = Json.encodeToString(scan)
    prefs.edit { putString("latest_scan", data) }
}


suspend fun saveLatestScansV2(context: Context, scans: List<Scan>): Unit {
    context.dataStore.updateData { scanHistory ->
        ScanHistory(scans.toMutableList())
    }
}


private fun loadLatestScans(context: Context): MutableList<Scan> {
    val prefs = context.getSharedPreferences("izon_prefs", Context.MODE_PRIVATE)
    val invalid_scan = mutableListOf<Scan>()
    val data = prefs.getString("latest_scan", null) ?: return invalid_scan
    return runCatching {
        Json.decodeFromString<MutableList<Scan>>(data)
    }.getOrDefault(invalid_scan)
}

fun loadLatestScansV2(context: Context): Flow<MutableList<Scan>> {
    return context.dataStore.data.map { scanHistory ->
        scanHistory.scans
    }
}


@Preview(showBackground = true)
@Composable
fun MainMenuPreview() {
    IzondevicesTheme {
        MainMenuScreen(
            Scan.fromNow(
                Network(
                    networkAddress = "192.168.1.0",
                    networkName = "My SSID",
                    devices = listOf(
                        NetworkDevice(
                            ipAddress = "192.168.1.10",
                            macAddress = "AA:BB:CC:DD:EE:01",
                            hostName = "Printer"
                        ),
                        NetworkDevice(
                            ipAddress = "192.168.1.20",
                            macAddress = "AA:BB:CC:DD:EE:02",
                            hostName = "Laptop"
                        )
                    )
                )
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CVEPreview() {
    IzondevicesTheme {
        CVEScreen(
            vuln = Vulnerability(
                name = "Sample CVE",
                description = "Sample description",
                mitigation = "Sample mitigation",
                details = "Sample details"
            )
        )
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
        HistoriqueScreen(
            listOf(
                Scan.fromNow(
                    Network(
                        networkAddress = "192.168.1.0",
                        networkName = "My SSID",
                        devices = listOf(
                            NetworkDevice(
                                ipAddress = "192.168.1.10",
                                macAddress = "AA:BB:CC:DD:EE:01",
                                hostName = "Printer"
                            ),
                            NetworkDevice(
                                ipAddress = "192.168.1.20",
                                macAddress = "AA:BB:CC:DD:EE:02",
                                hostName = "Laptop"
                            )
                        )
                    )
                )
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DeviceInfoPreview() {
    IzondevicesTheme {
        DeviceInfoScreen(
            NetworkDevice(
                ipAddress = "192.168.1.10",
                macAddress = "AA:BB:CC:DD:EE:01",
                hostName = "Printer"
            )
        )
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
