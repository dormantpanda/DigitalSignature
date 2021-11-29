package com.example.digitalsignature.app.services

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.digitalsignature.R
import com.example.digitalsignature.data.models.SigningResult
import com.example.digitalsignature.data.models.SigningResult.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BiometricService @Inject constructor (
    @ApplicationContext val context: Context
) {
    companion object {
        private const val CANCEL_CODE = 13
        private const val TOO_MANY_ATTEMPTS_CODE = 7
        private const val SENSOR_DISABLED = 9
    }

    private var _authResult = MutableLiveData<SigningResult>()
    val authResult: LiveData<SigningResult> = _authResult

    fun canAuth(): Boolean {
        val manager = BiometricManager.from(context)
        return when (manager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                true
            }
            else -> {
                false
            }
        }
    }

    fun authUser(fragment: Fragment) {
        val executor = ContextCompat.getMainExecutor(context)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.fingerprint_title))
            .setNegativeButtonText(context.getString(R.string.fingerprint_negative_button))
            .build()

        val biometricPrompt = BiometricPrompt(fragment, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    _authResult.postValue(AUTH_SUCCESS)
                }
                override fun onAuthenticationError(
                    errorCode: Int, errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        (CANCEL_CODE) -> {
                            _authResult.postValue(AUTH_CANCELED)
                        }
                        (TOO_MANY_ATTEMPTS_CODE) -> {
                            _authResult.postValue(TOO_MANY_ATTEMPTS)
                        }
                        else -> {
                            _authResult.postValue(AUTH_ERROR)
                        }
                    }
                }
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    _authResult.postValue(AUTH_FAILED)
                }
            })
        biometricPrompt.authenticate(promptInfo)
    }
}