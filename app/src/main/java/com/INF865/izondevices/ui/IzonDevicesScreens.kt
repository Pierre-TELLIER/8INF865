package com.INF865.izondevices.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.core.content.ContextCompat
import com.INF865.izondevices.model.NetworkDevice
import com.INF865.izondevices.R
import com.INF865.izondevices.service.NetworkScanService
import com.INF865.izondevices.ui.theme.*
import java.util.concurrent.CompletableFuture

sealed class NavScreen(val route: String) {
    data object MainMenu : NavScreen("main_menu")
    data object DeviceInfo : NavScreen("device_info")
    data object CVE : NavScreen("cve")
    data object Parametres : NavScreen("parametres")
    data object Historique : NavScreen("historique")
    data object Scan : NavScreen("scan")
}

@Composable
fun IzonDevicesApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF9F9FB),
        bottomBar = {
            if (currentRoute != NavScreen.Scan.route) {
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
                    onDeviceClick = { navController.navigate(NavScreen.DeviceInfo.route) },
                    onScanClick = { navController.navigate(NavScreen.Scan.route) }
                )
            }
            composable(NavScreen.DeviceInfo.route) {
                DeviceInfoScreen(
                    onBack = { navController.popBackStack() },
                    onVulnerabilityClick = { navController.navigate(NavScreen.CVE.route) }
                )
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
                    onCancel = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun MainMenuScreen(
    modifier: Modifier = Modifier,
    onDeviceClick: () -> Unit = {},
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

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(grid_spacing),
            verticalArrangement = Arrangement.spacedBy(grid_spacing),
            contentPadding = PaddingValues(bottom = medium_space)
        ) {
            items(4) {
                DeviceItem(onClick = onDeviceClick)
            }
        }

        Spacer(modifier = Modifier.height(medium_space))

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray, thickness = divider_thickness)
                Spacer(modifier = Modifier.width(grid_spacing))
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray, thickness = divider_thickness)
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
fun DeviceItem(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(border_thickness, Color.Gray)
            .clickable(onClick = onClick)
            .padding(large_space),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(border_thickness, Color.Gray)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceInfoScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onVulnerabilityClick: () -> Unit = {}
) {
    Column(modifier = modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = { Text(stringResource(id = R.string.device_info_title), fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(painterResource(id = R.drawable.ic_arrow_back), contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(painterResource(id = R.drawable.ic_refresh), contentDescription = "Refresh")
                }
            }
        )

        Column(
            modifier = Modifier
                .padding(horizontal = medium_space)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color.DarkGray,
                shape = RoundedCornerShape(extra_small_space),
                modifier = Modifier.padding(vertical = small_space)
            ) {
                Text(
                    text = stringResource(id = R.string.vulnerabilities_detected),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = medium_space, vertical = extra_small_space),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = stringResource(id = R.string.ip_address),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(medium_space))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(mega_space)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(medium_space))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(id = R.string.name_label))
                        Spacer(modifier = Modifier.width(extra_small_space))
                        Icon(painterResource(id = R.drawable.ic_edit), contentDescription = null, modifier = Modifier.size(icon_size_small))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(id = R.string.type_label))
                        Spacer(modifier = Modifier.width(extra_small_space))
                        Icon(painterResource(id = R.drawable.ic_edit), contentDescription = null, modifier = Modifier.size(icon_size_small))
                    }
                    Spacer(modifier = Modifier.height(extra_small_space))
                    Box(modifier = Modifier.width(bar_width_large).height(small_space).background(Color.LightGray))
                    Spacer(modifier = Modifier.height(extra_small_space))
                    Box(modifier = Modifier.width(bar_width_medium).height(small_space).background(Color.LightGray))
                }
            }

            Spacer(modifier = Modifier.height(large_space))

            Text(
                text = stringResource(id = R.string.vulnerabilities_label),
                modifier = Modifier.align(Alignment.Start),
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(small_space))

            VulnerabilityItem(onClick = onVulnerabilityClick)
            Spacer(modifier = Modifier.height(small_space))
            VulnerabilityItem(onClick = onVulnerabilityClick)

            Spacer(modifier = Modifier.height(large_space))

            Text(
                text = stringResource(id = R.string.actions_label),
                modifier = Modifier.align(Alignment.Start),
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(medium_space))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(text = stringResource(id = R.string.ping_label))
                ActionButton(text = stringResource(id = R.string.port_scan_label))
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
                    Box(modifier = Modifier.width(bar_width_small).height(small_space).background(Color.LightGray))
                }
                Icon(painterResource(id = R.drawable.ic_chevron_right), contentDescription = null)
            }
        }
    }
}

