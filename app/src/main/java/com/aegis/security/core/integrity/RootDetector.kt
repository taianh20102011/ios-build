package com.aegis.security.core.integrity

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

data class IntegrityCheckResult(
    val isCompromised: Boolean,
    val title: String,
    val details: List<String>,
    val riskLevel: RiskLevel
)

enum class RiskLevel {
    SAFE,
    WARNING,
    CRITICAL
}

object RootDetector {

    private val SU_PATHS = arrayOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su",
        "/vendor/bin/su",
        "/system/xbin/daemonsu"
    )

    private val ROOT_PACKAGES = arrayOf(
        "com.noshufou.android.su",
        "com.thirdparty.superuser",
        "eu.chainfire.supersu",
        "com.koushikdutta.superuser",
        "com.topjohnwu.magisk",
        "io.github.vvb2060.magisk",
        "me.weishu.kernelsu",
        "io.github.a13e300.ksu",
        "com.chelpus.lackypatch"
    )

    fun checkDeviceRoot(context: Context): IntegrityCheckResult {
        val findings = mutableListOf<String>()

        // 1. Check test-keys in Build tags
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            findings.add("ROM Build Tags chứa 'test-keys' (bản build tùy chỉnh hoặc không bảo mật)")
        }

        // 2. Check SU binary in known system paths
        for (path in SU_PATHS) {
            try {
                val file = File(path)
                if (file.exists()) {
                    findings.add("Tìm thấy tệp nhị phân SU tại: $path")
                }
            } catch (_: SecurityException) {
            }
        }

        // 3. Check 'which su' execution
        if (canExecuteSu()) {
            findings.add("Có thể thực thi lệnh 'su' trực tiếp trong shell hệ thống")
        }

        // 4. Check known root management packages
        val pm = context.packageManager
        for (pkg in ROOT_PACKAGES) {
            try {
                pm.getPackageInfo(pkg, 0)
                findings.add("Phát hiện ứng dụng quản lý Root / Mod: $pkg")
            } catch (_: PackageManager.NameNotFoundException) {
            }
        }

        // 5. Inspect /proc/mounts for Magisk, KernelSU, or overlay mounts
        checkMountPoints(findings)

        val isCompromised = findings.isNotEmpty()
        return IntegrityCheckResult(
            isCompromised = isCompromised,
            title = if (isCompromised) "Thiết bị đã bị Root / Mod ROM" else "Hệ điều hành nguyên bản (Chưa Root)",
            details = if (findings.isEmpty()) listOf("Không phát hiện SU binary, Magisk, KernelSU hoặc test-keys.") else findings,
            riskLevel = if (isCompromised) RiskLevel.CRITICAL else RiskLevel.SAFE
        )
    }

    private fun canExecuteSu(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.readLine() != null
        } catch (_: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }

    private fun checkMountPoints(findings: MutableList<String>) {
        try {
            val mountsFile = File("/proc/mounts")
            if (mountsFile.canRead()) {
                mountsFile.forEachLine { line ->
                    if (line.contains("magisk", ignoreCase = true)) {
                        findings.add("Phát hiện mount point liên quan tới Magisk")
                    } else if (line.contains("kernelsu", ignoreCase = true) || line.contains("ksu", ignoreCase = true)) {
                        findings.add("Phát hiện mount point liên quan tới KernelSU")
                    }
                }
            }
        } catch (_: Throwable) {
        }
    }
}
