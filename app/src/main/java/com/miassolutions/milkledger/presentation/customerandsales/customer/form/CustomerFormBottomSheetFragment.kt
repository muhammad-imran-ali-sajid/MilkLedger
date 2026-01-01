package com.miassolutions.milkledger.presentation.customerandsales.customer.form


import android.os.Bundle
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.core.ui.BaseBottomSheet
import com.miassolutions.milkledger.utils.extensions.setTextIfDifferent
import com.miassolutions.milkledger.databinding.CustomerFormLayoutBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomerFormBottomSheetFragment : BaseBottomSheet<CustomerFormLayoutBinding>(
    CustomerFormLayoutBinding::inflate
) {
    private val viewModel: CustomerFormViewModel by viewModels()


    override fun onViewReady(savedInstanceState: Bundle?) = with(binding) {

        collectFlow(viewModel.uiState) { state ->
            etName.setTextIfDifferent(state.name)
            etRate.setTextIfDifferent(state.rate)
            etPosition.setTextIfDifferent(state.position)
            etAdvanceAmount.setTextIfDifferent(state.advanceAmount)

            nameLayout.error = state.nameError
            etRateLayout.error = state.rateError
            etPosLayout.error = state.positionError

            btnSave.apply {
                isEnabled = !state.isSaving

                text = when {
                    state.isSaving -> "Saving..."
                    state.isEdit -> "Update"
                    else -> "Save"
                }
            }
        }


        collectEffect(viewModel.uiEffect) { effect ->
            when (effect) {
                CustomerFormUiEffect.Dismiss -> dismiss()
                is CustomerFormUiEffect.ShowMessage -> {
                    showToast(effect.message)
                }
            }
        }

        // --- INPUTS ---
        etName.doAfterTextChanged {
            viewModel.onEvent(
                CustomerFormUiEvent.OnNameChanged(it.toString())
            )
        }
        etRate.doAfterTextChanged {
            viewModel.onEvent(
                CustomerFormUiEvent.OnRateChanged(it.toString())
            )
        }
        etPosition.doAfterTextChanged {
            viewModel.onEvent(
                CustomerFormUiEvent.OnPositionChanged(it.toString())
            )
        }
        etAdvanceAmount.doAfterTextChanged {
            viewModel.onEvent(
                CustomerFormUiEvent.OnAdvanceAmountChanged(it.toString())
            )
        }

        btnSave.setOnClickListener {
            viewModel.onEvent(
                CustomerFormUiEvent.OnSaveClicked
            )
        }

    }
}
