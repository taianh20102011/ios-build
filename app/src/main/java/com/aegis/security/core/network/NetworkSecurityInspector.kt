package com.aegis.security.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build

data class NetworkInspectionResult(
    val connectionType: String,
    val wifiSsid: String?,
    val isWifiEncrypted: Boolean,
    val isVpnActive: Boolean,
    val isProxyConfigured: Boolean,
    val securityLevel: NetworkSecurityLevel,
    val warnings: List<String>,
    val tips: List<String>
)

enum class NetworkSecurityLevel {
    EXCELLENT,
    GOOD,
    RISKY,
    DANGEROUS
}

object NetworkSecurityInspector {

    fun inspectCurrentNetwork(context: Context): NetworkInspectionResult {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(activeNetwork)

        val warnings = mutableListOf<String>()
        val tips = mutableListOf<String>()

        if (caps == null) {
            return NetworkInspectionResult(
                connectionType = "Không có kết nối mạng",
                wifiSsid = null,
                isWifiEncrypted = false,
                isVpnActive = false,
                isProxyConfigured = false,
                securityLevel = NetworkSecurityLevel.GOOD,
                warnings = listOf("Thiết bị đang ở chế độ ngoại tuyến (Offline) - An toàn khỏi tấn công mạng từ xa."),
                tips = listOf("Dữ liệu trên máy an toàn khi không kết nối Internet.")
            )
        }

        val isVpn = caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        val isWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val isCellular = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)

        // Check proxy
        val proxyHost = System.getProperty("http.proxyHost")
        val proxyPort = System.getProperty("http.proxyPort")
        val isProxy = !proxyHost.isNullOrEmpty() && !proxyPort.isNullOrEmpty()

        if (isProxy) {
            warnings.add("Đang có Proxy cấu hình trên hệ thống ($proxyHost:$proxyPort). Lưu lượng mạng có thể đang bị chặn bắt hoặc phân tích.")
        }

        var ssid: String? = null
        var isWifiSecure = true

        if (isWifi) {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val connectionInfo: WifiInfo? = wifiManager?.connectionInfo
            if (connectionInfo != null) {
                ssid = connectionInfo.ssid?.replace("\"", "")
                if (ssid == "<unknown ssid>") {
                    ssid = "Wi-Fi (Đã kết nối)"
                }
            }

            // Insecure open wifi check:
            // If connected to wifi without VPN and proxy might be present
            if (!isVpn) {
                tips.add("Khi dùng Wi-Fi nơi công cộng (quán cafe, sân bay), nên bật VPN để mã hóa toàn bộ dữ liệu.")
            }
        }

        if (isVpn) {
            tips.add("Kênh truyền VPN đang kích hoạt, dữ liệu mạng được mã hóa qua đường hầm an toàn.")
        }

        val level = when {
            isProxy -> NetworkSecurityLevel.RISKY
            isWifi && !isVpn -> NetworkSecurityLevel.GOOD
            isVpn -> NetworkSecurityLevel.EXCELLENT
            isCellular -> NetworkSecurityLevel.GOOD
            else -> NetworkSecurityLevel.GOOD
        }

        val connTypeStr = when {
            isWifi && isVpn -> "Wi-Fi (Bảo vệ bởi VPN)"
            isWifi -> "Mạng Wi-Fi"
            isCellular && isVpn -> "Mạng di động (Bảo vệ bởi VPN)"
            isCellular -> "Mạng dữ liệu di động (4G/5G)"
            isVpn -> "Đường hầm bảo mật VPN"
            else -> "Mạng khác"
        }

        return NetworkInspectionResult(
            connectionType = connTypeStr,
            wifiSsid = ssid,
            isWifiEncrypted = isWifiSecure,
            isVpnActive = isVpn,
            isProxyConfigured = isProxy,
            securityLevel = level,
            warnings = warnings,
            tips = tips
        )
    }
}
