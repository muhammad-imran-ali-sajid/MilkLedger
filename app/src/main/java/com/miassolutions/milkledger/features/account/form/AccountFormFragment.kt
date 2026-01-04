package com.miassolutions.milkledger.features.account.form

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.ui.BaseBottomSheet
import com.miassolutions.milkledger.databinding.FragmentAccountFormBinding
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountFormFragment :
    BaseBottomSheet<FragmentAccountFormBinding>(FragmentAccountFormBinding::inflate) {

    private val viewModel by viewModels<AccountFormViewModel>()

    override fun onViewReady(savedInstanceState: Bundle?) {

        setupInputs()
        setupObservers()
        setupClicks()
        setupAccountTypeDropDown()

    }

    private fun setupClicks() = with(binding) {
        btnSave.setOnClickListener {
            viewModel.onEvent(AccountFormEvent.SaveClicked)
        }

    }

    private fun setupAccountTypeDropDown() {
        val items = AccountType.entries.map { it.name }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)

        binding.dropdownAccountType.setAdapter(adapter)

        binding.dropdownAccountType.setOnItemClickListener { _, _, position, _ ->
            viewModel.onAccountTypeSelected(AccountType.entries[position])
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
        }

        etAdvanceAmount.doAfterTextChanged {
            viewModel.onAdvanceAmountChanged(it.toString())
        }

    }

    private fun setupObservers() {
        collectFlow(viewModel.uiState) { state ->
            renderState(state)

        }

        collectEffect(viewModel.uiEffect) { effect ->
            handleEffect(effect)
        }
    }

    private fun handleEffect(effect: AccountFormEffect) = with(binding) {
        when (effect) {
            is AccountFormEffect.ShowToast -> showToast(effect.toString())
            is AccountFormEffect.CloseScreen -> dismiss()
        }

    }

    private fun renderState(state: AccountFormUiState) = with(binding) {
        btnSave.isEnabled = !state.isSaving

        sortOrderLayout.error = state.validation.sortOrderError
        nameLayout.error = state.validation.nameError
        rateLayout.error = state.validation.rateError
        initialBalanceLayout.error = state.validation.initialBalanceError
    }


}