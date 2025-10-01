package com.miassolutions.milkledger.presentation.suppliers

import android.view.MenuItem
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentSuppliersBinding

class SuppliersFragment :
    BaseFragment<FragmentSuppliersBinding>(FragmentSuppliersBinding::inflate) {

    override fun setupViews() {
        setToolbarTitle(getString(R.string.suppliers))
        showBottomNav(false)

    }


}