package com.miassolutions.milkledger.presentation.activities



import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.miassolutions.milkledger.databinding.ActivityLoginBinding

/**
 * A full-screen Activity demonstrating a Material 3 login form with only
 * Email/Username and Password, and a Forgot Password recovery link.
 */
class LoginActivity : AppCompatActivity() {

    // Initialize View Binding instance. Activities only need a single binding instance.
    private lateinit var binding: ActivityLoginBinding

    // Mock Firebase instances for structure
    // private lateinit var auth: FirebaseAuth
    // private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize View Binding and set the content view for the Activity
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: 1. Initialize Firebase (if this were a real app)
        // auth = FirebaseAuth.getInstance()
        // firestore = FirebaseFirestore.getInstance()

        setupLoginButton()
        setupForgotPassword()
    }

    /**
     * Sets up the listener for the main Login button.
     */
    private fun setupLoginButton() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                binding.tvStatusMessage.text = "Email/Username and Password are required."
                // No return@setOnClickListener needed here as we are in an Activity method
            } else {
                // Clear previous status messages
                binding.tvStatusMessage.text = ""

                // Start login process (Replace this with real Firebase code)
                attemptFirebaseLogin(email, password)
            }
        }
    }

    /**
     * Sets up the listener for the Forgot Password button.
     * This triggers the password recovery flow.
     */
    private fun setupForgotPassword() {
        binding.btnForgotPassword.setOnClickListener {
            // In a real application, you would typically collect the user's email
            // and call FirebaseAuth.sendPasswordResetEmail(email) here.
            Toast.makeText(this, "Initiating password recovery...", Toast.LENGTH_SHORT).show()
            Log.d("LoginActivity", "MOCK: Initiating Firebase password reset flow.")
            // A more complete implementation would show a dialog for email input.
        }
    }

    /**
     * Mocks the Firebase authentication process using email and password.
     */
    private fun attemptFirebaseLogin(email: String, password: String) {
        // Show loading state and clear messages
        binding.tvStatusMessage.text = "Attempting login..."
        binding.btnLogin.isEnabled = false

        // --- MOCK LOGIC FOR DEMONSTRATION ---
        // Simulate a delay for a network call. Use postDelayed on the view for UI thread execution.
        binding.root.postDelayed({
            if (email.isNotEmpty() && password.length >= 6) {
                // Simulate successful login
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_LONG).show()
                binding.tvStatusMessage.text = "Welcome, user!"
                // navigateToHomeScreen()
            } else {
                // Simulate generic login failure
                binding.tvStatusMessage.text = "Authentication failed. Invalid credentials."
            }
            binding.btnLogin.isEnabled = true
        }, 1500)
    }

    // Function to navigate after successful login (not implemented here)
    // private fun navigateToHomeScreen() {
    //     Log.d("LoginActivity", "Navigating to main application screen.")
    // }
}

