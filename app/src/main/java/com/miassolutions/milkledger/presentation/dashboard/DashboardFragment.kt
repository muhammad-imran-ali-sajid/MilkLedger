package com.miassolutions.milkledger.presentation.dashboard


import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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

        setupToggleGroup()

    }



    override fun setupObservers() = with(binding) {
        viewModel.uiState.collectState { state ->
            tvSelectedDate.text = state.period
            tvTotalPurchases.text = "Rs. ${state.purchaseTotal.toRoundedStr()}"
            tvTotalSales.text = "Rs. ${state.salesTotal.toRoundedStr()}"
            tvTotalExpense.text = "Rs. ${state.expensesTotal.toRoundedStr()}"
            tvNetProfit.text = "Rs. ${state.profit.toRoundedStr()}"
            tvMilkPurchase.text = "${state.milkPurchase.toRoundedStr()} Litre"
            tvMilkSold.text = "${state.milkSold.toRoundedStr()} Litre"

        }
    }

    private fun setupToggleGroup() {
        binding.togglePeriod.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            when (checkedId) {
                R.id.btnToday -> viewModel.loadToday()
                R.id.btnWeekly -> viewModel.loadWeekly()
                R.id.btnMonthly -> viewModel.loadMonthly()
                R.id.btnYearly -> viewModel.loadYearly()
            }
        }
    }


    override fun setupListeners() {
        binding.navSales.setOnClickListener {
            val dest = DashboardFragmentDirections.actionDashboardFragmentToSalesFragment()
            navigateTo(dest.actionId)

        }

        binding.navPurchase.setOnClickListener {
            val dest = DashboardFragmentDirections.actionDashboardFragmentToPurchaseFragment()
            navigateTo(dest.actionId)
        }

        binding.navExpenses.setOnClickListener {
            val dest = DashboardFragmentDirections.actionDashboardFragmentToExpensesFragment()
            navigateTo(dest.actionId)
        }

        binding.navSummary.setOnClickListener {
            val dest = DashboardFragmentDirections.actionDashboardFragmentToStatsFragment()
            navigateTo(dest.actionId)
        }

        binding.apply {
            btnNextDate.setOnClickListener { viewModel.onNextClicked() }
            btnPrevDate.setOnClickListener { viewModel.onPrevClicked() }
        }

    }


    private fun navigateTo(destinationId: Int) {
        findNavController().navigate(destinationId)
    }
}
