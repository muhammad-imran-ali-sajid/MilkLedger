package com.miassolutions.milkledger.features.sale.customerhistory

import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentCustomerHistoryBinding
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.setBalanceWithColor
import com.miassolutions.milkledger.utils.extensions.toMilkAmount
import com.miassolutions.milkledger.utils.extensions.toPrice
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomerHistoryFragment : BaseFragment<FragmentCustomerHistoryBinding>(
    FragmentCustomerHistoryBinding::inflate
) {

    private val viewModel: CustomerHistoryViewModel by viewModels()
    private val args: CustomerHistoryFragmentArgs by navArgs()

    private val adapter by lazy {
        CustomerHistoryAdapter(
//            onItemClick = { saleItem ->
//                // Item click par Edit Event fire karein
//                viewModel.onEvent(CustomerHistoryUiEvent.OnTransactionClick(saleItem.id))
//            }
        )
    }

    override fun setupViews() {
        super.setupViews()


        // 2. Setup RecyclerView
        binding.rvCustomerDetail.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CustomerHistoryFragment.adapter
        }

        // ✅ 3. Setup Date Filter View
        // Ye code bilkul same rahega. View khud logic chalaye ga (Arrows/Calendar)
        // aur jab user final karega, ye callback chalega.
        binding.dateFilterView.setup(childFragmentManager) { start, end, label ->

            // ViewModel ko naya range bhejen
            viewModel.onEvent(CustomerHistoryUiEvent.OnDateFilterChanged(start, end, label))
        }
    }

    override fun setupObservers() {
        super.setupObservers()

        // A. UI State
        collectFlow(viewModel.uiState) { state ->
            renderState(state)
        }

        // B. Effects
        collectEffect(viewModel.uiEffect) { effect ->
            handleEffect(effect)
        }
    }

    private fun renderState(state: CustomerHistoryUiState) = with(binding) {
        // 1. List Update
        adapter.submitList(state.transactions)

        // 2. Empty State
        val isEmpty = !state.isLoading && state.transactions.isEmpty()
        tvEmptyState.isVisible = isEmpty
        rvCustomerDetail.isVisible = !isEmpty



        // 4. Update Summary Card
        customerSummary.apply {
            // NOTE: Replace these IDs with actual IDs from your CollapsibleCardView layout
            // Agar aap methods nahi banaye, to findViewById use karein:
            findViewById<TextView>(R.id.tvMilk)?.text = state.summaryMilk.toMilkAmount()
            findViewById<TextView>(R.id.tvPayment)?.text = state.summaryReceived.toPrice()
            findViewById<TextView>(R.id.tvBalance)?.setBalanceWithColor(state.currentTotalBalance)
        }
    }

    private fun handleEffect(effect: CustomerHistoryUiEffect) {
        when (effect) {
            is CustomerHistoryUiEffect.NavigateToEditSale -> {
                // ✅ Navigation to Sale Form
//                val action = CustomerHistoryFragmentDirections
//                    .actionCustomerHistoryFragmentToSaleFormFragment(
//                        saleId = effect.saleId,
//                        saleDate = -1L
//                    )
//                findNavController().navigate(action)
            }

            CustomerHistoryUiEffect.NavigateBack -> {
                findNavController().navigateUp()
            }

            is CustomerHistoryUiEffect.ShowSnackbar -> {
                showSnackbar(effect.message)
            }

            else -> {}
        }
    }
}