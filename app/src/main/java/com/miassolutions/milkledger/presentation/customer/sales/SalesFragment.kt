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

        val isAdmin = SharedPrefsHelper.isAdmin(requireContext())

        // --- 1. INITIALIZE SWITCH STATE ---
        initializeEditModeState(isAdmin, viewModel.uiState.value.currentDate)

        // --- 2. SETUP LISTENER ---
        editModeSwitch?.setOnCheckedChangeListener { _, isChecked ->
            val selectedDate = viewModel.uiState.value.currentDate
            val isCurrentDateToday = selectedDate.isToday()

            // Step A: Immediately save the intended status
            SalesPrefsHelper.setEditModeActive(requireContext(), isChecked)

            if (isChecked) {
                // Logic when trying to turn ON:

                // If the user is NOT an admin, they are immediately blocked from turning ON.
                if (!isAdmin) {
                    // Revert UI and Preferences because the action is blocked
                    editModeSwitch?.isChecked = false
                    SalesPrefsHelper.setEditModeActive(requireContext(), false)
                    showSnackbar("As a non-admin, you cannot re-enable edit mode once disabled for today.")
                    return@setOnCheckedChangeListener
                }

                // If user IS an admin:

                // 1. Enable mode
                enableEditMode()

                // 2. Remove the permanent lock (admin always overrides the lock to ON)
                SalesPrefsHelper.setEditModeLockedForToday(requireContext(), false)


            } else {
                // Logic when trying to turn OFF (Allowed for everyone):
                disableEditMode()

                // Apply PERMANENT LOCK: If it's today, set the lock.
                if (isCurrentDateToday) {
                    SalesPrefsHelper.setEditModeLockedForToday(requireContext(), true)

                    if (!isAdmin) {
                        showSnackbar("Edit mode disabled and permanently locked for today.")
                    }
                } else {
                    showSnackbar("Edit mode disabled")
                }
            }
        }
    }

    /**
     * Determines and sets the initial state of the edit mode based on admin status,
     * persistent active state, and permanent daily lock status.
     */
    /**
     * Determines and sets the initial state of the edit mode based on admin status,
     * persistent active state, and permanent daily lock status.
     */
    private fun initializeEditModeState(isAdmin: Boolean, selectedDate: LocalDate) {

        val isToday = selectedDate.isToday()
        val isLocked = isToday && SalesPrefsHelper.isEditModeLockedForToday(requireContext())

        var shouldBeActive: Boolean

        if (isAdmin) {
            // ADMIN LOGIC: Can use the last saved active state (shouldBeActive)
            // but is restricted by date and temporary lock.

            shouldBeActive = SalesPrefsHelper.isEditModeActive(requireContext())

            if (isLocked || !isToday) {
                // If permanently locked OR if it's not today, it must be OFF for the Admin
                shouldBeActive = false
                // Note: We don't save this 'false' state here, the admin can re-enable later.
            }

        } else {
            // NON-ADMIN LOGIC:
            // 1. If it's today AND NOT permanently locked, they start ON.
            // 2. Otherwise (not today OR locked), it must be OFF.
            shouldBeActive = isToday && !isLocked

            // Crucial: Update active pref to reflect this calculated state for the non-admin.
            // This ensures if they navigate away and come back, they return to this state.
            SalesPrefsHelper.setEditModeActive(requireContext(), shouldBeActive)
        }

        // 2. Apply the final determined state
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
            val isUserAuthorized = isAdmin // Replace with actual auth check

            showExpenseDatePicker(
                isAuthorized = isUserAuthorized,
                initialDate = currentDate,
                onPicked = { selectedDate: LocalDate ->

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

                val isAdmin = SharedPrefsHelper.isAdmin(requireContext())
                initializeEditModeState(isAdmin, state.currentDate)


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