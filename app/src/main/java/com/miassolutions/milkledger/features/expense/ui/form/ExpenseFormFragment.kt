package com.miassolutions.milkledger.features.expense.ui.form

import android.os.Bundle
import android.view.View
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
import com.miassolutions.milkledger.utils.extensions.setTextIfDifferent
import com.miassolutions.milkledger.utils.extensions.openDatePicker
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toPrice
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExpenseFormFragment :
    BaseFragment<FragmentAddExpenseBinding>(FragmentAddExpenseBinding::inflate) {

    private val viewModel: ExpenseFormViewModel by viewModels()
    private lateinit var personalExpenseAdapter: PersonalExpenseAdapter

    // ---------------- RECYCLER SETUP ----------------

    private fun setupPersonalExpenseRecycler() = with(binding) {
        personalExpenseAdapter = PersonalExpenseAdapter(

            // 🔹 Draft typing (NO state update, NO jitter)
            onDraftChanged = { id, field, value ->
                viewModel.onEvent(
                    ExpenseFormUiEvent.OnPersonalDraftChanged(
                        id = id,
                        field = field,
                        value = value
                    )
                )
            },

            // 🔹 Commit when focus lost
            onCommit = { id ->
                viewModel.onEvent(
                    ExpenseFormUiEvent.OnPersonalCommit(id)
                )
            },

            // 🔹 Remove item
            onRemove = { id ->
                viewModel.onEvent(
                    ExpenseFormUiEvent.OnRemovedPersonalExpense(id)
                )
            }
        )

        rvPersonalExpense.adapter = personalExpenseAdapter
//        rvPersonalExpense.setHasFixedSize(true)
        rvPersonalExpense.isNestedScrollingEnabled = false
    }

    // ---------------- OBSERVERS ----------------

    override fun setupObservers() = with(binding) {
        super.setupObservers()

        // 1️⃣ UI STATE
        collectFlow(viewModel.uiState) { state ->

            tvTotalExpense.text = state.totalExpense.toString()

            tvDate.text ="Dated: ${state.date.toCompleteDateFormat()}"
            etDate.setTextIfDifferent(state.date.toCompleteDateFormat())
            dateLayout.error = state.dateError

            etFuelAmount.setTextIfDifferent(state.fuelAmount)
            fuelLayout.error = state.fuelError

            etVehicleAmount.setTextIfDifferent(state.vehicleAmount)
            vehicleLayout.error = state.vehicleError

            etRefreshmentAmount.setTextIfDifferent(state.refreshmentAmount)
            refreshLayout.error = state.refreshmentError

            etOtherBusinessExpense.setTextIfDifferent(state.otherBusiness)
            tilOtherLayout.error = state.otherBusinessError

            etNotes.setTextIfDifferent(state.notes)

            btnSave.isEnabled = !state.isSaving

            // 🔹 IMPORTANT:
            // submitList ONLY on logical state change
            personalExpenseAdapter.submitList(
                state.personalExpenses.toList()
            )
        }

        // 2️⃣ UI EFFECTS (one-time)
        collectEffect(viewModel.uiEffect) { effect ->
            when (effect) {

                is ExpenseFormUiEffect.ExpenseSaved -> {
                    findNavController().navigateUp()
                }

                is ExpenseFormUiEffect.OpenDatePicker -> {

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

    // ---------------- VIEW CREATED ----------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPersonalExpenseRecycler()
        setupKeyboardInsets()
    }

    // ---------------- LISTENERS ----------------

    override fun setupListeners() = with(binding) {
        super.setupListeners()

        etDate.setOnClickListener {
            viewModel.onEvent(ExpenseFormUiEvent.OnDateClick)
        }

        etFuelAmount.doAfterTextChanged {
            viewModel.onEvent(
                ExpenseFormUiEvent.OnFuelChanged(it.toString())
            )
        }

        etVehicleAmount.doAfterTextChanged {
            viewModel.onEvent(
                ExpenseFormUiEvent.OnVehicleChanged(it.toString())
            )
        }

        etRefreshmentAmount.doAfterTextChanged {
            viewModel.onEvent(
                ExpenseFormUiEvent.OnRefreshmentChanged(it.toString())
            )
        }

        etOtherBusinessExpense.doAfterTextChanged {
            viewModel.onEvent(
                ExpenseFormUiEvent.OnOtherChanged(it.toString())
            )
        }

        etNotes.doAfterTextChanged {
            viewModel.onEvent(
                ExpenseFormUiEvent.OnNotesChanged(it.toString())
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

            binding.saveContainer.isVisible = !imeVisible

            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                initialScrollPaddingBottom + if (imeVisible) ime.bottom else systemBars.bottom
            )

            insets
        }
    }
}
