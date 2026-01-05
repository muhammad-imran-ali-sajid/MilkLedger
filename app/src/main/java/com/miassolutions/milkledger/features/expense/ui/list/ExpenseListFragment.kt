package com.miassolutions.milkledger.features.expense.ui.list

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentExpensesBinding
import com.miassolutions.milkledger.utils.extensions.showDeleteActionDialog
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toPrice
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExpenseListFragment :
    BaseFragment<FragmentExpensesBinding>(FragmentExpensesBinding::inflate) {


    private val viewModel: ExpenseListViewModel by viewModels()
    private val adapter = ExpenseAdapter(
        onItemClick = { expense ->
            viewModel.onEvent(ExpenseListUiEvent.OnExpenseClicked(expense))
        },
        onDeleteClick = {
            showDeleteActionDialog {

                viewModel.onEvent(ExpenseListUiEvent.OnDeleteClicked(it))
            }
        }
    )

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
        binding.dateHeader.btnPrevDate.setOnClickListener {
            viewModel.onEvent(ExpenseListUiEvent.OnPrevDate)
        }
        binding.dateHeader.btnNextDate.setOnClickListener {
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

                binding.dateHeader.tvSelectedDate.text = state.date.toCompleteDateFormat()


                binding.tvTotalExpense.text = "Rs. ${state.displayTotal.toPrice()}"
                binding.tvAvgExpenses.text = "Rs. ${state.displayAvgExpense.toPrice("%.2f")}"
            }
        }
    }

    private fun observeEffects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiEffect.collectLatest { effect ->
                when (effect) {
                    is ExpenseListUiEffect.NavigateToAddExpense -> {
                        val action =
                            ExpenseListFragmentDirections.actionExpenseFragmentToExpenseFormFragment(
                                effect.dateMillis
                            )

                        findNavController().navigate(action)
                    }

                    is ExpenseListUiEffect.ShowSnackbar -> {
                        // Show snackbar
                        showSnackbar(effect.message)
                    }

                    is ExpenseListUiEffect.NavigateToEditExpense -> {
                        val action =
                            ExpenseListFragmentDirections.actionExpenseFragmentToEditExpenseBottomSheet(
                                effect.expense
                            )
                        findNavController().navigate(action)
                    }
                }
            }
        }
    }


}