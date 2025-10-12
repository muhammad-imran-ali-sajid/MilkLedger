package com.miassolutions.milkledger.presentation.customers

import androidx.fragment.app.viewModels
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentCustomersBinding
import com.miassolutions.milkledger.presentation.forms.CustomerFormBottomSheetFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomerListFragment :
    BaseFragment<FragmentCustomersBinding>(FragmentCustomersBinding::inflate) {

    private val viewModel by viewModels<CustomerListViewModel>()
    private lateinit var adapter: CustomerListAdapter


    override fun setupViews() {
        setToolbarTitle(getString(R.string.customers))

        setupRecyclerView()

        viewModel.uiState.collectState { state ->
            adapter.submitList(state.customers)
        }


        binding.fabAddCustomer.setOnClickListener {
            CustomerFormBottomSheetFragment(
                customer = null
            ) {
                viewModel.saveCustomer(it)

            }.show(parentFragmentManager, null)
        }

    }


    private fun setupRecyclerView() {

        adapter = CustomerListAdapter { showToast("$it is clicked") }
        binding.rvCustomers.adapter = adapter
    }


}