package com.miassolutions.milkledger.presentation.customer.sales

import android.util.Log
import android.view.Menu
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.materialswitch.MaterialSwitch
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.helper.BiometricHelper
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.ui.extensions.formattedDate
import com.miassolutions.milkledger.core.ui.extensions.pickSingleDate
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.local.entities.SalesEntryEntity
import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import com.miassolutions.milkledger.databinding.FragmentSalesBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import kotlin.math.roundToInt

@AndroidEntryPoint
class SalesFragment : BaseFragment<FragmentSalesBinding>(FragmentSalesBinding::inflate) {

    private lateinit var adapter: SalesEntryAdapter
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
        val switch =
            editModeItem.actionView?.findViewById<MaterialSwitch>(R.id.switch_toolbar_edit_mode)

        switch?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                BiometricHelper.authenticate(
                    fragment = this,
                    title = "Authenticate to enable edit mode",
                    subtitle = "Use your fingerprint or device credentials",
                    onSuccess = { enableEditMode() },
                    onFailure = {
                        switch.isChecked = false
                        showToast("Authentication failed.")
                    }
                )
            } else {
                disableEditMode()
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
            val summaryView =
                layoutInflater.inflate(R.layout.layout_sales_summary, binding.root, false)
            cardSalesSummary.setContent(summaryView)
            cardSalesSummary.collapse()


            // You can access child TextViews like this:
            val tvMilkAmount = summaryView.findViewById<TextView>(R.id.tv_total_milk)
            val tvDeduction = summaryView.findViewById<TextView>(R.id.tv_deduction)
            val tvTotalNetMilk = summaryView.findViewById<TextView>(R.id.tv_total_net_milk)
            val tvTotalAmount = summaryView.findViewById<TextView>(R.id.tv_total_amount)
            val tvAvgRate = summaryView.findViewById<TextView>(R.id.tv_avg_price)

            tvMilkAmount.text = milkAmount.toRoundedStr()
            tvDeduction.text = deduction.toRoundedStr()
            tvTotalNetMilk.text = totalNetMilk.toRoundedStr()
            tvAvgRate.text = avgRate.toRoundedStr()
            tvTotalAmount.text = totalAmount.toRoundedStr()
        }
    }

    override fun setupListeners() {
        binding.tvSelectedDate.setOnClickListener {
            val currentDate = viewModel.uiState.value.currentDate

            pickSingleDate(
                initialDate = currentDate,
                onPicked = { viewModel.onDateSelected(it) }
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
                tvSelectedDate.text = state.currentDate.formattedDate()



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