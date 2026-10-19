package com.aegis.security.core.scanner

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build

data class AppRiskItem(
    val appName: String,
    val packageName: String,
    val icon: Drawable?,
    val isSystemApp: Boolean,
    val installerSource: String,
    val riskScore: Int, // 0 - 100
    val riskLevel: AppRiskLevel,
    val detectedBehaviors: List<String>,
    val dangerousPermissions: List<String>
)

enum class AppRiskLevel {
    SAFE,
    MODERATE,
    ELEVATED,
    CRITICAL
}

object AppRiskScanner {

    private val BANKING_TROJAN_PERMS = listOf(
        "android.permission.BIND_ACCESSIBILITY_SERVICE",
        "android.permission.SYSTEM_ALERT_WINDOW",
        "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"
    )

    private val SPYWARE_PERMS = listOf(
        "android.permission.READ_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.READ_CALL_LOG",
        "android.permission.RECORD_AUDIO",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_BACKGROUND_LOCATION",
        "android.permission.READ_CONTACTS"
    )

    fun scanInstalledApps(context: Context): List<AppRiskItem> {
        val pm = context.packageManager
        val packages: List<PackageInfo> = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
            } else {
                pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            }
        } catch (_: Throwable) {
            emptyList()
        }

        val results = mutableListOf<AppRiskItem>()

        for (pkg in packages) {
            // Skip own package
            if (pkg.packageName == context.packageName) continue

            val appInfo = pkg.applicationInfo ?: continue
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val appName = try {
                pm.getApplicationLabel(appInfo).toString()
            } catch (_: Throwable) {
                pkg.packageName
            }
            val icon = try {
                pm.getApplicationIcon(appInfo)
            } catch (_: Throwable) {
                null
            }

            val installerSource = getInstallerSource(pm, pkg.packageName)
            val requestedPerms = pkg.requestedPermissions?.toList() ?: emptyList()

            val behaviors = mutableListOf<String>()
            val flaggedPerms = mutableListOf<String>()
            var calculatedScore = 0

            val hasOverlay = requestedPerms.contains("android.permission.SYSTEM_ALERT_WINDOW")
            val hasAccessibility = requestedPerms.contains("android.permission.BIND_ACCESSIBILITY_SERVICE")
            val hasNotificationListener = requestedPerms.contains("android.permission.BIND_NOTIFICATION_LISTENER_SERVICE")
            val hasDeviceAdmin = requestedPerms.contains("android.permission.BIND_DEVICE_ADMIN")

            // Critical Check: Classic Android Trojan Pattern (Accessibility + Overlay)
            if (hasAccessibility && hasOverlay && !isSystem) {
                calculatedScore += 50
                behaviors.add("Hành vi nguy hiểm: Vừa có quyền Trợ năng (Accessibility) vừa có quyền Vẽ đè màn hình (Overlay) - Nguy cơ tấn công chiếm quyền thiết bị / đánh cắp OTP ngân hàng.")
                flaggedPerms.add("SYSTEM_ALERT_WINDOW")
                flaggedPerms.add("BIND_ACCESSIBILITY_SERVICE")
            } else if (hasAccessibility && !isSystem) {
                calculatedScore += 25
                behaviors.add("Sử dụng quyền Trợ năng (Accessibility Service) - Cần chú ý nếu không phải app hỗ trợ người khuyết tật.")
                flaggedPerms.add("BIND_ACCESSIBILITY_SERVICE")
            }

            if (hasNotificationListener && !isSystem) {
                calculatedScore += 15
                behaviors.add("Có quyền đọc tất cả thông báo hệ thống (Notification Listener) - Có thể đọc nội dung tin nhắn OTP.")
                flaggedPerms.add("BIND_NOTIFICATION_LISTENER_SERVICE")
            }

            if (hasDeviceAdmin && !isSystem) {
                calculatedScore += 30
                behaviors.add("Quyền Quản trị viên thiết bị (Device Admin) - Có thể tự khóa máy hoặc ngăn gỡ cài đặt.")
                flaggedPerms.add("BIND_DEVICE_ADMIN")
            }

            // Sideloaded untrusted source check
            val isSideloaded = installerSource == "Tải từ bên ngoài (APK Sideload)"
            if (isSideloaded && !isSystem) {
                calculatedScore += 10
            }

            // Spyware permissions
            var spyPermsCount = 0
            for (sp in SPYWARE_PERMS) {
                if (requestedPerms.contains(sp)) {
                    spyPermsCount++
                    flaggedPerms.add(sp.substringAfterLast("."))
                }
            }

            if (spyPermsCount >= 3 && !isSystem) {
                calculatedScore += 25
                behaviors.add("Yêu cầu đồng thời nhiều quyền nhạy cảm (Ghi âm, Vị trí nền, Tin nhắn, Danh bạ, Nhật ký cuộc gọi).")
            }

            // Reduce score for recognized system apps to prevent false alarms
            if (isSystem) {
                calculatedScore = (calculatedScore / 3).coerceAtMost(20)
            }

            val finalScore = calculatedScore.coerceIn(0, 100)
            val riskLevel = when {
                finalScore >= 50 -> AppRiskLevel.CRITICAL
                finalScore >= 30 -> AppRiskLevel.ELEVATED
                finalScore >= 15 -> AppRiskLevel.MODERATE
                else -> AppRiskLevel.SAFE
            }

            results.add(
                AppRiskItem(
                    appName = appName,
                    packageName = pkg.packageName,
                    icon = icon,
                    isSystemApp = isSystem,
                    installerSource = installerSource,
                    riskScore = finalScore,
                    riskLevel = riskLevel,
                    detectedBehaviors = behaviors,
                    dangerousPermissions = flaggedPerms.distinct()
                )
            )
        }

        // Sort descending by risk score
        return results.sortedByDescending { it.riskScore }
    }

    private fun getInstallerSource(pm: PackageManager, packageName: String): String {
        return try {
            val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                pm.getInstallSourceInfo(packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(packageName)
            }

            when (installer) {
                "com.android.vending" -> "Google Play Store"
                "com.google.android.packageinstaller" -> "Trình cài đặt hệ thống"
                "com.sec.android.app.samsungapps" -> "Galaxy Store"
                "com.xiaomi.mipicks" -> "Xiaomi GetApps"
                null -> "Tải từ bên ngoài (APK Sideload)"
                else -> installer
            }
        } catch (_: Throwable) {
            "Không xác định"
        }
    }
}
