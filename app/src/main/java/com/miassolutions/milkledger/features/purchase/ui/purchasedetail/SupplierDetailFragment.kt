package com.miassolutions.milkledger.features.purchase.ui.purchasedetail


import android.view.Menu
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.ui.CustomDateRangeBottomSheet
import com.miassolutions.milkledger.databinding.FragmentSupplierDetailBinding
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.formatPeriodLabel
import com.miassolutions.milkledger.utils.helper.numberFormat
import com.miassolutions.milkledger.utils.helper.textColor
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
            showDialog(
                title = "Confirmation",
                message = "Do you want to generate pdf report?",
                onAction = {  }
            )
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
        collectFlow(viewModel.uiState) { state ->
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
//        binding.apply {
//            supplierSummary.setTitle("Summary")
//            supplierSummary.collapse()
//
//            val summaryBinding =
//                LayoutSupplierDetailSummaryBinding.inflate(layoutInflater, root, false)
//            supplierSummary.setContent(summaryBinding.root)
//
//            summaryBinding.apply {
//                // Use the passed dateRange parameter
//                tvDateRangeValue.text = dateRange
//                tvTotalMilk.text = milkAmount
//                tvTotalTs.text = totalTS
//                tvTotalAmount.text = totalPrice
//                tvAvgFat.text = avgFat
//                tvAvgLr.text = avgLr
//                tvPaymentValue.text = payment
//                tvBalanceValue.text = balance
//                tvBalanceValue.setTextColor(balanceColor)
//            }
//        }
    }
}