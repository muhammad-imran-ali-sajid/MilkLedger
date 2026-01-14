package com.miassolutions.milkledger.features.sale.salelist


import android.util.Log
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentMilkSaleListBinding
import com.miassolutions.milkledger.features.common.BalanceHistoryBottomSheet
import com.miassolutions.milkledger.features.purchase.model.SaleSummary
import com.miassolutions.milkledger.features.purchase.ui.list.PurchaseListUiEvent
import com.miassolutions.milkledger.features.sale.salelist.MilkSaleListUiEvent.*
import com.miassolutions.milkledger.features.sale.ui.list.MilkSaleListViewModel
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.showDeleteActionDialog
import com.miassolutions.milkledger.utils.extensions.openDatePicker
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MilkSaleListFragment :
    BaseFragment<FragmentMilkSaleListBinding>(FragmentMilkSaleListBinding::inflate) {

    private val viewModel: MilkSaleListViewModel by viewModels()

    // Adapter Initialization
    private val adapter = MilkSaleListAdapter(
        onEditClick = { item ->
            Log.d("MilkSaleListFragment", item)
            viewModel.onEvent(OnEditSaleClicked(item))
        },
        onDeleteClick = {
            showDeleteActionDialog {
                viewModel.onEvent(OnDeleteClicked(it))
            }
        },
        onDetailClick = { item ->
            viewModel.onEvent(
                OnCustomerDetailClicked(
                    item.customerId,
                    item.customerName
                )
            )
        },
        onBalanceClick = { id, name ->
            // Navigate to Bottom Sheet

            viewModel.onEvent(
                OnBalanceClick(
                    id,
                    name
                )
            )
//
//            val action =
//                MilkSaleListFragmentDirections.actionMilkSaleListFragmentToCustomerBalanceHistoryBottomSheet(
//                    customerId = id,
//                    customerName = name
//                )
//            findNavController().navigate(action)

        }

    )

    override fun setupViews() {
        super.setupViews()

        // 1. RecyclerView Setup
        binding.rvSales.adapter = adapter

        // 2. Click Listeners
        setupClicks()
    }

    private fun setupClicks() {
        // FAB (Add Sale)
        binding.fabAddSale.setOnClickListener {
            viewModel.onEvent(OnAddSaleClicked)
        }

        // Date Header Actions
        binding.dateHeader.btnPrevDate.setOnClickListener {
            viewModel.onEvent(OnPrevDate)
        }
        binding.dateHeader.btnNextDate.setOnClickListener {
            viewModel.onEvent(OnNextDate)
        }
        binding.dateHeader.tvSelectedDate.setOnClickListener {
            // Date Picker Event
            viewModel.onEvent(OnDateClick) // ViewModel effect trigger karega
        }
    }

    override fun setupObservers() = with(binding) {
        super.setupObservers()


        collectFlow(viewModel.uiState) { state ->
            // A. Update List
            adapter.submitList(state.sales)

            // B. Handle Empty State & Loading
            binding.progressBar.isVisible = state.isLoading
            binding.emptyLayout.emptyStateLayout.isVisible =
                !state.isLoading && state.sales.isEmpty()
            binding.rvSales.isVisible = !state.isLoading && state.sales.isNotEmpty()

            // C. Update Date Text
            // Note: Ensure IDs match your included layout
            val tvDate = binding.dateHeader.tvSelectedDate
            tvDate.text = state.date.toCompleteDateFormat()

            // D. Update Summary Card (Custom View Handling)
            updateSummary(state.summary, state.date.toCompleteDateFormat())
        }


        collectEffect(viewModel.uiEffect) { effect ->
            when (effect) {
                is MilkSaleListUiEffect.NavigateToEditSale -> {
                    val action = MilkSaleListFragmentDirections
                        .actionMilkSaleListFragmentToSaleAddFragment(
                            saleId = effect.saleId, // ID pass karein
                            saleDate = -1L // Edit me date DB se load hogi, is liye dummy value pass kr den
                        )
                    findNavController().navigate(action)
                }

                // ... baqi existing cases ...
                is MilkSaleListUiEffect.NavigateToAddSale -> {
                    val action = MilkSaleListFragmentDirections
                        .actionMilkSaleListFragmentToSaleAddFragment(
                            null, // ID null matlab New Sale
                            effect.dateMillis
                        )
                    findNavController().navigate(action)
                }

                is MilkSaleListUiEffect.NavigateToCustomerLedger -> {
                    val action =
                        MilkSaleListFragmentDirections.actionMilkSaleListFragmentToCustomerHistoryFragment(
                            customerId = effect.customerId,
                            customerName = effect.customerName
                        )

                    findNavController().navigate(action)
                }

                is MilkSaleListUiEffect.ShowSnackbar -> {
                    // Show snackbar logic
                }

                MilkSaleListUiEffect.OnDateClick -> {
                    // Open Date Picker Extension
                    openDatePicker(
                        initialDate = viewModel.currentState.date,
                    ) { selectedDate ->
                        viewModel.onEvent(OnDateSelected(selectedDate))
                    }
                }

                is MilkSaleListUiEffect.OpenBalanceHistorySheet -> {
                    val sheet = BalanceHistoryBottomSheet.newInstance(
                        accountId = effect.id,  // Customer ki ID
                        accountName = effect.name
                    )
                    sheet.show(childFragmentManager, "History")
                }
            }
        }


    }


    private fun updateSummary(summary: SaleSummary, date: String) = with(binding) {
        summaryView.bindSale(
            dateRange = date,
            grossVol = summary.grossVolume,
            deduction = summary.totalDeduction,
            netVol = summary.netVolume,
            totalAmount = summary.totalAmount,
            avgRate = summary.avgRate,
            totalReceived = summary.totalReceived
        )

    }
}