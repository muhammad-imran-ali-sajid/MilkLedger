package com.miassolutions.milkledger.presentation.expenses

import android.view.MenuItem
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
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
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Expense")
            .setMessage("Are you sure to delete this expense?")
            .setNeutralButton("Yes") { d, _ ->
                viewModel.deleteExpense(entry)
                showToast("Expense deleted")
                d.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
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
        viewModel.uiState.collectState { state ->
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
            binding.dateHeader.tvSelectedDate.text = state.currentDate.format(formatter)

            adapter.submitList(state.expensesList)

            binding.apply {
                tvTotalExpense.text = state.todayTotalExpenses.toRoundedStr()
                tvAvgExpenses.text = state.todayAvgExpenses.toRoundedStr()
            }


        }
    }

    override fun setupListeners() = with(binding) {
        dateHeader.btnNextDate.setOnClickListener {
            viewModel.onEvent(ExpensesUiEvent.NextDate)
        }

        dateHeader.btnPrevDate.setOnClickListener {
            viewModel.onEvent(ExpensesUiEvent.PrevDate)
        }

    }
}
