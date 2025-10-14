package com.miassolutions.milkledger.presentation.purchase

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import com.miassolutions.milkledger.databinding.FragmentPurchasesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
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

    private var isEditable = true
    private val viewModel: PurchaseViewModel by viewModels()
    private lateinit var purchaseAdapter: PurchaseAdapter

    override fun setupViews() {
        setToolbarTitle(getString(R.string.purchases))
        setupRecyclerView()

        binding.btnDate.setOnClickListener {
            val currentDate = viewModel.uiState.value.currentDate

            // Convert LocalDate to milliseconds for MaterialDatePicker
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentDate.year)
                set(Calendar.MONTH, currentDate.monthValue - 1)
                set(Calendar.DAY_OF_MONTH, currentDate.dayOfMonth)
            }

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Purchase Date")
                .setSelection(calendar.timeInMillis)
                .build()

            datePicker.addOnPositiveButtonClickListener { selectedDateInMillis ->
                val selectedDate = Instant.ofEpochMilli(selectedDateInMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                viewModel.onDateSelected(selectedDate)
            }

            datePicker.show(parentFragmentManager, "MaterialDatePicker")
        }


        isEditable = loadEditModeState()
        purchaseAdapter.isEditable = isEditable

        binding.switchEditMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
//                binding.switchEditMode.text = "Edit Mode: ON"
                showBiometricPrompt(
                    onSuccess = { enableEditMode() },
                    onFailure = {
                        showToast("Authentication failed. Cannot enable edit mode.")

                    }
                )

            } else {
//                binding.switchEditMode.text = "Edit Mode: OFF"
                disableEditMode()
            }
        }

    }

    private fun setupRecyclerView() {
        purchaseAdapter = PurchaseAdapter(
            onItemClick = { purchaseWithSupplier ->
                if (!isEditable) return@PurchaseAdapter
                showEditBottomSheet(purchaseWithSupplier)
            }
        )

        binding.rvPurchases.apply {
            adapter = purchaseAdapter
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = null
            setHasFixedSize(true)
        }
    }

    private fun showEditBottomSheet(purchaseWithSupplier: PurchaseWithSupplier) {
        val bottomSheet = PurchaseEditBottomSheet(
            entry = purchaseWithSupplier,
            onSave = { updatedEntry ->
                viewModel.updatePurchaseManually(updatedEntry)
            }
        )
        bottomSheet.show(parentFragmentManager, "EditPurchaseSheet")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeUiState()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                purchaseAdapter.submitList(state.purchasesForDate)

                binding.btnDate.text = getString(
                    R.string.date_format,
                    state.currentDate.dayOfMonth,
                    state.currentDate.monthValue,
                    state.currentDate.year
                )

                binding.tvTotalAmount.text =
                    getString(R.string.rs, state.grandTotalForDate.roundToInt())
                binding.tvTotalMilk.text = "%.2f".format(state.totalVolume)
                binding.tvAvgFat.text = "%.2f".format(state.avgFat)
                binding.tvAvgLr.text = "%.2f".format(state.avgLr)
                binding.tvAvgPrice.text = "%.2f".format(state.avgRatePerLiter)
            }
        }
    }

    private fun showBiometricPrompt(onSuccess: () -> Unit, onFailure: () -> Unit = {}) {
        val biometricManager = BiometricManager.from(requireContext())
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            == BiometricManager.BIOMETRIC_SUCCESS
        ) {
            val executor: Executor = ContextCompat.getMainExecutor(requireContext())
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authenticate to enable edit mode")
                .setSubtitle("Use your fingerprint or device credentials")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()

            val biometricPrompt = BiometricPrompt(
                this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }

                    override fun onAuthenticationError(code: Int, errString: CharSequence) {
                        super.onAuthenticationError(code, errString)
                        Toast.makeText(requireContext(), errString, Toast.LENGTH_SHORT).show()
                        onFailure()
                    }
                }
            )

            biometricPrompt.authenticate(promptInfo)
        } else {
            Toast.makeText(
                requireContext(),
                "Biometric authentication not available",
                Toast.LENGTH_SHORT
            ).show()
            onFailure()
        }
    }

    private fun enableEditMode() {
        isEditable = true
        purchaseAdapter.isEditable = true
        saveEditModeState(true)
        Toast.makeText(requireContext(), "Edit mode enabled", Toast.LENGTH_SHORT).show()
    }

    private fun disableEditMode() {
        isEditable = false
        purchaseAdapter.isEditable = false
        saveEditModeState(false)
        Toast.makeText(requireContext(), "Edit mode disabled", Toast.LENGTH_SHORT).show()
    }

    private fun saveEditModeState(isEditable: Boolean) {
        prefs.edit { putBoolean("edit_mode_enabled", isEditable) }
    }

    private fun loadEditModeState() =
        prefs.getBoolean("edit_mode_enabled", false)
}
