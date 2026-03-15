package com.miassolutions.milkledger.features.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.core.ui.BaseActivity
import com.miassolutions.milkledger.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * A full-screen Activity demonstrating a Material 3 login form with only
 * Email/Username and Password, and a Forgot Password recovery link.
 */
@AndroidEntryPoint
class LoginActivity : BaseActivity() {


    // Tag for logging purposes
    private val TAG = "LoginActivity"

    // Initialize View Binding instance.
    private lateinit var binding: ActivityLoginBinding

    // Initialize Firebase Auth instance
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize View Binding and set the content view for the Activity
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // If a user is already signed in, navigate away immediately (optional check)
        // if (auth.currentUser != null) {
        //     navigateToHomeScreen()
        //     return
        // }

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
                return@setOnClickListener
            } else {
                SharedPrefsHelper.saveUserMail(this, email)
                // Clear previous status messages
                binding.tvStatusMessage.text = ""

                // Start login process
                attemptFirebaseLogin(email, password)
            }
        }
    }

    /**
     * Sets up the listener for the Forgot Password button.
     * This prompts the user for their email and sends a recovery link.
     */
    private fun setupForgotPassword() {
        binding.btnForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                binding.tvStatusMessage.text = "Please enter your email to reset the password."
                return@setOnClickListener
            }

            // Send password reset email
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Password reset email sent to $email.",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d(TAG, "Password reset email sent.")
                    } else {
                        val message =
                            task.exception?.localizedMessage ?: "Failed to send reset email."
                        binding.tvStatusMessage.text = message
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    /**
     * Attempts to sign in the user with Firebase Authentication.
     */
    private fun attemptFirebaseLogin(email: String, password: String) {

        binding.tvStatusMessage.text = "Attempting login..."
        binding.btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.d(TAG, "signInWithEmail:success")
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                fetchUserRoleAndProceed()


            }
            .addOnFailureListener { e ->
                Log.w(TAG, "signInWithEmail:failure", e)

                // Display the failure message to the user
                val message =
                    e.localizedMessage ?: "Authentication failed. Please check your credentials."
                binding.tvStatusMessage.text = message
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                // Re-enable the button
                binding.btnLogin.isEnabled = true
            }
    }

    private fun fetchUserRoleAndProceed() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            showToast("User not logged in")
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    showToast("User data not found")

                    return@addOnSuccessListener
                }

                val role = doc.getString("role") ?: "user" // default to "user" if missing
                SharedPrefsHelper.saveUserRole(this, role)




                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("role", role)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to fetch user role", e)
                Toast.makeText(this, "Failed to get user role", Toast.LENGTH_SHORT).show()
            }
    }


    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
