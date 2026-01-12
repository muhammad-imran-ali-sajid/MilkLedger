package com.miassolutions.milkledger.features.cashflow

import androidx.fragment.app.viewModels
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentCashflowBinding
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.setBalanceWithColor
import com.miassolutions.milkledger.utils.extensions.toPrice
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CashflowFragment : BaseFragment<FragmentCashflowBinding>(FragmentCashflowBinding::inflate) {

    private val viewModel: CashflowViewModel by viewModels()
    // Reuse your existing LedgerAdapter or create a generic one
    private val adapter by lazy { CashflowAdapter() }

    override fun setupViews() {
        super.setupViews()

        binding.rvTransactions.adapter = adapter

        // Setup Date Filter
        binding.dateFilterView.setup(childFragmentManager) { start, end, label ->
            // Jab date change ho, ViewModel ko batayen
            viewModel.onEvent(CashflowUiEvent.OnDateFilterChanged(start, end))
        }
    }

    override fun setupObservers() {
        super.setupObservers()

        collectFlow(viewModel.uiState) { state ->

            // 1. Update Cards
            binding.tvTotalIn.text = state.totalIn.toPrice()
            binding.tvTotalOut.text = state.totalOut.toPrice()
            binding.tvNetCash.setBalanceWithColor(state.netCash) // Green/Red logic

            // 2. Update List
            adapter.submitList(state.transactions)
        }
    }
}