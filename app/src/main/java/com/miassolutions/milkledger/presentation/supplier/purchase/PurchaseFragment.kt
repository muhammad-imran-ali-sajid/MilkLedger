package com.miassolutions.milkledger.presentation.supplier.purchase

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.materialswitch.MaterialSwitch
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import com.miassolutions.milkledger.databinding.FragmentPurchasesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.util.Calendar
import java.util.concurrent.Executor
import kotlin.math.roundToInt

@AndroidEntryPoint
class PurchaseFragment :
    BaseFragment<FragmentPurchasesBinding>(FragmentPurchasesBinding::inflate) {

    override fun getMenuResId(): Int? = R.menu.purchase_menu

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

        binding.tvSelectedDate.setOnClickListener {
            val currentDate = viewModel.uiState.value.currentDate
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentDate.year)
                set(Calendar.MONTH, currentDate.monthValue - 1)
                set(Calendar.DAY_OF_MONTH, currentDate.dayOfMonth)
            }

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
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

//        binding.switchEditMode.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
////                binding.switchEditMode.text = "Edit Mode: ON"
//                showBiometricPrompt(
//                    onSuccess = { enableEditMode() },
//                    onFailure = {
//                        showToast("Authentication failed. Cannot enable edit mode.")
//
//                    }
//                )
//
//            } else {
////                binding.switchEditMode.text = "Edit Mode: OFF"
//                disableEditMode()
//            }
//        }

    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_edit_mode) {
            val switch =
                item.actionView?.findViewById<MaterialSwitch>(R.id.switch_toolbar_edit_mode)

            switch?.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    showBiometricPrompt(
                        onSuccess = { enableEditMode() },
                        onFailure = {
                            switch.isChecked = false
                            showToast("Authentication failed.")
                        }
                    )
                } else {
                    disableEditMode()
                }
            }

            // Optionally sync switch with current state
            switch?.isChecked = loadEditModeState()

            return true
        }
        return false
    }


    private fun setupRecyclerView() {
        purchaseAdapter = PurchaseAdapter(::showEditBottomSheet, ::navToSupplierDetail)

        binding.rvPurchases.apply {
            adapter = purchaseAdapter
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = null
            setHasFixedSize(true)
        }
    }

    private fun navToSupplierDetail(supplier: PurchaseWithSupplier) {
        findNavController().navigate(
            PurchaseFragmentDirections.actionPurchaseFragmentToSupplierDetailFragment(
                supplier.supplier.supplierName
            )
        )
    }

    private fun showEditBottomSheet(purchaseWithSupplier: PurchaseWithSupplier) {
        if (!isEditable) return

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

                binding.tvSelectedDate.text = getString(
                    R.string.date_format,
                    state.currentDate.dayOfMonth,
                    state.currentDate.monthValue,
                    state.currentDate.year
                )

                binding.tvTotalAmount.text =
                    getString(R.string.rs, state.grandTotalForDate.roundToInt())
                binding.tvTotalMilk.text = state.totalVolume.toRoundedStr()
                binding.tvAvgFat.text = state.avgFat.toRoundedStr()
                binding.tvAvgLr.text = state.avgLr.toRoundedStr()
                binding.tvAvgPrice.text = state.avgRatePerLiter.toRoundedStr()
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
