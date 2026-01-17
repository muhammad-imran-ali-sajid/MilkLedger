package com.miassolutions.milkledger.features.purchase.ui.supplierhistory


import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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

    // Current Data hold karne k liye
    private var currentPdfModel: PdfReportModel? = null

//    private val createPdfLauncher = registerForActivityResult(
//        ActivityResultContracts.CreateDocument("application/pdf")
//    ) { uri ->
//        uri?.let {
//            currentPdfModel?.let { model ->
//                lifecycleScope.launch {
//                    pdfGenerator.generatePdf(it, model) // 🔥 Call Engine
//                    showSnackbar("Pdf saved")
//                }
//            }
//        }
//    }

    override fun onPdfUriCreated(uri: Uri) {
        super.onPdfUriCreated(uri)
        val model = currentPdfModel ?: return
        lifecycleScope.launch {
            pdfGenerator.generatePdf(uri, model)
            showSnackbar("Pdf Saved")
        }
    }

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

        generatePdfReport()
    }

    private fun generatePdfReport() {
        binding.btnPdf.setOnClickListener {
            val state = viewModel.uiState.value

            // 1. Prepare Data
            currentPdfModel = PdfMapper.mapSupplierHistoryToPdf(
                supplierName = state.supplierName,
                dateRange = state.dateRangeText,
                list = state.transactions,
                initialBalance = 0L
            )

            // 2. Open File Picker
            createPdfLauncher.launch(currentPdfModel?.fileName ?: "report.pdf")
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