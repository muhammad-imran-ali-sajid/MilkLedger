package com.miassolutions.milkledger.features.account.form

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
import com.miassolutions.milkledger.features.account.form.AccountFormEvent.DeleteClicked
import com.miassolutions.milkledger.features.account.form.AccountFormEvent.OnActiveStatusChanged
import com.miassolutions.milkledger.features.account.form.AccountFormEvent.OnOpeningDateClicked
import com.miassolutions.milkledger.features.account.form.AccountFormEvent.OnOpeningDateSelected
import com.miassolutions.milkledger.features.account.form.AccountFormEvent.SaveClicked
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
        setupObservers()
        setupClicks()
        setupAccountTypeRadioGroup()
    }

    private fun setupClicks() = with(binding) {
        btnSave.setOnClickListener {
            viewModel.onEvent(SaveClicked)
        }

        btnDelete.setOnClickListener {
            viewModel.onEvent(DeleteClicked)
        }

        // ✅ Date Click moved here for clarity
        etOpeningDate.setOnClickListener {
            viewModel.onEvent(OnOpeningDateClicked)
        }
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

        // 🔥 FIX 1: Switch Listener Setup
        // Yahan text set mat karein, sirf event bhejen. Text renderState me set hoga.
        switchActive.setOnCheckedChangeListener { _, isChecked ->
            // Check karein k ye change user ne kia hai ya code ne?
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
        val type =
            forcedType ?: if (rbCustomer.isChecked) AccountType.CUSTOMER else AccountType.SUPPLIER
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
                // Millis pass karein taake picker sahi initial date uthaye
                showLedgerDatePicker(effect.currentDateMillis) { selectedDate ->
                    viewModel.onEvent(OnOpeningDateSelected(selectedDate))
                }
            }

            // 🔥 FIX 3: Dialog Dismissal
            is AccountFormEffect.ShowBalanceError -> {
                showDialog(
                    title = "Cannot Delete!",
                    message = "Is account ka balance (Rs. ${effect.balance}) baqi hai. \n\nAap isay Delete nahi kar sakte. \nKya aap isay Deactivate karna chahte hain?",
                    positiveText = "Deactivate",
                    onAction = {
                        viewModel.onEvent(OnActiveStatusChanged(false))
                        // Note: Dialog khud dismiss ho jana chahiye agar extension sahi hai,
                        // warna yahan dialog.dismiss() call karna parta hai.
                        // Assuming your showDialog handles dismissal automatically.
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

        // ✅ Date update
        etOpeningDate.setTextIfDifferent(state.openingDate.toDisplayDate())

        // 🔥 FIX 4: Switch Loop Prevention
        // Listener ko temporarily null karen taake infinite loop na banay
        switchActive.setOnCheckedChangeListener(null)
        switchActive.isChecked = state.isActive
        switchActive.text =
            if (state.isActive) "Account Status: Active" else "Account Status: Inactive (Archived)"

        // Listener wapis lagayen
        switchActive.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onEvent(OnActiveStatusChanged(isChecked))
        }

        // Radio Buttons
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