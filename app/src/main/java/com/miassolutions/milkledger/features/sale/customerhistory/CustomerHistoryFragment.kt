package com.miassolutions.milkledger.features.sale.customerhistory

import android.net.Uri
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
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
import com.miassolutions.milkledger.databinding.FragmentCustomerHistoryBinding
import com.miassolutions.milkledger.features.sale.model.SaleSummary
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
            showSnackbar(
                message = "Pdf Saved",
                actionText = "OPEN",
                duration = Snackbar.LENGTH_LONG
            ) {
                openPdf(uri)
            }
        }
    }
    
    
    private val adapter by lazy { CustomerHistoryAdapter() }
    
    private fun setupMenu() {
        setupMenuWithCustomView(R.menu.menu_customer_history) { menu ->
            val item = menu.findItem(R.id.action_pdf) ?: return@setupMenuWithCustomView
            item.setOnMenuItemClickListener {
                generatePdfReport()
                true
            }
            
            // 🔥 2. NAYA: Search View Logic
            val searchItem = menu.findItem(R.id.action_search)
            val searchView = searchItem?.actionView as? androidx.appcompat.widget.SearchView
            
            searchView?.apply {
                queryHint = "Search Amount, Vol..." // Chota hint rakhein
                
                // Jab user type karega toh ViewModel ko event bhejen
                setOnQueryTextListener(object :
                    androidx.appcompat.widget.SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        // Jab keyboard ka "Search/Enter" button press ho
                        return false
                    }
                    
                    override fun onQueryTextChange(newText: String?): Boolean {
                        // Jab bhi koi lafz type ho ya mitaaya jaye
                        viewModel.onEvent(
                            CustomerHistoryUiEvent.OnSearchQueryChanged(
                                newText ?: ""
                            )
                        )
                        return true
                    }
                })
            }
        }
    }
    
    private fun generatePdfReport() {
        val state = viewModel.currentState
        
        currentPdfModel = PdfMapper.mapCustomerHistoryToPdf(
            customerName = state.customerName,
            dateRang = state.dateRangeText,
            list = state.displayedTransactions,
            initialBalance = 0L
        )
        
        createPdfLauncher.launch(currentPdfModel?.fileName ?: "report.pdf")
    }
    
    override fun setupViews() {
        super.setupViews()
        
        setupMenu()
        
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
        adapter.submitList(state.displayedTransactions)
        
        val isEmpty = !state.isLoading && state.displayedTransactions.isEmpty()
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
