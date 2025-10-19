package com.miassolutions.milkledger.core.helper

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.concurrent.Executor

object BiometricHelper {

    fun authenticate(
        fragment: Fragment,
        title: String = "Authenticate",
        subtitle: String = "Use your fingerprint or device credentials",
        onSuccess: () -> Unit,
        onFailure: () -> Unit = {}
    ) {
        val context = fragment.requireContext()
        val biometricManager = BiometricManager.from(context)

        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            == BiometricManager.BIOMETRIC_SUCCESS
        ) {
            val executor: Executor = ContextCompat.getMainExecutor(context)

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()

            val biometricPrompt = BiometricPrompt(
                fragment,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }

                    override fun onAuthenticationError(code: Int, errString: CharSequence) {
                        super.onAuthenticationError(code, errString)
                        Toast.makeText(context, errString, Toast.LENGTH_SHORT).show()
                        onFailure()
                    }
                }
            )

            biometricPrompt.authenticate(promptInfo)
        } else {
            Toast.makeText(
                context,
                "Biometric authentication not available",
                Toast.LENGTH_SHORT
            ).show()
            onFailure()
        }
    }
}
