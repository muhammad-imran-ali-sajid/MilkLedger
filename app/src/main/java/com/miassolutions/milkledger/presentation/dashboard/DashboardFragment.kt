package com.miassolutions.milkledger.presentation.dashboard


import android.content.Intent
import android.provider.Settings
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.datesort.DateFilterBottomSheet
import com.miassolutions.datesort.DateRangeType
import com.miassolutions.datesort.OnDateRangeSelected
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.databinding.FragmentDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class DashboardFragment :
    BaseFragment<FragmentDashboardBinding>(FragmentDashboardBinding::inflate) {

    private val viewModel by viewModels<DashboardViewModel>()

    override fun setupViews() {
        setToolbarTitle(getString(R.string.app_name)) // or "ڈیش بورڈ" in Urdu

        // Example of setting today's date
        val todayDate = LocalDate.now().toString()
        binding.tvTodayDate.text = todayDate


    }

    override fun setupObservers() {
        viewModel.uiState.collectState { state ->
            binding.tvTodayDate.text = state.date.toString()
            binding.tvTotalSales.text = "Rs. ${state.totalSales.toRoundedStr("%.0f")}"
            binding.tvTotalExpense.text = "Rs. ${state.totalPurchases.toRoundedStr("%.0f")}"
            binding.tvProfit.text = "Rs. ${state.profit.toRoundedStr("%.0f")}"
        }
    }

    override fun setupListeners() {
        binding.cardSales.setOnClickListener {
            val dest = DashboardFragmentDirections.actionDashboardFragmentToSalesFragment()
            navigateTo(dest.actionId)

        }

        binding.cardPurchases.setOnClickListener {
            val dest = DashboardFragmentDirections.actionDashboardFragmentToPurchaseFragment()
            navigateTo(dest.actionId)
        }

        binding.cardExpenses.setOnClickListener {
            val dest = DashboardFragmentDirections.actionDashboardFragmentToExpensesFragment()
            navigateTo(dest.actionId)
        }

        binding.cardStats.setOnClickListener {
            val dest = DashboardFragmentDirections.actionDashboardFragmentToStatsFragment()
            navigateTo(dest.actionId)
        }

        binding.cardCustomerHistory.setOnClickListener {
            navigateTo(R.id.customersFragment)
        }

        binding.cardSupplierHistory.setOnClickListener {
            navigateTo(R.id.suppliersFragment)
        }

        binding.btnTestA.setOnClickListener {
            val sheet = DateFilterBottomSheet(object : OnDateRangeSelected {
                override fun onDateRangeSelected(
                    start: LocalDate,
                    end: LocalDate,
                    type: DateRangeType
                ) {
                    binding.tvTestDate.text = "Type: $type\nFrom: $start\nTo: $end"
                }
            })
            sheet.show(parentFragmentManager, "DateFilter")
        }


    }

    private fun navigateTo(destinationId: Int) {
        findNavController().navigate(destinationId)
    }
}
