package com.miassolutions.milkledger.presentation.stats

import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentStatsBinding

class StatsFragment : BaseFragment<FragmentStatsBinding>(FragmentStatsBinding::inflate) {

    override fun setupViews() {
        setToolbarTitle(getString(R.string.stats))
        showBottomNav(true)
    }
}