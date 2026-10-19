package com.aegis.security.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aegis.security.core.network.NetworkInspectionResult
import com.aegis.security.core.network.NetworkSecurityLevel
import com.aegis.security.core.scanner.HardeningCheckItem

@Composable
fun DeviceAndNetworkScreen(
    hardeningItems: List<HardeningCheckItem>,
    networkStatus: NetworkInspectionResult?
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "AN NINH HỆ THỐNG & MẠNG",
            color = CyberPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Gia cố bề mặt tấn công thiết bị và kiểm tra an toàn kết nối",
            color = CyberTextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Network Inspection Card
        Text(
            text = "KIỂM TRA KẾT NỐI MẠNG",
            color = CyberTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = CyberPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = networkStatus?.connectionType ?: "Mạng chưa xác định",
                            color = CyberTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        if (networkStatus?.wifiSsid != null) {
                            Text(
                                text = "Tên mạng: ${networkStatus.wifiSsid}",
                                color = CyberTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = CyberSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Đường hầm VPN:", color = CyberTextSecondary, fontSize = 12.sp)
                    Text(
                        text = if (networkStatus?.isVpnActive == true) "Đang kích hoạt" else "Chưa bật",
                        color = if (networkStatus?.isVpnActive == true) CyberSafe else CyberTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Trạng thái Proxy:", color = CyberTextSecondary, fontSize = 12.sp)
                    Text(
                        text = if (networkStatus?.isProxyConfigured == true) "Cảnh báo: Có Proxy" else "Không có Proxy (An toàn)",
                        color = if (networkStatus?.isProxyConfigured == true) CyberError else CyberSafe,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (networkStatus?.warnings?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(8.dp))
                    networkStatus.warnings.forEach { w ->
                        Text(text = "⚠ $w", color = CyberError, fontSize = 11.sp)
                    }
                }

                if (networkStatus?.tips?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(8.dp))
                    networkStatus.tips.forEach { t ->
                        Text(text = "💡 $t", color = CyberPrimary, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Device Hardening Section
        Text(
            text = "DANH SÁCH GIA CỐ THIẾT BỊ (DEVICE HARDENING)",
            color = CyberTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        hardeningItems.forEach { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (item.isSecure) CyberSurfaceVariant else CyberWarning.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (item.isSecure) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (item.isSecure) CyberSafe else CyberWarning,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.title,
                            color = CyberTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.statusText,
                        color = if (item.isSecure) CyberSafe else CyberWarning,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.recommendation,
                        color = CyberTextSecondary,
                        fontSize = 11.sp
                    )

                    if (!item.isSecure && item.settingsAction != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                try {
                                    context.startActivity(Intent(item.settingsAction))
                                } catch (_: Throwable) {
                                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberSurfaceVariant,
                                contentColor = CyberWarning
                            )
                        ) {
                            Text(text = "Mở cài đặt để khắc phục ngay", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
