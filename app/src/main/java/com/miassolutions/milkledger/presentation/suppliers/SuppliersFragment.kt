package com.miassolutions.milkledger.presentation.suppliers

import android.view.MenuItem
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentPurchasesBinding
import com.miassolutions.milkledger.databinding.FragmentSuppliersBinding

class SuppliersFragment : BaseFragment<FragmentSuppliersBinding>(FragmentSuppliersBinding::inflate) {

    override fun setupViews() {
        setToolbarTitle(getString(R.string.expenses))
        showBottomNav(true)

    }

    override fun getMenuResId(): Int? = R.menu.menu_expenses

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_category -> {
                showToast("Expense Category")
                true
            }
            else -> false
        }
    }


}