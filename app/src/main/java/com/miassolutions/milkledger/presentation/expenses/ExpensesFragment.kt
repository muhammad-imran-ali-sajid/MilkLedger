package com.miassolutions.milkledger.presentation.expenses

import android.view.MenuItem
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.databinding.BottomsheetEditSalesBinding
import com.miassolutions.milkledger.databinding.FragmentExpensesBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class ExpensesFragment : BaseFragment<FragmentExpensesBinding>(FragmentExpensesBinding::inflate) {

    private val viewModel: ExpenseViewModel by viewModels()
    private lateinit var adapter: ExpensesAdapter
    override fun getMenuResId(): Int? = R.menu.expenses_menu

    override fun setupViews() {
        setToolbarTitle(getString(R.string.expenses))

        adapter = ExpensesAdapter { selectedExpense ->
            viewModel.onEvent(ExpensesUiEvent.OnExpenseSelected(selectedExpense.expenseId))
        }

        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenses.adapter = adapter


    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when {
            item.itemId == R.id.actionNewExpense -> {
                ExpenseEditBottomSheet(
                    entry = ExpensesEntity(
                        expenseTitle = "New Title",
                        expenseAmount = 0.0,
                        expenseNote = ""
                    ),
                    onSave = {},
                    isNewExpense = false
                ).show(parentFragmentManager, null)
                true
            }

            else -> false

        }
    }


    override fun setupObservers() {
        viewModel.uiState.collectState { state ->
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
            binding.dateHeader.tvSelectedDate.text = state.currentDate.format(formatter)

            adapter.submitList(state.expensesList)

//            binding.tvTotalExpense.text = getString(R.string.total_expenses, state.todayTotalExpenses)
//            binding.tvAvgExpenses.text = getString(R.string.avg_expenses, state.todayAvgExpenses)
        }
    }

    override fun setupListeners() = with(binding) {
        dateHeader.btnNextDate.setOnClickListener {
            viewModel.onEvent(ExpensesUiEvent.NextDate)
        }

        dateHeader.btnPrevDate.setOnClickListener {
            viewModel.onEvent(ExpensesUiEvent.PrevDate)
        }

        // (Optional) Add floating action button click or other interactions
//        binding.fabAddExpense.setOnClickListener {
//            showToast("Add Expense Clicked") // or navigate to AddExpenseBottomSheet
//        }
    }
}
