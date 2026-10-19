package com.aegis.security.core.vault

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class VaultItem(
    val id: String,
    val title: String,
    val secretContent: String,
    val timestamp: Long
)

class HardwareVault(private val context: Context) {

    private val keyStoreAlias = "AegisHardwareMasterKey"
    private val keyStoreProvider = "AndroidKeyStore"
    private val transformation = "AES/GCM/NoPadding"
    private val prefs = context.getSharedPreferences("aegis_secure_vault", Context.MODE_PRIVATE)

    init {
        ensureMasterKey()
    }

    private fun ensureMasterKey() {
        val keyStore = KeyStore.getInstance(keyStoreProvider).apply { load(null) }
        if (!keyStore.containsAlias(keyStoreAlias)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, keyStoreProvider)
            val builder = KeyGenParameterSpec.Builder(
                keyStoreAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)

            keyGenerator.init(builder.build())
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(keyStoreProvider).apply { load(null) }
        val entry = keyStore.getEntry(keyStoreAlias, null) as? KeyStore.SecretKeyEntry
            ?: throw IllegalStateException("Không tìm thấy Master Key trong Android KeyStore")
        return entry.secretKey
    }

    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val cipherText = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

        // Format: Base64(IV) + ":" + Base64(CipherBytes)
        val ivStr = Base64.encodeToString(iv, Base64.NO_WRAP)
        val dataStr = Base64.encodeToString(cipherText, Base64.NO_WRAP)
        return "$ivStr:$dataStr"
    }

    fun decrypt(encryptedPayload: String): String {
        val parts = encryptedPayload.split(":")
        if (parts.size != 2) return ""

        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val cipherText = Base64.decode(parts[1], Base64.NO_WRAP)

        val cipher = Cipher.getInstance(transformation)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

        val originalBytes = cipher.doFinal(cipherText)
        return String(originalBytes, StandardCharsets.UTF_8)
    }

    fun saveVaultItem(title: String, secretContent: String): Boolean {
        return try {
            val id = System.currentTimeMillis().toString()
            val encryptedSecret = encrypt(secretContent)
            prefs.edit()
                .putString("item_title_$id", title)
                .putString("item_secret_$id", encryptedSecret)
                .putLong("item_time_$id", System.currentTimeMillis())
                .apply()
            true
        } catch (_: Throwable) {
            false
        }
    }

    fun getAllVaultItems(): List<VaultItem> {
        val items = mutableListOf<VaultItem>()
        val allKeys = prefs.all.keys
        val ids = allKeys.filter { it.startsWith("item_title_") }.map { it.removePrefix("item_title_") }

        for (id in ids) {
            val title = prefs.getString("item_title_$id", "") ?: ""
            val encryptedSecret = prefs.getString("item_secret_$id", "") ?: ""
            val time = prefs.getLong("item_time_$id", 0L)

            val decrypted = try {
                decrypt(encryptedSecret)
            } catch (_: Throwable) {
                "[Lỗi giải mã: Khóa phần cứng bị can thiệp]"
            }

            items.add(VaultItem(id, title, decrypted, time))
        }
        return items.sortedByDescending { it.timestamp }
    }

    fun deleteVaultItem(id: String) {
        prefs.edit()
            .remove("item_title_$id")
            .remove("item_secret_$id")
            .remove("item_time_$id")
            .apply()
    }
}
