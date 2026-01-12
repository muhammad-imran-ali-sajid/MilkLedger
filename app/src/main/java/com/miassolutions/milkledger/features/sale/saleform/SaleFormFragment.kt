package com.miassolutions.milkledger.features.sale.saleform

import android.text.InputType
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentAddSaleBinding
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.*
import com.miassolutions.milkledger.utils.extensions.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SaleFormFragment :
    BaseFragment<FragmentAddSaleBinding>(FragmentAddSaleBinding::inflate) {

    private val viewModel: SaleFormViewModel by viewModels()

    //
    // Local variable to store list for Bottom Sheet
    private var currentCustomerList: List<CustomerDropDownUiModel> = emptyList()

    override fun setupViews() {
        super.setupViews()

        // 1. Setup Customer Field as Read-Only Button
        binding.actvCustomerName.apply {
            inputType = InputType.TYPE_NULL // Disable keyboard
            isFocusable = false             // Disable focus
            isClickable = true              // Enable Click
            isCursorVisible = false
        }
    }

    override fun setupListeners() = with(binding) {
        super.setupListeners()

        // --- Inputs ---
        etMilkVolume.doAfterTextChanged { viewModel.onEvent(OnVolumeChanged(it.toString())) }
        etDeduction.doAfterTextChanged { viewModel.onEvent(OnDeductionChanged(it.toString())) }
        etPayment.doAfterTextChanged { viewModel.onEvent(OnAmountPaidChanged(it.toString())) }
        etNote.doAfterTextChanged { viewModel.onEvent(OnNoteChanged(it.toString())) }

        // --- Customer Click (Opens Bottom Sheet) ---
        actvCustomerName.setOnClickListener {
            openCustomerBottomSheet()
        }
        tilCustomerName.setOnClickListener {
            openCustomerBottomSheet()
        }

        // --- Dates & Save ---
        btnDate.setOnClickListener { viewModel.onEvent(OnDateClick) }
        btnPaymentDate.setOnClickListener { viewModel.onEvent(OnPaymentDateClick) }
        btnSave.setOnClickListener { viewModel.onEvent(OnSaveClicked) }
    }

    override fun setupObservers() {
        super.setupObservers()

        // 1. Observe List for Bottom Sheet
        collectFlow(viewModel.customersDropDown) { list ->
            currentCustomerList = list
        }

        // 2. Handle UI State
        collectFlow(viewModel.uiState) { state ->
            renderState(state)
        }

        // 3. Handle Effects
        collectEffect(viewModel.uiEffect) { effect ->
            handleEffect(effect)
        }
    }

    private fun openCustomerBottomSheet() {
        if (currentCustomerList.isEmpty()) {
            showSnackbar("No customers found")
            return
        }

        val sheet = CustomerSelectionBottomSheet(
            customersList = currentCustomerList,
            onCustomerSelected = { selectedAccount ->
                viewModel.onEvent(OnCustomerSelected(selectedAccount))
            }
        )
        sheet.show(childFragmentManager, CustomerSelectionBottomSheet.TAG)
    }

    private fun renderState(state: SaleFormUiState) = with(binding) {

        // --- Edit Mode Logic ---
        tilCustomerName.isEnabled = !state.isEditMode
        actvCustomerName.isEnabled = !state.isEditMode
        actvCustomerName.alpha = if (state.isEditMode) 0.7f else 1.0f

        // --- Update Customer Name ---
        if (state.selectedCustomer != null) {
            val currentText = actvCustomerName.text.toString()
            val newText = state.selectedCustomer.name
            if (currentText != newText) {
                actvCustomerName.setText(newText)
            }
        }

        // --- Dates ---
        btnDate.text = state.date.toCompleteDateFormat()
        btnPaymentDate.text = state.paymentDate.toCompleteDateFormat()

        // --- Inputs (Update only if different to avoid cursor jumps) ---
        etMilkVolume.setTextIfDifferent(state.volume)
        etDeduction.setTextIfDifferent(state.deduction)
        etPayment.setTextIfDifferent(state.amountPaid)
        etNote.setTextIfDifferent(state.note)

        // --- Calculations ---
        tvNetMilk.text = state.displayNetMilk
        tvMilkPrice.text = "Price: ${state.calculatedTotal.toPrice()}"
        tvRate.text = state.displayRate
        tvBalance.setBalanceWithColorRupee(state.currentBalance, prefix = "Balance: ")

        // --- Save Button ---
        btnSave.text = if (state.isEditMode) "Update" else "Save"
        btnSave.isEnabled = !state.isSaving
    }

    private fun handleEffect(effect: SaleFormUiEffect) {
        when (effect) {
            SaleFormUiEffect.NavigateBack -> findNavController().navigateUp()
            SaleFormUiEffect.OpenDatePicker -> {
                openDatePicker { date -> viewModel.onEvent(OnDateSelected(date)) }
            }
            SaleFormUiEffect.OpenPaymentDatePicker -> {
                openDatePicker { date -> viewModel.onEvent(OnPaymentDateSelected(date)) }
            }
            is SaleFormUiEffect.ShowSnackbar -> showSnackbar(effect.message)
            is SaleFormUiEffect.OpenPaymentDatePicker -> { /* Handled in VM */ }
        }
    }
}