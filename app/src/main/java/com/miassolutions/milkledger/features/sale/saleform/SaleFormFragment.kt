package com.miassolutions.milkledger.features.sale.saleform

import android.R
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentAddSaleBinding
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnAmountPaidChanged
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnCustomerSelected
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnDateClick
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnDateSelected
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnDeductionChanged
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnNoteChanged
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnPaymentDateClick
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnPaymentDateSelected
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnSaveClicked
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnVolumeChanged
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.setBalanceWithColorRupee
import com.miassolutions.milkledger.utils.extensions.setTextIfDifferent
import com.miassolutions.milkledger.utils.extensions.showLedgerDatePicker
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toPrice
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SaleFormFragment :
    BaseFragment<FragmentAddSaleBinding>(FragmentAddSaleBinding::inflate) {

    private val viewModel: SaleFormViewModel by viewModels()

    override fun setupListeners() = with(binding) {
        super.setupListeners()

        etMilkVolume.doAfterTextChanged {
            viewModel.onEvent(OnVolumeChanged(it.toString()))
        }

        etDeduction.doAfterTextChanged {
            viewModel.onEvent(OnDeductionChanged(it.toString()))
        }

        etPayment.doAfterTextChanged {
            viewModel.onEvent(OnAmountPaidChanged(it.toString()))
        }

        etNote.doAfterTextChanged {
            viewModel.onEvent(OnNoteChanged(it.toString()))
        }

        btnDate.setOnClickListener { viewModel.onEvent(OnDateClick) }
        btnPaymentDate.setOnClickListener { viewModel.onEvent(OnPaymentDateClick) }
        btnSave.setOnClickListener { viewModel.onEvent(OnSaveClicked) }
    }

    override fun setupObservers() {
        super.setupObservers()

        // 1. Handle Customer List & Dropdown
        collectFlow(viewModel.customersList) { customers ->
            val names = customers.map { it.name }
            val adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, names)

            binding.actvCustomerName.setAdapter(adapter)

            // 🔥 CRITICAL FIX: Position ki bajaye Name se object dhunden
            binding.actvCustomerName.setOnItemClickListener { parent, _, position, _ ->
                val selectedName = parent.getItemAtPosition(position) as String
                val selectedCustomer = customers.find { it.name == selectedName }

                if (selectedCustomer != null) {
                    viewModel.onEvent(OnCustomerSelected(selectedCustomer))
                }
            }
        }

        // 2. Handle UI State
        collectFlow(viewModel.uiState) { state ->
            renderState(state)
        }

        // 3. Handle Effects (Navigation, Toasts)
        collectEffect(viewModel.uiEffect) { effect ->
            handleEffect(effect)
        }
    }

    private fun renderState(state: SaleFormUiState) = with(binding) {

        // --- 🔒 Edit Mode Restriction Logic ---
        // Agar ID exist karti hai (Edit Mode), to Customer Name disable kr den
        // (Assuming ViewModel state me 'isEditMode' ya 'saleId != null' check ho skta hai)
        // Behtar hai ViewModel State me aik boolean 'isEditMode' add kr len.

        // Example Logic:
        val isEditMode = state.selectedCustomer != null && !state.isLoading && /* Check if logic allows */ true
        // Lekin simple UI logic k liye:
        // Agar pehle se saved sale edit ho rahi hai, to input disable karein.

        // Aapne ViewModel me agar 'isEditMode' flag nahi rakha, to ap 'btnSave' text se andaza laga skty hen
        // ya simple logic: Agar state me customer set hai aur hum load kr chukay hen

        // ✅ BEST WAY:
        binding.actvCustomerName.isEnabled = !state.isEditMode // Disable in Edit Mode
        binding.actvCustomerName.alpha = if (state.isEditMode) 0.7f else 1.0f // Visual feedback


        // --- Customer Name Setting ---
        if (state.selectedCustomer != null) {
            val currentText = actvCustomerName.text.toString()
            val newText = state.selectedCustomer.name
            if (currentText != newText) {
                actvCustomerName.setText(newText, false)
                if (!state.isEditMode) {
                    actvCustomerName.setSelection(newText.length)
                }
            }
        }

        // --- Other Fields ---
        btnDate.text = state.date.toCompleteDateFormat()
        btnPaymentDate.text = state.paymentDate.toCompleteDateFormat()

        etMilkVolume.setTextIfDifferent(state.volume)
        etDeduction.setTextIfDifferent(state.deduction)
        etPayment.setTextIfDifferent(state.amountPaid)
        etNote.setTextIfDifferent(state.note)

        // --- Calculated Views ---
        tvNetMilk.text = state.displayNetMilk
        tvMilkPrice.text = "Price: ${state.calculatedTotal.toPrice()}"
        tvRate.text = state.displayRate
        tvBalance.setBalanceWithColorRupee(state.currentBalance, prefix = "Balance: ")

        // --- Save Button Text ---
        btnSave.text = if (state.isEditMode) "Update" else "Save"
        btnSave.isEnabled = !state.isSaving

        // Loading State
//        progressBar.isVisible = state.isLoading
    }

    private fun handleEffect(effect: SaleFormUiEffect) {
        when (effect) {
            SaleFormUiEffect.NavigateBack -> findNavController().navigateUp()

            SaleFormUiEffect.OpenDatePicker -> {
                showLedgerDatePicker { date -> viewModel.onEvent(OnDateSelected(date)) }
            }

            SaleFormUiEffect.OpenPaymentDatePicker -> {
                showLedgerDatePicker { date -> viewModel.onEvent(OnPaymentDateSelected(date)) }
            }

            is SaleFormUiEffect.ShowSnackbar -> showSnackbar(effect.message)

        }
    }
}