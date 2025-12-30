package com.miassolutions.milkledger.presentation.customerandsales.sales.saleslist

import android.view.Menu
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.extensions.collectEffect
import com.miassolutions.milkledger.core.extensions.collectFlow
import com.miassolutions.milkledger.core.extensions.toDisplayFormat
import com.miassolutions.milkledger.core.extensions.toPriceStr
import com.miassolutions.milkledger.core.extensions.toRoundedStr
import com.miassolutions.milkledger.core.pdf.salereport.PdfSalesSummary
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.showExpenseDatePicker
import com.miassolutions.milkledger.databinding.FragmentSalesBinding
import com.miassolutions.milkledger.databinding.LayoutSalesSummaryBinding
import com.miassolutions.milkledger.domain.model.Sale
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SalesFragment : BaseFragment<FragmentSalesBinding>(FragmentSalesBinding::inflate) {
    private lateinit var adapter: SalesEntryAdapter
    private val viewModel by viewModels<SalesViewModel>()

    override fun setupViews() {

        setupSalesRV()

    }

    override fun getMenuResId(): Int {
        return R.menu.menu_sales
    }


    override fun onMenuCreated(menu: Menu) {

        val pdfMenuItem = menu.findItem(R.id.action_sale_pdf)

        pdfMenuItem?.setOnMenuItemClickListener {
            if (!isPremiumEnabled) {
                showSnackbar("Premium feature")
                return@setOnMenuItemClickListener true
            }

            showDialog("Generate PDF?", "Do you want to create PDF?") {
                generateReport()
            }

            true
        }


    }


    private fun showSummary(
        milkAmount: Double,
        deduction: Double,
        totalNetMilk: Double,
        totalAmount: Double,
        receivedAmount: Double,
        avgRate: Double
    ) {


        binding.apply {
            cardSalesSummary.setTitle("Summary")

            val summaryBinding by lazy {
                LayoutSalesSummaryBinding.inflate(layoutInflater)
            }

            cardSalesSummary.setContent(summaryBinding.root)
            cardSalesSummary.collapse()


            summaryBinding.apply {
                tvTotalMilk.text = "${milkAmount.toRoundedStr()} L"
                tvDeduction.text = "${deduction.toRoundedStr()} L"
                tvTotalNetMilk.text = "${totalNetMilk.toRoundedStr()} L"
                tvTotalAmount.text = "Rs. ${totalAmount.toPriceStr()}"
                tvReceivedAmount.text = "Rs. ${receivedAmount.toPriceStr()}"
                tvAvgPrice.text = "Rs. ${avgRate.toRoundedStr()}"
            }

        }
    }

    override fun setupListeners() {

        binding.dateHeader.tvSelectedDate.setOnClickListener {
            val isAdmin = SharedPrefsHelper.isAdmin(requireContext())

            showExpenseDatePicker(
                isAuthorized = isAdmin,
                initialDate = viewModel.uiState.value.currentDate,
                onPicked = { date ->
                    viewModel.onEvent(SalesUiEvent.SelectDate(date))
                }
            )
        }

        binding.dateHeader.btnPrevDate.setOnClickListener {
            viewModel.onEvent(SalesUiEvent.PreviousDate)
        }

        binding.dateHeader.btnNextDate.setOnClickListener {
            viewModel.onEvent(SalesUiEvent.NextDate)
        }

        binding.fabAddSale.setOnClickListener {
            findNavController()
                .navigate(SalesFragmentDirections.actionSalesFragmentToSaleAddFragment())
        }
    }


    private fun generateReport() {
//        val state = viewModel.uiState.value
//        val filteredList: List<SaleUi> = state.salesUi
//
//
//
//        val recordList = filteredList.toSaleRecordList()
//
//        if (recordList.isEmpty()) {
//
//            showSnackbar("The record is empty. PDF can't be generated.")
//            return
//        }
//
//        val pdfSummary = pdfSummary(
//            totalQty = state.totalMilk,
//            totalDeduction = state.totalDeduction,
//            totalAmount = state.grandSaleTotalForDate,
//            totalPaid = state.totalPaid,
//            balanceDue = state.totalBalance
//
//        )
//
//        val data = SalesReportPdf(
//            footerNote = "Developed by: miassolutions contact no: 03127430906",
//            date = state.currentDate.toDisplayFormat(),
//            recordList = recordList,
//            salesSummary = pdfSummary
//        )
//
//
//        TodaySalesPdf.generateAndSharePdf(
//            context = requireContext(),
//            data = data,
//            baseName = "Customer",
//            showLogo = false,
//        )
//
//        showToast("Generating pdf report...")
    }

    private fun pdfSummary(
        totalQty: Double,
        totalDeduction: Double,
        totalAmount: Double,
        totalPaid: Double,
        balanceDue: Double
    ): PdfSalesSummary {
        return PdfSalesSummary(
            totalQty = totalQty.toRoundedStr(),
            totalDeduction = totalDeduction.toRoundedStr(),
            totalAmount = totalAmount.toPriceStr(),
            totalPaid = totalPaid.toPriceStr(),
            balanceDue = balanceDue.toPriceStr()
        )
    }

    private fun setupSalesRV() {

        adapter = SalesEntryAdapter(
            onEditClick = {},
            onCustomerClick = { id, name ->
                viewModel.onEvent(
                    SalesUiEvent.OpenCustomerLedger(id, name)
                )
            },
            onDeleteClick = { saleId ->
                showDialog(
                    title = "WARNING!!",
                    message = "This will delete the sale record",
                    onAction = {
                        viewModel.onEvent(SalesUiEvent.DeleteSale(saleId))
                    }
                )
            },
            onBalanceClick = ::showCustomerBalanceHistory
        )

        binding.rvSales.adapter = adapter
    }


//    private fun deleteSale(saleId: String) {
//        showDialog(
//            title = "WARNING!!",
//            message = "This will delete the sale record",
//            onAction = { viewModel.deleteSale(saleId) }
//        )
//
//    }

    private fun showCustomerBalanceHistory(id: String, name: String) {
        if (!isPremiumEnabled) {
            showSnackbar("Premium Feature")
            return
        }
        val btmSheet = CustomerBalanceHistoryBottomSheet.newInstance(id, name)
        btmSheet.show(childFragmentManager, null)
    }

    private fun navToDetail(id: String, name: String) {


        val isAdmin = SharedPrefsHelper.isAdmin(requireContext())
        if (!isAdmin) {
            showSnackbar("Only ADMIN is allowed")
            return
        }

        if (!isPremiumEnabled) {
            showSnackbar("Premium Feature")
            return
        }


        findNavController().navigate(
            SalesFragmentDirections.actionSalesFragmentToCustomerDetailFragment(
                id,
                name
            )
        )
    }


    override fun setupObservers() {

        collectFlow(viewModel.uiState) { state ->

            binding.progressBar.visibility =
                if (state.isLoading) View.VISIBLE else View.GONE

            adapter.submitList(state.sales)

            val isEmpty = state.sales.isEmpty()
            binding.emptyLayout.emptyStateLayout.visibility =
                if (isEmpty) View.VISIBLE else View.GONE
            binding.rvSales.visibility =
                if (isEmpty) View.GONE else View.VISIBLE

            binding.dateHeader.tvSelectedDate.text =
                state.currentDate.toDisplayFormat()

            showSummary(
                milkAmount = state.totalMilk,
                deduction = state.totalDeduction,
                totalNetMilk = state.totalNetMilk,
                totalAmount = state.totalAmount,
                receivedAmount = state.receivedAmount,
                avgRate = state.avgRatePerLiter
            )
        }

        collectEffect(viewModel.uiEffect) { effect ->
            handleEffect(effect)
        }
    }

    private fun handleEffect(effect: SalesUiEffect) {
        when (effect) {

            is SalesUiEffect.NavigateToCustomerLedger -> {
                findNavController().navigate(
                    SalesFragmentDirections
                        .actionSalesFragmentToCustomerDetailFragment(
                            effect.customerId,
                            effect.name
                        )
                )
            }

            is SalesUiEffect.ShowMessage -> {
                showSnackbar(effect.message)
            }

//            is SalesUiEffect.GeneratePdf -> {
//                generatePdf(effect.data)
//            }
        }
    }


    private fun showEditSaleBottomSheet(sale: Sale) {

        SalesEditBottomSheet(
            entry = sale,
            onSave = { salesEntity ->

//                viewModel.updateSaleManually(salesEntity)

            }
        ).show(parentFragmentManager, "SaleEditBottomSheet")
    }


}