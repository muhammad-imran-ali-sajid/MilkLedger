package com.miassolutions.milkledger.features.purchase.ui.list

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentMilkPurchaseListBinding
import com.miassolutions.milkledger.features.common.BalanceHistoryBottomSheet
import com.miassolutions.milkledger.features.purchase.ui.list.MilkPurchaseAdapter
import com.miassolutions.milkledger.features.purchase.ui.list.MilkPurchaseListViewModel
import com.miassolutions.milkledger.features.purchase.ui.list.PurchaseListUiEffect
import com.miassolutions.milkledger.features.purchase.ui.list.PurchaseListUiEvent
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.hide
import com.miassolutions.milkledger.utils.extensions.openDatePicker
import com.miassolutions.milkledger.utils.extensions.show
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toMillis
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class MilkPurchaseListFragment : BaseFragment<FragmentMilkPurchaseListBinding>(
    FragmentMilkPurchaseListBinding::inflate
) {

    private val viewModel: MilkPurchaseListViewModel by viewModels()
    private val args: MilkPurchaseListFragmentArgs by navArgs()


    private val adapter by lazy {
        MilkPurchaseAdapter(
            onEditClick = { id ->
                viewModel.onEvent(PurchaseListUiEvent.OnEditClick(id))
            },
            onBalanceHistoryClick = { id, name, dateMillis ->
                viewModel.onEvent(
                    PurchaseListUiEvent.OnBalanceClick(
                        id,
                        name,
                        dateMillis
                    )
                )
            },

            onSupplierHistoryClick = { supplierId, supplierName ->
                viewModel.onEvent(
                    PurchaseListUiEvent.OnSupplierHistoryClick(
                        supplierId,
                        supplierName
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

        viewModel.setDateAndLoad(args.date)

        addPurchaseAction()
    }

    private fun addPurchaseAction() {
        setupMenuWithCustomView(R.menu.menu_purchase_list) { menu ->
            val item = menu.findItem(R.id.actionAdd) ?: return@setupMenuWithCustomView
            val btn = item.actionView
                ?.findViewById<MaterialButton>(R.id.btnAddPurchase)
                ?: return@setupMenuWithCustomView

            btn.setOnClickListener {
                viewModel.onEvent(PurchaseListUiEvent.OnAddPurchaseClick)
            }

            val summaryItem =
                menu.findItem(R.id.actionShowSummary) ?: return@setupMenuWithCustomView
            summaryItem.setOnMenuItemClickListener {
                val isVisible = binding.summaryView.isShown
                if (isVisible) {
                    binding.summaryView.hide()
                } else {
                    binding.summaryView.show()
                }
                true
            }

        }
    }


    override fun setupListeners() {
        super.setupListeners()

        binding.dateHeader.btnPrevDate.setOnClickListener {
            val prevDate = viewModel.uiState.value.date.minusDays(1)
            viewModel.onEvent(PurchaseListUiEvent.OnDateSelected(prevDate))
        }

        binding.dateHeader.btnNextDate.setOnClickListener {
            val current = viewModel.uiState.value.date
            val today = LocalDate.now()
            val nextDate = current.plusDays(1)

            if (nextDate.isAfter(today)) {
                showSnackbar("Future date allowed is not allowed")
            } else {
                viewModel.onEvent(PurchaseListUiEvent.OnDateSelected(nextDate))
            }

        }

        binding.dateHeader.tvSelectedDate.setOnClickListener {
            viewModel.onEvent(PurchaseListUiEvent.OnDateClick)
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
            binding.dateHeader.tvSelectedDate.text = state.date.toCompleteDateFormat()


            // 4. Summary Card (Assuming CollapsibleCardView layout logic)
            binding.summaryView.bindPurchase(
                dateRange = state.date.toCompleteDateFormat(), // e.g. "1 Jan - 31 Jan"

                totalVol = state.summary.totalVolume,
                totalAmount = state.summary.totalAmount,

                avgFat = state.summary.avgFat,
                avgLr = state.summary.avgLr,
                avgRate = state.summary.avgRate/100.0,
                avgTs = state.summary.totalTs,

                totalPaid = state.summary.totalPaid
            )

        }

        collectEffect(viewModel.uiEffect) { effect ->
            when (effect) {
                is PurchaseListUiEffect.ShowSnackbar -> showSnackbar(effect.message)

                PurchaseListUiEffect.NavigateToAddPurchase -> {
                    findNavController().navigate(
                        MilkPurchaseListFragmentDirections.actionPurchaseListFragmentToPurchaseFormFragment(
                            null,
                            viewModel.uiState.value.date.toMillis()
                        )
                    )
                }

                is PurchaseListUiEffect.NavigateToEditPurchase -> {
                    findNavController().navigate(
                        MilkPurchaseListFragmentDirections.actionPurchaseListFragmentToPurchaseFormFragment(
                            effect.id,
                            viewModel.uiState.value.date.toMillis()
                        )
                    )
                }


                PurchaseListUiEffect.OpenDatePicker -> openDatePicker { date ->
                    viewModel.onEvent(PurchaseListUiEvent.OnDateSelected(date))
                }

                is PurchaseListUiEffect.OpenBalanceHistorySheet -> {
                    BalanceHistoryBottomSheet.newInstance(
                        accountId = effect.id,
                        accountName = effect.name,
                        dateLimit = effect.dateMillis // 🔥 Us item ki date
                    ).show(childFragmentManager, "History")

                }

                is PurchaseListUiEffect.OpenSupplierHistory -> {
                    val action =
                        MilkPurchaseListFragmentDirections.actionPurchaseListFragmentToSupplierDetailFragment(
                            effect.supplierName,
                            effect.supplierId
                        )
                    findNavController().navigate(action)
                }
            }
        }
    }


}
