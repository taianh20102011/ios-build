package com.aegis.security.core.scanner

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

data class HardeningCheckItem(
    val id: String,
    val title: String,
    val statusText: String,
    val isSecure: Boolean,
    val recommendation: String,
    val settingsAction: String?
)

object DeviceHardeningScanner {

    fun scanDevicePosture(context: Context): List<HardeningCheckItem> {
        val list = mutableListOf<HardeningCheckItem>()

        // 1. Lock screen & Biometric
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val isScreenLockSecure = keyguardManager?.isDeviceSecure == true
        list.add(
            HardeningCheckItem(
                id = "screen_lock",
                title = "Khóa màn hình & Sinh trắc học",
                statusText = if (isScreenLockSecure) "Đã bật (PIN/Mật khẩu/Vân tay)" else "Chưa thiết lập khóa màn hình bảo mật",
                isSecure = isScreenLockSecure,
                recommendation = if (isScreenLockSecure) "Rất tốt. Thiết bị được bảo vệ khi bị thất lạc." else "Hãy bật khóa màn hình mã PIN hoặc Vân tay ngay lập tức.",
                settingsAction = Settings.ACTION_SECURITY_SETTINGS
            )
        )

        // 2. USB Debugging (ADB)
        val isAdbEnabled = try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        } catch (_: Throwable) {
            false
        }
        list.add(
            HardeningCheckItem(
                id = "adb_debugging",
                title = "Gỡ lỗi qua USB (USB Debugging / ADB)",
                statusText = if (isAdbEnabled) "Đang bật (Nguy cơ bị trích xuất dữ liệu khi cắm cáp)" else "Đã tắt (An toàn)",
                isSecure = !isAdbEnabled,
                recommendation = if (isAdbEnabled) "Nếu bạn không lập trình ứng dụng, hãy tắt tính năng này để tránh tấn công chiếm quyền qua cổng sạc/máy tính." else "Rất an toàn.",
                settingsAction = Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
            )
        )

        // 3. Developer Options
        val isDevOptionsEnabled = try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1
        } catch (_: Throwable) {
            false
        }
        list.add(
            HardeningCheckItem(
                id = "dev_options",
                title = "Tùy chọn cho nhà phát triển (Developer Options)",
                statusText = if (isDevOptionsEnabled) "Đang mở" else "Đã ẩn / tắt",
                isSecure = !isDevOptionsEnabled,
                recommendation = if (isDevOptionsEnabled) "Tắt tùy chọn nhà phát triển giúp ngăn các tinh chỉnh can thiệp hệ thống ngoài ý muốn." else "Tối ưu và bảo mật.",
                settingsAction = Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
            )
        )

        // 4. Hardware File-based Encryption
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        val encryptionStatus = dpm?.storageEncryptionStatus ?: DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED
        val isEncrypted = encryptionStatus == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE ||
                encryptionStatus == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY ||
                encryptionStatus == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER ||
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q // Android 10+ requires FBE encryption by default
        list.add(
            HardeningCheckItem(
                id = "storage_encryption",
                title = "Mã hóa bộ nhớ phần cứng (FBE)",
                statusText = if (isEncrypted) "Đã kích hoạt mã hóa cấp phần cứng (File-Based Encryption)" else "Chưa mã hóa",
                isSecure = isEncrypted,
                recommendation = "Dữ liệu cá nhân được mã hóa an toàn trên chip nhớ.",
                settingsAction = null
            )
        )

        // 5. Android Security Patch Level
        val patch = Build.VERSION.SECURITY_PATCH ?: "N/A"
        list.add(
            HardeningCheckItem(
                id = "security_patch",
                title = "Bản vá bảo mật Android (Security Patch)",
                statusText = "Phiên bản bản vá: $patch (Android ${Build.VERSION.RELEASE})",
                isSecure = true,
                recommendation = "Nên thường xuyên kiểm tra cập nhật phần mềm hệ thống để nhận các bản vá lỗ hổng mới nhất.",
                settingsAction = "android.settings.SYSTEM_UPDATE_SETTINGS"
            )
        )

        return list
    }
}
