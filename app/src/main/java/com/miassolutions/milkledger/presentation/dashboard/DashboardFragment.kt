package com.miassolutions.milkledger.presentation.dashboard


import android.view.Menu
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.filterdata.CustomDateRangeBottomSheet
import com.miassolutions.milkledger.core.helper.RemoteConfigHelper
import com.miassolutions.milkledger.core.pdf.dashboardreport.DashboardReportGenerator
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.hide
import com.miassolutions.milkledger.core.util.show
import com.miassolutions.milkledger.core.util.showExpenseDatePicker
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.databinding.FragmentDashboardBinding
import com.miassolutions.milkledger.presentation.customer.sales.SalesUiEvent
import com.miassolutions.milkledger.presentation.stats.toPdfSummary
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class DashboardFragment :
    BaseFragment<FragmentDashboardBinding>(FragmentDashboardBinding::inflate) {

    private val viewModel by viewModels<DashboardViewModel>()
    private val syncViewModel by viewModels<SyncViewModel>()

    override fun getMenuResId(): Int {
        return R.menu.menu_dashboard
    }

    override fun setupViews() {

        syncViewModel.startInitialSync() //todo

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



        setupToggleGroup()
        setupCustomRangeCalendar()

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
        viewModel.uiState.collectState { state ->
            val milkFatLr = state.totalMilkWithFatAndLr

            val qtyDiff = state.milkSold - state.milkPurchase

            tvSelectedDate.text = state.period

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



    private fun setupToggleGroup() {
        binding.togglePeriod.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            when (checkedId) {
                R.id.btn_daily -> {
                    btnVisibilityControl()
                    viewModel.loadDaily()
                }

                R.id.btnWeekly -> {
                    btnVisibilityControl()
                    viewModel.loadWeekly()
                }

                R.id.btnMonthly -> {
                    btnVisibilityControl()
                    viewModel.loadMonthly()
                }

                R.id.btnYearly -> {
                    btnVisibilityControl()
                    viewModel.loadYearly()
                }

                R.id.btn_all -> {
                    btnVisibilityControl(true)
                    viewModel.loadCustom(null, null)


                }
            }
        }
    }

    private fun showDateFilter() {
        val bottomSheet = CustomDateRangeBottomSheet()
        bottomSheet.show(parentFragmentManager, "CUSTOM_RANGE_ONLY_FILTER_DATA")
    }

    private fun setupCustomRangeCalendar() {
        // 1. Set up the listener
        setFragmentResultListener(CustomDateRangeBottomSheet.REQUEST_KEY) { requestKey, bundle ->
            if (requestKey == CustomDateRangeBottomSheet.REQUEST_KEY) {

                val startDateString = bundle.getString(CustomDateRangeBottomSheet.BUNDLE_START_DATE)
                val endDateString = bundle.getString(CustomDateRangeBottomSheet.BUNDLE_END_DATE)

                if (startDateString != null && endDateString != null) {

                    val startDate = LocalDate.parse(startDateString)
                    val endDate = LocalDate.parse(endDateString)

                    handleSelectedDateRange(startDate, endDate)
                }
            }
        }
    }


    private fun handleSelectedDateRange(startDate: LocalDate?, endDate: LocalDate?) {

        if (startDate != null && endDate != null)
            viewModel.loadCustom(startDate, endDate)



    }

    private fun btnVisibilityControl(toShow: Boolean = false) {
        if (toShow) {
            binding.apply {
                btnCustomRange.show()
                btnNextDate.visibility = View.INVISIBLE
                btnPrevDate.visibility = View.INVISIBLE


            }
        } else {
            binding.apply {
                btnCustomRange.hide()
                btnNextDate.show()
                btnPrevDate.show()

            }
        }
    }


    override fun setupListeners() {
        binding.btnCustomRange.setOnClickListener {
            showDateFilter()
        }

//        // later will pullToRefresh TODO()
//        binding.cardProfit.setOnClickListener {
//            syncViewModel.startInitialSync()
//        }

        binding.tvSelectedDate.setOnClickListener {


            val isAdmin = SharedPrefsHelper.isAdmin(requireContext())
            val isUserAuthorized = isAdmin // Replace with actual auth check

            showExpenseDatePicker(
                isAuthorized = isUserAuthorized,
                initialDate = LocalDate.now(),
                onPicked = { selectedDate: LocalDate ->
                    // Load DAILY mode for selected date
                    binding.togglePeriod.check(R.id.btn_daily)
                    viewModel.loadRange(selectedDate, selectedDate)
                }
            )
        }

        binding.cashFlowCard.setOnClickListener {

            if (!isPremiumEnabled) {
                showSnackbar("This feature requires Premium")
                return@setOnClickListener
            }

            val dest = DashboardFragmentDirections.actionDashboardFragmentToStatsFragment()
            navigateTo(dest.actionId)
        }





        binding.saleCard.setOnClickListener {
            val dest = DashboardFragmentDirections.actionDashboardFragmentToSalesFragment()
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

        binding.apply {
            btnNextDate.setOnClickListener { viewModel.onNextClicked() }
            btnPrevDate.setOnClickListener { viewModel.onPrevClicked() }
        }

    }


    private fun navigateTo(destinationId: Int) {
        findNavController().navigate(destinationId)
    }
}
