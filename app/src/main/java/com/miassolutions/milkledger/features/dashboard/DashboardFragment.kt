package com.miassolutions.milkledger.features.dashboard


import android.view.Menu
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentDashboardBinding
import com.miassolutions.milkledger.features.cashflow.toPdfSummary
import com.miassolutions.milkledger.utils.datefilter.DateFilterCallback
import com.miassolutions.milkledger.utils.datefilter.DateFilterController
import com.miassolutions.milkledger.utils.datefilter.DatePeriod
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.hide
import com.miassolutions.milkledger.utils.extensions.show
import com.miassolutions.milkledger.utils.extensions.toPriceStr
import com.miassolutions.milkledger.utils.extensions.toRoundedStr
import com.miassolutions.milkledger.utils.helper.RemoteConfigHelper
import com.miassolutions.milkledger.utils.pdf.dashboardreport.DashboardReportGenerator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment :
    BaseFragment<FragmentDashboardBinding>(FragmentDashboardBinding::inflate), DateFilterCallback {

    private val viewModel by viewModels<DashboardViewModel>()

    private lateinit var controller: DateFilterController


    override fun getMenuResId(): Int {
        return R.menu.menu_dashboard
    }

    override fun setupViews() {


        controller = DateFilterController(
            this,
            binding.dateFilterLayout,
            callback = this
        )
        controller.init()

        RemoteConfigHelper.fetchValue(viewLifecycleOwner) { isTrialVersion ->

            // If Remote Config says trial is active, enable UI; else disable UI
            val isTrialExpired = !isTrialVersion

            if (isTrialExpired) {
                binding.apply {
                    purchaseCard.isEnabled = false
                    saleCard.isEnabled = false
                    expenseCard.isEnabled = false
                    cardProfit.isEnabled = false
                    tvTrial.show() // or tvTrial.visibility = View.VISIBLE
                }
            } else {
                binding.apply {
                    purchaseCard.isEnabled = true
                    saleCard.isEnabled = true
                    expenseCard.isEnabled = true
                    cardProfit.isEnabled = true
                    tvTrial.hide() // or tvTrial.visibility = View.GONE
                }
            }
        }


        val isAdmin = SharedPrefsHelper.isAdmin(requireContext())

        if (!isAdmin) {
            hideTextViews()
        }


    }

    override fun onMenuCreated(menu: Menu) {
        val item = menu.findItem(R.id.action_dashboard_pdf)

        item.setOnMenuItemClickListener {
            if (!isPremiumEnabled) {
                showSnackbar("Premium feature")
                return@setOnMenuItemClickListener true
            }

            showDialog("Generate PDF?", "Do you want to create PDF?") {
                generatePdfReport()
            }

            true
        }
    }


    private fun generatePdfReport() {
        val state = viewModel.uiState.value

        val data = state.toPdfSummary()

        DashboardReportGenerator.generateAndSharePdf(
            context = requireContext(),
            data = data,
            baseName = "dashboard",
        )

    }

    private fun hideTextViews() {
        binding.apply {
            tvTotalPurchases.hide()
            tvTotalExpense.hide()
            tvTotalSales.hide()
            tvNetProfit.hide()
        }
    }


    override fun setupObservers() = with(binding) {
        collectFlow(viewModel.uiState) { state ->
            val milkFatLr = state.totalMilkWithFatAndLr

            val qtyDiff = state.milkSold - state.milkPurchase

            dateFilterLayout.tvSelectedDate.text = state.period

            tvTotalPurchases.text = state.purchaseTotal.toPriceStr()
            tvTotalSales.text = state.salesTotal.toPriceStr()
            tvTotalExpense.text = state.fixedExpense.toPriceStr()
            tvNetProfit.text = state.profit.toPriceStr()
            tvMilkPurchase.text = "${state.milkPurchase.toRoundedStr("%.0f")} L"
            tvMilkSold.text = "${state.milkSold.toRoundedStr("%.0f")} L"
            tvQtyDiff.text = "${qtyDiff.toRoundedStr(" % .0f")} L"
            tvAvgFat.text = "${state.avgFat.toRoundedStr()}% ($milkFatLr)"
            tvAvgLr.text = "${state.avgLr.toRoundedStr()} ($milkFatLr)"
            tvTotalTs.text = "${state.totalTs.toRoundedStr()} ($milkFatLr)"
            tvAvgSP.text = state.avgSP?.toRoundedStr() ?: "0.0"
            tvAvgCP.text = state.avgCP?.toRoundedStr() ?: "0.0"
            tvAvgPriceDiff.text = state.difference?.toRoundedStr() ?: "0.0"
            tvPersonalExpense.text = state.personalExpense.toPriceStr()
            tvRemainingProfit.text = state.profitAfter.toPriceStr()


        }
    }


    override fun setupListeners() {


        binding.cashFlowCard.setOnClickListener {

            if (!isPremiumEnabled) {
                showSnackbar("This feature requires Premium")
                return@setOnClickListener
            }

            val dest = DashboardFragmentDirections.actionDashboardFragmentToStatsFragment()
            navigateTo(dest.actionId)
        }





        binding.saleCard.setOnClickListener {
            val dest = DashboardFragmentDirections.actionDashboardFragmentToSaleListFragment()
            navigateTo(dest.actionId)

        }

        binding.purchaseCard.setOnClickListener {
            val dest = DashboardFragmentDirections.actionDashboardFragmentToPurchaseFragment()
            navigateTo(dest.actionId)
        }

        binding.expenseCard.setOnClickListener {
            val dest = DashboardFragmentDirections.actionDashboardFragmentToExpensesFragment()
            navigateTo(dest.actionId)
        }

        binding.cardProfit.setOnClickListener {
            val isAdmin = SharedPrefsHelper.isAdmin(requireContext())

            if (isAdmin) {
                val dest = DashboardFragmentDirections.actionDashboardFragmentToProfitFragment()
                navigateTo(dest.actionId)

            } else {
                showSnackbar("Only ADMIN is allowed here")
            }


        }


    }


    private fun navigateTo(destinationId: Int) {
        findNavController().navigate(destinationId)
    }

    override fun onPeriodChanged(period: DatePeriod) {
        viewModel.loadData(period)
    }
}
