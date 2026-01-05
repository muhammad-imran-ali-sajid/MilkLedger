package com.miassolutions.milkledger.features.expense.ui.form

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentAddExpenseBinding

import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.setTextIfDifferent
import com.miassolutions.milkledger.utils.extensions.showLedgerDatePicker
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExpenseFormFragment :
    BaseFragment<FragmentAddExpenseBinding>(FragmentAddExpenseBinding::inflate) {


    private val viewModel: ExpenseFormViewModel by viewModels()
    private lateinit var personalExpenseAdapter: PersonalExpenseAdapter

    private fun setupPersonalExpenseRecycler() = with(binding) {
        personalExpenseAdapter = PersonalExpenseAdapter(
            onTitleChanged = { id, value ->
                viewModel.onEvent(
                    ExpenseFormUiEvent.OnPersonalTitleChanged(id, value)
                )
            },
            onAmountChanged = { id, value ->
                viewModel.onEvent(
                    ExpenseFormUiEvent.OnPersonalAmountChanged(id, value)
                )
            },
            onRemove = { id ->
                viewModel.onEvent(
                    ExpenseFormUiEvent.OnRemovedPersonalExpense(id)
                )
            }
        )

        rvPersonalExpense.adapter = personalExpenseAdapter
        rvPersonalExpense.setHasFixedSize(true)
        rvPersonalExpense.isNestedScrollingEnabled = false

    }


    override fun setupObservers() = with(binding) {
        super.setupObservers()

        // 1️⃣ Collect UI STATE
        collectFlow(viewModel.uiState) { state ->

            tvTotalExpense.text = "Total Expenses: Rs. ${state.totalExpense}"

            etDate.setTextIfDifferent(state.date.toCompleteDateFormat())
            dateLayout.error = state.dateError

            etFuelAmount.setTextIfDifferent(state.fuelAmount)
            fuelLayout.error = state.fuelError

            etVehicleAmount.setTextIfDifferent(state.vehicleAmount)
            vehicleLayout.error = state.vehicleError

            etRefreshmentAmount.setTextIfDifferent(state.refreshmentAmount)
            refreshLayout.error = state.refreshmentError

            etNotes.setTextIfDifferent(state.notes)

            btnSave.isEnabled = !state.isSaving

            personalExpenseAdapter.submitList(state.personalExpenses.toList())

        }

        // 2️⃣ Collect UI EFFECTS (ONCE)
        collectEffect(viewModel.uiEffect) { effect ->
            when (effect) {
                is ExpenseFormUiEffect.ExpenseSaved -> {
                    findNavController().navigateUp()
                }

                is ExpenseFormUiEffect.OpenDatePicker -> {
                    showLedgerDatePicker { date ->
                        viewModel.onEvent(
                            ExpenseFormUiEvent.OnDateSelected(date)
                        )
                    }
                }

                is ExpenseFormUiEffect.ShowSnackbar -> {
                    showSnackbar(effect.message)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPersonalExpenseRecycler()
    }




    override fun setupListeners() = with(binding) {
        super.setupListeners()



        etDate.setOnClickListener {
            viewModel.onEvent(ExpenseFormUiEvent.OnDateClick)
        }

        etFuelAmount.doAfterTextChanged {
            viewModel.onEvent(ExpenseFormUiEvent.OnFuelChanged(it.toString()))
        }

        etVehicleAmount.doAfterTextChanged {
            viewModel.onEvent(ExpenseFormUiEvent.OnVehicleChanged(it.toString()))
        }

        etRefreshmentAmount.doAfterTextChanged {
            viewModel.onEvent(ExpenseFormUiEvent.OnRefreshmentChanged(it.toString()))
        }

        etNotes.doAfterTextChanged {
            viewModel.onEvent(ExpenseFormUiEvent.OnNotesChanged(it.toString()))
        }

        btnAddPersonal.setOnClickListener {
            viewModel.onEvent(ExpenseFormUiEvent.OnAddPersonalExpense)
        }

        btnSave.setOnClickListener {
            viewModel.onEvent(ExpenseFormUiEvent.OnSaveClicked)
        }



    }


}
