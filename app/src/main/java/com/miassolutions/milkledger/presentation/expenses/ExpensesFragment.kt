package com.miassolutions.milkledger.presentation.expenses

import android.view.MenuItem
import androidx.fragment.app.viewModels
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.autoSelectOnFocus
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
        val isAdmin = SharedPrefsHelper.isAdmin(requireContext())

        if (isAdmin){
            showDialog("Delete Expense", "Are you sure to delete this expense?") {
                viewModel.deleteExpense(entry)
            }
        } else {
            showSnackbar("Only ADMIN can delete")
        }


    }





    override fun setupObservers() {
        viewModel.uiState.collectState { state ->

            // Format date
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
            binding.tvSelectedDate.text = state.currentDate.format(formatter)

            // Merge fixed + variable for UI
            val combinedList = state.fixedExpenses + state.variableExpenses
            adapter.submitList(combinedList)

            // Totals
            binding.apply {
                tvTotalExpense.text = (state.fixedTotal + state.variableTotal).toRoundedStr()
                tvAvgExpenses.text =
                    if (combinedList.isNotEmpty())
                        ((state.fixedTotal + state.variableTotal) / combinedList.size).toRoundedStr()
                    else "0"
            }
        }
    }




    override fun setupListeners() = with(binding) {


        fabAddExpense.setOnClickListener {
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
        }


        tvSelectedDate.setOnClickListener {
            val admin = SharedPrefsHelper.getUserRole(requireContext())
            val isAuth = admin == "admin"
            // Assume you fetch the authorization status dynamically
            val isUserAuthorized = isAuth // Replace with actual auth check

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
