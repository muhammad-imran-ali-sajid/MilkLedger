package com.miassolutions.milkledger.presentation.supplier.supplierdetail

import android.graphics.Color
import android.util.Log
import android.view.Menu
import androidx.core.graphics.toColorInt
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.miassolutions.datesort.DateFilterBottomSheet
import com.miassolutions.datesort.OnDateRangeSelected
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.pdf.HybridPdfGenerator
import com.miassolutions.milkledger.core.pdf.PdfReceiptData
import com.miassolutions.milkledger.core.pdf.PdfViewGenerator
import com.miassolutions.milkledger.core.pdf.RecordItem
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.ui.datesort.DateRangeType
import com.miassolutions.milkledger.core.ui.extensions.formatDateRange
import com.miassolutions.milkledger.core.ui.extensions.formattedDate
import com.miassolutions.milkledger.core.ui.sort.FilterSharedViewModel
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.databinding.FragmentSupplierDetailBinding
import com.miassolutions.milkledger.databinding.LayoutPurchaseSummaryBinding
import com.miassolutions.milkledger.databinding.LayoutSupplierDetailSummaryBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@AndroidEntryPoint
class SupplierDetailFragment :
    BaseFragment<FragmentSupplierDetailBinding>(FragmentSupplierDetailBinding::inflate) {

    private val viewModel: SupplierDetailViewModel by viewModels()
    private var selectedDateRange: String = ""
    private val filterViewModel by activityViewModels<FilterSharedViewModel>()
    private val args by navArgs<SupplierDetailFragmentArgs>()
    private lateinit var adapter: SupplierDetailListAdapter
    override fun getMenuResId(): Int {
        return R.menu.menu_supplier_detail
    }

    override fun setupViews() {
        viewModel.onSelectedSupplierId(args.supplierId)
        setupRecyclerView()


    }

    override fun setupListeners() {
        binding.tvSelectedDate.setOnClickListener {


            val sheet = DateFilterBottomSheet(object : OnDateRangeSelected {
                override fun onDateRangeSelected(
                    start: LocalDate,
                    end: LocalDate,
                    type: com.miassolutions.datesort.DateRangeType
                ) {
                    viewModel.setCustomDateRange(start, end)
                }
            })
            sheet.show(parentFragmentManager, "DateFilter")
        }

    }

    override fun onMenuCreated(menu: Menu) {
        val sortMenu = menu.findItem(R.id.menu_sort_item)





        sortMenu.setOnMenuItemClickListener {
            showDialog(
                "Generate Receipt",
                "Are you want to generate receipt for the selected date range"
            ) {
                generateReport()
            }
            true
        }
    }

    private fun generateReport() {
        val filteredList = viewModel.uiState.value.filteredList
        val fromDate = viewModel.uiState.value.selectedStartDate?.formattedDate() ?: ""
        val toDate = viewModel.uiState.value.selectedEndDate?.formattedDate() ?: ""


        val dateRange = "$fromDate - $toDate"
        Log.d("SupplierDetailFragment", "$dateRange")
        if (filteredList.isEmpty()) {
            showToast("No data to generate PDF")
            return
        }

        val recordList = filteredList.toRecordList()
        val totalAmount = recordList.sumOf { it.amount }
        val totalPaid = recordList.sumOf { it.paid }
        val totalBalance = recordList.sumOf { it.balance }

        val data = PdfReceiptData(
            title = args.supplierName,
            dateRange = dateRange,
            partyName = args.supplierName,
            recordList = recordList,
            totalAmount = totalAmount.toRoundedStr(),
            totalPaid = totalPaid.toRoundedStr(),
            totalBalance = totalBalance.toRoundedStr(),
            footerNote = "Receipt generated on : ${LocalDate.now().formattedDate()}"

        )

// 🧾 Create + Share with logo and auto-numbering
        PdfViewGenerator.generateAndSharePdf(
            context = requireContext(),
            data = data,
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

    private fun updateDateLabel(rangeType: DateRangeType, start: LocalDate?, end: LocalDate?) {

        selectedDateRange = when (rangeType) {
            DateRangeType.TODAY -> start?.formattedDate().orEmpty()
            DateRangeType.WEEK -> if (start != null && end != null) {
                formatDateRange(start, end)
            } else ""

            DateRangeType.MONTH -> start?.formattedDate().orEmpty()
            DateRangeType.CUSTOM -> if (start != null && end != null) {
                formatDateRange(start, end)
            } else ""

            else -> "All Records"
        }
    }

    override fun setupObservers() {
        viewModel.uiState.collectState { state ->
            adapter.submitList(state.filteredList)
            binding.tvSelectedDate.text = selectedDateRange

            // Move this here to always update the label when the state changes
            updateDateLabel(state.dateRangeType, state.selectedStartDate, state.selectedEndDate)
            with(state.summary) {
                val color = when {
                    balance < 0 -> Color.RED
                    balance == 0.0 -> "#000000".toColorInt()
                    else -> "#4CAF50".toColorInt() // Material green 500
                }

                val balanceText = when {
                    balance > 0 -> "+${balance.toRoundedStr()}"
                    else -> balance.toRoundedStr()
                }

                showSummary(
                    milkAmount = totalMilk,
                    totalTS = totalTs,
                    totalPrice = totalPrice,
                    payment = paidAmount,
                    balance = balanceText,
                    balanceColor = color
                )
            }

        }

        filterViewModel.filterOptions.collectState { filter ->
            viewModel.onEvent(SupplierUiEvent.ApplyFilter(filter))
        }


    }

    private fun showSummary(

        milkAmount: String,
        totalTS: String,
        totalPrice: String,
        payment: String,
        balance: String,
        balanceColor: Int
    ) {
        binding.apply {
            supplierSummary.setTitle("Summary")
//            supplierSummary.collapse()
            // inflate the layout using viewbinding
            val summaryBinding =
                LayoutSupplierDetailSummaryBinding.inflate(layoutInflater, root, false)
            supplierSummary.setContent(summaryBinding.root)
            // 2. Use the ViewBinding object to set the data efficiently
            summaryBinding.apply {

                tvDateRangeValue.text = selectedDateRange
                tvTotalMilkValue.text = milkAmount
                tvTotalTsValue.text = totalTS
                tvTotalPriceValue.text = totalPrice
                tvPaymentValue.text = payment
                tvBalanceValue.text = balance
                tvBalanceValue.setTextColor(balanceColor)
            }
        }
    }

}