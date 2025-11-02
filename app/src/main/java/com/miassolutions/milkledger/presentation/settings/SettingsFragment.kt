package com.miassolutions.milkledger.presentation.settings

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentSettingsBinding

class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    private val TAG = "FIRESTORE_DEBUG"

    // Safe Firestore instance
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun setupViews() {
        setToolbarTitle(getString(R.string.settings))

        // WRITE button
        binding.writeButton.setOnClickListener {
            writeTestDocument()
        }

        // READ button
        binding.readButton.setOnClickListener {
            readTestDocuments()
        }
    }

    private fun writeTestDocument() {
        val testData = mapOf(
            "name" to "Alice",
            "age" to 25
        )

        db.collection("test")
            .add(testData)
            .addOnSuccessListener { docRef ->
                Log.d(TAG, "Document added with ID: ${docRef.id}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding document", e)
            }
    }

    private fun readTestDocuments() {
        db.collection("test")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error reading documents", e)
            }
    }
}
