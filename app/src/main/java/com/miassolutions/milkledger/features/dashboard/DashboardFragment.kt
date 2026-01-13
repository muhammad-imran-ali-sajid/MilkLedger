package com.miassolutions.milkledger.features.dashboard

import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentDashboardBinding
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.toLongPaisa
import com.miassolutions.milkledger.utils.extensions.toPrice
import com.miassolutions.milkledger.utils.extensions.toRupeesStr
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment :
    BaseFragment<FragmentDashboardBinding>(FragmentDashboardBinding::inflate) {

    private val viewModel: DashboardViewModel by viewModels()

    override fun setupViews() {
        super.setupViews()

        // 1. Date Filter Setup
        // Ye callback dega jab screen load hogi ya date change hogi
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

        // --- STATE OBSERVER (Update UI) ---
        collectFlow(viewModel.uiState) { state ->
            binding.apply {

                // 1. Financial Cards
                tvTotalPurchases.text = state.totalPurchases.toPrice()
                tvTotalSales.text = state.totalSales.toPrice()
                tvTotalExpense.text = state.totalExpenses.toPrice()

                // Gross Profit
                tvNetProfit.text = state.grossProfit.toPrice()
                // Profit Color (Green if > 0, Red if < 0)
                setProfitColor(tvNetProfit, state.grossProfit)


                // 2. Milk Overview (Quantities)
                tvMilkPurchase.text = "${state.milkPurchasedQty.format(1)} L"
                tvMilkSold.text = "${state.milkSoldQty.format(1)} L"

                tvQtyDiff.text = "${state.qtyDiff.format(1)} L"
                setDiffColor(tvQtyDiff, state.qtyDiff) // Custom Color Logic


                // 3. Averages
                tvAvgPurchasePrice.text = state.avgPurchasePrice.format(1)
                tvAvgSalePrice.text = state.avgSalePrice.format(1)

                tvAvgPriceDiff.text = state.avgPriceDiff.format(1)
                setDiffColor(tvAvgPriceDiff, state.avgPriceDiff)


                // 4. Quality
                tvAvgFat.text = state.avgFat.format(2)
                tvAvgLr.text = state.avgLr.format(2)
            }
        }

        // --- EFFECT OBSERVER (Navigation) ---
        collectEffect(viewModel.uiEffect) { effect ->
            when (effect) {
                DashboardUiEffect.NavigateToCashFlow -> {
                    // Make sure ID matches your nav_graph.xml
                    findNavController().navigate(DashboardFragmentDirections.actionDashboardFragmentToCashflowFragment())
                }

                DashboardUiEffect.NavigateToNotes -> {
                    // Navigate to Notes Fragment
                    // findNavController().navigate(R.id.action_dashboard_to_notes)
                }
            }
        }
    }

    // --- Helpers ---

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

    // Helper to set Text Color (Red/Green) based on value
    private fun setDiffColor(textView: TextView, value: Double) {
        val colorRes = if (value >= 0) R.color.green_700 else R.color.red
        textView.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
    }

    private fun setProfitColor(textView: TextView, value: Long) {
        val colorRes =
            if (value >= 0) R.color.white else R.color.red // Profit card dark green hai, islye white/light-red
        textView.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
    }
}