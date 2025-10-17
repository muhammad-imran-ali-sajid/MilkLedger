package com.miassolutions.milkledger.presentation


import android.content.Intent
import android.provider.Settings
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentDashboardBinding
import java.time.LocalDate

class DashboardFragment :
    BaseFragment<FragmentDashboardBinding>(FragmentDashboardBinding::inflate) {

    override fun setupViews() {
        setToolbarTitle(getString(R.string.app_name)) // or "ڈیش بورڈ" in Urdu

        // Example of setting today's date
        val todayDate = LocalDate.now().toString()
        binding.tvTodayDate.text = todayDate
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

        binding.btnChangeDate.setOnClickListener {
            startActivity(Intent(Settings.ACTION_DATE_SETTINGS))
        }
    }

    private fun navigateTo(destinationId: Int) {
        findNavController().navigate(destinationId)
    }
}
