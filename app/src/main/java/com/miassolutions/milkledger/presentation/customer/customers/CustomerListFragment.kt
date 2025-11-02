package com.miassolutions.milkledger.presentation.customer.customers

import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentCustomersBinding
import com.miassolutions.milkledger.domain.model.Customer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomerListFragment :
    BaseFragment<FragmentCustomersBinding>(FragmentCustomersBinding::inflate) {

    private val viewModel by viewModels<CustomerListViewModel>()
    private lateinit var adapter: CustomerListAdapter


    override fun setupViews() {
        setToolbarTitle(getString(R.string.customers))
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
            adapter.submitList(state.customers.toMutableList())
        }

        viewModel.uiEvent.collectState { event ->
            when (event) {
                CustomerUiEvent.ShowCustomerForm -> {
                    CustomerFormBottomSheetFragment(
                        customer = null,
                        currentCustomers = adapter.currentList
                    ) {
                        viewModel.saveCustomer(it)

                    }.show(parentFragmentManager, null)
                }

                is CustomerUiEvent.ShowMessage -> showToast(event.message)
            }
        }
    }

    private fun showEditDeleteDialog(customer: Customer?) {
        val options = arrayOf("Edit", "Delete")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Action")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> handleEditCustomer(customer)
                    1 -> confirmDeleteCustomer(customer)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun handleEditCustomer(customer: Customer?) {
        val currentCustomers = adapter.currentList
        val fragment = CustomerFormBottomSheetFragment(
            customer = customer,
            currentCustomers = currentCustomers,
            onSave = { updatedCustomer ->
                viewModel.saveCustomer(updatedCustomer)
            }
        )
        fragment.show(parentFragmentManager, null)
    }

    private fun confirmDeleteCustomer(customer: Customer?) {
        if (customer == null) return

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Customer")
            .setMessage("This will erase all records. Are you sure you want to delete ${customer.name}?")
            .setPositiveButton("Delete") { dialog, _ ->
                viewModel.deleteCustomer(customer)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


}