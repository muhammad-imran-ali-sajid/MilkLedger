package com.miassolutions.milkledger.presentation.customer.form


import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.miassolutions.milkledger.core.extensions.collectEvent
import com.miassolutions.milkledger.core.extensions.collectFlow
import com.miassolutions.milkledger.core.ui.BaseBottomSheet
import com.miassolutions.milkledger.core.util.setTextIfDifferent
import com.miassolutions.milkledger.databinding.CustomerFormLayoutBinding
import com.miassolutions.milkledger.presentation.customer.customers.model.CustomerUi
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomerFormBottomSheetFragment : BaseBottomSheet<CustomerFormLayoutBinding>(
    CustomerFormLayoutBinding::inflate
) {


    private val viewModel: CustomerFormViewModel by viewModels()

    companion object {
        private const val ARG_CUSTOMER = "customer"

        fun newInstance(customer: CustomerUi?) =
            CustomerFormBottomSheetFragment().apply {
                arguments = bundleOf(ARG_CUSTOMER to customer)
            }
    }


    override fun onViewReady(savedInstanceState: Bundle?)=with(binding) {

        collectFlow(viewModel.uiState) { state ->
            etName.setTextIfDifferent(state.name)
            etRate.setTextIfDifferent(state.rate)
            etPosition.setTextIfDifferent(state.position)
            etAdvanceAmount.setTextIfDifferent(state.advanceAmount)

            nameLayout.error = state.nameError
            etRateLayout.error = state.rateError
            etPosLayout.error = state.positionError

            btnSave.text = if (state.isEdit) "Update" else "Save"
        }

        // --- EVENTS ---
        collectEvent(viewModel.uiEvent) { event ->
            when (event) {
                CustomerFormUiEvent.Dismiss -> dismiss()
            }
        }

        // --- INPUTS ---
        etName.doAfterTextChanged {
            viewModel.onNameChanged(it.toString())
        }
        etRate.doAfterTextChanged {
            viewModel.onRateChanged(it.toString())
        }
        etPosition.doAfterTextChanged {
            viewModel.onPositionChanged(it.toString())
        }
        etAdvanceAmount.doAfterTextChanged {
            viewModel.onAdvanceAmountChanged(it.toString())
        }

        btnSave.setOnClickListener {
            viewModel.onSaveClicked()
        }

    }
}
