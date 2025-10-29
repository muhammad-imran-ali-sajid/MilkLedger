package com.miassolutions.milkledger.presentation.dashboard


import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.filterdata.CustomDateRangeBottomSheet
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.hide
import com.miassolutions.milkledger.core.util.show
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
        setupCustomRangeCalendar()

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
                // 2. Extract the data
                val startDateString = bundle.getString(CustomDateRangeBottomSheet.BUNDLE_START_DATE)
                val endDateString = bundle.getString(CustomDateRangeBottomSheet.BUNDLE_END_DATE)

                if (startDateString != null && endDateString != null) {
                    // 3. Convert the String dates back to LocalDate
                    val startDate = LocalDate.parse(startDateString)
                    val endDate = LocalDate.parse(endDateString)

                    // 4. Use the selected dates -> Call ViewModel to update state
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

//        binding.navSummary.setOnClickListener {
//            val dest = DashboardFragmentDirections.actionDashboardFragmentToStatsFragment()
//            navigateTo(dest.actionId)
//        }

        binding.apply {
            btnNextDate.setOnClickListener { viewModel.onNextClicked() }
            btnPrevDate.setOnClickListener { viewModel.onPrevClicked() }
        }

    }


    private fun navigateTo(destinationId: Int) {
        findNavController().navigate(destinationId)
    }
}
