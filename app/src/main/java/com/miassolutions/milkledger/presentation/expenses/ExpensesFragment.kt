package com.miassolutions.milkledger.presentation.expenses

import android.view.MenuItem
import androidx.fragment.app.viewModels
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.showExpenseDatePicker
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.databinding.FragmentExpensesBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class ExpensesFragment : BaseFragment<FragmentExpensesBinding>(FragmentExpensesBinding::inflate) {

    private val viewModel: ExpenseViewModel by viewModels()
    private lateinit var adapter: ExpensesAdapter
    override fun getMenuResId(): Int? = R.menu.expenses_menu

    override fun setupViews() {
        setToolbarTitle(getString(R.string.expenses))
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter =
            ExpensesAdapter(onClick = ::showBottomSheet, onLongClick = ::onConfirmDeleteDialog)
        binding.rvExpenses.adapter = adapter
    }

    private fun showBottomSheet(entry: ExpensesEntity) {
        ExpenseEditBottomSheet(
            entry = entry,
            onSave = { viewModel.updateExpense(it) },
            isNewExpense = false
        ).show(parentFragmentManager, null)
    }

    private fun onConfirmDeleteDialog(entry: ExpensesEntity) {
        showDialog("Delete Expense", "Are you sure to delete this expense?") {
            viewModel.deleteExpense(entry)
        }
    }


    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionNewExpense -> {
                // Use the currently selected date from the UI state for the new expense
                val selectedDate = viewModel.uiState.value.currentDate
                ExpenseEditBottomSheet(
                    entry = ExpensesEntity(
                        expenseTitle = "New Title",
                        expenseAmount = 0.0,
                        createdAt = LocalDateTime.now().toString(),
                        date = selectedDate, // Use current date
                    ),
                    onSave = {
                        viewModel.insertExpense(it)
                    },
                    isNewExpense = true
                ).show(parentFragmentManager, null)
                true
            }

            else -> false
        }
    }


    override fun setupObservers() {
        // collectState is a custom extension of collectLatest or similar
        viewModel.uiState.collectState { state ->
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
            binding.tvSelectedDate.text = state.currentDate.format(formatter)

            // FIX: Removed the redundant call to viewModel.collectExpenses(state.currentDate)
            // The ViewModel is already collecting data based on its internal state changes.

            adapter.submitList(state.expensesList)

            binding.apply {
                tvTotalExpense.text = state.todayTotalExpenses.toRoundedStr()
                tvAvgExpenses.text = state.todayAvgExpenses.toRoundedStr()
            }
        }
    }

    override fun setupListeners() = with(binding) {
        // Allow clicking the date text to open the date picker
//        tvSelectedDate.setOnClickListener {
////            // Pass the current date as the pre-selected date for better UX
////            val initialDate = viewModel.uiState.value.currentDate
////
////            pickSingleDate(
////                title = "Select Expense Date",
////                initialDate = initialDate,
////                onPicked = { selectedDate: LocalDate ->
////                    viewModel.onEvent(ExpensesUiEvent.SelectDate(selectedDate))
////                }
////            )
//
//            val datePickerLogic = DatePickerLogic()
//            val isAuth = datePickerLogic.buildConstraints(isAuthorized = false)
//
//        }

        tvSelectedDate.setOnClickListener {
            // Assume you fetch the authorization status dynamically
            val isUserAuthorized = false // Replace with actual auth check

            // Pass the current date as the pre-selected date for better UX
            val initialDate = viewModel.uiState.value!!.currentDate

            showExpenseDatePicker(

                isAuthorized = isUserAuthorized,
                initialDate = initialDate,
                // The selectedDate (LocalDate) is available here!
                onPicked = { selectedDate: LocalDate ->
                    // This is where you pass the result to your ViewModel
                    viewModel.onEvent(ExpensesUiEvent.SelectDate(selectedDate))
                }
            )
        }

    }
}
