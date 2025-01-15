package com.happypet.app.activities

import android.content.Intent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.petme.userapp.activities.ui.theme.HappyPetTheme
import java.util.concurrent.Executor

class BioLockActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HappyPetTheme {
                // Call the biometric authentication function
                BioLockScreen(
                    onAuthenticationSuccess = { isNewRegistration ->
                        if (isNewRegistration) {
                            // Save biometric data logic here
                            // Navigate to MainActivity
                            startActivity(Intent(this@BioLockActivity, MainActivity::class.java))
                        } else {
                            // Navigate directly to MainActivity
                            startActivity(Intent(this@BioLockActivity, MainActivity::class.java))
                        }
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun BioLockScreen(onAuthenticationSuccess: (Boolean) -> Unit) {
    var isNewRegistration by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val biometricManager = BiometricManager.from(context)
    val executor: Executor = ContextCompat.getMainExecutor(context)
    val biometricPrompt = BiometricPrompt(
        context as FragmentActivity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // If authentication is successful, pass the new registration flag to callback
                onAuthenticationSuccess(isNewRegistration)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Handle failure case here, maybe show a message to the user
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Biometric Authentication")
        .setSubtitle("Use fingerprint to authenticate")
        .setNegativeButtonText("Cancel")
        .build()

    LaunchedEffect(Unit) {
        // Check if biometric is enrolled
        val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        isNewRegistration = canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS
        biometricPrompt.authenticate(promptInfo)
    }

    // UI to show during authentication
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Authenticating...")
    }
}

@Preview(showBackground = true)
@Composable
fun BioLockScreenPreview() {
    HappyPetTheme {
        BioLockScreen(onAuthenticationSuccess = {})
    }
}

