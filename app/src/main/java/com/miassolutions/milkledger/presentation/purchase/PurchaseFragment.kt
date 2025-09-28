package com.miassolutions.milkledger.presentation.purchase

import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentPurchaseBinding

class PurchaseFragment : BaseFragment<FragmentPurchaseBinding>(FragmentPurchaseBinding::inflate) {

    override fun setupViews() {
        setToolbarTitle("Purchase")
        showBottomNav(true)
    }
}