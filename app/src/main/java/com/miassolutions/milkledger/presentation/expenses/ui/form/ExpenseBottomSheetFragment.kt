package com.miassolutions.milkledger.presentation.expenses.ui.form

import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.utils.extensions.toDisplayFormat
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.utils.extensions.setTextIfDifferent
import com.miassolutions.milkledger.databinding.FragmentAddExpenseBinding
import com.miassolutions.milkledger.databinding.ItemPersonalExpenseBinding
import com.miassolutions.milkledger.presentation.expenses.ui.detail.ExpenseInput
import com.miassolutions.milkledger.presentation.expenses.ui.detail.ExpenseUiEvent
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExpenseAddBottomSheetFragment :
    BaseFragment<FragmentAddExpenseBinding>(FragmentAddExpenseBinding::inflate) {

    private val viewModel: ExpenseAddViewModel by viewModels()

    override fun setupViews() {
        setupDatePicker()
        setupBusinessInputs()
    }

    override fun setupListeners() = with(binding) {
        btnAddPersonal.setOnClickListener {
            viewModel.addPersonalRow()
        }

        btnSave.setOnClickListener {
            viewModel.save()
        }
    }

    override fun setupObservers() {

        // UI STATE
        collectFlow(viewModel.uiState) { state ->
            binding.etDate.setText(state.date.toDisplayFormat())

            renderBusinessInputs(state.businessInputs)
            renderPersonalInputs(state.personalItems)
        }

        // EVENTS
        collectEffect(viewModel.uiEvent) { event ->
            when (event) {
                is ExpenseUiEvent.ShowMessage -> showToast(event.message)
                ExpenseUiEvent.Dismiss -> findNavController().popBackStack()
            }
        }
    }

    // ─────────────────────────────────────────────
    // DATE PICKER
    // ─────────────────────────────────────────────
    private fun setupDatePicker() {
//        binding.etDate.setOnClickListener {
//            showExpenseDatePicker(
//                isAuthorized = true,
//                initialDate = viewModel.uiState.value.date,
//                onPicked = { viewModel.onDateSelected(it) }
//            )
//        }
    }

    // ─────────────────────────────────────────────
    // BUSINESS (FIXED)
    // ─────────────────────────────────────────────
    private fun setupBusinessInputs() {
        // Static fields already in layout (Fuel, Vehicle, Refreshment)
        binding.etFuelAmount.doAfterTextChanged {
            viewModel.updateBusinessAmount("Fuel", it.toString())
        }
        binding.etVehicleAmount.doAfterTextChanged {
            viewModel.updateBusinessAmount("Vehicle", it.toString())
        }
        binding.etRefreshmentAmount.doAfterTextChanged {
            viewModel.updateBusinessAmount("Refreshment", it.toString())
        }
    }

    private fun renderBusinessInputs(inputs: List<ExpenseInput>) {
        inputs.forEach { input ->
            when (input.title) {
                "Fuel" -> binding.etFuelAmount.setTextIfDifferent(input.amount.toString())
                "Vehicle" -> binding.etVehicleAmount.setTextIfDifferent(input.amount.toString())
                "Refreshment" -> binding.etRefreshmentAmount.setTextIfDifferent(input.amount.toString())
            }
        }
    }

    // ─────────────────────────────────────────────
    // PERSONAL (DYNAMIC)
    // ─────────────────────────────────────────────
    private fun renderPersonalInputs(items: List<ExpenseInput>) {
        binding.personalContainer.removeAllViews()

        items.forEachIndexed { index, item ->
            val row = ItemPersonalExpenseBinding.inflate(
                layoutInflater,
                binding.personalContainer,
                false
            )

            row.etPersonalTitle.setTextIfDifferent(item.title)
            row.etPersonalAmount.setTextIfDifferent(item.amount.toString())

            row.etPersonalTitle.doAfterTextChanged {
                viewModel.updatePersonalTitle(index, it.toString())
            }

            row.etPersonalAmount.doAfterTextChanged {
                viewModel.updatePersonalAmount(index, it.toString())
            }

            row.btnRemove.setOnClickListener {
                viewModel.removePersonalRow(index)
            }

            binding.personalContainer.addView(row.root)
        }
    }
}
