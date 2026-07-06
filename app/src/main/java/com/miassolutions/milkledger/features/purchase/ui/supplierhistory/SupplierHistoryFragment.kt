package com.miassolutions.milkledger.features.purchase.ui.supplierhistory


import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.pdf.PdfGenerator
import com.miassolutions.milkledger.core.pdf.PdfMapper
import com.miassolutions.milkledger.core.pdf.PdfReportModel
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentSupplierHistoryBinding
import com.miassolutions.milkledger.features.purchase.model.PurchaseSummary
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SupplierHistoryFragment : BaseFragment<FragmentSupplierHistoryBinding>(
    FragmentSupplierHistoryBinding::inflate
) {

    @Inject
    lateinit var pdfGenerator: PdfGenerator
    private var currentPdfModel: PdfReportModel? = null



    override fun onPdfUriCreated(uri: Uri) {
        super.onPdfUriCreated(uri)
        val model = currentPdfModel ?: return
        lifecycleScope.launch {
            pdfGenerator.generatePdf(uri, model)
            showSnackbar(
                message = "Pdf Saved",
                actionText = "OPEN",
                duration = Snackbar.LENGTH_LONG
            ) {
                openPdf(uri)
            }
        }
    }

    private fun setupMenu() {
        setupMenuWithCustomView(R.menu.menu_supplier_history) { menu ->
            val item = menu.findItem(R.id.action_pdf) ?: return@setupMenuWithCustomView



            item.setOnMenuItemClickListener {
                generatePdfReport()
                true
            }
            
            // 🔥 2. NAYA: Search View Logic
            val searchItem = menu.findItem(R.id.action_search)
            val searchView = searchItem?.actionView as? SearchView
            
            searchView?.apply {
                queryHint = "Search Amount, Vol..."
                
                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean = false
                    
                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.onEvent(SupplierHistoryUiEvent.OnSearchQueryChanged(newText ?: ""))
                        return true
                    }
                })
            }
        }
    }
    private val viewModel: SupplierHistoryViewModel by viewModels()

    private val adapter by lazy { SupplierHistoryAdapter() }

    override fun setupViews() {
        super.setupViews()

        setupMenu()

        binding.rvSupplierHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SupplierHistoryFragment.adapter
        }

        // Setup Date Filter
        binding.dateFilterView.setup(childFragmentManager) { start, end, label ->
            viewModel.onEvent(SupplierHistoryUiEvent.OnDateFilterChanged(start, end, label))
        }

    }

    private fun generatePdfReport() {
        val state = viewModel.uiState.value

        // 1. Prepare Data
        currentPdfModel = PdfMapper.mapSupplierHistoryToPdf(
            supplierName = state.supplierName,
            dateRange = state.dateRangeText,
            list = state.displayedTransactions,
            initialBalance = 0L
        )

        // 2. Open File Picker
        createPdfLauncher.launch(currentPdfModel?.fileName ?: "report.pdf")
    }

    override fun setupObservers() {
        super.setupObservers()

        collectFlow(viewModel.uiState) { state ->
            

            adapter.submitListWithSearch(
                list =  state.displayedTransactions,
                query = state.searchQuery
            )
            
            val isEmpty = !state.isLoading && state.displayedTransactions.isEmpty()
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