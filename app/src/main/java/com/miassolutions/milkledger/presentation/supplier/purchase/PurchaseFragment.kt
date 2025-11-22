package com.miassolutions.milkledger.presentation.supplier.purchase

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.core.content.edit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.materialswitch.MaterialSwitch
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.pdf.purchasereport.PurchaseReportPdf
import com.miassolutions.milkledger.core.pdf.purchasereport.PdfPurchaseSummary
import com.miassolutions.milkledger.core.pdf.purchasereport.TodayPurchasePdf
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.isToday
import com.miassolutions.milkledger.core.util.showExpenseDatePicker
import com.miassolutions.milkledger.core.util.toDisplayFormat
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import com.miassolutions.milkledger.databinding.FragmentPurchasesBinding
import com.miassolutions.milkledger.databinding.LayoutPurchaseSummaryBinding
import com.miassolutions.milkledger.presentation.supplier.SupplierBalanceHistoryBottomSheet
import com.miassolutions.milkledger.presentation.supplier.supplierdetail.toPurchaseRecordList
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

@AndroidEntryPoint
class PurchaseFragment :
    BaseFragment<FragmentPurchasesBinding>(FragmentPurchasesBinding::inflate) {

    override fun getMenuResId(): Int = R.menu.purchase_menu

    private var editModeSwitch: MaterialSwitch? = null
    private var biometricRequiredForToday = false
    private var isEditable = true
    var isUserAuthorized = false

    private val viewModel: PurchaseViewModel by viewModels()
    private lateinit var purchaseAdapter: PurchaseAdapter

    override fun setupViews() {
        setToolbarTitle(getString(R.string.purchases))
        setupRecyclerView()

        val role = SharedPrefsHelper.getUserRole(requireContext())

        binding.tvSelectedDate.setOnClickListener {
            val currentDate = viewModel.uiState.value.currentDate
            // Assume you fetch the authorization status dynamically
            isUserAuthorized = role == "admin"

            showExpenseDatePicker(

                isAuthorized = isUserAuthorized, // only admin is allowed todo()
                initialDate = currentDate,

                // The selectedDate (LocalDate) is available here!
                onPicked = { selectedDate: LocalDate ->
                    // This is where you pass the result to your ViewModel
                    viewModel.onEvent(PurchaseUiEvent.SelectDate(selectedDate))
                }
            )
        }


    }

    override fun onMenuCreated(menu: Menu) {
        val editModeItem = menu.findItem(R.id.action_edit_mode)
        val pdfMenuItem = menu.findItem(R.id.action_gen_pdf)
        editModeSwitch = editModeItem.actionView?.findViewById(R.id.switch_toolbar_edit_mode)

        pdfMenuItem?.setOnMenuItemClickListener {
            generateReport()
            true
        }

        // Fetch the user role once for the switch logic
        val role = SharedPrefsHelper.getUserRole(requireContext())

        editModeSwitch?.setOnCheckedChangeListener { _, isChecked ->
            val selectedDate = viewModel.uiState.value.currentDate
            val isToday = selectedDate.isToday()
            val isLockedToday = isEditModeLockedForToday() // Check lock state

            if (isChecked) {
                // Check 1: Block non-admins from enabling at all times.
                if (role != "admin") {
                    // Revert the switch state to off and show a message
                    editModeSwitch?.isChecked = false
                    showSnackbar("Only administrators are allowed to enable edit mode.")
                    return@setOnCheckedChangeListener
                }

                // If we reach here, the user IS an admin. Admin can always enable.
                showSnackbar("Edit mode enabled")

                setEditModeLockedForToday(false)
                enableEditMode()

            } else {
                // Allow anyone to disable (turn off) the switch
                showSnackbar("Edit mode disabled")
                disableEditMode()

                // Apply PERMANENT LOCK: If it's today, set the lock to prevent re-enabling by non-admins.
                if (isToday) {
                    setEditModeLockedForToday(true)
                }
            }
        }


    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                purchaseAdapter.submitList(state.purchasesForDate)
                binding.tvSelectedDate.text = state.currentDate.toDisplayFormat()

                showSummary(
                    milkAmount = state.totalVolume,
                    avgFat = state.avgFat,
                    avgLr = state.avgLr,
                    totalTS = state.totalTS,
                    totalAmount = state.grandTotalForDate,
                    avgRate = state.avgRatePerLiter,
                    milkFatLr = state.volumeWithFatLr
                )

                val isToday = state.currentDate.isToday()
                val isLockedToday = isEditModeLockedForToday()

                if (isToday) {
                    biometricRequiredForToday = isLockedToday
                    if (isLockedToday) {
                        disableEditMode()
                    } else {
                        enableEditMode()
                    }
                } else {
                    biometricRequiredForToday = true
                    disableEditMode()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        purchaseAdapter = PurchaseAdapter(
            ::showEditBottomSheet,
            ::navToSupplierDetail
        ) { id, name -> showBalanceHistory(id, name) }
        binding.rvPurchases.apply {
            adapter = purchaseAdapter
            itemAnimator = null
            setHasFixedSize(true)
        }
    }

    private fun showBalanceHistory(supplierId: String, supplierName: String) {

        val btmSheet = SupplierBalanceHistoryBottomSheet.newInstance(supplierId, supplierName)

        btmSheet.show(childFragmentManager, null)
    }

    private fun generateReport() {
        val state = viewModel.uiState.value
        val filteredList = state.purchasesForDate


        val recordList = filteredList.toPurchaseRecordList()

        val pdfSummary = pdfSummary(
            totalQty = state.totalVolume,
            avgFat = state.avgFat,
            avgLr = state.avgLr,
            totalTs = state.totalTS,
            totalAmount = state.grandTotalForDate,
            totalPaid = state.grandTotalForDate,
            balanceDue = state.grandTotalForDate

        )

        val data = PurchaseReportPdf(
            footerNote = "Receipt generated on : ${LocalDate.now().toDisplayFormat()}",
            date = state.currentDate.toDisplayFormat(),
            recordList = recordList,
            pdfPurchaseSummary = pdfSummary
        )


        TodayPurchasePdf.generateAndSharePdf(
            context = requireContext(),
            data = data,
            baseName = "Supplier",
            showLogo = true,
//                logoResId = R.drawable.ic_launcher_foreground
        )

        showToast("Generating pdf report...")
    }

    private fun pdfSummary(
        totalQty: Double,
        avgFat: Double,
        avgLr: Double,
        totalTs: Double,
        totalAmount: Double,
        totalPaid: Double,
        balanceDue: Double
    ): PdfPurchaseSummary {
        return PdfPurchaseSummary(
            totalQty = totalQty.toRoundedStr(),
            avgFat = avgFat.toRoundedStr(),
            avgLr = avgLr.toRoundedStr(),
            totalTs = totalTs.toRoundedStr(),
            totalAmount = totalAmount.toRoundedStr(),
            totalPaid = totalPaid.toRoundedStr(),
            balanceDue = balanceDue.toRoundedStr()
        )
    }

    private fun showSummary(
        milkAmount: Double,
        avgLr: Double,
        avgFat: Double,
        totalTS: Double,
        milkFatLr: Double,
        totalAmount: Double,
        avgRate: Double
    ) {
        binding.apply {
            cardSummary.setTitle("Today Summary")
            cardSummary.collapse()

            val summaryBinding = LayoutPurchaseSummaryBinding.inflate(layoutInflater, root, false)
            cardSummary.setContent(summaryBinding.root)
            // 2. Use the ViewBinding object to set the data efficiently
            summaryBinding.apply {


                tvTotalMilk.text = "${milkAmount.toRoundedStr()} L"
                tvAvgFat.text = "${avgFat?.toRoundedStr()}% (${milkFatLr})"
                tvAvgLr.text = "${avgLr?.toRoundedStr()} (${milkFatLr})"
                tvTotalTs.text = "${totalTS?.toRoundedStr()} (${milkFatLr})"
                tvTotalAmount.text = "Rs. ${totalAmount.toPriceStr()}"
                tvAvgPrice.text = "Rs. ${avgRate.toPriceStr("%.2f")}"
            }
        }
    }

    private fun navToSupplierDetail(supplier: PurchaseWithSupplier) {
        val isAdmin = SharedPrefsHelper.isAdmin(requireContext())
        if (!isAdmin){
            showSnackbar("Only ADMIN is allowed")
            return
        }

        findNavController().navigate(
            PurchaseFragmentDirections.actionPurchaseFragmentToSupplierDetailFragment(
                supplier.supplier.supplierName,
                supplier.supplier.supplierId
            )
        )
    }

    private fun showEditBottomSheet(purchaseWithSupplier: PurchaseWithSupplier) {
        if (!isEditable) {
            showSnackbar("Enable from the top menu switch")
            return
        }

        val bottomSheet = PurchaseEditBottomSheet(
            entry = purchaseWithSupplier,
            onSave = { updatedEntry ->
                viewModel.updatePurchaseManually(updatedEntry)
            }
        )
        bottomSheet.show(parentFragmentManager, "EditPurchaseSheet")
    }

    private fun enableEditMode() {
        isEditable = true
        editModeSwitch?.isChecked = true
    }

    private fun disableEditMode() {
        isEditable = false
        editModeSwitch?.isChecked = false
    }

    // --- SharedPreferences Helpers ---

    private val prefs by lazy {
        requireContext().getSharedPreferences("purchase_prefs", Context.MODE_PRIVATE)
    }

    private fun isEditModeLockedForToday(): Boolean {
        val savedDate = prefs.getString("edit_mode_locked_date", null)
        return savedDate == LocalDate.now().toString() &&
                prefs.getBoolean("edit_mode_locked_today", false)
    }

    private fun setEditModeLockedForToday(locked: Boolean) {
        prefs.edit {
            putBoolean("edit_mode_locked_today", locked)
                .putString("edit_mode_locked_date", LocalDate.now().toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeUiState()
    }
}
