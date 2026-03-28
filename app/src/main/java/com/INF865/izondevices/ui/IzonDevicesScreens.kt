package com.INF865.izondevices.ui

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.core.content.ContextCompat
import com.INF865.izondevices.R
import com.INF865.izondevices.model.NetworkDevice
import com.INF865.izondevices.ui.screens.DeviceInfoScreen
import com.INF865.izondevices.ui.screens.ScanScreen
import com.INF865.izondevices.ui.theme.*

sealed class NavScreen(val route: String) {
    data object MainMenu : NavScreen("main_menu")
    data object DeviceInfo : NavScreen("device_info/{ip}") {
        fun createRoute(ip: String) = "device_info/$ip"
    }
    data object CVE : NavScreen("cve")
    data object Parametres : NavScreen("parametres")
    data object Historique : NavScreen("historique")
    data object Scan : NavScreen("scan")
}

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
        containerColor = Color(0xFFF9F9FB),
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
fun MainMenuScreen(
    devices: List<NetworkDevice>,
    modifier: Modifier = Modifier,
    onDeviceClick: (NetworkDevice) -> Unit = {},
    onScanClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = large_space, vertical = large_space),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.align(Alignment.Start)) {
            Text(
                text = stringResource(id = R.string.reseau_label),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = medium_large_text
                ),
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(extra_small_space))
            Box(
                modifier = Modifier
                    .width(giant_space)
                    .height(tiny_space)
                    .background(Color.Gray)
            )
        }

        Spacer(modifier = Modifier.height(huge_space))

        if (devices.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(text = "Aucun scan récent", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(grid_spacing),
                verticalArrangement = Arrangement.spacedBy(grid_spacing),
                contentPadding = PaddingValues(bottom = medium_space)
            ) {
                items(devices) { device ->
                    DeviceItem(device = device, onClick = { onDeviceClick(device) })
                }
            }
        }

        Spacer(modifier = Modifier.height(medium_space))

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.Gray,
                    thickness = divider_thickness
                )
                Spacer(modifier = Modifier.width(grid_spacing))
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.Gray,
                    thickness = divider_thickness
                )
            }
            Spacer(modifier = Modifier.height(extra_large_space))

            Button(
                onClick = onScanClick,
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(giga_space)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(medium_space),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0D0D0))
            ) {
                Text(
                    text = stringResource(id = R.string.scan),
                    color = Color.Black,
                    fontSize = large_text,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(large_space))
    }
}

@Composable
fun DeviceItem(
    device: NetworkDevice,
    modifier: Modifier = Modifier, 
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(border_thickness, Color.Gray)
            .clickable(onClick = onClick)
            .padding(large_space),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(huge_space)
                    .border(border_thickness, Color.Gray)
            )
            Spacer(modifier = Modifier.height(small_space))
            Text(
                text = device.ipAddress,
                fontSize = small_text,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            device.hostname?.let {
                Text(
                    text = it,
                    fontSize = small_text,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun VulnerabilityItem(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(elevation_none),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column {
            HorizontalDivider(color = Color.LightGray)
            Row(
                modifier = Modifier
                    .padding(vertical = small_medium_space)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Nom", color = Color.Gray)
                    Box(
                        modifier = Modifier
                            .width(bar_width_small)
                            .height(small_space)
                            .background(Color.LightGray)
                    )
                }
                Icon(painterResource(id = R.drawable.ic_chevron_right), contentDescription = null)
            }
        }
    }
}

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit = { },
    backgroundColor: Color = Color.White
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(small_medium_space),
        modifier = modifier
            .height(huge_space)
            .background(backgroundColor, shape = RoundedCornerShape(small_medium_space))
    ) {
        Text(text = text, color = Color.Black)
    }
}

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
                        contentDescription = "Back"
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
                .background(Color(0xFFE0E0E0))
        )
    }
}

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
                .background(Color(0xFFE0E0E0))
        )

        when (index) {
            3 -> {
                Box(
                    modifier = Modifier
                        .width(huge_space)
                        .height(grid_spacing)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .padding(tiny_space)
                            .size(medium_space)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                }
            }

            1, 2 -> {
                Icon(
                    painterResource(id = R.drawable.ic_edit),
                    contentDescription = null,
                    modifier = Modifier.size(icon_size_medium)
                )
            }

            else -> {
                Icon(
                    painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = null,
                    modifier = Modifier.size(icon_size_medium)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoriqueScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    stringResource(id = R.string.history_scans_title),
                    fontWeight = FontWeight.Bold
                )
            }
        )

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
                modifier = Modifier.size(icon_size_medium)
            )
        }
        HorizontalDivider(color = Color.LightGray)
    }
}

fun hasRequiredPermissions(context: Context, permissions: Array<String>): Boolean {
    return permissions.all { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun ScanStatusItem(
    modifier: Modifier = Modifier,
    text: String,
    backgroundColor: Color = Color.Gray
) {
    Surface(
        color = backgroundColor,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = border_thickness)
    ) {
        Text(
            text = text,
            color = Color.White,
            modifier = Modifier.padding(horizontal = medium_space, vertical = extra_small_space),
            fontSize = medium_small_text
        )
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
        containerColor = Color.White,
        tonalElevation = elevation_none
    ) {
        NavigationBarItem(
            selected = currentRoute == NavScreen.MainMenu.route || 
                       currentRoute?.startsWith("device_info") == true || 
                       currentRoute == NavScreen.CVE.route,
            onClick = { onScreenSelected(NavScreen.MainMenu) },
            icon = { BottomNavIconPlaceholder(shape = "triangle_up") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0xFFE8EDFF)
            )
        )
        NavigationBarItem(
            selected = currentRoute == NavScreen.Parametres.route,
            onClick = { onScreenSelected(NavScreen.Parametres) },
            icon = { BottomNavIconPlaceholder(shape = "square_x") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0xFFE8EDFF)
            )
        )
        NavigationBarItem(
            selected = currentRoute == NavScreen.Historique.route,
            onClick = { onScreenSelected(NavScreen.Historique) },
            icon = { BottomNavIconPlaceholder(shape = "triangle_down") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0xFFE8EDFF)
            )
        )
    }
}

@Composable
fun BottomNavIconPlaceholder(modifier: Modifier = Modifier, shape: String) {
    Box(
        modifier = modifier
            .size(huge_space)
            .border(border_thickness, Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (shape) {
                "triangle_up" -> "▲"
                "square_x" -> "⊠"
                "triangle_down" -> "▼"
                else -> ""
            },
            color = Color.Gray,
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
        MainMenuScreen(devices = emptyList())
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
