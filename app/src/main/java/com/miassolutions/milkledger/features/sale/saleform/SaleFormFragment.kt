package com.miassolutions.milkledger.features.sale.saleform

import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentAddSaleBinding
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnAmountPaidChanged
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnCustomerSelected
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnDateSelected
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnDeductionChanged
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnNoteChanged
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnVolumeChanged
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.openDatePicker
import com.miassolutions.milkledger.utils.extensions.setBalanceColorWithRoundRupee
import com.miassolutions.milkledger.utils.extensions.setTextIfDifferent
import com.miassolutions.milkledger.utils.extensions.showDeleteActionDialog
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toFormattedMilk
import com.miassolutions.milkledger.utils.extensions.toPrice
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SaleFormFragment :
    BaseFragment<FragmentAddSaleBinding>(FragmentAddSaleBinding::inflate) {

    private val viewModel: SaleFormViewModel by viewModels()
    private var shouldShowDeleteButton = false

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

        setupKeyboardInsets()
    }

    private fun setupKeyboardInsets() {
        val initialBottomPadding = binding.scrollView.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollView) { view, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.btnDelete.isVisible = shouldShowDeleteButton && !imeVisible

            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                initialBottomPadding + if (imeVisible) imeInsets.bottom else systemBars.bottom
            )

            insets
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

        btnDelete.setOnClickListener {
            showDeleteActionDialog {
                viewModel.onEvent(SaleFormUiEvent.OnDeleteClicked)
                navigateUp()
            }
        }
        // --- Dates & Save ---
//        btnDate.setOnClickListener { viewModel.onEvent(OnDateClick) }
//        btnPaymentDate.setOnClickListener { viewModel.onEvent(OnPaymentDateClick) }
        btnSave.setOnClickListener {
            submitSale(saveAndNew = false)
        }

        btnSaveNew.setOnClickListener {
            submitSale(saveAndNew = true)
        }

        etPayment.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitSale(saveAndNew = true)
                true
            } else {
                false
            }
        }
    }

    private fun submitSale(saveAndNew: Boolean) {
        if (
            customerEntryAlreadyExists() &&
            !viewModel.uiState.value.isEditMode
        ) {
            showSnackbar("Customer entry already exists for this date")
            return
        }

        val event = if (saveAndNew) {
            SaleFormUiEvent.OnSaveAndNewClicked
        } else {
            SaleFormUiEvent.OnSaveClicked
        }

        viewModel.onEvent(event)
    }

    private fun customerEntryAlreadyExists(): Boolean {
        val supplierId = viewModel.uiState.value.selectedCustomer?.accountId
            ?: return false

        return currentCustomerList.any {
            it.account.accountId == supplierId &&
                    it.isEntryDoneToday
        }
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

        val currentText = actvCustomerName.text.toString()
        val newText = state.selectedCustomer?.name ?: ""

        if (currentText != newText) {
            actvCustomerName.setText(newText)
        }

        // --- Dates ---
        btnDate.text = "Dated: ${state.date.toCompleteDateFormat()}"
        if (state.paymentDate != null) {
            btnPaymentDate.text = state.paymentDate.toDisplayDate()
            btnPaymentDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        } else {
            btnPaymentDate.text = "Select Date" // "Abhi select nahi hoi"
            btnPaymentDate.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.red
                )
            ) // Red color for attention
        }

        // --- Inputs (Update only if different to avoid cursor jumps) ---
        etMilkVolume.setTextIfDifferent(state.volume)
        etDeduction.setTextIfDifferent(state.deduction)
        etPayment.setTextIfDifferent(state.amountPaid)
        etNote.setTextIfDifferent(state.note)

        // --- Calculations ---
        tvNetMilk.text = state.netMilk.toFormattedMilk()
        tvMilkPrice.text = state.totalPrice.toPrice()
        tvRate.text = state.rate
        tvBalance.setBalanceColorWithRoundRupee(state.currentBalance)

        // --- Save Button ---
        btnSave.text = if (state.isEditMode) "Update" else "Save"
        btnSave.isEnabled = !state.isSaving
        btnDelete.isVisible = state.isEditMode
        btnSaveNew.isVisible = !state.isEditMode
    }

    private fun handleEffect(effect: SaleFormUiEffect) {
        when (effect) {
            SaleFormUiEffect.NavigateBack -> findNavController().navigateUp()
            SaleFormUiEffect.OpenDatePicker -> {
                openDatePicker { date -> viewModel.onEvent(OnDateSelected(date)) }
            }


            is SaleFormUiEffect.ShowSnackbar -> showToast(effect.message)
            SaleFormUiEffect.FocusMilkVolumeInput -> {
                focusMilkVolumeInput()
            }
        }
    }

    private fun focusMilkVolumeInput() {
        binding.etMilkVolume.post {
            binding.etMilkVolume.requestFocus()
            binding.etMilkVolume.setSelection(binding.etMilkVolume.text?.length ?: 0)

            val imm = ContextCompat.getSystemService(
                requireContext(),
                InputMethodManager::class.java
            )

            imm?.showSoftInput(
                binding.etMilkVolume,
                InputMethodManager.SHOW_IMPLICIT
            )
        }
    }
}