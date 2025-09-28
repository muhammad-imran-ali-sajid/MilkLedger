package com.miassolutions.milkledger.presentation.sales

import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentSalesBinding

class SalesFragment : BaseFragment<FragmentSalesBinding>(FragmentSalesBinding::inflate) {

    override fun setupViews() {
        setToolbarTitle(getString(R.string.sales))
        showBottomNav(true)
    }
}