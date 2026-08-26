package com.example.ui.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.NoteApplication
import com.example.data.security.BiometricAuthHelper
import com.example.data.security.SecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * حالة الأمان وقفل التطبيق.
 */
data class LockUiState(
    val isSecurityEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val hasPin: Boolean = false,
    val isUnlocked: Boolean = true,
    val enteredPin: String = "",
    val pinError: String? = null,
    val isBiometricAvailable: Boolean = false
)

/**
 * ViewModel لإدارة قفل التطبيق برمز المرور أو البصمة.
 */
class LockViewModel(
    private val securityManager: SecurityManager,
    private val biometricAuthHelper: BiometricAuthHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LockUiState(
            isSecurityEnabled = securityManager.isSecurityEnabled(),
            isBiometricEnabled = securityManager.isBiometricEnabled(),
            hasPin = securityManager.hasPin(),
            isUnlocked = securityManager.isUnlocked.value,
            isBiometricAvailable = biometricAuthHelper.canAuthenticate()
        )
    )
    val uiState: StateFlow<LockUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            securityManager.isUnlocked.collect { unlocked ->
                _uiState.value = _uiState.value.copy(
                    isUnlocked = unlocked,
                    isSecurityEnabled = securityManager.isSecurityEnabled(),
                    isBiometricEnabled = securityManager.isBiometricEnabled(),
                    hasPin = securityManager.hasPin()
                )
            }
        }
    }

    fun onPinDigitEntered(digit: String) {
        val current = _uiState.value.enteredPin
        if (current.length < 6) {
            val updated = current + digit
            _uiState.value = _uiState.value.copy(enteredPin = updated, pinError = null)
            if (updated.length >= 4) {
                // التحقق التلقائي إذا تطابق الرمز
                verifyEnteredPin(updated)
            }
        }
    }

    fun onPinBackspace() {
        val current = _uiState.value.enteredPin
        if (current.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                enteredPin = current.dropLast(1),
                pinError = null
            )
        }
    }

    fun verifyEnteredPin(pin: String = _uiState.value.enteredPin) {
        val success = securityManager.verifyPin(pin)
        if (success) {
            _uiState.value = _uiState.value.copy(
                enteredPin = "",
                pinError = null
            )
        } else {
            if (pin.length >= 4 && pin.length == _uiState.value.enteredPin.length) {
                _uiState.value = _uiState.value.copy(
                    enteredPin = "",
                    pinError = "رمز PIN غير صحيح، يرجى المحاولة ثانية"
                )
            }
        }
    }

    fun triggerBiometricPrompt(activity: FragmentActivity) {
        if (!securityManager.isBiometricEnabled() || !biometricAuthHelper.canAuthenticate()) return

        biometricAuthHelper.promptBiometric(
            activity = activity,
            title = "فتح تطبيق Note A",
            subtitle = "استخدم بصمة الإصبع للوصول إلى ملاحظاتك",
            onSuccess = {
                securityManager.unlockByBiometric()
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(pinError = error)
            }
        )
    }

    fun setPin(pin: String) {
        if (pin.length >= 4) {
            securityManager.setPin(pin)
            refreshState()
        }
    }

    fun removePin() {
        securityManager.removePin()
        refreshState()
    }

    fun toggleBiometric(enabled: Boolean) {
        securityManager.setBiometricEnabled(enabled)
        refreshState()
    }

    fun toggleSecurity(enabled: Boolean) {
        securityManager.setSecurityEnabled(enabled)
        refreshState()
    }

    fun lockNow() {
        securityManager.lockApp()
    }

    private fun refreshState() {
        _uiState.value = _uiState.value.copy(
            isSecurityEnabled = securityManager.isSecurityEnabled(),
            isBiometricEnabled = securityManager.isBiometricEnabled(),
            hasPin = securityManager.hasPin(),
            isUnlocked = securityManager.isUnlocked.value
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NoteApplication)
                LockViewModel(
                    securityManager = app.container.securityManager,
                    biometricAuthHelper = app.container.biometricAuthHelper
                )
            }
        }
    }
}
