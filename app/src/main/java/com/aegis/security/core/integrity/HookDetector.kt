package com.aegis.security.core.integrity

import java.io.BufferedReader
import java.io.File
import java.io.FileReader

object HookDetector {

    fun checkHookingFrameworks(): IntegrityCheckResult {
        val findings = mutableListOf<String>()

        // 1. Check for Frida in /proc/self/maps
        checkFridaInMaps(findings)

        // 2. Check Xposed / LSPosed classes in runtime classloader
        checkXposedClasses(findings)

        // 3. Inspect stack trace for hooking frames
        checkStackTraceForHooks(findings)

        val isCompromised = findings.isNotEmpty()
        return IntegrityCheckResult(
            isCompromised = isCompromised,
            title = if (isCompromised) "Phát hiện Hook Framework (Frida/Xposed)" else "Bộ nhớ & Runtime an toàn",
            details = if (findings.isEmpty()) listOf("Không phát hiện Frida agent, Xposed bridge hoặc kỹ thuật tiêm mã (code injection).") else findings,
            riskLevel = if (isCompromised) RiskLevel.CRITICAL else RiskLevel.SAFE
        )
    }

    private fun checkFridaInMaps(findings: MutableList<String>) {
        try {
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.canRead()) {
                BufferedReader(FileReader(mapsFile)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        val lower = line.lowercase()
                        if (lower.contains("frida-agent") || lower.contains("frida-gadget") || lower.contains("frida")) {
                            findings.add("Phát hiện thư viện động Frida tiêm vào bộ nhớ tiến trình")
                            break
                        }
                        line = reader.readLine()
                    }
                }
            }
        } catch (_: Throwable) {
        }
    }

    private fun checkXposedClasses(findings: MutableList<String>) {
        val xposedClasses = arrayOf(
            "de.robv.android.xposed.XposedBridge",
            "de.robv.android.xposed.XposedHelpers",
            "org.lsposed.lspd.nativebridge.LSPDynamicBridge",
            "com.elderdrivers.riru.edxp.core.EdXposed"
        )
        for (className in xposedClasses) {
            try {
                Class.forName(className)
                findings.add("Phát hiện class Hook Framework đang hoạt động: $className")
            } catch (_: ClassNotFoundException) {
            } catch (_: Throwable) {
            }
        }
    }

    private fun checkStackTraceForHooks(findings: MutableList<String>) {
        try {
            val stackTrace = Exception().stackTrace
            for (element in stackTrace) {
                val className = element.className
                if (className.contains("de.robv.android.xposed") ||
                    className.contains("org.lsposed") ||
                    className.contains("frida")
                ) {
                    findings.add("Dấu vết can thiệp (Call Stack) chứa hook: $className")
                    break
                }
            }
        } catch (_: Throwable) {
        }
    }
}
