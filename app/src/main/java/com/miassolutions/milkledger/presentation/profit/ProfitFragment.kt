package com.miassolutions.milkledger.presentation.profit

import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.databinding.FragmentProfitBinding
import com.miassolutions.milkledger.databinding.LayoutSummaryProfitBinding
import com.miassolutions.milkledger.presentation.datefilter.DateFilterCallback
import com.miassolutions.milkledger.presentation.datefilter.DateFilterController
import com.miassolutions.milkledger.presentation.datefilter.DatePeriod
import dagger.hilt.android.AndroidEntryPoint


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
                viewModel.deleteProfit(profit.id)
                d.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun showSummary(
        businessProfit: Double,
        netProfitAfterPersonal: Double,
        receivedProfit: Double,
        remainingProfit: Double,

        ) {


        binding.apply {
            cardProfitSummary.setTitle("Summary")

            val summaryBinding by lazy {
                LayoutSummaryProfitBinding.inflate(layoutInflater)
            }

            cardProfitSummary.setContent(summaryBinding.root)
            cardProfitSummary.collapse()


            summaryBinding.apply {
                tvBusinessProfit.text = businessProfit.toPriceStr()
                tvNetProfitAfterPersonal.text = netProfitAfterPersonal.toPriceStr()
                tvTotalReceivedProfit.text = receivedProfit.toPriceStr()
                tvRemainingProfit.text = remainingProfit.toPriceStr()

            }

        }
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


            showSummary(
                businessProfit = state.grossProfit,
                netProfitAfterPersonal = state.netProfit,
                receivedProfit = state.totalReceived,
                remainingProfit = state.remainingProfit
            )



            adapter.submitList(state.filteredList)

            val isEmpty = state.filteredList.isEmpty()

            binding.emptyLayout.emptyTitle.text = "No record yet"
            binding.emptyLayout.emptySubtitle.text = "Tap the + button to add your first record"

            binding.emptyLayout.emptyStateLayout.visibility =
                if (isEmpty) View.VISIBLE else View.GONE
            binding.rvProfit.visibility = if (isEmpty) View.GONE else View.VISIBLE


            // 2. Update the UI text label with the current state value
            binding.dateFilterLayout.tvSelectedDate.text = state.periodLabel
        }
        binding.rvProfit.adapter = adapter
    }


    override fun onPeriodChanged(period: DatePeriod) {
        viewModel.loadData(period)
    }


}