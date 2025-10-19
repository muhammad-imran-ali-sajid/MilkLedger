package com.miassolutions.milkledger.presentation.supplier.supplierdetail

import android.util.Log
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentCustomerDetailBinding
import com.miassolutions.milkledger.databinding.SupplierFormLayoutBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SupplierDetailFragment :
    BaseFragment<FragmentCustomerDetailBinding>(FragmentCustomerDetailBinding::inflate) {

    private val viewModel: SupplierDetailViewModel by viewModels()
    private val args by navArgs<SupplierDetailFragmentArgs>()

    override fun setupViews() {
        viewModel.onSelectedSupplierId(args.supplierId)
    }

    override fun setupObservers() {
        viewModel.uiState.collectState { state ->
            Log.d("SupplierDetailFragment", "${state.supplierDetailList}")
        }
    }
}