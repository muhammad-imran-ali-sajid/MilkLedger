package com.miassolutions.milkledger.presentation.purchase

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentPurchasesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.concurrent.Executor
import kotlin.math.roundToInt

@AndroidEntryPoint
class PurchaseFragment :
    BaseFragment<FragmentPurchasesBinding>(FragmentPurchasesBinding::inflate) {

    private val prefs by lazy {
        requireContext().getSharedPreferences(
            "edit_text_enable_state",
            Context.MODE_PRIVATE
        )
    }
    private var isEditable = true // keep track of edit mode

    private val viewModel: PurchaseViewModel by viewModels()
    private lateinit var purchaseAdapter: PurchaseAdapter

    override fun setupViews() {
        setToolbarTitle(getString(R.string.purchases))
        setupRecyclerView()

        binding.tvDate.setOnClickListener {
            val currentDate = viewModel.uiState.value.currentDate
            val year = currentDate.year
            val month = currentDate.monthValue - 1
            val day = currentDate.dayOfMonth

            DatePickerDialog(requireContext(), { _, y, m, d ->
                val newDate = LocalDate.of(y, m + 1, d)
                viewModel.onDateSelected(newDate)
            }, year, month, day).show()
        }


        // Load previously saved state
        isEditable = loadEditModeState()

        // Apply it to the adapter immediately
        purchaseAdapter.isEditable = isEditable

        binding.btnToggleEdit.setOnClickListener {

            if (!isEditable) {
                showBiometricPrompt(
                    onSuccess = {
                        enableEditMode()
                    },
                    onFailure = {
                        Toast.makeText(
                            requireContext(),
                            "Authentication failed. Cannot enable edit mode.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            } else {
                disableEditMode()
            }
        }

    }


    private fun showBiometricPrompt(onSuccess: () -> Unit, onFailure: () -> Unit = {}) {
        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val executor: Executor = ContextCompat.getMainExecutor(requireContext())

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Authenticate to reset password")
                    .setSubtitle("Use your fingerprint or device credentials")
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    .build()

                val biometricPrompt = BiometricPrompt(
                    this, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            onSuccess()
                        }

                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                        ) {
                            super.onAuthenticationError(errorCode, errString)
                            Toast.makeText(
                                requireContext(),
                                "Authentication error: $errString",
                                Toast.LENGTH_SHORT
                            ).show()
                            onFailure()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            Toast.makeText(
                                requireContext(),
                                "Authentication failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })

                biometricPrompt.authenticate(promptInfo)
            }

            else -> {
                Toast.makeText(
                    requireContext(),
                    "Biometric authentication not available",
                    Toast.LENGTH_LONG
                ).show()
                onFailure()
            }
        }
    }


    private fun enableEditMode() {
        isEditable = true
        purchaseAdapter.isEditable = true
        saveEditModeState(isEditable)
        Toast.makeText(requireContext(), "Edit mode enabled", Toast.LENGTH_SHORT).show()
    }

    private fun disableEditMode() {
        isEditable = false
        purchaseAdapter.isEditable = false
        saveEditModeState(isEditable)
        Toast.makeText(requireContext(), "Edit mode disabled", Toast.LENGTH_SHORT).show()
    }

    private fun saveEditModeState(isEditable: Boolean) {
        prefs.edit {
            putBoolean("edit_mode_enabled", isEditable)
        }
    }

    private fun loadEditModeState(): Boolean {
        return prefs.getBoolean("edit_mode_enabled", false) // default to false if not set
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeUiState()
    }

    // ----------------------------------------------------------
    // 🧩 Setup RecyclerView
    // ----------------------------------------------------------
    private fun setupRecyclerView() {
        purchaseAdapter = PurchaseAdapter(
            onSupplierClick = { supplierId ->
                viewModel.onEvent(PurchaseUiEvent.OnSupplierSelected(supplierId))
            },
            onVolumeChanged = { entryId, volume ->
                viewModel.onEvent(PurchaseUiEvent.OnVolumeChanged(entryId, volume))
            },
            onFatChanged = { entryId, fat ->
                viewModel.onEvent(PurchaseUiEvent.OnFatChanged(entryId, fat))
            },
            onLrChanged = { entryId, lr ->
                viewModel.onEvent(PurchaseUiEvent.OnLrChanged(entryId, lr))
            },
            onNotesChanged = { entryId, notes ->
                viewModel.onEvent(PurchaseUiEvent.OnNotesChanged(entryId, notes))
            },
            onPaidChanged = { entryId, paid ->
                viewModel.onEvent(PurchaseUiEvent.OnPaidChanged(entryId, paid))
            }

        )

        binding.rvPurchases.apply {
            adapter = purchaseAdapter
            itemAnimator = null
            layoutManager = LinearLayoutManager(requireContext())
            setRecyclerListener { holder ->
                holder.itemView.findFocus()?.clearFocus()
            }
            setHasFixedSize(true)
        }
    }

    // ----------------------------------------------------------
    // 👀 Observe UI State
    // ----------------------------------------------------------
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                Log.d("PurchaseFragment", "${state.purchasesForDate}")

                // Update RecyclerView list
                binding.rvPurchases.setItemViewCacheSize(state.purchasesForDate.size)
                purchaseAdapter.submitList(state.purchasesForDate)

                // Update total text
                binding.tvTotalAmount.text =
                    getString(R.string.rs, state.grandTotalForDate.roundToInt())

                // Update date text
                binding.tvDate.text = getString(
                    R.string.date_format,
                    state.currentDate.dayOfMonth,
                    state.currentDate.monthValue,
                    state.currentDate.year
                )

                // Handle navigation
                state.navigateToLedgerForSupplierId?.let { supplierId ->
                    navigateToLedger(supplierId)
                    viewModel.onLedgerNavigated()
                }
            }
        }
    }


    // ----------------------------------------------------------
    // 🧭 Navigate to Supplier Ledger
    // ----------------------------------------------------------
    private fun navigateToLedger(supplierId: String) {
        // TODO: Replace with actual navigation direction when you create the ledger screen
        // Example if using Navigation Component:
        // val action = PurchaseFragmentDirections.actionPurchaseFragmentToSupplierLedgerFragment(supplierId)
        // findNavController().navigate(action)
    }
}
