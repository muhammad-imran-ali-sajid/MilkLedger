package com.miassolutions.milkledger.presentation.sales

import android.util.Log
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
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

    override fun setupViews() {
        setToolbarTitle(getString(R.string.sales))

        setupSalesRV()


    }

    override fun setupListeners() {
        binding.tvSelectedDate.setOnClickListener {
            val currentDate = viewModel.uiState.value.currentDate
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentDate.year)
                set(Calendar.MONTH, currentDate.monthValue - 1)
                set(Calendar.DAY_OF_MONTH, currentDate.dayOfMonth)
            }

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(calendar.timeInMillis)
                .build()

            datePicker.addOnPositiveButtonClickListener { selectedDateInMillis ->
                val selectedDate = Instant.ofEpochMilli(selectedDateInMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                viewModel.onDateSelected(selectedDate)
            }

            datePicker.show(parentFragmentManager, "MaterialDatePicker")
        }

        binding.btnPrevDate.setOnClickListener {
            val currentDate = viewModel.uiState.value.currentDate
            val previousDate = currentDate.minusDays(1)
            viewModel.onDateSelected(previousDate)
        }

        binding.btnNextDate.setOnClickListener {
            val currentDate = viewModel.uiState.value.currentDate
            val nextDate = currentDate.plusDays(1)
            viewModel.onDateSelected(nextDate)
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
                tvSelectedDate.text = getString(
                    R.string.date_format,
                    state.currentDate.dayOfMonth,
                    state.currentDate.monthValue,
                    state.currentDate.year
                )

                tvTotalAmount.text =
                    getString(R.string.rs, state.totalAmount.roundToInt())

                tvDeduction.text = state.totalDeduction.toRoundedStr()
                tvTotalNetMilk.text = state.totalMilk.toRoundedStr()
                tvAvgPrice.text = state.avgRatePerLiter.toRoundedStr()
                tvTotalMilk.text = state.totalMilk.toRoundedStr()

            }
        }
    }

    private fun showEditSaleBottomSheet(saleWithCustomer: SaleWithCustomer) {
        SalesEditBottomSheet(
            entry = saleWithCustomer,
            onSave = { salesEntryEntity ->
                viewModel.updateSaleManually(salesEntryEntity)

            }
        ).show(parentFragmentManager, "SaleEditBottomSheet")
    }


}