package com.miassolutions.milkledger.features.supplier.ui.form

import android.os.Bundle
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.core.ui.BaseBottomSheet
import com.miassolutions.milkledger.utils.extensions.setTextIfDifferent
import com.miassolutions.milkledger.databinding.SupplierFormLayoutBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SupplierFormBottomSheetFragment : BaseBottomSheet<SupplierFormLayoutBinding>(
    SupplierFormLayoutBinding::inflate
) {

    private val viewModel by viewModels<SupplierFormViewModel>()

    override fun onViewReady(savedInstanceState: Bundle?) = with(binding) {

        collectFlow(viewModel.uiState) { state ->
            etPosition.setTextIfDifferent(state.position)
            etName.setTextIfDifferent(state.name)
            etRate.setTextIfDifferent(state.rate)
            etAdvanceAmount.setTextIfDifferent(state.advanceAmount)

            etPosLayout.error = state.positionError
            nameLayout.error = state.nameError
            etRateLayout.error = state.rateError

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
                SupplierFormUiEffect.Dismiss -> {
                    dismiss()
                }

                is SupplierFormUiEffect.ShowMessage -> {
                    showToast(effect.message)
                }
            }
        }

        etPosition.doAfterTextChanged {
            viewModel.onEvent(SupplierFormUiEvent.OnPositionChanged(it.toString()))
        }

        etName.doAfterTextChanged {
            viewModel.onEvent(SupplierFormUiEvent.OnNameChanged(it.toString()))
        }

        etRate.doAfterTextChanged {
            viewModel.onEvent(SupplierFormUiEvent.OnRateChanged(it.toString()))
        }

        etAdvanceAmount.doAfterTextChanged {
            viewModel.onEvent(SupplierFormUiEvent.OnAdvanceAmountChanged(it.toString()))
        }

        btnSave.setOnClickListener {
            viewModel.onEvent(
                SupplierFormUiEvent.OnSaveClicked
            )
        }

    }
}
