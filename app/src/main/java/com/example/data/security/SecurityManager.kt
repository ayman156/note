package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

/**
 * مدير الأمان والحماية لتطبيق Note A باستخدام EncryptedSharedPreferences.
 * يدعم قفل التطبيق برمز مرور PIN وبصمة الإصبع Biometrics.
 * الميزة اختيارية بالكامل ومعطلة افتراضياً حتى يختار المستخدم تفعيلها من الإعدادات.
 */
class SecurityManager(context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "note_a_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // كحل احتياطي في حال عدم توفر Keystore على بيئات معينة
            context.getSharedPreferences("note_a_fallback_prefs", Context.MODE_PRIVATE)
        }
    }

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    init {
        // إذا كان الأمان غير مفعل، يكون التطبيق مفتوحاً تلقائياً
        if (!isSecurityEnabled()) {
            _isUnlocked.value = true
        }
    }

    fun isSecurityEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_SECURITY_ENABLED, false)
    }

    fun isBiometricEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun hasPin(): Boolean {
        return sharedPreferences.getString(KEY_PIN_HASH, null) != null
    }

    fun setSecurityEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_SECURITY_ENABLED, enabled).apply()
        if (!enabled) {
            _isUnlocked.value = true
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun setPin(pin: String) {
        val hash = hashString(pin)
        sharedPreferences.edit()
            .putString(KEY_PIN_HASH, hash)
            .putBoolean(KEY_SECURITY_ENABLED, true)
            .apply()
        _isUnlocked.value = true
    }

    fun removePin() {
        sharedPreferences.edit()
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_SECURITY_ENABLED, false)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .apply()
        _isUnlocked.value = true
    }

    fun verifyPin(pin: String): Boolean {
        val savedHash = sharedPreferences.getString(KEY_PIN_HASH, null) ?: return false
        val inputHash = hashString(pin)
        val matches = savedHash == inputHash
        if (matches) {
            _isUnlocked.value = true
        }
        return matches
    }

    fun unlockByBiometric() {
        _isUnlocked.value = true
    }

    fun lockApp() {
        if (isSecurityEnabled()) {
            _isUnlocked.value = false
        }
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_SECURITY_ENABLED = "key_security_enabled"
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
        private const val KEY_PIN_HASH = "key_pin_hash"
    }
}
