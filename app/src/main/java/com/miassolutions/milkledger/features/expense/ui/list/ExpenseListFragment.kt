package com.miassolutions.milkledger.features.expense.ui.list

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentExpensesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class ExpenseListFragment :
    BaseFragment<FragmentExpensesBinding>(FragmentExpensesBinding::inflate) {


    private val viewModel: ExpenseListViewModel by viewModels()
    private val adapter = ExpenseAdapter { expense ->
        viewModel.onEvent(ExpenseListUiEvent.OnExpenseClicked(expense))
    }

    override fun setupViews() {
        super.setupViews()
        setupRecyclerView()
        setupClicks()
        observeState()
        observeEffects()
    }


    private fun setupRecyclerView() {
        binding.rvExpenses.adapter = adapter
    }

    private fun setupClicks() {
        binding.fabAddExpense.setOnClickListener {
            viewModel.onEvent(ExpenseListUiEvent.OnAddExpenseClicked)
        }

        // Date Header Clicks (Assuming IDs inside included layout)
        binding.dateHeader.btnNextDate.setOnClickListener {
            viewModel.onEvent(ExpenseListUiEvent.OnPrevDate)
        }
        binding.dateHeader.btnPrevDate.setOnClickListener {
            viewModel.onEvent(ExpenseListUiEvent.OnNextDate)
        }
        binding.dateHeader.tvSelectedDate.setOnClickListener {
            // Yahan DatePicker dialog open kar k OnDateSelected call karein
            // Filhal simple rakhte hain
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                // 1. Update List
                adapter.submitList(state.expenses)

                // 2. Handle Empty State
                binding.progressBar.isVisible = state.isLoading
                binding.emptyStateLayout.isVisible = !state.isLoading && state.expenses.isEmpty()
                binding.rvExpenses.isVisible = !state.isLoading && state.expenses.isNotEmpty()

                // 3. Update Header Date
                // Note: Aapka Date Format Utils use karein
                val dateText = state.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                // binding.dateHeader.tvDate.text = dateText (Binding k zariye access karein)
                // Lekin include binding access karne k liye:
                // binding.dateHeader is NOT a layout, it's a View in generated binding unless typed.
                // Simple View access:
                binding.dateHeader.tvSelectedDate.text = dateText

                // 4. Update Summary (Bottom Bar)
                val totalRupees = state.totalExpense / 100.0
                val avgRupees = state.avgExpense / 100.0

                binding.tvTotalExpense.text = "Rs. ${String.format("%.0f", totalRupees)}"
                binding.tvAvgExpenses.text = "Rs. ${String.format("%.1f", avgRupees)}"
            }
        }
    }

    private fun observeEffects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiEffect.collectLatest { effect ->
                when (effect) {
                    ExpenseListUiEffect.NavigateToAddExpense -> {
                        findNavController().navigate(R.id.action_expenseFragment_to_expenseFormFragment)
                    }

                    is ExpenseListUiEffect.ShowSnackbar -> {
                        // Show snackbar
                    }
                }
            }
        }
    }


}