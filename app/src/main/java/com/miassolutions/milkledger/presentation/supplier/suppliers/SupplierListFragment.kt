package com.miassolutions.milkledger.presentation.supplier.suppliers

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.helper.recyclerviewhelper.DragDropReorderHelper
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentSuppliersBinding
import com.miassolutions.milkledger.domain.model.Supplier
import com.miassolutions.milkledger.presentation.supplier.SupplierFormBottomSheetFragment
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
            adapter.submitList(state.displayedSuppliers.toMutableList()) // Make list mutable for reordering
        }

        viewModel.uiEvent.collectState { event ->
            when (event) {
                is SupplierUiEvent.ShowMessage -> showToast(event.message)
                SupplierUiEvent.ShowSupplierForm -> {
                    SupplierFormBottomSheetFragment(
                        supplier = null
                    ) { newSupplier ->
                        viewModel.saveSupplier(newSupplier)
                    }.show(parentFragmentManager, null)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = SupplierListAdapter(
            onItemDelete = { viewModel.deleteSupplier(it) }
        )
        binding.rvSupplier.adapter = adapter

        val dragDropHelper = DragDropReorderHelper(
            adapter,
            onMoveCompleted = { newList ->
                viewModel.saveNewOrder(newList)
            },
            enableSwipe = true,
            swipeDirs = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        )
        dragDropHelper.createTouchHelper().attachToRecyclerView(binding.rvSupplier)

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
