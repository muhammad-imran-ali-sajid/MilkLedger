package com.miassolutions.milkledger.features.expense.ui.list

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentExpensesBinding
import com.miassolutions.milkledger.utils.extensions.hide
import com.miassolutions.milkledger.utils.extensions.openDatePicker
import com.miassolutions.milkledger.utils.extensions.show
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
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
        }
    )

    override fun setupViews() {
        super.setupViews()
        setupRecyclerView()
        setupClicks()
        observeState()
        observeEffects()
        actionMenus()
    }


    private fun actionMenus() {
        setupMenuWithCustomView(R.menu.menu_expense_list) { menu ->
            val item = menu.findItem(R.id.actionAdd) ?: return@setupMenuWithCustomView
            val btn = item.actionView
                ?.findViewById<MaterialButton>(R.id.btnAddExpense)
                ?: return@setupMenuWithCustomView

            btn.setOnClickListener {

                viewModel.onEvent(ExpenseListUiEvent.OnAddExpenseClicked)
            }

            val summaryItem =
                menu.findItem(R.id.actionShowSummary) ?: return@setupMenuWithCustomView
            summaryItem.setOnMenuItemClickListener {
                val isVisible = binding.expensesSummary.isShown
                if (isVisible) {
                    binding.expensesSummary.hide()
                } else {
                    binding.expensesSummary.show()
                }
                true
            }

        }
    }


    private fun setupRecyclerView() {
        binding.rvExpenses.adapter = adapter
    }

    private fun setupClicks() {


        // Date Header Clicks (Assuming IDs inside included layout)
        binding.dateHeader.btnPrevDate.setOnClickListener {
            viewModel.onEvent(ExpenseListUiEvent.OnPrevDate)
        }
        binding.dateHeader.btnNextDate.setOnClickListener {
            viewModel.onEvent(ExpenseListUiEvent.OnNextDate)
        }
        binding.dateHeader.tvSelectedDate.setOnClickListener {
            openDatePicker { date ->
                viewModel.onEvent(ExpenseListUiEvent.OnDateSelected(date))
            }

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
                binding.tvAvgExpenses.text = "Rs. ${state.displayAvgExpense.toLong().toPrice()}"
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