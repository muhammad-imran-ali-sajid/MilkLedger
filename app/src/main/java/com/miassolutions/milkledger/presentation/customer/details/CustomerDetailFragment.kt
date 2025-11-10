package com.miassolutions.milkledger.presentation.customer.details

import android.util.Log
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.miassolutions.milkledger.core.filterdata.CustomDateRangeBottomSheet
import com.miassolutions.milkledger.core.pdf.customerreport.CustomerReportGenerator
import com.miassolutions.milkledger.core.pdf.customerreport.SalesReceiptPdf
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.formatPeriodLabel
import com.miassolutions.milkledger.core.util.toDisplayFormat
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.databinding.FragmentCustomerDetailBinding
import com.miassolutions.milkledger.databinding.LayoutCustomerDetailSummaryBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class CustomerDetailFragment :
    BaseFragment<FragmentCustomerDetailBinding>(FragmentCustomerDetailBinding::inflate) {

    private val viewModel by viewModels<CustomerDetailViewModel>()
    private val args: CustomerDetailFragmentArgs by navArgs()

    private lateinit var adapter: CustomerDetailListAdapter

    override fun setupViews() {
        viewModel.onSelectedCustomerId(args.customerId, args.customerName)
        setupRecyclerView()
        setupCustomRangeCalendar()

    }

    private fun setupRecyclerView() {
        adapter = CustomerDetailListAdapter()
        binding.rvCustomerDetail.adapter = adapter
        binding.rvCustomerDetail.setHasFixedSize(true)
    }

    override fun setupObservers() {
        viewModel.uiState.collectState { state ->

            // 1. Get the current formatted date range from the state
            val currentSelectedDateRange =
                getFormattedDateRange(state.selectedStartDate, state.selectedEndDate)

            // 2. Update the UI text label with the current state value
            binding.tvSelectedDate.text = currentSelectedDateRange

            with(state.summary) {
                Log.d("CustomerDetail", "setupObservers: $totalMilk -$balance")
                customerSummary(
                    summaryPeriod = currentSelectedDateRange,
                    totalVolume = totalMilk,
                    totalDeduction = totalDeduction,
                    totalPrice = totalPrice,
                    totalPaid = paidAmount,
                    balance = balance.toString()
                )
            }


            adapter.submitList(state.filteredList)
        }
    }

    override fun setupListeners() {
        binding.tvSelectedDate.setOnClickListener {
            showDateFilter(isGeneratingReport = false)
        }
    }

    // Modify showDateFilter to accept a flag
    private fun showDateFilter(isGeneratingReport: Boolean = true) {
        val bottomSheet = CustomDateRangeBottomSheet()
        if (isGeneratingReport) {
            showDialog("Select Range", "Set date range for generating report") {
                bottomSheet.show(parentFragmentManager, "CUSTOM_RANGE_TAG")
            }
        } else {
            bottomSheet.show(parentFragmentManager, "CUSTOM_RANGE_ONLY_FILTER_DATA")
        }

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

    private fun handleSelectedDateRange(start: LocalDate, end: LocalDate) {
        // This updates the ViewModel's state, which triggers setupObservers()
        viewModel.setCustomDateRange(start, end)
        // --- NEW: Trigger confirmation after dates are set ---
        // Since this runs after *any* date selection, we now ask for confirmation to generate the report.
        showDialog(
            "Generate Receipt",
            "Generate receipt for range\n${formatPeriodLabel(start, end)}?"
        ) {
            // Only call generateReport AFTER confirmation
            generateReport()
        }
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
        Log.d("SupplierDetailFragment", "Report Date Range: $dateRange")

        val recordList = filteredList.toRecordList()
        val totalAmount = recordList.sumOf { it.amount }
        val totalPaid = recordList.sumOf { it.paid }
        val totalBalance = recordList.sumOf { it.balance }

        val data = SalesReceiptPdf(

            dateRange = dateRange,
            partyName = args.customerName,
            recordList = recordList,
            totalAmount = totalAmount.toRoundedStr(),
            totalPaid = totalPaid.toRoundedStr(),
            totalBalance = totalBalance.toRoundedStr(),
            footerNote = "Receipt generated on : ${LocalDate.now().toDisplayFormat()}"

        )

        CustomerReportGenerator.generateAndSharePdf(
            context = requireContext(),
            data = data,
            baseName = "Customer",
            showLogo = true,
//                logoResId = R.drawable.ic_launcher_foreground
        )

        showToast("Generating pdf report...")
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

    private fun customerSummary(
        summaryPeriod: String,
        totalVolume: String,
        totalDeduction: String,
        totalPrice: String,
        totalPaid: String,
        balance: String
    ) {


        binding.customerSummary.setTitle("Customer Summary")


        binding.apply {
            val summaryBinding =
                LayoutCustomerDetailSummaryBinding.inflate(layoutInflater, root, false)
            customerSummary.setContent(summaryBinding.root)
            customerSummary.collapse()
            summaryBinding.apply {

                tvDateRangeValue.text = summaryPeriod
                tvTotalMilkValue.text = totalVolume
                tvDeductionValue.text = totalDeduction
                tvTotalPriceValue.text = totalPrice
                tvPaymentValue.text = totalPaid
                tvBalanceValue.text = balance
            }
        }

    }


}
