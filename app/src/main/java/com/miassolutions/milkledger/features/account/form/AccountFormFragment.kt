package com.miassolutions.milkledger.features.account.form

import android.text.InputType
import android.view.View
import android.widget.ScrollView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentAccountFormBinding
import com.miassolutions.milkledger.features.account.form.AccountFormEvent.*
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.setBalanceWithColorRupee
import com.miassolutions.milkledger.utils.extensions.setTextIfDifferent
import com.miassolutions.milkledger.utils.extensions.showDeleteActionDialog
import com.miassolutions.milkledger.utils.extensions.showLedgerDatePicker
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toPaisa
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountFormFragment :
    BaseFragment<FragmentAccountFormBinding>(FragmentAccountFormBinding::inflate) {

    private val viewModel by viewModels<AccountFormViewModel>()

    override fun setupViews() {
        super.setupViews()
        setupInputs()
        setupClicks()
        setupAccountTypeRadioGroup()
    }

    private fun setupClicks() = with(binding) {
        btnSave.setOnClickListener { viewModel.onEvent(SaveClicked) }
        btnDelete.setOnClickListener { viewModel.onEvent(DeleteClicked) }

        // Date listener hum setupInputs me lagayenge taake duplication na ho
    }

    private fun setupAccountTypeRadioGroup() = with(binding) {
        rgAccountType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbCustomer -> viewModel.onAccountTypeSelected(AccountType.CUSTOMER)
                R.id.rbSupplier -> viewModel.onAccountTypeSelected(AccountType.SUPPLIER)
            }
        }
    }

    private fun setupInputs() = with(binding) {
        etSortOrder.doAfterTextChanged { viewModel.onSortOrderChanged(it.toString()) }
        etAccountName.doAfterTextChanged { viewModel.onNameChanged(it.toString()) }
        etDefaultRate.doAfterTextChanged { viewModel.onRateChanged(it.toString()) }
        etAdvanceAmount.doAfterTextChanged { viewModel.onAdvanceAmountChanged(it.toString()) }

        etInitialBalance.doAfterTextChanged {
            viewModel.onInitialBalanceChanged(it.toString())
            updateBalancePreview()
        }

        // 🔥 FIX 1: Date Field ko Button jaisa banayen
        etOpeningDate.apply {
            inputType = InputType.TYPE_NULL // Keyboard nahi khulega
            keyListener = null // Typing allowed nahi
            isFocusable = false // Focus nahi lega
            isClickable = true

            setOnClickListener {
                viewModel.onEvent(OnOpeningDateClicked)
            }
        }

        // Switch Logic (Prevent Loop)
        switchActive.setOnCheckedChangeListener { _, isChecked ->
            if (switchActive.isPressed) {
                viewModel.onEvent(OnActiveStatusChanged(isChecked))
            }
        }

        etSortOrder.focusWithScroll(binding.scrollView)
    }

    private fun updateBalancePreview(
        forcedType: AccountType? = null,
        forcedAmount: String? = null
    ) = with(binding) {
        val type = forcedType ?: if (rbCustomer.isChecked) AccountType.CUSTOMER else AccountType.SUPPLIER
        val amountString = forcedAmount ?: etInitialBalance.text.toString()
        val rawAmount = amountString.toPaisa()
        val finalAmount = if (type == AccountType.SUPPLIER) -rawAmount else rawAmount
        tvBalancePreview.setBalanceWithColorRupee(finalAmount, prefix = "Net Impact: ")
    }

    override fun setupObservers() {
        collectFlow(viewModel.uiState) { state -> renderState(state) }
        collectEffect(viewModel.uiEffect) { effect -> handleEffect(effect) }
    }

    private fun handleEffect(effect: AccountFormEffect) = with(binding) {
        when (effect) {
            is AccountFormEffect.ShowToast -> showToast(effect.message)

            AccountFormEffect.CloseScreen -> findNavController().popBackStack(
                R.id.accountListFragment,
                false
            )

            is AccountFormEffect.FocusField -> focusField(effect.field)

            // 🔥 FIX 2: Date Picker
            is AccountFormEffect.OpenDatePicker -> {
                showLedgerDatePicker(
                    initialDate = effect.currentDate
                ) { selectedDate ->
                    viewModel.onEvent(
                        AccountFormEvent.OnOpeningDateSelected(selectedDate)
                    )
                }
            }

            // 🔥 FIX 3: Dialog Dismissal Logic
            is AccountFormEffect.ShowBalanceError -> {
                showDialog(
                    title = "Cannot Delete!",
                    message = "Is account ka balance (Rs. ${effect.balance}) baqi hai. \n\nAap isay Delete nahi kar sakte. \nKya aap isay Deactivate karna chahte hain?",
                    positiveText = "Deactivate",
                    onAction = {
                        viewModel.onEvent(OnActiveStatusChanged(false))
                    }
                )
            }

            is AccountFormEffect.ShowDeleteConfirmation -> {
                showDeleteActionDialog {
                    viewModel.confirmDelete()
                }
            }
        }
    }

    private fun focusField(field: Field) = with(binding) {
        when (field) {
            Field.SORT_ORDER -> etSortOrder.requestFocus()
            Field.NAME -> etAccountName.requestFocus()
            Field.ACCOUNT_TYPE -> rgAccountType.requestFocus()
            Field.RATE -> etDefaultRate.requestFocus()
            Field.INITIAL_BALANCE -> etInitialBalance.requestFocus()
        }
    }

    private fun View.focusWithScroll(scrollView: ScrollView) {
        scrollView.post {
            scrollView.smoothScrollTo(0, this.top)
            this.requestFocus()
        }
    }

    private fun renderState(state: AccountFormUiState) = with(binding) {
        btnSave.isEnabled = !state.isSaving
        btnSave.text = if (state.isEditMode) "Update" else "Save"

        etSortOrder.setTextIfDifferent(state.sortOrder)
        etAccountName.setTextIfDifferent(state.personName)
        etDefaultRate.setTextIfDifferent(state.rate)
        etInitialBalance.setTextIfDifferent(state.initialBalance)
        etAdvanceAmount.setTextIfDifferent(state.advanceAmount)

        // ✅ Date Render Fix
        // Ensure toDisplayDate() extension sahi format return kr rhi ho (e.g., "dd MMM yyyy")
        etOpeningDate.setTextIfDifferent(state.openingDate.toDisplayDate())

        // Switch Logic (Remove listener before setting state to avoid loops)
        switchActive.setOnCheckedChangeListener(null)
        switchActive.isChecked = state.isActive
        switchActive.text = if (state.isActive) "Account Status: Active" else "Account Status: Inactive (Archived)"
        switchActive.setOnCheckedChangeListener { _, isChecked ->
            if (switchActive.isPressed) viewModel.onEvent(OnActiveStatusChanged(isChecked))
        }

        when (state.selectAccountType) {
            AccountType.CUSTOMER -> if (!rbCustomer.isChecked) rbCustomer.isChecked = true
            AccountType.SUPPLIER -> if (!rbSupplier.isChecked) rbSupplier.isChecked = true
            else -> {}
        }

        btnDelete.isVisible = state.showDeleteButton
        updateBalancePreview(state.selectAccountType, state.initialBalance)

        sortOrderLayout.error = state.validation.sortOrderError
        nameLayout.error = state.validation.nameError
        rateLayout.error = state.validation.rateError
    }
}