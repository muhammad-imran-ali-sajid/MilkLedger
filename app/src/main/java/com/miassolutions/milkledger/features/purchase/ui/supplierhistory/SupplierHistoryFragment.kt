package com.miassolutions.milkledger.features.purchase.ui.supplierhistory


import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentSupplierHistoryBinding
import com.miassolutions.milkledger.features.purchase.model.PurchaseSummary
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SupplierHistoryFragment : BaseFragment<FragmentSupplierHistoryBinding>(
    FragmentSupplierHistoryBinding::inflate
) {

    private val viewModel: SupplierHistoryViewModel by viewModels()

    private val adapter by lazy { SupplierHistoryAdapter() }

    override fun setupViews() {
        super.setupViews()

        binding.rvSupplierHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SupplierHistoryFragment.adapter
        }

        // Setup Date Filter
        binding.dateFilterView.setup(childFragmentManager) { start, end, label ->
            viewModel.onEvent(SupplierHistoryUiEvent.OnDateFilterChanged(start, end, label))
        }
    }

    override fun setupObservers() {
        super.setupObservers()

        collectFlow(viewModel.uiState) { state ->

            adapter.submitList(state.transactions)

            val isEmpty = !state.isLoading && state.transactions.isEmpty()
            binding.tvEmptyState.isVisible = isEmpty
            binding.rvSupplierHistory.isVisible = !isEmpty

            updateSummary(state.dateRangeText, state.summary)

        }

        collectEffect(viewModel.uiEffect) { effect ->
            when (effect) {
                SupplierHistoryUiEffect.NavigateBack -> findNavController().navigateUp()
            }
        }
    }

    private fun updateSummary(period: String, summary: PurchaseSummary) = with(binding) {
        summary.apply {
            summaryView.bindPurchase(
                dateRange = period,
                totalVol = totalVolume,
                totalAmount = totalAmount,
                avgFat = avgFat,
                avgLr = avgLr,
                avgTs = totalTs,
                avgRate = avgRate,
                totalPaid = totalPaid,
                hideForSupplier = true

            )
        }
    }
}