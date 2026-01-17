package com.miassolutions.milkledger.features.sale.customerhistory

import android.net.Uri
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.miassolutions.milkledger.core.pdf.PdfGenerator
import com.miassolutions.milkledger.core.pdf.PdfReportModel
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentCustomerHistoryBinding
import com.miassolutions.milkledger.features.purchase.model.SaleSummary
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CustomerHistoryFragment : BaseFragment<FragmentCustomerHistoryBinding>(
    FragmentCustomerHistoryBinding::inflate
) {

    @Inject
    lateinit var pdfGenerator: PdfGenerator
    private var currentPdfModel: PdfReportModel? = null

    private val viewModel: CustomerHistoryViewModel by viewModels()


    override fun onPdfUriCreated(uri: Uri) {
        val model = currentPdfModel ?: return
        lifecycleScope.launch {
            pdfGenerator.generatePdf(uri, model)
            showSnackbar("Pdf Saved")
        }
    }

    private val adapter by lazy { CustomerHistoryAdapter() }

    override fun setupViews() {
        super.setupViews()


        // 2. Setup RecyclerView
        binding.rvCustomerDetail.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CustomerHistoryFragment.adapter
        }

        binding.dateFilterView.setup(childFragmentManager) { start, end, label ->
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

        updateSummary(state.dateRangeText, state.summary)


    }

    fun updateSummary(dateRange: String, summary: SaleSummary) = with(binding) {
        summary.apply {
            summaryView.bindSale(
                dateRange = dateRange,
                grossVol = grossVolume,
                deduction = totalDeduction,
                netVol = netVolume,
                totalAmount = totalAmount,
                avgRate = avgRate,
                totalReceived = totalReceived
            )
        }

    }

    private fun handleEffect(effect: CustomerHistoryUiEffect) {
        when (effect) {


            CustomerHistoryUiEffect.NavigateBack -> {
                findNavController().navigateUp()
            }

            is CustomerHistoryUiEffect.ShowSnackbar -> {
                showSnackbar(effect.message)
            }

        }
    }
}