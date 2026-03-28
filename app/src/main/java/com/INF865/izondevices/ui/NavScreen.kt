package com.INF865.izondevices.ui

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