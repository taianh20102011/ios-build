package com.aegis.security.core.integrity

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.security.MessageDigest

object TamperDetector {

    fun checkTamperAndDebugging(context: Context): IntegrityCheckResult {
        val findings = mutableListOf<String>()

        // 1. Debugger attachment check
        if (Debug.isDebuggerConnected() || Debug.waitingForDebugger()) {
            findings.add("Trình gỡ lỗi (Debugger) đang được kết nối với ứng dụng")
        }

        // 2. TracerPid inspection from /proc/self/status (ptrace detection)
        val tracerPid = getTracerPid()
        if (tracerPid > 0) {
            findings.add("Phát hiện tiến trình bên ngoài đang trace/theo dõi (TracerPid = $tracerPid)")
        }

        // 3. Emulation / Sandbox check (basic heuristics)
        if (isLikelyEmulator()) {
            findings.add("Ứng dụng có thể đang chạy trong môi trường giả lập (Emulator / Sandbox)")
        }

        val isCompromised = findings.isNotEmpty()
        return IntegrityCheckResult(
            isCompromised = isCompromised,
            title = if (isCompromised) "Cảnh báo can thiệp gỡ lỗi / Giả lập" else "Tiến trình an toàn (Chống can thiệp)",
            details = if (findings.isEmpty()) listOf("Không có trình gỡ lỗi (Debugger), ptrace hoặc sandbox trái phép.") else findings,
            riskLevel = if (isCompromised) RiskLevel.WARNING else RiskLevel.SAFE
        )
    }

    fun getAppSignatureSha256(context: Context): String {
        return try {
            val pm = context.packageManager
            val packageName = context.packageName
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                val signingInfo = packageInfo.signingInfo ?: return "UNKNOWN"
                val certBytes = if (signingInfo.hasMultipleSigners()) {
                    signingInfo.apkContentsSigners?.getOrNull(0)?.toByteArray() ?: return "UNKNOWN"
                } else {
                    signingInfo.signingCertificateHistory?.getOrNull(0)?.toByteArray() ?: return "UNKNOWN"
                }
                sha256Hex(certBytes)
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                @Suppress("DEPRECATION")
                val certBytes = packageInfo.signatures?.getOrNull(0)?.toByteArray() ?: return "UNKNOWN"
                sha256Hex(certBytes)
            }
        } catch (_: Throwable) {
            "UNKNOWN"
        }
    }

    private fun sha256Hex(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02X".format(it) }
    }

    private fun getTracerPid(): Int {
        try {
            val statusFile = File("/proc/self/status")
            if (statusFile.canRead()) {
                BufferedReader(FileReader(statusFile)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        if (line.startsWith("TracerPid:")) {
                            val parts = line.split(":")
                            if (parts.size > 1) {
                                return parts[1].trim().toIntOrNull() ?: 0
                            }
                        }
                        line = reader.readLine()
                    }
                }
            }
        } catch (_: Throwable) {
        }
        return 0
    }

    private fun isLikelyEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT
        val model = Build.MODEL
        val manufacturer = Build.MANUFACTURER
        val hardware = Build.HARDWARE
        val product = Build.PRODUCT

        return fingerprint.startsWith("generic") ||
                fingerprint.startsWith("unknown") ||
                model.contains("google_sdk") ||
                model.contains("Emulator") ||
                model.contains("Android SDK built for x86") ||
                manufacturer.contains("Genymotion") ||
                hardware.contains("goldfish") ||
                hardware.contains("ranchu") ||
                product.contains("sdk_gphone") ||
                product.contains("vbox86p")
    }
}
