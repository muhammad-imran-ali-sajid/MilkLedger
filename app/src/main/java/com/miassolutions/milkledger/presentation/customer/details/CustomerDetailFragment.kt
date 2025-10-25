package com.miassolutions.milkledger.presentation.customer.details

import android.util.Log
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.miassolutions.milkledger.core.filterdata.CustomDateRangeBottomSheet
import com.miassolutions.milkledger.core.pdf.PdfReceiptData
import com.miassolutions.milkledger.core.pdf.PdfViewGenerator
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.ui.extensions.formatDateRange
import com.miassolutions.milkledger.core.ui.extensions.formattedDate
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.databinding.FragmentCustomerDetailBinding
import com.miassolutions.milkledger.presentation.supplier.supplierdetail.toRecordList
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
            "Generate receipt for range\n${formatDateRange(start, end)}?"
        ) {
            // Only call generateReport AFTER confirmation
            generateReport()
        }
    }


    private fun generateReport() {
        val state = viewModel.uiState.value
        val filteredList = state.filteredList

        // --- Determine the actual dates used for the current filter ---
        val startDate = state.selectedStartDate ?: LocalDate.now().minusYears(1)
        val endDate = state.selectedEndDate ?: LocalDate.now()

        // Format the dates (assuming formattedDate() is an extension on LocalDate)
        val fromDate = startDate.formattedDate()
        val toDate = endDate.formattedDate()

        val dateRange = "$fromDate - $toDate"
        Log.d("SupplierDetailFragment", "Report Date Range: $dateRange")

        val recordList = filteredList.toRecordList()
        val totalAmount = recordList.sumOf { it.amount }
        val totalPaid = recordList.sumOf { it.paid }
        val totalBalance = recordList.sumOf { it.balance }

        val data = PdfReceiptData(

            dateRange = dateRange,
            partyName = args.customerName,
            recordList = recordList,
            totalAmount = totalAmount.toRoundedStr(),
            totalPaid = totalPaid.toRoundedStr(),
            totalBalance = totalBalance.toRoundedStr(),
            footerNote = "Receipt generated on : ${LocalDate.now().formattedDate()}"

        )

        PdfViewGenerator.generateAndSharePdf(
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
                formatDateRange(start, end)
            }

            else -> "All Records"
        }
    }

//    private fun setupDateRangeToggle() = with(binding) {
//        toggleGroupFilter.addOnButtonCheckedListener { _, checkedId, isChecked ->
//            if (!isChecked) return@addOnButtonCheckedListener
//
//            val rangeType = when (checkedId) {
//                R.id.btnDaily -> DateRangeType.TODAY
//                R.id.btnWeekly -> DateRangeType.WEEK
//                R.id.btnMonthly -> DateRangeType.MONTH
//                else -> DateRangeType.ALL
//            }
//
//            val (start, end) = DateRangeHelper.getRange(rangeType)
//            updateDateLabel(rangeType, start, end)
//
//        }
//
//        datePickerActions.tvSelectedDate.setOnClickListener {
//            when (toggleGroupFilter.checkedButtonId) {
//                R.id.btnDaily -> pickSingleDate { date ->
//                    updateDateLabel(DateRangeType.TODAY, date, date)
//                    viewModel.setCustomDateRange(date, date)
//                }
//
//                R.id.btnWeekly -> pickWeek { start, end ->
//                    updateDateLabel(DateRangeType.WEEK, start, end)
//                    viewModel.setCustomDateRange(start, end)
//                }
//
//                R.id.btnMonthly -> pickMonth { start, end, label ->
//                    datePickerActions.tvSelectedDate.text = label
//                    viewModel.setCustomDateRange(start, end)
//                }
//
//                else -> toggleGroupFilter.check(R.id.btnDaily)
//            }
//        }
//
//        // Default selection on screen load
//        toggleGroupFilter.check(R.id.btnDaily)
//        val today = LocalDate.now()
//        updateDateLabel(DateRangeType.TODAY, today, today)
//        viewModel.setCustomDateRange(today, today)
//    }

//    private fun updateDateLabel(rangeType: DateRangeType, start: LocalDate?, end: LocalDate?) {
//        binding.datePickerActions.tvSelectedDate.text = when (rangeType) {
//            DateRangeType.TODAY -> start?.toString().orEmpty()
//            DateRangeType.WEEK -> if (start != null && end != null) {
//                formatDateRange(start, end)
//            } else ""
//            DateRangeType.MONTH -> start?.format(DateTimeFormatter.ofPattern("MMMM yyyy")).orEmpty()
//            else -> "All Records"
//        }
//    }

//    override fun setupListeners() = with(binding.datePickerActions) {
//
//    }


}
