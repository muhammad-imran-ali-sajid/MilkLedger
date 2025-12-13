package com.miassolutions.milkledger.presentation.customer.sales

import android.util.Log
import android.view.Menu
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.materialswitch.MaterialSwitch
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.pdf.salereport.PdfSalesSummary
import com.miassolutions.milkledger.core.pdf.salereport.SalesReportPdf
import com.miassolutions.milkledger.core.pdf.salereport.TodaySalesPdf
import com.miassolutions.milkledger.core.prefs.SalesPrefsHelper
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.isToday
import com.miassolutions.milkledger.core.util.showExpenseDatePicker
import com.miassolutions.milkledger.core.util.toDisplayFormat
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import com.miassolutions.milkledger.databinding.FragmentSalesBinding
import com.miassolutions.milkledger.databinding.LayoutSalesSummaryBinding
import com.miassolutions.milkledger.domain.model.Sale
import com.miassolutions.milkledger.presentation.customer.CustomerBalanceHistoryBottomSheet
import com.miassolutions.milkledger.presentation.customer.details.toSaleRecordList
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class SalesFragment : BaseFragment<FragmentSalesBinding>(FragmentSalesBinding::inflate) {

    private lateinit var adapter: SalesEntryAdapter
    private var editModeSwitch: MaterialSwitch? = null
    private val viewModel by viewModels<SalesViewModel>()
    private var isEditable = false

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


//        val editModeItem = menu.findItem(R.id.action_edit_mode)
//        editModeSwitch =
//            editModeItem.actionView?.findViewById(R.id.switch_toolbar_edit_mode)

//        val isAdmin = SharedPrefsHelper.isAdmin(requireContext())
//
//        // --- 1. INITIALIZE SWITCH STATE ---
//        initializeEditModeState(isAdmin, viewModel.uiState.value.currentDate)
//
//        // --- 2. SETUP LISTENER ---
//        editModeSwitch?.setOnCheckedChangeListener { _, isChecked ->
//
//            // Step A: Immediately save the intended status
//            SalesPrefsHelper.setEditModeActive(requireContext(), isChecked)
//
//            if (isChecked) {
//
//                if (!isAdmin) {
//                    // Non-admin trying to enable when disabled for today
//                    editModeSwitch?.isChecked = false
//                    SalesPrefsHelper.setEditModeActive(requireContext(), false)
//                    showSnackbar("As a non-admin, you cannot re-enable edit mode once disabled for today.")
//                    return@setOnCheckedChangeListener
//                }
//
//                // --- ADMIN ALLOWED FREELY ---
//                enableEditMode()
//                SalesPrefsHelper.setEditModeActive(requireContext(), true)
//
//                // Admin ignores lock
//                SalesPrefsHelper.setEditModeLockedForToday(requireContext(), false)
//
//            } else {
//
//                disableEditMode()
//                SalesPrefsHelper.setEditModeActive(requireContext(), false)
//
//                val isToday = viewModel.uiState.value.currentDate.isToday()
//
//                if (!isAdmin && isToday) {
//                    // Apply permanent lock ONLY for non-admin
//                    SalesPrefsHelper.setEditModeLockedForToday(requireContext(), true)
//                    showSnackbar("Edit mode disabled and permanently locked for today.")
//                } else {
//                    showSnackbar("Edit mode disabled")
//                }
//            }
//
//        }
    }

    /**
     * Determines and sets the initial state of the edit mode based on admin status,
     * persistent active state, and permanent daily lock status.
     */

    private fun initializeEditModeState(isAdmin: Boolean, selectedDate: LocalDate) {

        val isToday = selectedDate.isToday()
        val isLocked = SalesPrefsHelper.isEditModeLockedForToday(requireContext())

        val shouldBeActive: Boolean

        if (isAdmin) {
            // --- ADMIN LOGIC ---
            // Admin is NEVER restricted by lock OR date
            shouldBeActive = SalesPrefsHelper.isEditModeActive(requireContext())

        } else {
            // --- NON-ADMIN LOGIC ---
            // Non-admin only editable if: Today + NOT locked
            shouldBeActive = isToday && !isLocked

            // Always store this for non-admin
            SalesPrefsHelper.setEditModeActive(requireContext(), shouldBeActive)
        }

        // Apply to UI
        isEditable = shouldBeActive
        adapter.isEditable = shouldBeActive
        editModeSwitch?.isChecked = shouldBeActive
    }


    private fun enableEditMode() {
        isEditable = true
        adapter.isEditable = true
    }

    private fun disableEditMode() {
        isEditable = false
        adapter.isEditable = false
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
            val currentDate = viewModel.uiState.value.currentDate

            val isAdmin = SharedPrefsHelper.isAdmin(requireContext())
            val isUserAuthorized = isAdmin // Replace with actual auth check

            showExpenseDatePicker(
                isAuthorized = isUserAuthorized,
                initialDate = currentDate,
                onPicked = { selectedDate: LocalDate ->

                    viewModel.onEvent(SalesUiEvent.SelectDate(selectedDate))
                }
            )

        }

        binding.fabAddSale.setOnClickListener {
            findNavController().navigate(SalesFragmentDirections.actionSalesFragmentToSaleAddFragment())
        }


        binding.dateHeader.btnPrevDate.setOnClickListener {
            viewModel.goToPreviousDate()
        }

        binding.dateHeader.btnNextDate.setOnClickListener {
            viewModel.goToNextDate()
        }


    }


    private fun generateReport() {
        val state = viewModel.uiState.value
        val filteredList: List<Sale> = state.salesForDate


        val recordList = filteredList.toSaleRecordList()

        if (recordList.isEmpty()) {

            showSnackbar("The record is empty. PDF can't be generated.")
            return
        }

        val pdfSummary = pdfSummary(
            totalQty = state.totalMilk,
            totalDeduction = state.totalDeduction,
            totalAmount = state.grandSaleTotalForDate,
            totalPaid = state.totalPaid,
            balanceDue = state.totalBalance

        )

        val data = SalesReportPdf(
            footerNote = "Receipt generated on : ${LocalDate.now().toDisplayFormat()}",
            date = state.currentDate.toDisplayFormat(),
            recordList = recordList,
            salesSummary = pdfSummary
        )


        TodaySalesPdf.generateAndSharePdf(
            context = requireContext(),
            data = data,
            baseName = "Customer",
            showLogo = false,
        )

        showToast("Generating pdf report...")
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
            ::showEditSaleBottomSheet,
            ::navToDetail,
            ::deleteSale,
            ::showCustomerBalanceHistory
        )
        binding.rvSales.adapter = adapter

    }

    private fun deleteSale(saleId: String) {
        showDialog(
            title = "WARNING!!",
            message = "This will delete the sale record",
            onAction = { viewModel.deleteSale(saleId) }
        )

    }

    private fun showCustomerBalanceHistory(id: String, name: String) {
        if (!isPremiumEnabled){
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

        if (!isPremiumEnabled){
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
        viewModel.uiState.collectState { state ->

            binding.progressBar.visibility =
                if (state.isLoading) View.VISIBLE else View.GONE

            adapter.submitList(state.salesUi)

            val isEmpty = state.salesForDate.isEmpty()
            binding.emptyLayout.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvSales.visibility = if (isEmpty) View.GONE else View.VISIBLE

            binding.apply {
                dateHeader.tvSelectedDate.text = state.currentDate.toDisplayFormat()

                val isAdmin = SharedPrefsHelper.isAdmin(requireContext())
                initializeEditModeState(isAdmin, state.currentDate)


                showSummary(
                    milkAmount = state.totalMilk,
                    deduction = state.totalDeduction,
                    totalNetMilk = state.totalNetMilk,
                    totalAmount = state.grandSaleTotalForDate,
                    receivedAmount = state.receivedAmount,
                    avgRate = state.avgRatePerLiter
                )

            }
        }
    }

    private fun showEditSaleBottomSheet(sale: Sale) {
//        if (!isEditable) {
//            showSnackbar("Enable from the top menu switch")
//            return
//        }
        SalesEditBottomSheet(
            entry = sale,
            onSave = { salesEntity ->

                viewModel.updateSaleManually(salesEntity)

            }
        ).show(parentFragmentManager, "SaleEditBottomSheet")
    }


}