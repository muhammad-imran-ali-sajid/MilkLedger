package com.miassolutions.milkledger.features.purchase.ui.supplierhistory


import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentSupplierHistoryBinding
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SupplierHistoryFragment : BaseFragment<FragmentSupplierHistoryBinding>(
    FragmentSupplierHistoryBinding::inflate
) {

    private val viewModel: SupplierHistoryViewModel by viewModels()
    private val args: SupplierHistoryFragmentArgs by navArgs()

    private val adapter by lazy { SupplierHistoryAdapter() }

    override fun setupViews() {
        super.setupViews()

        // Initial Title (until state loads)
        // Note: Title/Header ka logic layout par depend krta hai, agar header card hai to wahan set kren.

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
            // 1. Adapter List
            adapter.submitList(state.transactions)

            // 2. Empty State
            val isEmpty = !state.isLoading && state.transactions.isEmpty()
            binding.tvEmptyState.isVisible = isEmpty
            binding.rvSupplierHistory.isVisible = !isEmpty

            // 3. Update Bottom Sheet Summary

        }

        collectEffect(viewModel.uiEffect) { effect ->
            when (effect) {
                SupplierHistoryUiEffect.NavigateBack -> findNavController().navigateUp()
            }
        }
    }
}