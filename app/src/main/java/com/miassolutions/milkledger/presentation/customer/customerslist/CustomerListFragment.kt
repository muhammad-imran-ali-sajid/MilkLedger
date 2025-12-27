package com.miassolutions.milkledger.presentation.customer.customerslist

import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.miassolutions.milkledger.core.extensions.showDeleteActionDialog
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentCustomersBinding
import com.miassolutions.milkledger.presentation.customer.model.CustomerUi
import com.miassolutions.milkledger.presentation.customer.form.CustomerFormBottomSheetFragment
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
            viewModel.onAddCustomerClick()
        }
    }

    private fun setupRecyclerView() {
        adapter = CustomerListAdapter { customer ->
            showEditDeleteDialog(customer)
            true
        }
        binding.rvCustomers.adapter = adapter
    }

    override fun setupObservers() {

        viewModel.uiState.collectState { state ->
            adapter.submitList(state.customers)
        }

        viewModel.uiEvent.collectState { event ->
            when (event) {
                CustomerUiEvent.ShowCustomerForm -> {
                    CustomerFormBottomSheetFragment
                        .newInstance(null)
                        .show(parentFragmentManager, null)
                }

                is CustomerUiEvent.ShowMessage ->
                    showToast(event.message)
            }
        }
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
        CustomerFormBottomSheetFragment
            .newInstance(customerId)
            .show(parentFragmentManager, null)
    }

    private fun confirmDeleteCustomer(customerId: String) {
        showDeleteActionDialog(message = "Be careful this will delete precious records") {
            viewModel.deleteCustomer(
                customerId
            )
        }
    }
}
