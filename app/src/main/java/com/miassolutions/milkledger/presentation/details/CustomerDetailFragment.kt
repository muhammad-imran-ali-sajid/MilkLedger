package com.miassolutions.milkledger.presentation.details

import androidx.fragment.app.viewModels
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentCustomerDetailBinding

class CustomerDetailFragment :
    BaseFragment<FragmentCustomerDetailBinding>(FragmentCustomerDetailBinding::inflate) {


    private val viewModel by viewModels<CustomerDetailViewModel>()
    private lateinit var adapter: CustomerDetailListAdapter


    override fun setupViews() {

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.rvCustomerDetail.adapter = adapter
    }

    override fun setupObservers() {
        viewModel.uiState.collectState { state ->

            adapter.submitList(state.customerDetailList)

            setToolbarTitle(state.customerName)
        }
    }

}