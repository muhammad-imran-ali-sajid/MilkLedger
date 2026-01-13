package com.miassolutions.milkledger.features.account.form

import android.text.InputType
import android.view.View
import android.widget.ScrollView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentAccountFormBinding
import com.miassolutions.milkledger.features.account.form.AccountFormEvent.*
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.setBalanceColorWithRoundRupee
import com.miassolutions.milkledger.utils.extensions.setTextIfDifferent
import com.miassolutions.milkledger.utils.extensions.openDatePicker
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toPaisa
import com.miassolutions.milkledger.utils.extensions.toRupees
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountFormFragment :
    BaseFragment<FragmentAccountFormBinding>(FragmentAccountFormBinding::inflate) {

    private val viewModel by viewModels<AccountFormViewModel>()
    private val args by navArgs<AccountFormFragmentArgs>()

    override fun setupViews() {
        super.setupViews()

        args.type?.let {
            viewModel.setInitialAccountType(AccountType.valueOf(it))
        }

        setupInputs()
        setupClicks()
        setupAccountTypeRadioGroup()
    }

    /* --------------------------------------------------
     * Clicks
     * -------------------------------------------------- */

    private fun setupClicks() = with(binding) {
        btnSave.setOnClickListener {
            viewModel.onEvent(SaveClicked)
        }

        btnDelete.setOnClickListener {
            viewModel.onEvent(DeleteClicked)
        }
    }

    /* --------------------------------------------------
     * Inputs
     * -------------------------------------------------- */

    private fun setupInputs() = with(binding) {

        etSortOrder.doAfterTextChanged {
            viewModel.onSortOrderChanged(it.toString())
        }

        etAccountName.doAfterTextChanged {
            viewModel.onNameChanged(it.toString())
        }

        etDefaultRate.doAfterTextChanged {
            viewModel.onRateChanged(it.toString())
        }

        etAdvanceAmount.doAfterTextChanged {
            viewModel.onAdvanceAmountChanged(it.toString())
        }

        etInitialBalance.doAfterTextChanged {
            viewModel.onInitialBalanceChanged(it.toString())
            updateBalancePreview()
        }

        // Opening Date → acts like button
        etOpeningDate.apply {
            inputType = InputType.TYPE_NULL
            keyListener = null
            isFocusable = false
            isClickable = true

            setOnClickListener {
                viewModel.onEvent(OnOpeningDateClicked)
            }
        }

        switchActive.setOnCheckedChangeListener { _, isChecked ->
            if (switchActive.isPressed) {
                viewModel.onEvent(
                    OnActiveStatusChanged(isChecked)
                )
            }
        }

        etSortOrder.focusWithScroll(binding.scrollView)
    }

    /* --------------------------------------------------
     * Account Type
     * -------------------------------------------------- */

    private fun setupAccountTypeRadioGroup() = with(binding) {
        rgAccountType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbCustomer ->
                    viewModel.onAccountTypeSelected(AccountType.CUSTOMER)

                R.id.rbSupplier ->
                    viewModel.onAccountTypeSelected(AccountType.SUPPLIER)
            }
        }
    }

    /* --------------------------------------------------
     * Observers
     * -------------------------------------------------- */

    override fun setupObservers() {
        collectFlow(viewModel.uiState) { renderState(it) }
        collectEffect(viewModel.uiEffect) { handleEffect(it) }
    }

    /* --------------------------------------------------
     * Effects
     * -------------------------------------------------- */

    private fun handleEffect(effect: AccountFormEffect) = with(binding) {
        when (effect) {

            is AccountFormEffect.ShowToast ->
                showSnackbar(effect.message)

            AccountFormEffect.CloseScreen ->
                findNavController().popBackStack(
                    R.id.accountListFragment,
                    false
                )

            is AccountFormEffect.FocusField ->
                focusField(effect.field)

            is AccountFormEffect.OpenDatePicker -> {
                openDatePicker(
                    initialDate = effect.currentDate
                ) { selectedDate ->
                    viewModel.onEvent(
                        OnOpeningDateSelected(selectedDate)
                    )
                }
            }

            is AccountFormEffect.ShowBalanceError -> {
                showDialog(
                    title = "Cannot Delete!",
                    message = """
                        This account has balance of (Rs. ${effect.balance.toRupees()}).
                        
                        You can not delete this.
                         
                        First clear balance.
                    """.trimIndent(),
                    positiveText = "OK",
                    onAction = {}
                )
            }
        }
    }

    /* --------------------------------------------------
     * Render State
     * -------------------------------------------------- */

    private fun renderState(state: AccountFormUiState) = with(binding) {

        btnSave.isEnabled = !state.isSaving
        btnSave.text = if (state.isEditMode) "Update" else "Save"

        etSortOrder.setTextIfDifferent(state.sortOrder)
        etAccountName.setTextIfDifferent(state.personName)
        etDefaultRate.setTextIfDifferent(state.rate)
        etInitialBalance.setTextIfDifferent(state.initialBalance)
        etAdvanceAmount.setTextIfDifferent(state.advanceAmount)

        etOpeningDate.setTextIfDifferent(
            state.openingDate?.toCompleteDateFormat() ?: ""
        )

        openingDateLayout.error = state.validation.openingDateError

        // Switch
        switchActive.setOnCheckedChangeListener(null)
        switchActive.isChecked = state.isActive
        switchActive.text =
            if (state.isActive) "Account Status: Active"
            else "Account Status: Inactive (Archived)"

        switchActive.setOnCheckedChangeListener { _, isChecked ->
            if (switchActive.isPressed) {
                viewModel.onEvent(
                    OnActiveStatusChanged(isChecked)
                )
            }
        }

        when (state.selectAccountType) {
            AccountType.CUSTOMER ->
                if (!rbCustomer.isChecked) rbCustomer.isChecked = true

            AccountType.SUPPLIER ->
                if (!rbSupplier.isChecked) rbSupplier.isChecked = true

            else -> {}
        }

        btnDelete.isVisible = state.showDeleteButton

        updateBalancePreview(
            state.selectAccountType,
            state.initialBalance
        )

        sortOrderLayout.error = state.validation.sortOrderError
        nameLayout.error = state.validation.nameError
        rateLayout.error = state.validation.rateError
    }

    /* --------------------------------------------------
     * Helpers
     * -------------------------------------------------- */

    private fun updateBalancePreview(
        forcedType: AccountType? = null,
        forcedAmount: String? = null
    ) = with(binding) {

        val type =
            forcedType ?: if (rbCustomer.isChecked)
                AccountType.CUSTOMER
            else AccountType.SUPPLIER

        val amountString =
            forcedAmount ?: etInitialBalance.text.toString()

        val rawAmount = amountString.toPaisa()

        val finalAmount =
            if (type == AccountType.SUPPLIER) -rawAmount else rawAmount

        tvBalancePreview.setBalanceColorWithRoundRupee(
            finalAmount,
            prefix = "Net Impact: "
        )
    }

    private fun focusField(field: Field) = with(binding) {
        when (field) {
            Field.SORT_ORDER -> etSortOrder.requestFocus()
            Field.NAME -> etAccountName.requestFocus()
            Field.ACCOUNT_TYPE -> rgAccountType.requestFocus()
            Field.RATE -> etDefaultRate.requestFocus()
            Field.INITIAL_BALANCE -> etInitialBalance.requestFocus()
            Field.OPENING_DATE -> {
                etOpeningDate.requestFocus()
                // Optional: You can perform a shake animation here if desired
            }
        }
    }

    private fun View.focusWithScroll(scrollView: ScrollView) {
        scrollView.post {
            scrollView.smoothScrollTo(0, this.top)
            this.requestFocus()
        }
    }
}
