package com.miassolutions.milkledger.features.owner.dasboard


import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentOwnerDashboardBinding
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.setBalanceWithColor
import com.miassolutions.milkledger.utils.extensions.toPrice
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OwnerDashboardFragment : BaseFragment<FragmentOwnerDashboardBinding>(
    FragmentOwnerDashboardBinding::inflate
) {

    private val viewModel: OwnerViewModel by viewModels()
    private val adapter by lazy {
        OwnerTransactionAdapter { item ->
            if (item.isPersonalExpense){
                // navigate to personal expense
            } else {
                val sheet = WithdrawCashBottomSheet.newInstance(
                    availableBalance = viewModel.uiState.value.dashboardData.retainedEarnings,
                    id = item.id,
                    amount = item.amount,
                    dateMillis = item.dateMillis,
                    note = item.note
                )

                sheet.show(childFragmentManager, "EditWithDrawSheet")
            }
        }
    }

    override fun setupViews() {
        super.setupViews()

        // Setup RecyclerView
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@OwnerDashboardFragment.adapter
        }

        // Setup Date Filter
        binding.dateFilterView.setup(childFragmentManager) { start, end, label ->
            viewModel.onEvent(OwnerUiEvent.OnDateFilterChanged(start, end, label))
        }
    }

    override fun setupListeners() {
        super.setupListeners()

        // 1. Withdraw Button
        binding.btnWithdraw.setOnClickListener {
            viewModel.onEvent(OwnerUiEvent.OnWithdrawClicked)
        }

        // 2. Personal Expense Button (Short cut)
//        binding.btnWithdraw.setOnClickListener {
//            viewModel.onEvent(OwnerUiEvent.OnAddExpenseClicked)
//        }
    }

    override fun setupObservers() {
        super.setupObservers()

        // A. Observe State
        collectFlow(viewModel.uiState) { state ->
            val data = state.dashboardData

            // 1. Summary Card Update
            binding.tvTotalProfit.text = data.netProfit.toPrice()
            binding.tvTotalDrawings.text = data.totalDrawings.toPrice()
            binding.tvRetained.setBalanceWithColor(data.retainedEarnings)

            // 2. List Update
            adapter.submitList(data.transactions)

            // 3. Empty State
            val isEmpty = !state.isLoading && data.transactions.isEmpty()
            binding.tvEmpty.isVisible = isEmpty
            binding.rvTransactions.isVisible = !isEmpty
        }

        // B. Observe Effects
        collectEffect(viewModel.uiEffect) { effect ->
            when (effect) {
                is OwnerUiEffect.ShowSnackbar -> showSnackbar(effect.message)

                is OwnerUiEffect.OpenWithdrawSheet -> {
                    // Open Bottom Sheet with Available Balance
                    val sheet = WithdrawCashBottomSheet.newInstance(effect.availableBalance)
                    // Note: Use childFragmentManager because sheet belongs to this fragment
                    sheet.show(childFragmentManager, "WithdrawSheet")
                }

                OwnerUiEffect.CloseWithdrawSheet -> {
                    // Try to find and dismiss if open
                    val sheet =
                        childFragmentManager.findFragmentByTag("WithdrawSheet") as? WithdrawCashBottomSheet
                    sheet?.dismiss()
                }

                OwnerUiEffect.NavigateToAddExpense -> {
                    // Navigate to Expense Form (Assuming Action exists in NavGraph)
                    // findNavController().navigate(OwnerDashboardFragmentDirections.actionOwnerToExpenseForm())
                    // Or explicit ID if action not defined yet:
                    // findNavController().navigate(R.id.expenseFormFragment)
                }
            }
        }
    }
}