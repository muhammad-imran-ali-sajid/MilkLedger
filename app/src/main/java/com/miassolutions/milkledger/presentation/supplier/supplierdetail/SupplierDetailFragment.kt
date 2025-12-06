package com.miassolutions.milkledger.presentation.supplier.supplierdetail


import android.view.Menu
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.filterdata.CustomDateRangeBottomSheet
import com.miassolutions.milkledger.core.helper.numberFormat
import com.miassolutions.milkledger.core.helper.textColor
import com.miassolutions.milkledger.core.pdf.supplierreport.SupplierReceiptPdf
import com.miassolutions.milkledger.core.pdf.supplierreport.SupplierReportGenerator
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.formatPeriodLabel
import com.miassolutions.milkledger.core.util.toDisplayFormat
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.databinding.FragmentSupplierDetailBinding
import com.miassolutions.milkledger.databinding.LayoutSupplierDetailSummaryBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class SupplierDetailFragment :
    BaseFragment<FragmentSupplierDetailBinding>(FragmentSupplierDetailBinding::inflate) {

    private val viewModel: SupplierDetailViewModel by viewModels()
    private val args by navArgs<SupplierDetailFragmentArgs>()
    private lateinit var adapter: SupplierDetailListAdapter

    override fun getMenuResId(): Int {
        return R.menu.menu_supplier_detail
    }

    override fun setupViews() {
        viewModel.onSelectedSupplierId(args.supplierId)
        setupRecyclerView()
        setupCustomRangeCalendar()
    }

    private fun setupCustomRangeCalendar() {
        // 1. Set up the listener
        setFragmentResultListener(CustomDateRangeBottomSheet.REQUEST_KEY) { requestKey, bundle ->
            if (requestKey == CustomDateRangeBottomSheet.REQUEST_KEY) {
                // 2. Extract the data
                val startDateString = bundle.getString(CustomDateRangeBottomSheet.BUNDLE_START_DATE)
                val endDateString = bundle.getString(CustomDateRangeBottomSheet.BUNDLE_END_DATE)

                if (startDateString != null && endDateString != null) {
                    // 3. Convert the String dates back to LocalDate
                    val startDate = LocalDate.parse(startDateString)
                    val endDate = LocalDate.parse(endDateString)

                    // 4. Use the selected dates -> Call ViewModel to update state
                    handleSelectedDateRange(startDate, endDate)
                }
            }
        }
    }


    override fun setupListeners() {
        binding.tvSelectedDate.setOnClickListener {
            showDateFilter()
        }
    }

    override fun onMenuCreated(menu: Menu) {
        val sortMenu = menu.findItem(R.id.menu_sort_item)

        sortMenu.setOnMenuItemClickListener {
            generateReport()
            true
        }
    }


    private fun showDateFilter() {
        val bottomSheet = CustomDateRangeBottomSheet()
        bottomSheet.show(parentFragmentManager, "CUSTOM_RANGE_ONLY_FILTER_DATA")
    }

    private fun handleSelectedDateRange(start: LocalDate, end: LocalDate) {
        viewModel.setCustomDateRange(start, end)
    }

    private fun generateReport() {
        val state = viewModel.uiState.value
        val filteredList = state.filteredList

        // --- Determine the actual dates used for the current filter ---
        val startDate = state.selectedStartDate ?: LocalDate.now()
        val endDate = state.selectedEndDate ?: LocalDate.now()

        // Format the dates (assuming formattedDate() is an extension on LocalDate)
        val fromDate = startDate.toDisplayFormat()
        val toDate = endDate.toDisplayFormat()

        val dateRange = "$fromDate - $toDate"

        val recordList = filteredList.toRecordList()
        val totalQty = recordList.sumOf { it.quantity }
        val avgTS = recordList.sumOf { it.ts }
        val totalAmount = recordList.sumOf { it.amount }
        val totalPaid = recordList.sumOf { it.paid }
        val totalBalance = recordList.sumOf { it.balance }



        val data = SupplierReceiptPdf(

            dateRange = dateRange,
            supplierName = args.supplierName,
            totalQty = totalQty.toRoundedStr(),
            avgTs = avgTS.toRoundedStr(),
            recordList = filteredList.toRecordList(),
            totalAmount = totalAmount.toRoundedStr(),
            totalPaid = totalPaid.toRoundedStr(),
            totalBalance = totalBalance.toRoundedStr(),
            footerNote = "Receipt generated on : ${LocalDate.now().toDisplayFormat()}"
        )

        SupplierReportGenerator.generateAndSharePdf(
            context = requireContext(),
            data = data,
            baseName = "Supplier",
            showLogo = true,
//                logoResId = R.drawable.ic_launcher_foreground
        )

        showToast("Generating pdf report...")
    }


    private fun setupRecyclerView() {
        adapter = SupplierDetailListAdapter()
        binding.rvSupplierDetails.adapter = adapter
        binding.rvSupplierDetails.setHasFixedSize(true)

    }

    /**
     * Helper to format the date range string based on ViewModel state.
     */
    private fun getFormattedDateRange(start: LocalDate?, end: LocalDate?): String {
        return when {
            start != null && end != null -> {
                formatPeriodLabel(start, end)
            }

            else -> "All Records"
        }
    }

    override fun setupObservers() {
        viewModel.uiState.collectState { state ->
            // 1. Get the current formatted date range from the state
            val currentSelectedDateRange =
                getFormattedDateRange(state.selectedStartDate, state.selectedEndDate)

            // 2. Update the UI text label with the current state value
            binding.tvSelectedDate.text = currentSelectedDateRange

            adapter.submitList(state.filteredList)

            with(state.summary) {
                val color = textColor(balance)
                val balanceText = numberFormat(balance)

                showSummary(
                    milkAmount = totalMilk,
                    totalTS = totalTS,
                    totalPrice = totalPrice,
                    payment = paidAmount,
                    balance = balanceText,
                    balanceColor = color,
                    dateRange = currentSelectedDateRange,
                    avgFat = avgFat,
                    avgLr = avgLr
                )
            }
        }
    }

    private fun showSummary(
        milkAmount: String,
        totalTS: String,
        avgFat: String,
        avgLr: String,
        totalPrice: String,
        payment: String,
        balance: String,
        balanceColor: Int,
        dateRange: String // Added dateRange parameter
    ) {
        binding.apply {
            supplierSummary.setTitle("Summary")
            supplierSummary.collapse()

            val summaryBinding =
                LayoutSupplierDetailSummaryBinding.inflate(layoutInflater, root, false)
            supplierSummary.setContent(summaryBinding.root)

            summaryBinding.apply {
                // Use the passed dateRange parameter
                tvDateRangeValue.text = dateRange
                tvTotalMilk.text = milkAmount
                tvTotalTs.text = totalTS
                tvTotalAmount.text = totalPrice
                tvAvgFat.text = avgFat
                tvAvgLr.text = avgLr
                tvPaymentValue.text = payment
                tvBalanceValue.text = balance
                tvBalanceValue.setTextColor(balanceColor)
            }
        }
    }
}