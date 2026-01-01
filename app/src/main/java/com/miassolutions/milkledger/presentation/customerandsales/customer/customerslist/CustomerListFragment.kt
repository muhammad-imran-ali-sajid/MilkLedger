package com.miassolutions.milkledger.presentation.customerandsales.customer.customerslist

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.showDeleteActionDialog
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentCustomersBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomerListFragment :
    BaseFragment<FragmentCustomersBinding>(FragmentCustomersBinding::inflate) {

    private val viewModel by viewModels<CustomerListViewModel>()
    private lateinit var adapter: CustomerListAdapter

    override fun setupViews() {
        setToolbarTitle("Customers")
        setupRecyclerView()
    }

    override fun setupListeners() = with(binding) {
        btnAddCustomer.setOnClickListener {
            viewModel.onEvent(CustomerUiEvent.OnAddCustomerClick)
        }
    }

    private fun setupRecyclerView() {
        adapter = CustomerListAdapter { customerId ->
            viewModel.onEvent(CustomerUiEvent.OnCustomerItemClicked(customerId))
            true
        }
        binding.rvCustomers.adapter = adapter
    }

    override fun setupObservers() {

        collectFlow(viewModel.uiState) { state ->
            adapter.submitList(state.visibleCustomers)
        }

        collectEffect(viewModel.uiEffect) { effect ->
            when (effect) {
                CustomerUiEffect.NavigateToAddCustomer -> {
                    openAddCustomerForm()
                }
                is CustomerUiEffect.OpenOptionDialog -> {
                    showEditDeleteDialog(effect.customerId)
                }

                is CustomerUiEffect.ShowMessage -> showToast(effect.message)
            }
        }
    }

    private fun openAddCustomerForm() {
        val action =
            CustomerListFragmentDirections.actionCustomersFragmentToCustomerFormBottomSheetFragment(
                null
            )
        findNavController().navigate(action)
    }

    private fun showEditDeleteDialog(customerId: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Action")
            .setItems(arrayOf("Edit", "Delete")) { dialog, which ->
                when (which) {
                    0 -> openEditCustomer(customerId)
                    1 -> confirmDeleteCustomer(customerId)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun openEditCustomer(customerId: String) {
        val action =
            CustomerListFragmentDirections.actionCustomersFragmentToCustomerFormBottomSheetFragment(
                customerId
            )
        findNavController().navigate(action)
    }

    private fun confirmDeleteCustomer(customerId: String) {
        showDeleteActionDialog(message = "Be careful this will delete precious records") {
            viewModel.onEvent(CustomerUiEvent.OnDeleteCustomer(customerId))
        }
    }
}
