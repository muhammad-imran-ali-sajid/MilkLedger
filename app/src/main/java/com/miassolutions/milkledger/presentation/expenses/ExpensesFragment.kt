package com.miassolutions.milkledger.presentation.expenses

import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentExpensesBinding

class ExpensesFragment : BaseFragment<FragmentExpensesBinding>(FragmentExpensesBinding::inflate) {

    override fun setupViews() {
        setToolbarTitle(getString(R.string.expenses))



    }





}