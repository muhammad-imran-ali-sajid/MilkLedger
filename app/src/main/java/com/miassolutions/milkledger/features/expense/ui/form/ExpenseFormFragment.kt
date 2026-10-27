package com.miassolutions.milkledger.features.expense.ui.form

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentAddExpenseBinding
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.openDatePicker
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExpenseFormFragment :
    BaseFragment<FragmentAddExpenseBinding>(FragmentAddExpenseBinding::inflate) {

    private val viewModel: ExpenseFormViewModel by viewModels()
    private lateinit var personalExpenseAdapter: PersonalExpenseAdapter

    private fun setupPersonalExpenseRecycler() = with(binding) {
        personalExpenseAdapter = PersonalExpenseAdapter(
            onDraftChanged = { id, field, value ->
                viewModel.onEvent(
                    ExpenseFormUiEvent.OnPersonalDraftChanged(
                        id = id,
                        field = field,
                        value = value
                    )
                )
            },

            onCommit = { id ->
                viewModel.onEvent(
                    ExpenseFormUiEvent.OnPersonalCommit(id)
                )
            },

            onRemove = { id ->
                viewModel.onEvent(
                    ExpenseFormUiEvent.OnRemovedPersonalExpense(id)
                )
            }
        )

        rvPersonalExpense.adapter = personalExpenseAdapter
        rvPersonalExpense.isNestedScrollingEnabled = false
    }

    override fun setupObservers() = with(binding) {
        super.setupObservers()

        collectFlow(viewModel.uiState) { state ->

            etDate.setTextIfChanged(
                state.date.toCompleteDateFormat()
            )
            etDate.error = state.dateError

            etFuelAmount.setTextIfChanged(state.fuelAmount)
            etFuelAmount.error = state.fuelError

            etVehicleAmount.setTextIfChanged(state.vehicleAmount)
            etVehicleAmount.error = state.vehicleError

            etRefreshmentAmount.setTextIfChanged(state.refreshmentAmount)
            etRefreshmentAmount.error = state.refreshmentError

            etOtherBusinessExpense.setTextIfChanged(state.otherBusiness)
            etOtherBusinessExpense.error = state.otherBusinessError

            etNotes.setTextIfChanged(state.notes)

            tvTotalExpense.text = state.totalExpense.toString()

            btnSave.isEnabled = !state.isSaving

            personalExpenseAdapter.submitList(
                state.personalExpenses.toList()
            )
        }

        collectEffect(viewModel.uiEffect) { effect ->
            when (effect) {

                ExpenseFormUiEffect.ExpenseSaved -> {
                    findNavController().navigateUp()
                }

                ExpenseFormUiEffect.OpenDatePicker -> {
                    openDatePicker { date ->
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

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupPersonalExpenseRecycler()
        setupKeyboardInsets()
    }

    override fun setupListeners() = with(binding) {
        super.setupListeners()

        etDate.setOnClickListener {
            viewModel.onEvent(
                ExpenseFormUiEvent.OnDateClick
            )
        }

        etFuelAmount.doAfterTextChanged {
            viewModel.onEvent(
                ExpenseFormUiEvent.OnFuelChanged(
                    it.toString()
                )
            )
        }

        etVehicleAmount.doAfterTextChanged {
            viewModel.onEvent(
                ExpenseFormUiEvent.OnVehicleChanged(
                    it.toString()
                )
            )
        }

        etRefreshmentAmount.doAfterTextChanged {
            viewModel.onEvent(
                ExpenseFormUiEvent.OnRefreshmentChanged(
                    it.toString()
                )
            )
        }

        etOtherBusinessExpense.doAfterTextChanged {
            viewModel.onEvent(
                ExpenseFormUiEvent.OnOtherChanged(
                    it.toString()
                )
            )
        }

        etNotes.doAfterTextChanged {
            viewModel.onEvent(
                ExpenseFormUiEvent.OnNotesChanged(
                    it.toString()
                )
            )
        }

        btnAddPersonal.setOnClickListener {
            viewModel.onEvent(
                ExpenseFormUiEvent.OnAddPersonalExpense
            )
        }

        btnSave.setOnClickListener {
            viewModel.onEvent(
                ExpenseFormUiEvent.OnSaveClicked
            )
        }
    }

    private fun setupKeyboardInsets() {
        val initialScrollPaddingBottom = binding.scrollView.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollView) { view, insets ->

            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())


            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                initialScrollPaddingBottom +
                        if (imeVisible) ime.bottom else systemBars.bottom
            )

            insets
        }
    }

    private fun EditText.setTextIfChanged(value: String) {
        if (text.toString() == value) return

        setText(value)
        setSelection(text.length)
    }
}