package com.aegis.security.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.aegis.security.core.integrity.HookDetector
import com.aegis.security.core.integrity.IntegrityCheckResult
import com.aegis.security.core.integrity.RootDetector
import com.aegis.security.core.integrity.TamperDetector
import com.aegis.security.core.network.NetworkInspectionResult
import com.aegis.security.core.network.NetworkSecurityInspector
import com.aegis.security.core.scanner.AppRiskItem
import com.aegis.security.core.scanner.AppRiskLevel
import com.aegis.security.core.scanner.AppRiskScanner
import com.aegis.security.core.scanner.DeviceHardeningScanner
import com.aegis.security.core.scanner.HardeningCheckItem
import com.aegis.security.core.vault.HardwareVault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ScreenTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Tổng quan", Icons.Default.Shield),
    APP_AUDIT("Kiểm toán App", Icons.Default.Apps),
    DEVICE_NET("Thiết bị & Mạng", Icons.Default.Devices),
    VAULT("Két sắt", Icons.Default.Lock)
}

class MainActivity : FragmentActivity() {

    private lateinit var vault: HardwareVault

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enforce Anti-Screen-Capture & Anti-Recording across the app
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        vault = HardwareVault(applicationContext)

        setContent {
            AegisSecurityTheme {
                var currentTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }
                var isScanning by remember { mutableStateOf(false) }

                var rootStatus by remember { mutableStateOf<IntegrityCheckResult?>(null) }
                var hookStatus by remember { mutableStateOf<IntegrityCheckResult?>(null) }
                var tamperStatus by remember { mutableStateOf<IntegrityCheckResult?>(null) }
                var appsList by remember { mutableStateOf<List<AppRiskItem>>(emptyList()) }
                var hardeningItems by remember { mutableStateOf<List<HardeningCheckItem>>(emptyList()) }
                var networkStatus by remember { mutableStateOf<NetworkInspectionResult?>(null) }

                fun runSecurityAudit() {
                    lifecycleScope.launch {
                        isScanning = true
                        withContext(Dispatchers.IO) {
                            val r = RootDetector.checkDeviceRoot(applicationContext)
                            val h = HookDetector.checkHookingFrameworks()
                            val t = TamperDetector.checkTamperAndDebugging(applicationContext)
                            val a = AppRiskScanner.scanInstalledApps(applicationContext)
                            val d = DeviceHardeningScanner.scanDevicePosture(applicationContext)
                            val n = NetworkSecurityInspector.inspectCurrentNetwork(applicationContext)

                            withContext(Dispatchers.Main) {
                                rootStatus = r
                                hookStatus = h
                                tamperStatus = t
                                appsList = a
                                hardeningItems = d
                                networkStatus = n
                                isScanning = false
                            }
                        }
                    }
                }

                // Initial scan on launch
                LaunchedEffect(Unit) {
                    runSecurityAudit()
                }

                val highRiskCount = appsList.count {
                    it.riskLevel == AppRiskLevel.CRITICAL || it.riskLevel == AppRiskLevel.ELEVATED
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = CyberBackground,
                    bottomBar = {
                        NavigationBar(
                            containerColor = CyberSurface,
                            tonalElevation = NavigationBarDefaults.Elevation
                        ) {
                            ScreenTab.values().forEach { tab ->
                                NavigationBarItem(
                                    selected = currentTab == tab,
                                    onClick = { currentTab = tab },
                                    icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                                    label = { Text(text = tab.title) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = CyberPrimary,
                                        selectedTextColor = CyberPrimary,
                                        indicatorColor = CyberSurfaceVariant,
                                        unselectedIconColor = CyberTextSecondary,
                                        unselectedTextColor = CyberTextSecondary
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            ScreenTab.DASHBOARD -> DashboardScreen(
                                isScanning = isScanning,
                                rootStatus = rootStatus,
                                hookStatus = hookStatus,
                                tamperStatus = tamperStatus,
                                hardeningItems = hardeningItems,
                                networkStatus = networkStatus,
                                highRiskAppsCount = highRiskCount,
                                onStartDeepScan = { runSecurityAudit() }
                            )
                            ScreenTab.APP_AUDIT -> AppAuditScreen(
                                appList = appsList,
                                isScanning = isScanning
                            )
                            ScreenTab.DEVICE_NET -> DeviceAndNetworkScreen(
                                hardeningItems = hardeningItems,
                                networkStatus = networkStatus
                            )
                            ScreenTab.VAULT -> VaultScreen(
                                vault = vault,
                                activity = this@MainActivity
                            )
                        }
                    }
                }
            }
        }
    }
}
