package com.miassolutions.milkledger.presentation.purchase

import android.view.MenuItem
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.ui.form.FormBottomSheetFragment
import com.miassolutions.milkledger.databinding.FragmentPurchasesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PurchaseFragment : BaseFragment<FragmentPurchasesBinding>(FragmentPurchasesBinding::inflate) {

    override fun setupViews() {
        setToolbarTitle(getString(R.string.purchases))
        showBottomNav(true)
    }

    override fun getMenuResId(): Int? = R.menu.menu_purchases

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_supplier -> {
                FormBottomSheetFragment(
                    formLayoutRes = R.layout.form_customer
                ) { result ->
                    // Handle success data
                    // result is a Map<String, String>
                    println("Form data: $result")
                }.show(parentFragmentManager, "CustomerForm")
                true
            }
            else -> false
        }
    }
}