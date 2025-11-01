package com.miassolutions.milkledger.presentation.supplier.suppliers

import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentSuppliersBinding
import com.miassolutions.milkledger.domain.model.Supplier
import com.miassolutions.milkledger.presentation.supplier.SupplierFormBottomSheetFragment
import com.miassolutions.sort_filter.DataSortBottomSheet
import com.miassolutions.sort_filter.FilterOption
import com.miassolutions.sort_filter.SortOption
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SupplierListFragment :
    BaseFragment<FragmentSuppliersBinding>(FragmentSuppliersBinding::inflate) {

    // somewhere near your SupplierListFragment
    private val supplierFilters = listOf(
        FilterOption("active", "Active"),
        FilterOption("inactive", "Inactive")
    )

    private val supplierSorts = listOf(
        SortOption("name", "Name (A–Z)", ascending = true),
        SortOption("date", "Date Added (Newest First)", ascending = false)
    )

    private val viewModel by viewModels<SupplierListViewModel>()

    private lateinit var adapter: SupplierListAdapter


    override fun setupViews() {
        setToolbarTitle(getString(R.string.suppliers))

        setupRecyclerView()

        // Listen for sort/filter results from BottomSheet
        setFragmentResultListener(DataSortBottomSheet.REQUEST_KEY) { _, bundle ->
            val filters = bundle.getParcelableArrayList<FilterOption>("filters") ?: emptyList()
            val sorts = bundle.getParcelableArrayList<SortOption>("sorts") ?: emptyList()
            viewModel.applySortAndFilter(filters, sorts)
        }


    }

    override fun setupListeners() = with(binding) {
        btnAddSupplier.setOnClickListener {
            viewModel.onAddSupplierClick()
        }
    }

    override fun setupObservers() {

        viewModel.uiState.collectState { state ->
            adapter.submitList(state.displayedSuppliers)
        }


        viewModel.uiEvent.collectState { event ->
            when (event) {
                is SupplierUiEvent.ShowMessage -> showToast(event.message)
                SupplierUiEvent.ShowSupplierForm -> {
                    SupplierFormBottomSheetFragment(
                        supplier = null
                    ) {
                        viewModel.saveSupplier(it)

                    }.show(parentFragmentManager, null)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = SupplierListAdapter { supplier ->
            showEditDeleteDialog(supplier)
            true
        }
        binding.rvSupplier.adapter = adapter
    }

    private fun showEditDeleteDialog(supplier: Supplier?) {
        val options = arrayOf("Edit", "Delete")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Action")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> handleEditSupplier(supplier)
                    1 -> confirmDeleteSupplier(supplier)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun handleEditSupplier(supplier: Supplier?) {
        val fragment = SupplierFormBottomSheetFragment(
            supplier = supplier,
            onSave = { updatedSupplier ->
                viewModel.saveSupplier(updatedSupplier)
            }
        )
        fragment.show(parentFragmentManager, null)
    }

    private fun confirmDeleteSupplier(supplier: Supplier?) {
        if (supplier == null) return

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Supplier")
            .setMessage("This will erase all records. Are you sure you want to delete ${supplier.name}?")
            .setPositiveButton("Delete") { dialog, _ ->
                viewModel.deleteSupplier(supplier)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


}