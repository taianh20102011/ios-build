package com.aegis.security.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aegis.security.core.integrity.IntegrityCheckResult
import com.aegis.security.core.integrity.RiskLevel
import com.aegis.security.core.network.NetworkInspectionResult
import com.aegis.security.core.scanner.AppRiskItem
import com.aegis.security.core.scanner.HardeningCheckItem

@Composable
fun DashboardScreen(
    isScanning: Boolean,
    rootStatus: IntegrityCheckResult?,
    hookStatus: IntegrityCheckResult?,
    tamperStatus: IntegrityCheckResult?,
    hardeningItems: List<HardeningCheckItem>,
    networkStatus: NetworkInspectionResult?,
    highRiskAppsCount: Int,
    onStartDeepScan: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Calculate overall security score (0-100)
    var score = 100
    if (rootStatus?.isCompromised == true) score -= 35
    if (hookStatus?.isCompromised == true) score -= 30
    if (tamperStatus?.isCompromised == true) score -= 15
    if (highRiskAppsCount > 0) score -= (highRiskAppsCount * 10).coerceAtMost(25)
    val insecureHardeningCount = hardeningItems.count { !it.isSecure }
    score -= (insecureHardeningCount * 5).coerceAtMost(15)
    val finalScore = score.coerceIn(0, 100)

    val animatedScore by animateFloatAsState(
        targetValue = finalScore.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = LinearEasing),
        label = "score"
    )

    val scoreColor = when {
        finalScore >= 80 -> CyberSafe
        finalScore >= 50 -> CyberWarning
        else -> CyberError
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Brand Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "AEGIS SHIELD",
                    color = CyberPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Bảo Mật Phần Cứng & Hệ Thống",
                    color = CyberTextSecondary,
                    fontSize = 12.sp
                )
            }
            Surface(
                color = CyberSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isScanning) CyberWarning else CyberSafe)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isScanning) "ĐANG QUÉT..." else "ACTIVE",
                        color = CyberTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Big Shield Status Widget
        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(scoreColor.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
                .border(
                    width = 3.dp,
                    brush = Brush.sweepGradient(
                        listOf(scoreColor, CyberPrimary, scoreColor)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (finalScore >= 80) Icons.Default.Security else Icons.Default.Warning,
                    contentDescription = null,
                    tint = scoreColor,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${animatedScore.toInt()}%",
                    color = CyberTextPrimary,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (finalScore >= 80) "AN TOÀN" else if (finalScore >= 50) "CẦN TỐI ƯU" else "NGUY HIỂM",
                    color = scoreColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Deep Scan Button
        Button(
            onClick = onStartDeepScan,
            enabled = !isScanning,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberPrimary,
                contentColor = Color.Black
            )
        ) {
            Icon(
                imageVector = Icons.Default.Radar,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isScanning) "ĐANG QUÉT SÂU TOÀN DIỆN..." else "QUÉT SÂU TOÀN DIỆN NGAY",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Security Status Grid
        Text(
            text = "TRẠNG THÁI NGUYÊN BẢN & PHÒNG THỦ",
            color = CyberTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        StatusItemCard(
            title = "Kiểm tra Root & Kernel",
            subtitle = rootStatus?.title ?: "Chưa kiểm tra",
            isSafe = rootStatus?.isCompromised == false,
            icon = Icons.Default.Terminal
        )

        Spacer(modifier = Modifier.height(8.dp))

        StatusItemCard(
            title = "Phòng thủ Hooking (Frida / Xposed)",
            subtitle = hookStatus?.title ?: "Chưa kiểm tra",
            isSafe = hookStatus?.isCompromised == false,
            icon = Icons.Default.Memory
        )

        Spacer(modifier = Modifier.height(8.dp))

        StatusItemCard(
            title = "Chống Gỡ lỗi & Can thiệp (Anti-Tamper)",
            subtitle = tamperStatus?.title ?: "Chưa kiểm tra",
            isSafe = tamperStatus?.isCompromised == false,
            icon = Icons.Default.Lock
        )

        Spacer(modifier = Modifier.height(8.dp))

        StatusItemCard(
            title = "Kiểm toán Ứng dụng & Mã độc",
            subtitle = if (highRiskAppsCount == 0) "Không phát hiện app độc hại/Trojan" else "Phát hiện $highRiskAppsCount ứng dụng có rủi ro cao!",
            isSafe = highRiskAppsCount == 0,
            icon = Icons.Default.Apps
        )

        Spacer(modifier = Modifier.height(8.dp))

        StatusItemCard(
            title = "Mạng & Kết nối Wi-Fi",
            subtitle = networkStatus?.connectionType ?: "Đang phân tích",
            isSafe = networkStatus?.isProxyConfigured == false,
            icon = Icons.Default.Wifi
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun StatusItemCard(
    title: String,
    subtitle: String,
    isSafe: Boolean,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSafe) CyberSurfaceVariant else CyberError.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSafe) CyberSafe.copy(alpha = 0.15f) else CyberError.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSafe) CyberSafe else CyberError,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = CyberTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = if (isSafe) CyberTextSecondary else CyberError,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Icon(
                imageVector = if (isSafe) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (isSafe) CyberSafe else CyberError,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
