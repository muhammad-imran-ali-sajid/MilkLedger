package com.miassolutions.milkledger.presentation.customers

import android.view.MenuItem
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentCustomersBinding

class CustomersFragment :
    BaseFragment<FragmentCustomersBinding>(FragmentCustomersBinding::inflate) {

    override fun setupViews() {
        setToolbarTitle(getString(R.string.customers))
        showBottomNav(false)

    }



}