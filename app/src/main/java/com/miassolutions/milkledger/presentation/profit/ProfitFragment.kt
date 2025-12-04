package com.miassolutions.milkledger.presentation.profit

import android.util.Log
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.miassolutions.milkledger.core.filterdata.CustomDateRangeBottomSheet
import com.miassolutions.milkledger.core.pdf.profitreport.PdfProfitSummary
import com.miassolutions.milkledger.core.pdf.profitreport.ProfitReceiptPdf
import com.miassolutions.milkledger.core.pdf.profitreport.ProfitReportGenerator
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.DatePickerLogic
import com.miassolutions.milkledger.core.util.formatPeriodLabel
import com.miassolutions.milkledger.core.util.toDisplayFormat
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.databinding.FragmentProfitBinding
import com.miassolutions.milkledger.domain.model.Profit
import com.miassolutions.milkledger.presentation.datefilter.DateFilterCallback
import com.miassolutions.milkledger.presentation.datefilter.DateFilterController
import com.miassolutions.milkledger.presentation.datefilter.DatePeriod
import com.miassolutions.milkledger.presentation.stats.ProfitSummary
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate


@AndroidEntryPoint
class ProfitFragment : BaseFragment<FragmentProfitBinding>(FragmentProfitBinding::inflate),
    DateFilterCallback {

    private val viewModel by viewModels<ProfitViewModel>()
    private lateinit var controller: DateFilterController
    private lateinit var adapter: ProfitAdapter


    override fun setupViews() {
        adapter = ProfitAdapter(::editProfitRecord, ::showConfirmDialog)
        binding.rvProfit.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        controller = DateFilterController(
            this,
            binding.dateFilterLayout,
            callback = this
        )
        controller.init()
    }

    private fun showConfirmDialog(profit: ProfitListModel) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Entry")
            .setMessage("Are you sure to delete this entry?")
            .setPositiveButton("Yes") { d, _ ->
                viewModel.deleteProfit(profit)
                d.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun generateReport() {
//        val state = viewModel.uiState.value
//        val filteredList: List<ProfitListModel> = state.filteredList
//
//        val startDate = state.startDate ?: LocalDate.now()
//        val endDate = state.endDate ?: LocalDate.now()
//
//        val fromDate = startDate.toDisplayFormat()
//        val toDate = endDate.toDisplayFormat()
//
//        val dateRange = "$fromDate - $toDate"
//        Log.d("SupplierDetailFragment", "Report Date Range: $dateRange")
//
//        val recordList = filteredList.toProfitRecordList()
//
//        val totalNetProfit = state.netProfit
//        val totalReceived = state.totalReceived
//        val totalBalance = state.remainingProfit
//
//        val data = ProfitReceiptPdf(
//            dateRange = dateRange,
//            profitReceiver = "Shahid Afzaal",
//            recordList = recordList,
//            totalProfit = totalNetProfit.toPriceStr(),
//            totalReceived = totalReceived.toPriceStr(),
//            totalBalance = totalBalance.toPriceStr(),
//            footerNote = "MIAS SOLUTIONS"
//        )
//
//        ProfitReportGenerator.generateAndSharePdf(
//            requireContext(),
//            data,
//            "Profit",
//            false
//        )
//
//        showToast("Generating pdf report...")


    }


    private fun editProfitRecord(profit: ProfitListModel) {

        val sheet = AddEditProfitBottomSheet.newInstance(profit)
        sheet.onSave = { viewModel.saveProfit(it) }

        sheet.show(parentFragmentManager, null)
    }

    override fun setupListeners() {


        binding.fabAddProfit.setOnClickListener {
            val sheet = AddEditProfitBottomSheet()
            sheet.onSave = { profit ->
                viewModel.saveProfit(profit)
            }

            sheet.show(parentFragmentManager, null)

        }

        binding.fabPDF.setOnClickListener {
            generateReport()
        }

    }

    override fun setupObservers() {
        viewModel.uiState.collectState { state ->
            binding.tvBusinessProfit.text = state.netBusinessProfit.toPriceStr()
            binding.tvNetProfitAfterPersonal.text = state.netProfitAfterPersonalExpenses.toPriceStr()
            binding.tvTotalReceivedProfit.text = state.totalReceived.toPriceStr()
            binding.tvRemainingProfit.text = state.remainingProfit.toPriceStr()



            adapter.submitList(state.filteredList)


            // 2. Update the UI text label with the current state value
            binding.dateFilterLayout.tvSelectedDate.text = state.periodLabel
        }
        binding.rvProfit.adapter = adapter
    }


    override fun onPeriodChanged(period: DatePeriod) {
        viewModel.loadData(period)
    }


}