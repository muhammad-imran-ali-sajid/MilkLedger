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
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toMillis
import com.miassolutions.milkledger.utils.extensions.toPrice
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment :
    BaseFragment<FragmentDashboardBinding>(FragmentDashboardBinding::inflate) {

    private val viewModel: DashboardViewModel by viewModels()
    private val statsAdapter = DashboardStatsAdapter()

    override fun setupViews() {
        super.setupViews()

        // RecyclerView
        binding.rvMilkStats.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = statsAdapter
            setHasFixedSize(true)
        }

        val currentState = viewModel.uiState.value
        binding.dateFilterView.restoreFilterState(
            mode = currentState.filterMode,
            date = currentState.selectedDate
        )

        // Ab Setup call karein
        // Chunki humne uper state restore kr di hai, ab ye 'Default Today' emit nahi karega,
        // balki 'Saved Date' emit karega.
        binding.dateFilterView.setup(childFragmentManager) { start, end, label ->

            val anchorDate = binding.dateFilterView.selectedDate
            val currentMode = binding.dateFilterView.currentMode

            viewModel.onEvent(
                DashboardUiEvent.OnDateFilterChanged(
                    startDate = start.toLocalDate(),
                    endDate = end.toLocalDate(),
                    selectedSingleDate = anchorDate,
                    mode = currentMode
                )
            )
        }
    }

    override fun setupListeners() = with(binding) {
        super.setupListeners()

        btnCashFlow.setOnClickListener { viewModel.onEvent(DashboardUiEvent.OnCashFlowClicked) }
        btnNote.setOnClickListener { viewModel.onEvent(DashboardUiEvent.OnNotesClicked) }
        btnPurchase.setOnClickListener { viewModel.onEvent(DashboardUiEvent.OnPurchaseClicked) }
        btnSale.setOnClickListener { viewModel.onEvent(DashboardUiEvent.OnSaleClicked) }
        btnExpense.setOnClickListener { viewModel.onEvent(DashboardUiEvent.OnExpenseClicked) }
        btnWallet.setOnClickListener { viewModel.onEvent(DashboardUiEvent.OnWalletClicked) }

        setupToolbarMenu()
    }

    private fun setupToolbarMenu() {
        setupMenuWithCustomView(R.menu.menu_dashboard) { menu ->


            val accountItem =
                menu.findItem(R.id.accountListFragment) ?: return@setupMenuWithCustomView
            accountItem.setOnMenuItemClickListener {
                findNavController().navigate(R.id.accountListFragment)
                true
            }
        }
    }


    override fun setupObservers() {
        super.setupObservers()

        // --- STATE ---
        collectFlow(viewModel.uiState) { state ->
            binding.apply {

                if (dateFilterView.selectedDate != state.selectedDate || dateFilterView.currentMode != state.filterMode) {

                    dateFilterView.restoreFilterState(
                        mode = state.filterMode,
                        date = state.selectedDate,
                        // Agar custom label state me save nahi kia to empty chor den,
                        // ya state me add kr len
                        customLabel = ""
                    )
                }

                // Header Cards
                tvTotalPurchases.text = state.totalPurchases.toPrice()
                tvTotalSales.text = state.totalSales.toPrice()
                tvTotalExpense.text = state.totalExpenses.toPrice()
                tvNetProfit.text = state.grossProfit.toPrice()

                val profitColor =
                    if (state.grossProfit >= 0) R.color.md_theme_primary else R.color.md_theme_error
                tvNetProfit.setTextColor(
                    ContextCompat.getColor(requireContext(), profitColor)
                )

                // RecyclerView Stats
                statsAdapter.submitList(
                    buildStatsList(state)
                )


            }
        }

        // --- EFFECTS (Navigation) ---
        collectEffect(viewModel.uiEffect) { effect ->
            when (effect) {

                is DashboardUiEffect.NavigateToCashFlow -> {
                    findNavController().navigate(
                        DashboardFragmentDirections
                            .actionDashboardFragmentToCashflowFragment(effect.date.toMillis())
                    )
                }

                is DashboardUiEffect.NavigateToPurchase -> {
                    findNavController().navigate(
                        DashboardFragmentDirections
                            .actionDashboardFragmentToPurchaseListFragment(effect.date.toMillis())
                    )
                }

                is DashboardUiEffect.NavigateToSale -> {
                    findNavController().navigate(
                        DashboardFragmentDirections
                            .actionDashboardFragmentToMilkSaleListFragment(effect.date.toMillis())
                    )
                }

                is DashboardUiEffect.NavigateToExpense -> {
                    findNavController().navigate(
                        DashboardFragmentDirections
                            .actionDashboardFragmentToExpenseFragment(effect.date.toMillis())
                    )
                }

                is DashboardUiEffect.NavigateToWallet -> {
                    findNavController().navigate(
                        DashboardFragmentDirections
                            .actionDashboardFragmentToOwnerDashboardFragment(effect.date.toMillis())
                    )
                }

                DashboardUiEffect.NavigateToNotes -> {
                    findNavController().navigate(
                        DashboardFragmentDirections.actionDashboardFragmentToNotesListFragment()
                    )
                }
            }
        }
    }

    // --- Helpers ---

    private fun buildStatsList(state: DashboardUiState): List<DashboardStat> {
        return listOf(
            DashboardStat("Purchases", "${state.milkPurchasedQty.format(0)} L"),
            DashboardStat("Sales", "${state.milkSoldQty.format(0)} L"),
            DashboardStat(
                "Qty Diff",
                "${state.qtyDiff.format(0)} L",
                getDiffColor(state.qtyDiff)
            ),
            DashboardStat("Avg Sell Price", state.avgSalePrice.format(2)),
            DashboardStat("Avg Buy Price", state.avgPurchasePrice.format(2)),
            DashboardStat(
                "Price Diff",
                state.avgPriceDiff.format(2),
                getDiffColor(state.avgPriceDiff)
            ),
            DashboardStat(
                "Avg Fat",
                "${state.avgFat.format(2)} (${state.qualityVolume.format(1)})"
            ),
            DashboardStat("Avg LR", "${state.avgLr.format(2)} (${state.qualityVolume.format(1)})"),
            DashboardStat(
                "Total TS",
                "${state.totalTs.format(2)} (${state.qualityVolume.format(1)})"
            )
        )
    }

    private fun getDiffColor(value: Double): Int {
        return if (value >= 0)
            R.color.md_theme_primary
        else
            R.color.md_theme_error
    }
}
