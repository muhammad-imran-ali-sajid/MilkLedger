package com.miassolutions.milkledger.presentation.supplier.suppliers

import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentSuppliersBinding
import com.miassolutions.milkledger.domain.model.Supplier
import com.miassolutions.milkledger.presentation.supplier.form.SupplierFormBottomSheetFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SupplierListFragment :
    BaseFragment<FragmentSuppliersBinding>(FragmentSuppliersBinding::inflate) {

    private val viewModel by viewModels<SupplierListViewModel>()
    private lateinit var adapter: SupplierListAdapter

    override fun setupViews() {
        setToolbarTitle(getString(R.string.suppliers))
        setupRecyclerView()
    }

    override fun setupListeners() = with(binding) {
        btnAddSupplier.setOnClickListener {
            viewModel.onAddSupplierClick()
        }
    }

    override fun setupObservers() {

        viewModel.uiState.collectState { state ->
            adapter.submitList(state.suppliers)
        }

        viewModel.uiEvent.collectState { event ->
//            when (event) {
//                SupplierUiEvent.ShowSupplierForm -> {
//                    SupplierFormBottomSheetFragment
//                        .newInstance(null)
//                        .show(parentFragmentManager, null)
//                }
//
//                is SupplierUiEvent.ShowMessage ->
//                    showToast(event.message)
//            }
        }
    }

    private fun setupRecyclerView() {
        adapter = SupplierListAdapter { supplier ->
            showEditDeleteDialog(supplier)
            true
        }
        binding.rvSupplier.adapter = adapter
    }

    private fun showEditDeleteDialog(supplier: Supplier) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Action")
            .setItems(arrayOf("Edit", "Delete")) { dialog, which ->
                when (which) {
                    0 -> openEditSupplier(supplier)
                    1 -> confirmDeleteSupplier(supplier)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun openEditSupplier(supplier: Supplier) {
//        SupplierFormBottomSheetFragment
//            .newInstance(supplier)
//            .show(parentFragmentManager, null)
    }

    private fun confirmDeleteSupplier(supplier: Supplier) {
        showDialog(
            title = "Delete Supplier",
            message = "Delete ${supplier.name}?"
        ) {
            viewModel.deleteSupplier(supplier)
        }
    }
}

