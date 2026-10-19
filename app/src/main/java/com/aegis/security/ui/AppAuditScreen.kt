package com.aegis.security.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aegis.security.core.scanner.AppRiskItem
import com.aegis.security.core.scanner.AppRiskLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppAuditScreen(
    appList: List<AppRiskItem>,
    isScanning: Boolean
) {
    var selectedFilter by remember { mutableStateOf(0) } // 0: All, 1: High Risk, 2: Safe
    val context = LocalContext.current

    val filteredList = remember(appList, selectedFilter) {
        when (selectedFilter) {
            1 -> appList.filter { it.riskLevel == AppRiskLevel.CRITICAL || it.riskLevel == AppRiskLevel.ELEVATED }
            2 -> appList.filter { it.riskLevel == AppRiskLevel.SAFE }
            else -> appList
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "KIỂM TOÁN RỦI RO ỨNG DỤNG",
            color = CyberPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Phát hiện mã độc, Trojan ngân hàng & lạm dụng quyền trợ năng không báo nhầm",
            color = CyberTextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == 0,
                onClick = { selectedFilter = 0 },
                label = { Text("Tất cả (${appList.size})") }
            )
            FilterChip(
                selected = selectedFilter == 1,
                onClick = { selectedFilter = 1 },
                label = {
                    val count = appList.count { it.riskLevel == AppRiskLevel.CRITICAL || it.riskLevel == AppRiskLevel.ELEVATED }
                    Text("Cần chú ý ($count)", color = if (count > 0) CyberError else CyberTextPrimary)
                }
            )
            FilterChip(
                selected = selectedFilter == 2,
                onClick = { selectedFilter = 2 },
                label = { Text("An toàn") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isScanning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = CyberPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Đang phân tích quyền & chữ ký số các ứng dụng...",
                        color = CyberTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Không có ứng dụng nào trong danh mục này.",
                    color = CyberTextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(filteredList, key = { it.packageName }) { appItem ->
                    AppRiskCard(
                        appItem = appItem,
                        onOpenSettings = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", appItem.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppRiskCard(
    appItem: AppRiskItem,
    onOpenSettings: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val levelColor = when (appItem.riskLevel) {
        AppRiskLevel.CRITICAL -> CyberError
        AppRiskLevel.ELEVATED -> CyberWarning
        AppRiskLevel.MODERATE -> Color(0xFFFFD54F)
        AppRiskLevel.SAFE -> CyberSafe
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (appItem.riskLevel == AppRiskLevel.CRITICAL) CyberError.copy(alpha = 0.6f) else CyberSurfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (appItem.isSystemApp) Icons.Default.VerifiedUser else Icons.Default.Android,
                        contentDescription = null,
                        tint = if (appItem.isSystemApp) CyberPrimary else CyberTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appItem.appName,
                        color = CyberTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = appItem.packageName,
                        color = CyberTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                    Text(
                        text = "Nguồn: ${appItem.installerSource}",
                        color = CyberTextSecondary.copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = levelColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when (appItem.riskLevel) {
                            AppRiskLevel.CRITICAL -> "RỦI RO CAO"
                            AppRiskLevel.ELEVATED -> "CẢNH BÁO"
                            AppRiskLevel.MODERATE -> "TRUNG BÌNH"
                            AppRiskLevel.SAFE -> "AN TOÀN"
                        },
                        color = levelColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Divider(color = CyberSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (appItem.detectedBehaviors.isNotEmpty()) {
                        Text(
                            text = "HÀNH VI PHÁT HIỆN:",
                            color = CyberWarning,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        appItem.detectedBehaviors.forEach { b ->
                            Text(
                                text = "• $b",
                                color = CyberTextPrimary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (appItem.dangerousPermissions.isNotEmpty()) {
                        Text(
                            text = "QUYỀN TRUY CẬP ĐÁNG CHÚ Ý:",
                            color = CyberTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = appItem.dangerousPermissions.joinToString(", "),
                            color = CyberPrimary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberSurfaceVariant,
                            contentColor = CyberPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Quản lý quyền / Gỡ cài đặt trong Cài đặt", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
