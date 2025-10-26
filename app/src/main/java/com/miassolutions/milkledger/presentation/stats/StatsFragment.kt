package com.miassolutions.milkledger.presentation.stats

import android.app.DatePickerDialog
import android.util.Log
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButtonToggleGroup
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.databinding.FragmentStatsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

@AndroidEntryPoint
class StatsFragment : BaseFragment<FragmentStatsBinding>(FragmentStatsBinding::inflate) {

    private val viewModel by viewModels<AnalyticsViewModel>()

    override fun setupViews() {
        setupToggleGroup()
        collectUiState()



        // Load default data
        viewModel.loadCustomData(LocalDate.now(), LocalDate.now())
    }

    private fun setupToggleGroup() {
        binding.togglePeriod.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            when (checkedId) {
                R.id.btnWeekly -> viewModel.loadWeeklyData()
                R.id.btnMonthly -> viewModel.loadMonthlyData()
                R.id.btnYearly -> viewModel.loadYearlyData()
                R.id.btnCustom -> showCustomDatePicker()
            }
        }
    }

    private fun collectUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    financialOverViewCard(
                        purchaseAmount = state.purchaseTotal.toRoundedStr(),
                        saleAmount = state.salesTotal.toRoundedStr(),
                        expenseAmount = state.expensesTotal.toRoundedStr(),
                        netProfit = state.profit.toRoundedStr()
                    )

                    milkOverViewCard(
                        milkPurchase = state.milkPurchase.toRoundedStr(),
                        milkSold = state.milkSold.toRoundedStr()
                    )

                    binding.tvTitle.text = state.period

                }
            }
        }
    }

    private fun milkOverViewCard(milkPurchase: String, milkSold: String) = with(binding) {

        cardMilkPurchase.apply {
            tvTitle.text = "Milk Purchase"
            tvValue.text = milkPurchase
        }

        cardMilkSold.apply {
            tvTitle.text = "Milk Sold"
            tvValue.text = milkSold
        }


    }

    private fun financialOverViewCard(
        purchaseAmount: String,
        saleAmount: String,
        expenseAmount: String,
        netProfit: String
    ) =
        with(binding) {
            cardSales.apply {
                tvTitle.text = "Sales Amount"
                tvValue.text = saleAmount
            }
            cardPurchases.apply {
                tvTitle.text = "Purchase Amount"
                tvValue.text = purchaseAmount
            }
            cardExpenses.apply {
                tvTitle.text = "Expenses"
                tvValue.text = expenseAmount
            }
            cardProfit.apply {
                tvTitle.text = "Net Profit"
                tvValue.text = netProfit
            }
        }

    private fun showCustomDatePicker() {
        val today = LocalDate.now()

        val startPicker = DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                val start = LocalDate.of(y, m + 1, d)
                val endPicker = DatePickerDialog(
                    requireContext(),
                    { _, y2, m2, d2 ->
                        val end = LocalDate.of(y2, m2 + 1, d2)
                        viewModel.loadCustomData(start, end)
                    },
                    today.year, today.monthValue - 1, today.dayOfMonth
                )
                endPicker.setTitle("Select End Date")
                endPicker.show()
            },
            today.year, today.monthValue - 1, today.dayOfMonth
        )

        startPicker.setTitle("Select Start Date")
        startPicker.show()
    }
}
