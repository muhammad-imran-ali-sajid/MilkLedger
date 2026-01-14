package com.miassolutions.milkledger.features.dashboard

import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentDashboardBinding
import com.miassolutions.milkledger.features.dashboard.model.DashboardStat
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.format
import com.miassolutions.milkledger.utils.extensions.toPrice
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment :
    BaseFragment<FragmentDashboardBinding>(FragmentDashboardBinding::inflate) {

    private val viewModel: DashboardViewModel by viewModels()

    // Adapter define karen
    private val statsAdapter = DashboardStatsAdapter()

    override fun setupViews() {
        super.setupViews()

        // 1. Setup RecyclerView
        binding.rvMilkStats.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = statsAdapter
            setHasFixedSize(true)
        }

        // 2. Date Filter Setup
        binding.dateFilterView.setup(childFragmentManager) { start, end, label ->
            viewModel.onEvent(DashboardUiEvent.OnDateFilterChanged(start, end))
        }
    }

    override fun setupListeners() {
        super.setupListeners()
        binding.btnCashFlow.setOnClickListener {
            viewModel.onEvent(DashboardUiEvent.OnCashFlowClicked)
        }
        binding.btnNote.setOnClickListener {
            viewModel.onEvent(DashboardUiEvent.OnNotesClicked)
        }
    }

    override fun setupObservers() {
        super.setupObservers()

        // --- STATE OBSERVER ---
        collectFlow(viewModel.uiState) { state ->
            binding.apply {

                // 1. Financial Cards (Static Header Views)
                tvTotalPurchases.text = state.totalPurchases.toPrice()
                tvTotalSales.text = state.totalSales.toPrice()
                tvTotalExpense.text = state.totalExpenses.toPrice()
                tvNetProfit.text = state.grossProfit.toPrice()

                // Profit Color Helper (Header k liye)
                val profitColor =
                    if (state.grossProfit >= 0) R.color.milk_profit_color else R.color.red
                tvNetProfit.setTextColor(ContextCompat.getColor(requireContext(), profitColor))


                // 2. Milk Overview List (Dynamic RecyclerView Data)
                // Yahan hum State ko convert kr k List banayen gy adapter k liye
                val statsList = listOf(
                    DashboardStat("Purchases", "${state.milkPurchasedQty.format(1)} L"),

                    DashboardStat("Sales", "${state.milkSoldQty.format(1)} L"),

                    DashboardStat(
                        "Qty Diff",
                        "${state.qtyDiff.format(1)} L",
                        getDiffColor(state.qtyDiff)
                    ),

                    DashboardStat("Avg Buy Price", state.avgPurchasePrice.format(1)),

                    DashboardStat("Avg Sell Price", state.avgSalePrice.format(1)),

                    DashboardStat(
                        "Price Diff",
                        state.avgPriceDiff.format(1),
                        getDiffColor(state.avgPriceDiff)
                    ),

                    DashboardStat("Avg Fat", state.avgFat.format(2)),

                    DashboardStat("Avg LR", state.avgLr.format(2))
                )

                // Adapter ko list update karen
                statsAdapter.submitList(statsList)
            }
        }

        // --- EFFECT OBSERVER ---
        collectEffect(viewModel.uiEffect) { effect ->
            when (effect) {
                DashboardUiEffect.NavigateToCashFlow -> {
                    findNavController().navigate(DashboardFragmentDirections.actionDashboardFragmentToCashflowFragment())
                }

                DashboardUiEffect.NavigateToNotes -> {
                    // findNavController().navigate(R.id.action_dashboard_to_notes)
                }
            }
        }
    }

    // Helper to return Color Resource ID
    private fun getDiffColor(value: Double): Int {
        return if (value >= 0) R.color.milk_primary else R.color.milk_error // Green or Red
    }
}