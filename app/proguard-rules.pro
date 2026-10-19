# R8 / Proguard Rules for Aegis Security

# Strip debug and info logs in production release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Preserve Compose Runtime
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
    androidx.compose.runtime.RecomposeScopeImpl *;
}

# Anti-Tamper & Security Integrity checks must be kept and obfuscated
-keepclassmembers class com.aegis.security.core.integrity.** {
    public *;
}

# Preserve AndroidX Biometric UI callbacks
-keepclassmembers class * extends androidx.biometric.BiometricPrompt$AuthenticationCallback {
    public *;
}
