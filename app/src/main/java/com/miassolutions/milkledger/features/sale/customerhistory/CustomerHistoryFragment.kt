package com.miassolutions.milkledger.features.sale.customerhistory

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentCustomerHistoryBinding
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomerHistoryFragment : BaseFragment<FragmentCustomerHistoryBinding>(
    FragmentCustomerHistoryBinding::inflate
) {

    private val viewModel: CustomerHistoryViewModel by viewModels()

    // Navigation Arguments (Safe Args)
    // Make sure nav_graph me arguments define hon: customerId (String), customerName (String)
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

        // 1. Initial Setup (Arguments se Name set karein)
        binding.tvSelectedDate.text = "${args.customerName} - History"

        // 2. RecyclerView Setup
        binding.rvCustomerDetail.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CustomerHistoryFragment.adapter
        }
    }

    override fun setupListeners() {
        super.setupListeners()

        // Agar Header par click kr k Date Filter kholna ho (Future Implementation)
        binding.dateHeader.setOnClickListener {
            viewModel.onEvent(CustomerHistoryUiEvent.OnDateFilterClick)
        }
    }

    override fun setupObservers() {
        super.setupObservers()

        // --- A. Observe UI State ---
        collectFlow(viewModel.uiState) { state ->
            renderState(state)
        }

        // --- B. Observe Side Effects ---
        collectEffect(viewModel.uiEffect) { effect ->
            handleEffect(effect)
        }
    }

    private fun renderState(state: CustomerHistoryUiState) = with(binding) {
        // 1. Loading
        // (Agar ProgressBar XML me hota to yahan visible/gone krte)

        // 2. List Update
        adapter.submitList(state.transactions)

        // 3. Empty State Logic
        val isEmpty = !state.isLoading && state.transactions.isEmpty()
        tvEmptyState.isVisible = isEmpty
        rvCustomerDetail.isVisible = !isEmpty

        // 4. Header Text Update
        // Agar aap chahein k Date Range bhi dikhayen (e.g. "Ali - All Time")
        tvSelectedDate.text = "${state.customerName} (${state.dateRangeText})"

        // 5. Update Bottom Sheet Summary
        // Note: Assuming CollapsibleCardView k andar ye methods/views accessible hain.
        // Agar custom methods nahi banaye, to aapko IDs access krni hongi.

        // Example logic (Apne Custom View k mutabiq adjust karein):
        customerSummary.apply {
            // Agar aap ne Custom View me methods banaye hue hen:
            // setMilk(state.summaryMilk.toMilkAmount())
            // setReceived(state.summaryReceived.toPrice())
            // setBalance(state.currentTotalBalance)

            // Ya agar direct child access krna hai:
            // findViewById<TextView>(R.id.tvTotalMilk).text = state.summaryMilk.toMilkAmount()
            // findViewById<TextView>(R.id.tvTotalReceived).text = state.summaryReceived.toPrice()
            // findViewById<TextView>(R.id.tvCurrentBalance).setBalanceWithColor(state.currentTotalBalance)
        }
    }

    private fun handleEffect(effect: CustomerHistoryUiEffect) {
        when (effect) {
            is CustomerHistoryUiEffect.NavigateToEditSale -> {
                // Sale Form par navigate karein (ID k sath)
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

            CustomerHistoryUiEffect.ShowDateRangePicker -> {
                // Future Implementation
            }
        }
    }
}