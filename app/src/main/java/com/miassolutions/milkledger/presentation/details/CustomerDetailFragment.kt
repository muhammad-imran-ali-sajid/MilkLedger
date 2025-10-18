package com.miassolutions.milkledger.presentation.details

import androidx.fragment.app.viewModels
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentCustomerDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomerDetailFragment :
    BaseFragment<FragmentCustomerDetailBinding>(FragmentCustomerDetailBinding::inflate) {


    private val viewModel by viewModels<CustomerDetailViewModel>()
    private lateinit var adapter: CustomerDetailListAdapter
    private val args: CustomerDetailFragmentArgs by navArgs<CustomerDetailFragmentArgs>()


    override fun setupViews() {

        viewModel.onSelectedCustomerId(args.customerId)
        setupRecyclerView()

    }

    private fun setupRecyclerView() {
        adapter = CustomerDetailListAdapter()
        binding.rvCustomerDetail.adapter = adapter
    }

    override fun setupObservers() {
        viewModel.uiState.collectState { state ->

            adapter.submitList(state.customerDetailList)

            setToolbarTitle(state.customerName)
        }
    }

}