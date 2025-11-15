package com.miassolutions.milkledger.presentation.customer.sales

import android.util.Log
import android.view.Menu
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.materialswitch.MaterialSwitch
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.prefs.SalesPrefsHelper
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.isToday
import com.miassolutions.milkledger.core.util.showExpenseDatePicker
import com.miassolutions.milkledger.core.util.toDisplayFormat
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import com.miassolutions.milkledger.databinding.FragmentSalesBinding
import com.miassolutions.milkledger.databinding.LayoutSalesSummaryBinding
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

        val editModeItem = menu.findItem(R.id.action_edit_mode)
        editModeSwitch =
            editModeItem.actionView?.findViewById(R.id.switch_toolbar_edit_mode)

        // Fetch the user role once for the switch logic
        val role = SharedPrefsHelper.getUserRole(requireContext())

        editModeSwitch?.setOnCheckedChangeListener { _, isChecked ->
            val selectedDate = viewModel.uiState.value.currentDate
            val isToday = selectedDate.isToday()

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

                // USE HELPER HERE
                SalesPrefsHelper.setEditModeLockedForToday(requireContext(), false)
                enableEditMode()

            } else {
                // Allow anyone to disable (turn off) the switch
                showSnackbar("Edit mode disabled")
                disableEditMode()

                // Apply PERMANENT LOCK: If it's today, set the lock to prevent re-enabling by non-admins.
                if (isToday) {
                    // USE HELPER HERE
                    SalesPrefsHelper.setEditModeLockedForToday(requireContext(), true)
                }
            }
        }
    }


    private fun enableEditMode() {
        isEditable = true
        adapter.isEditable = true
        showSnackbar("Edit mode enabled")
    }

    private fun disableEditMode() {
        isEditable = false
        adapter.isEditable = false
        showSnackbar("Edit mode disabled")
    }

    private fun showSummary(
        milkAmount: Double,
        deduction: Double,
        totalNetMilk: Double,
        totalAmount: Double,
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
                tvAvgPrice.text = "Rs. ${avgRate.toRoundedStr()}"
                tvTotalAmount.text = "Rs. ${totalAmount.toRoundedStr()}"
            }

        }
    }

    override fun setupListeners() {
        binding.tvSelectedDate.setOnClickListener {
            val currentDate = viewModel.uiState.value.currentDate

            val isAdmin = SharedPrefsHelper.isAdmin(requireContext())
            // Assume you fetch the authorization status dynamically
            val isUserAuthorized = isAdmin // Replace with actual auth check

            showExpenseDatePicker(

                isAuthorized = isUserAuthorized,
                initialDate = currentDate,
                // The selectedDate (LocalDate) is available here!
                onPicked = { selectedDate: LocalDate ->
                    // This is where you pass the result to your ViewModel
                    viewModel.onEvent(SalesUiEvent.SelectDate(selectedDate))
                }
            )

        }


    }

    private fun setupSalesRV() {

        adapter = SalesEntryAdapter(::showEditSaleBottomSheet, ::navToDetail)
        binding.rvSales.adapter = adapter

    }

    private fun navToDetail(id: String, name: String) {
        findNavController().navigate(
            SalesFragmentDirections.actionSalesFragmentToCustomerDetailFragment(
                id,
                name
            )
        )
    }


    override fun setupObservers() {
        viewModel.uiState.collectState { state ->
            Log.d("SalesFragment", "${state.salesForDate}")


            adapter.submitList(state.salesForDate)

            binding.apply {
                tvSelectedDate.text = state.currentDate.toDisplayFormat()



                showSummary(
                    milkAmount = state.totalMilk,
                    deduction = state.totalDeduction,
                    totalNetMilk = state.totalNetMilk,
                    totalAmount = state.totalAmount,
                    avgRate = state.avgRatePerLiter
                )

            }
        }
    }

    private fun showEditSaleBottomSheet(saleWithCustomer: SaleWithCustomer) {
        if (!isEditable) {
            showSnackbar("Enable from the top menu switch")
            return
        }
        SalesEditBottomSheet(
            entry = saleWithCustomer,
            onSave = { salesEntryEntity ->
                viewModel.updateSaleManually(salesEntryEntity)

            }
        ).show(parentFragmentManager, "SaleEditBottomSheet")
    }


}