package com.miassolutions.milkledger.presentation.dashboard


import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.databinding.FragmentDashboardBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment :
    BaseFragment<FragmentDashboardBinding>(FragmentDashboardBinding::inflate) {

    private val viewModel by viewModels<DashboardViewModel>()

    override fun setupViews() {


    }

    override fun setupObservers() = with(binding){
        viewModel.uiState.collectState { state ->
            tvTodayDate.text = state.period
            tvTotalPurchases.text = "Rs. ${state.purchaseTotal.toRoundedStr()}"
            tvTotalSales.text = "Rs. ${state.salesTotal.toRoundedStr()}"
            tvTotalExpense.text = "Rs. ${state.expensesTotal.toRoundedStr()}"
            tvNetProfit.text = "Rs. ${state.profit.toRoundedStr()}"
            tvMilkPurchase.text = "${state.milkPurchase.toRoundedStr()} Litre"
            tvMilkSold.text  = "${state.milkSold.toRoundedStr()} Litre"

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

    }


    private fun navigateTo(destinationId: Int) {
        findNavController().navigate(destinationId)
    }
}
