package com.miassolutions.milkledger.features.dashboard

import android.graphics.Color
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.pdf.PdfGenerator
import com.miassolutions.milkledger.core.pdf.PdfMapper
import com.miassolutions.milkledger.core.pdf.PdfReportModel
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentDashboardBinding
import com.miassolutions.milkledger.features.dashboard.model.DashboardStat
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.format
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toMillis
import com.miassolutions.milkledger.utils.extensions.toPrice
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DashboardFragment :
    BaseFragment<FragmentDashboardBinding>(FragmentDashboardBinding::inflate) {

    // 1. Inject PdfGenerator & Hold Model State
    @Inject
    lateinit var pdfGenerator: PdfGenerator
    private var currentPdfModel: PdfReportModel? = null

    private val viewModel: DashboardViewModel by viewModels()
    private val statsAdapter = DashboardStatsAdapter()

    // 2. Override onPdfUriCreated (Just like CustomerHistoryFragment)
    override fun onPdfUriCreated(uri: Uri) {
        val model = currentPdfModel ?: return
        lifecycleScope.launch {
            try {
                pdfGenerator.generatePdf(uri, model)
                showSnackbar(
                    message = "PDF Saved Successfully",
                    actionText = "OPEN",
                    duration = Snackbar.LENGTH_LONG
                ) {
                    openPdf(uri)
                }
            } catch (e: Exception) {
                showSnackbar("Failed to generate PDF: ${e.message}")
            }
        }
    }

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




        binding.tvBackupInfo.text = if (viewModel.uiState.value.isBackupOld) {
            "❌ Backup is older than 48 hours"
        } else {

            "✅ Data is secured with drive backup"
        }


    }

    override fun setupListeners() = with(binding) {
        super.setupListeners()

        btnPurchase.setOnClickListener { viewModel.onEvent(DashboardUiEvent.OnPurchaseClicked) }
        btnSale.setOnClickListener { viewModel.onEvent(DashboardUiEvent.OnSaleClicked) }
        btnExpense.setOnClickListener { viewModel.onEvent(DashboardUiEvent.OnExpenseClicked) }


        setupToolbarMenu()
    }

    private fun setupToolbarMenu() {
        setupMenuWithCustomView(R.menu.menu_dashboard) { menu ->

            // 3. Handle PDF Menu Click
            val pdfItem = menu.findItem(R.id.action_dashboard_pdf)
            pdfItem?.setOnMenuItemClickListener {
                generatePdfReport()
                true
            }
        }
    }

    // 4. Generate Logic
    private fun generatePdfReport() {
        val state = viewModel.uiState.value

        // Get the readable date range string from your DateFilterView
        val dateRangeStr = state.selectedDate.toCompleteDateFormat()

        // Map the state to the PDF Model
        currentPdfModel = PdfMapper.mapDashboardToPdf(
            state = state,
            dateRange = dateRangeStr
        )

        // Launch the file creator (BaseFragment logic)
        // Use a timestamp to prevent overwriting if they generate multiple
        val fileName = "Dashboard_Report_${System.currentTimeMillis()}.pdf"
        createPdfLauncher.launch(fileName)
    }

    override fun setupObservers() {
        super.setupObservers()

        collectFlow(viewModel.uiState) { state ->
            binding.apply {
                if (dateFilterView.selectedDate != state.selectedDate || dateFilterView.currentMode != state.filterMode) {
                    dateFilterView.restoreFilterState(
                        mode = state.filterMode,
                        date = state.selectedDate,
                        customLabel = ""
                    )
                }
                tvTotalPurchases.text = state.totalPurchases.toPrice()
                tvTotalSales.text = state.totalSales.toPrice()
                tvTotalExpense.text = state.totalExpenses.toPrice()
                tvNetProfit.text = state.grossProfit.toPrice()
                tvPersonalExpense.text = state.totalPersonalExpense.toPrice()
                tvRemainingBalance.text = state.remainingBalance.toPrice()

                val profitColor =
                    if (state.grossProfit >= 0) R.color.md_theme_primary else R.color.md_theme_error
                tvNetProfit.setTextColor(ContextCompat.getColor(requireContext(), profitColor))

                statsAdapter.submitList(buildStatsList(state))
            }
        }

        collectEffect(viewModel.uiEffect) { effect ->
            when (effect) {

                is DashboardUiEffect.NavigateToPurchase -> {
                    findNavController().navigate(
                        DashboardFragmentDirections.actionDashboardFragmentToPurchaseListFragment(
                            effect.date.toMillis()
                        )
                    )
                }

                is DashboardUiEffect.NavigateToSale -> {
                    findNavController().navigate(
                        DashboardFragmentDirections.actionDashboardFragmentToMilkSaleListFragment(
                            effect.date.toMillis()
                        )
                    )
                }

                is DashboardUiEffect.NavigateToExpense -> {
                    findNavController().navigate(
                        DashboardFragmentDirections.actionDashboardFragmentToExpenseFragment(effect.date.toMillis())
                    )
                }

            }
        }
    }

    private fun buildStatsList(state: DashboardUiState): List<DashboardStat> {
        return listOf(
            DashboardStat("Purchases", "${state.milkPurchasedQty.format(0)} L"),
            DashboardStat("Sales", "${state.milkSoldQty.format(0)} L"),
            DashboardStat("Qty Diff", "${state.qtyDiff.format(0)} L", getDiffColor(state.qtyDiff)),
            DashboardStat("Avg S.P.", state.avgSalePrice.format(2)),
            DashboardStat("Avg P.P.", state.avgPurchasePrice.format(2)),
            DashboardStat(
                "Price Margin",
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
        return if (value >= 0) R.color.md_theme_primary else R.color.md_theme_error
    }
}