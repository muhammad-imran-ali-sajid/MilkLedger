package com.miassolutions.milkledger.features.purchase.ui.list

import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentListPurchaseBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PurchaseListFragment :
    BaseFragment<FragmentListPurchaseBinding>(FragmentListPurchaseBinding::inflate) {

    override fun getMenuResId(): Int = R.menu.purchase_menu

    override fun setupListeners() = with(binding) {
        super.setupListeners()

        btnAddPurchase.setOnClickListener {
            val action =
                PurchaseListFragmentDirections.actionPurchaseFragmentToPurchaseFormFragment(null)
            findNavController().navigate(action)
        }
    }


}