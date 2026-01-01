package com.miassolutions.milkledger.presentation.customerandsales.sales.customersalesdetail

import android.view.Menu
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.ui.CustomDateRangeBottomSheet
import com.miassolutions.milkledger.databinding.FragmentCustomerDetailBinding
import com.miassolutions.milkledger.databinding.LayoutCustomerDetailSummaryBinding
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.formatPeriodLabel
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

    override fun getMenuResId(): Int? {
        return R.menu.menu_customer_detail
    }

    override fun onMenuCreated(menu: Menu) {
        // Use the safe call operator or an 'if' check to prevent NPE
        menu.findItem(R.id.action_profit_gen_pdf)?.let { pdfMenuItem ->
            pdfMenuItem.setOnMenuItemClickListener {
                showDialog(
                    title = "Confirmation",
                    message = "Do you want to generate pdf report?",
                    onAction = {  }
                )
                true
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = CustomerDetailListAdapter()
        binding.rvCustomerDetail.adapter = adapter
        binding.rvCustomerDetail.setHasFixedSize(true)
    }

    override fun setupObservers() {
        collectFlow(viewModel.uiState) { state ->

            // 1. Get the current formatted date range from the state
            val currentSelectedDateRange =
                getFormattedDateRange(state.selectedStartDate, state.selectedEndDate)

            // 2. Update the UI text label with the current state value
            binding.tvSelectedDate.text = currentSelectedDateRange

            with(state.summary) {
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

        viewModel.setCustomDateRange(start, end)

    }





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


//        binding.customerSummary.setTitle("Customer Summary")
//
//
//        binding.apply {
//            val summaryBinding =
//                LayoutCustomerDetailSummaryBinding.inflate(layoutInflater, root, false)
//            customerSummary.setContent(summaryBinding.root)
//            customerSummary.collapse()
//            summaryBinding.apply {
//
//                tvDateRangeValue.text = summaryPeriod
//                tvTotalMilkValue.text = "${totalVolume} L"
//                tvDeductionValue.text = "${totalDeduction} L"
//                tvTotalPriceValue.text = "Rs. ${totalPrice}"
//                tvPaymentValue.text = "Rs. ${totalPaid}"
//                tvBalanceValue.text = balance
//            }
//        }

    }


}
