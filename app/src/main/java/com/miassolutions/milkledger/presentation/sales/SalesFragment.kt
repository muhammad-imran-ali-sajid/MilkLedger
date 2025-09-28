package com.miassolutions.milkledger.presentation.sales

import android.view.MenuItem
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentSalesBinding

class SalesFragment : BaseFragment<FragmentSalesBinding>(FragmentSalesBinding::inflate) {

    override fun setupViews() {
        setToolbarTitle(getString(R.string.sales))
        showBottomNav(true)
    }

    override fun getMenuResId(): Int? = R.menu.menu_sales

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_customer -> {
                showToast("New Customer")
                true
            }
            else -> false
        }
    }
}