package com.miassolutions.milkledger.features.purchase.ui.form

import android.text.InputType
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentPurchaseFormBinding
import com.miassolutions.milkledger.features.purchase.model.SupplierDropDownUiModel
import com.miassolutions.milkledger.features.purchase.purchaseform.PurchaseFormViewModel
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.format
import com.miassolutions.milkledger.utils.extensions.hide
import com.miassolutions.milkledger.utils.extensions.openDatePicker
import com.miassolutions.milkledger.utils.extensions.setBalanceWithColor
import com.miassolutions.milkledger.utils.extensions.show
import com.miassolutions.milkledger.utils.extensions.showDeleteActionDialog
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toPrice
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PurchaseFormFragment : BaseFragment<FragmentPurchaseFormBinding>(
    FragmentPurchaseFormBinding::inflate
) {

    private val viewModel: PurchaseFormViewModel by viewModels()

    // Local list store karne k liye taake Bottom Sheet ko pass kar saken
    private var currentSupplierList: List<SupplierDropDownUiModel> = emptyList()

    override fun setupViews() {
        super.setupViews()

        // 1. Make Supplier Field Read-Only (Like a Button)
        binding.actvSupplierName.apply {
            inputType = InputType.TYPE_NULL // Disable keyboard
            isFocusable = false             // Disable focus
            isClickable = true              // Enable click
            isCursorVisible = false
        }
    }

    override fun setupListeners() = with(binding) {
        super.setupListeners()

        // --- Inputs ---
        etMilkVolume.doAfterTextChanged {
            viewModel.onEvent(PurchaseFormUiEvent.OnVolumeChanged(it.toString()))
        }
        etFat.doAfterTextChanged {
            viewModel.onEvent(PurchaseFormUiEvent.OnFatChanged(it.toString()))
        }
        etLr.doAfterTextChanged {
            viewModel.onEvent(PurchaseFormUiEvent.OnLrChanged(it.toString()))
        }
        etPayment.doAfterTextChanged {
            viewModel.onEvent(PurchaseFormUiEvent.OnAmountPaidChanged(it.toString()))
        }
        etNote.doAfterTextChanged {
            viewModel.onEvent(PurchaseFormUiEvent.OnNoteChanged(it.toString()))
        }

        btnDelete.setOnClickListener {

            showDeleteActionDialog {
                viewModel.onEvent(PurchaseFormUiEvent.OnDeleteClicked)
                navigateUp()

            }
        }

        // --- Supplier Click (OPEN BOTTOM SHEET) ---
        // Instead of Adapter/ItemClick, hum bas Sheet open karenge
        actvSupplierName.setOnClickListener {
            openSupplierBottomSheet()
        }
        // Layout click support (optional UX improvement)
        tilSupplierName.setOnClickListener {
            openSupplierBottomSheet()
        }

        // Dates
        btnDate.setOnClickListener { viewModel.onEvent(PurchaseFormUiEvent.OnDateClick) }
        btnPaymentDate.setOnClickListener { viewModel.onEvent(PurchaseFormUiEvent.OnPaymentDateClick) }

        // Actions
        btnSave.setOnClickListener { viewModel.onEvent(PurchaseFormUiEvent.OnSaveClicked) }
        btnSaveNew.setOnClickListener {
            viewModel.onEvent(PurchaseFormUiEvent.OnSaveAndNewClicked)
        }
    }

    override fun setupObservers() {
        super.setupObservers()

        // 1. Observe Suppliers List (Store locally for Bottom Sheet)
        collectFlow(viewModel.suppliersDropDown) { list ->
            currentSupplierList = list
            // Ab hum adapter set nahi kar rahe
        }

        // 2. Observe UI State
        collectFlow(viewModel.uiState) { state ->
            renderState(state)
        }

        // 3. Observe Effects
        collectEffect(viewModel.uiEffect) { effect ->
            handleEffect(effect)
        }
    }

    private fun openSupplierBottomSheet() {
        if (currentSupplierList.isEmpty()) {
            showSnackbar("No suppliers found") // Or fetch from VM
            return
        }

        val sheet = SupplierSelectionBottomSheet(
            suppliersList = currentSupplierList,
            onSupplierSelected = { selectedAccount ->
                viewModel.onEvent(PurchaseFormUiEvent.OnSupplierSelected(selectedAccount))
            }
        )
        sheet.show(childFragmentManager, SupplierSelectionBottomSheet.TAG)
    }

    private fun renderState(state: PurchaseFormUiState) = with(binding) {
        // Date Buttons
        btnDate.text = "Dated: ${state.date.toCompleteDateFormat()}"
        if (state.paymentDate != null) {
            btnPaymentDate.text = state.paymentDate.toDisplayDate() // "18 Jan 2024"
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

        if (state.advance != null) {
            tilAdvance.show()
            tvAdvance.text = state.advance
        } else {
            tilAdvance.hide()
        }

        // Calculations
        tvTs.text = state.calculatedTs.format(2)
        tvMilkPrice.text = state.calculatedTotal.toPrice()
        tvRate.text = state.rate
        tvBalance.setBalanceWithColor(state.currentBalance)

        // Inputs Update (Avoid Cursor Jumps)
        if (etMilkVolume.text.toString() != state.volume) etMilkVolume.setText(state.volume)
        if (etFat.text.toString() != state.fat) etFat.setText(state.fat)
        if (etLr.text.toString() != state.lr) etLr.setText(state.lr)
        if (etPayment.text.toString() != state.amountPaid) etPayment.setText(state.amountPaid)
        if (etNote.text.toString() != state.note) etNote.setText(state.note)

        val currentText = actvSupplierName.text.toString()
        val newText = state.selectedSupplier?.name ?: ""

        if (currentText != newText) {
            actvSupplierName.setText(newText)
        }

        // Disable Interaction during saving
        btnSave.isEnabled = !state.isSaving
        btnSaveNew.isEnabled = !state.isSaving

        btnSave.text = if (state.isEditMode) "Update" else "Save"
        // Disable Supplier Change in Edit Mode

        tilSupplierName.isEnabled = !state.isEditMode
        // Agar edit mode hai to click bhi disable kar dein taake sheet na khule
        actvSupplierName.isEnabled = !state.isEditMode

        btnDelete.isVisible = state.isEditMode
        btnSaveNew.isVisible = !state.isEditMode
    }

    private fun handleEffect(effect: PurchaseFormUiEffect) {
        when (effect) {
            is PurchaseFormUiEffect.ShowSnackbar -> showSnackbar(effect.message)
            PurchaseFormUiEffect.NavigateBack -> findNavController().navigateUp()
            PurchaseFormUiEffect.OpenDatePicker -> {
                openDatePicker { date -> viewModel.onEvent(PurchaseFormUiEvent.OnDateSelected(date)) }
            }

            PurchaseFormUiEffect.OpenPaymentDatePicker -> {
                openDatePicker { date ->
                    viewModel.onEvent(
                        PurchaseFormUiEvent.OnPaymentDateSelected(
                            date
                        )
                    )
                }
            }
        }
    }
}