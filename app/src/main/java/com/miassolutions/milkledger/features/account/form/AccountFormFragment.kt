package com.miassolutions.milkledger.features.account.form

import android.view.View
import android.widget.ScrollView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentAccountFormBinding
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.setBalanceWithColorRupee
import com.miassolutions.milkledger.utils.extensions.setTextIfDifferent
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
            viewModel.onEvent(AccountFormEvent.SaveClicked)
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
        etSortOrder.doAfterTextChanged {
            viewModel.onSortOrderChanged(it.toString())
        }

        etAccountName.doAfterTextChanged {
            viewModel.onNameChanged(it.toString())
        }

        etDefaultRate.doAfterTextChanged {
            viewModel.onRateChanged(it.toString())
        }

        etInitialBalance.doAfterTextChanged {
            viewModel.onInitialBalanceChanged(it.toString())
            updateBalancePreview()
        }

        etAdvanceAmount.doAfterTextChanged {
            viewModel.onAdvanceAmountChanged(it.toString())
        }

        etSortOrder.focusWithScroll(binding.scrollView)

    }

    private fun updateBalancePreview(
        forcedType: AccountType? = null,
        forcedAmount: String? = null
    ) = with(binding) {

        // Current Type aur Amount uthayen (State se ya UI se)
        val type = forcedType ?: if (rbCustomer.isChecked) AccountType.CUSTOMER else AccountType.SUPPLIER
        val amountString = forcedAmount ?: etInitialBalance.text.toString()

        // Amount ko Paisa me convert karein
        val rawAmount = amountString.toPaisa()

        // LOGIC:
        // Agar Customer hai to Positive (Lene hen)
        // Agar Supplier hai to Negative (Dene hen)
        val finalAmount = if (type == AccountType.SUPPLIER) -rawAmount else rawAmount

        // Extension function use karein jo humne pehle banaya tha
        tvBalancePreview.setBalanceWithColorRupee(finalAmount, prefix = "Net Impact: ")
    }

    override fun setupObservers() {
        collectFlow(viewModel.uiState) { state ->
            renderState(state)

        }

        collectEffect(viewModel.uiEffect) { effect ->
            handleEffect(effect)
        }
    }

    private fun handleEffect(effect: AccountFormEffect) = with(binding) {
        when (effect) {
            is AccountFormEffect.ShowToast ->
                showToast(effect.message)

            AccountFormEffect.CloseScreen ->
                findNavController().popBackStack(
                    R.id.accountListFragment, false
                )


            is AccountFormEffect.FocusField ->
                focusField(effect.field)
        }
    }

    private fun focusField(field: Field) = with(binding) {
        when (field) {
            Field.SORT_ORDER -> {
                etSortOrder.requestFocus()
                etSortOrder.setSelection(etSortOrder.text?.length ?: 0)
            }

            Field.NAME -> {
                etAccountName.requestFocus()
                etAccountName.setSelection(etAccountName.text?.length ?: 0)
            }

            Field.ACCOUNT_TYPE -> {
                rgAccountType.requestFocus()
            }

            Field.RATE -> {
                etDefaultRate.requestFocus()
                etDefaultRate.setSelection(etDefaultRate.text?.length ?: 0)
            }

            Field.INITIAL_BALANCE -> {
                etInitialBalance.requestFocus()
                etInitialBalance.setSelection(etInitialBalance.text?.length ?: 0)
            }
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

        // 🔹 TEXT FIELDS (EDIT MODE PREFILL)
        etSortOrder.setTextIfDifferent(state.sortOrder)
        etAccountName.setTextIfDifferent(state.personName)
        etDefaultRate.setTextIfDifferent(state.rate)
        etInitialBalance.setTextIfDifferent(state.initialBalance)
        etAdvanceAmount.setTextIfDifferent(state.advanceAmount)

        // 🔹 RADIO BUTTONS
        when (state.selectAccountType) {
            AccountType.CUSTOMER -> rbCustomer.isChecked = true
            AccountType.SUPPLIER -> rbSupplier.isChecked = true
            else -> {}
        }

        // 🔥 State render hotay waqt bhi preview update karein
        updateBalancePreview(state.selectAccountType, state.initialBalance)

        // 🔹 ERRORS
        sortOrderLayout.error = state.validation.sortOrderError
        nameLayout.error = state.validation.nameError
        rateLayout.error = state.validation.rateError
    }


}