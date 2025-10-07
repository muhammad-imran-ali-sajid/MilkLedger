package com.miassolutions.milkledger.presentation.purchase

import android.view.MenuItem
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.presentation.forms.CustomerFormBottomSheetFragment
import com.miassolutions.milkledger.databinding.FragmentPurchasesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PurchaseFragment : BaseFragment<FragmentPurchasesBinding>(FragmentPurchasesBinding::inflate) {

    override fun setupViews() {
        setToolbarTitle(getString(R.string.purchases))
        showBottomNav(true)
    }


}