@Composable
fun ActionButton(modifier: Modifier = Modifier, text: String) {
    OutlinedButton(
        onClick = { },
        shape = RoundedCornerShape(small_medium_space),
        modifier = modifier.height(huge_space)
    ) {
        Text(text = text, color = Color.Black)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CVEScreen(modifier: Modifier = Modifier, onBack: () -> Unit = {}) {
    Column(modifier = modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = { Text(stringResource(id = R.string.vulnerability_title), fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(painterResource(id = R.drawable.ic_arrow_back), contentDescription = "Back")
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
            title = { Text(stringResource(id = R.string.parameters_title), fontWeight = FontWeight.Bold) }
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
        Box(modifier = Modifier.width(bar_width_large).height(small_space).background(Color(0xFFE0E0E0)))
        
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
                    Box(modifier = Modifier.padding(tiny_space).size(medium_space).clip(CircleShape).background(Color.Gray))
                }
            }
            1, 2 -> {
                Icon(painterResource(id = R.drawable.ic_edit), contentDescription = null, modifier = Modifier.size(icon_size_medium))
            }
            else -> {
                Icon(painterResource(id = R.drawable.ic_chevron_right), contentDescription = null, modifier = Modifier.size(icon_size_medium))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoriqueScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = { Text(stringResource(id = R.string.history_scans_title), fontWeight = FontWeight.Bold) }
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
            Icon(painterResource(id = R.drawable.ic_chevron_right), contentDescription = null, modifier = Modifier.size(icon_size_medium))
        }
        HorizontalDivider(color = Color.LightGray)
    }
}

@Composable
fun ScanScreen(modifier: Modifier = Modifier, onCancel: () -> Unit = {}) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val requiredPermissions = remember {
        arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
        )
    }
    var isScanning by remember { mutableStateOf(!isPreview) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var foundDevices by remember { mutableStateOf<List<NetworkDevice>>(emptyList()) }
    var permissionsGranted by remember {
        mutableStateOf(hasRequiredPermissions(context, requiredPermissions))
    }
    val activeScan = remember { mutableStateOf<CompletableFuture<List<NetworkDevice>>?>(null) }

    // check if permissions have been given
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grantResults ->
        permissionsGranted = requiredPermissions.all { permission ->
            grantResults[permission] == true ||
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        if (!permissionsGranted) {
            isScanning = false
            errorMessage = "Internet and network state permissions are required"
        }
    }

    LaunchedEffect(isPreview, permissionsGranted) {
        if (!isPreview && !permissionsGranted) {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    DisposableEffect(context, isPreview, permissionsGranted) {
        if (isPreview) {
            foundDevices = listOf(
                NetworkDevice(ipAddress = "192.168.1.10", macAddress = "AA:BB:CC:DD:EE:01"),
                NetworkDevice(ipAddress = "192.168.1.20", macAddress = "AA:BB:CC:DD:EE:02")
            )
            isScanning = false
            onDispose { }
        } else if (!permissionsGranted) {
            isScanning = false
            onDispose { }
        } else {
            var isBound = false
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as? NetworkScanService.LocalBinder ?: return
                    val networkService = binder.getService()
                    isScanning = true
                    errorMessage = null

                    val future = networkService.scanNetwork()
                    activeScan.value = future
                    future.whenComplete { devices, throwable ->
                        Handler(Looper.getMainLooper()).post {
                            isScanning = false
                            if (throwable != null && !future.isCancelled) {
                                errorMessage = throwable.message ?: "Scan failed"
                            } else if (!future.isCancelled) {
                                foundDevices = devices ?: emptyList()
                            }
                        }
                    }
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    isBound = false
                }
            }

            val serviceIntent = Intent(context, NetworkScanService::class.java)
            isBound = context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
            if (!isBound) {
                isScanning = false
                errorMessage = "Unable to bind scanner service"
            }

            onDispose {
                activeScan.value?.cancel(true)
                if (isBound) {
                    runCatching { context.unbindService(connection) }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(medium_space),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = Color.DarkGray,
            shape = RoundedCornerShape(extra_small_space)
        ) {
            Text(
                text = if (isScanning) stringResource(id = R.string.scan_in_progress) else "Scan complete",
                color = Color.White,
                modifier = Modifier.padding(horizontal = large_space, vertical = small_medium_space),
                fontSize = medium_large_text
            )
        }

        Spacer(modifier = Modifier.height(giant_space))

        Box(modifier = Modifier.size(mega_space), contentAlignment = Alignment.Center) {
            if (isScanning) {
                CircularProgressIndicator(color = Color.Gray, strokeWidth = tiny_space)
            } else {
                Text(
                    text = foundDevices.size.toString(),
                    color = Color.Gray,
                    fontSize = large_text,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(mega_space))

        Button(
            onClick = {
                activeScan.value?.cancel(true)
                onCancel()
            },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(button_height),
            shape = RoundedCornerShape(small_medium_space),
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
        ) {
            Text(
                text = stringResource(id = R.string.cancel),
                color = Color.Black,
                fontSize = medium_text,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Column(modifier = Modifier.fillMaxWidth()) {
            if (errorMessage != null) {
                ScanStatusItem(text = errorMessage.orEmpty(), backgroundColor = Color(0xFF9C2F2F))
            } else if (foundDevices.isEmpty() && !isScanning) {
                ScanStatusItem(text = "No devices found")
            } else {
                foundDevices.forEach { device ->
                    val macLabel = device.macAddress ?: "Unknown MAC"
                    ScanStatusItem(text = "${device.ipAddress} - $macLabel")
                }
            }
        }
    }
}

private fun hasRequiredPermissions(context: Context, permissions: Array<String>): Boolean {
    return permissions.all { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun ScanStatusItem(modifier: Modifier = Modifier, text: String, backgroundColor: Color = Color.Gray) {
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
            selected = currentRoute == NavScreen.MainMenu.route || currentRoute == NavScreen.DeviceInfo.route || currentRoute == NavScreen.CVE.route,
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
            text = when(shape) {
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

@Preview(showBackground = true)
@Composable
fun MainMenuPreview() {
    IzondevicesTheme {
        MainMenuScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun DeviceInfoPreview() {
    IzondevicesTheme {
        DeviceInfoScreen()
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
