package com.miassolutions.milkledger.features.purchase.list

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentMilkPurchaseListBinding
import com.miassolutions.milkledger.features.purchase.ui.list.MilkPurchaseAdapter
import com.miassolutions.milkledger.features.purchase.ui.list.MilkPurchaseListViewModel
import com.miassolutions.milkledger.features.purchase.ui.list.PurchaseListUiEffect
import com.miassolutions.milkledger.features.purchase.ui.list.PurchaseListUiEvent
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.ZoneId

@AndroidEntryPoint
class MilkPurchaseListFragment : BaseFragment<FragmentMilkPurchaseListBinding>(
    FragmentMilkPurchaseListBinding::inflate
) {

    private val viewModel: MilkPurchaseListViewModel by viewModels()

    private val adapter by lazy {
        MilkPurchaseAdapter(
            onEditClick = { id ->
                showSnackbar(id)
                viewModel.onEvent(PurchaseListUiEvent.OnEditClick(id)) },
            onHistoryClick = { id, name ->
                viewModel.onEvent(
                    PurchaseListUiEvent.OnSupplierHistoryClick(
                        id,
                        name
                    )
                )
            }
        )
    }

    override fun setupViews() {
        super.setupViews()

        binding.rvPurchases.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MilkPurchaseListFragment.adapter
        }
    }

    override fun setupListeners() {
        super.setupListeners()

        binding.dateHeader.btnPrevDate.setOnClickListener {
            val prevDate = viewModel.uiState.value.date.minusDays(1)
            viewModel.onEvent(PurchaseListUiEvent.OnDateSelected(prevDate))
        }

        binding.dateHeader.btnNextDate.setOnClickListener {
            val nextDate = viewModel.uiState.value.date.plusDays(1)
            viewModel.onEvent(PurchaseListUiEvent.OnDateSelected(nextDate))
        }

        binding.dateHeader.tvSelectedDate.setOnClickListener {
            viewModel.onEvent(PurchaseListUiEvent.OnDateClick)
        }

        binding.btnAddPurchase.setOnClickListener {
            viewModel.onEvent(PurchaseListUiEvent.OnAddPurchaseClick)
        }
    }

    override fun setupObservers() {
        super.setupObservers()

        collectFlow(viewModel.uiState) { state ->
            // 1. Update List
            adapter.submitList(state.purchases)

            // 2. Loading & Empty State
            binding.progressBar.isVisible = state.isLoading
            binding.emptyLayout.root.isVisible = !state.isLoading && state.purchases.isEmpty()

            // 3. Date Header
            binding.dateHeader.tvSelectedDate.text = state.date.toDisplayDate()

            // 4. Summary Card (Assuming CollapsibleCardView layout logic)
            binding.cardSummary.apply {
//                // FindViews using IDs from your summary layout
//                findViewById<TextView>(R.id.tvTotalMilk)?.text = state.totalVolume.toMilkAmount()
//                findViewById<TextView>(R.id.tvTotalAmount)?.text = state.totalPrice.toPrice() // Label: "Total Price"
//                findViewById<TextView>(R.id.tvTotalPaid)?.text = state.totalPaid.toPrice()    // Label: "Paid"

                // Balance ya Net Payable
                // findViewById<TextView>(R.id.tvNetPayable)?.text = (state.totalPrice - state.totalPaid).toPrice()
            }
        }

        collectEffect(viewModel.uiEffect) { effect ->
            when (effect) {
                is PurchaseListUiEffect.ShowSnackbar -> showSnackbar(effect.message)

                PurchaseListUiEffect.NavigateToAddPurchase -> {
                    findNavController().navigate(
                        MilkPurchaseListFragmentDirections.actionPurchaseListFragmentToPurchaseFormFragment(
                            null
                        )
                    )
                }

                is PurchaseListUiEffect.NavigateToEditPurchase -> {
                    findNavController().navigate(
                        MilkPurchaseListFragmentDirections.actionPurchaseListFragmentToPurchaseFormFragment(
                            effect.id
                        )
                    )
                }

                is PurchaseListUiEffect.NavigateToSupplierHistory -> {
                    // Navigate to history (Reuse CustomerHistory logic but for Supplier)
                    // You might need a separate Fragment or reuse existing with a Type flag
                }

                PurchaseListUiEffect.OpenDatePicker -> openDatePicker()
            }
        }
    }

    private fun openDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val date = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
            viewModel.onEvent(PurchaseListUiEvent.OnDateSelected(date))
        }
        picker.show(childFragmentManager, "PurchaseListDate")
    }
}