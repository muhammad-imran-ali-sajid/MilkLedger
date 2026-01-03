package com.miassolutions.milkledger.features.expense.ui.list

import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.localdb.expense.ExpenseEntity
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentExpensesBinding
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.showLedgerDatePicker
import com.miassolutions.milkledger.utils.extensions.toRoundedStr

import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class ExpenseFragment : BaseFragment<FragmentExpensesBinding>(FragmentExpensesBinding::inflate) {

    private val viewModel: ExpenseViewModel by viewModels()
    private lateinit var adapter: ExpensesAdapter

    override fun setupViews() {
        setToolbarTitle(getString(R.string.expenses))
        setupRecyclerView()

    }

    private fun setupRecyclerView() {
        adapter = ExpensesAdapter(
            onClick = ::openEditSheet,
            onLongClick = ::onConfirmDeleteDialog
        )
        binding.rvExpenses.adapter = adapter
    }




    // ─────────────────────────────────────────────────────────────────────────────
    // Edit existing entry
    // ─────────────────────────────────────────────────────────────────────────────
    private fun openEditSheet(entry: ExpenseEntity) {
//        val bottomSheet = ExpenseEditBottomSheet(
//            entry = entry,
//            onSave = { updated ->
//                viewModel.updateExpense(updated)  // update in DB
//            }
//        )
//
//        bottomSheet.show(parentFragmentManager, "editExpenseSheet")
    }


    // ─────────────────────────────────────────────────────────────────────────────
    // Delete entry
    // ─────────────────────────────────────────────────────────────────────────────
    private fun onConfirmDeleteDialog(entry: ExpenseEntity) {
//        val isAdmin = SharedPrefsHelper.isAdmin(requireContext())
//
//        if (isAdmin) {
//            showDialog("Delete Expense", "Are you sure to delete this expense?") {
//                viewModel.deleteExpense(entry)
//            }
//        } else {
//            showSnackbar("Only ADMIN can delete")
//        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Observers
    // ─────────────────────────────────────────────────────────────────────────────
    override fun setupObservers() {
        collectFlow(viewModel.uiState) { state ->

            binding.progressBar.visibility =
                if (state.isLoading) View.VISIBLE else View.GONE

            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
            binding.dateHeader.tvSelectedDate.text = state.currentDate.format(formatter)


            adapter.submitList(state.filteredList)


            val isEmpty = state.filteredList.isEmpty()
            binding.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvExpenses.visibility = if (isEmpty) View.GONE else View.VISIBLE

            binding.apply {
                val total = state.businessTotalExpenses + state.personalTotalExpenses
                tvTotalExpense.text = total.toRoundedStr()
                tvAvgExpenses.text = total.toRoundedStr()

            }
        }
    }




    private fun generatePdf() {
        val state = viewModel.uiState.value

    }


    // ─────────────────────────────────────────────────────────────────────────────
    // Listeners
    // ─────────────────────────────────────────────────────────────────────────────
    override fun setupListeners() = with(binding) {

        fabAddExpense.setOnClickListener {
            findNavController().navigate(
                R.id.action_expensesFragment_to_expenseAddEditFragment
            )
        }


        binding.dateHeader.tvSelectedDate.setOnClickListener {
            val currentDate = viewModel.uiState.value.currentDate

            val isAdmin = SharedPrefsHelper.isAdmin(requireContext())
            val isUserAuthorized = isAdmin // Replace with actual auth check

            showLedgerDatePicker(
                isAuthorized = isUserAuthorized,
                initialDate = currentDate,
                onPicked = { selectedDate: LocalDate ->

                    viewModel.onDateSelected(selectedDate)
                }
            )

        }


        binding.dateHeader.btnPrevDate.setOnClickListener {
            viewModel.goToPreviousDate()
        }

        binding.dateHeader.btnNextDate.setOnClickListener {
            viewModel.goToNextDate()
        }
    }
}
