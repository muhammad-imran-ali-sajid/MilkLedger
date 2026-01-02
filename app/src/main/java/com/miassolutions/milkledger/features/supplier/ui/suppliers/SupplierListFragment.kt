package com.miassolutions.milkledger.features.supplier.ui.suppliers

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentSuppliersBinding
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.showDeleteActionDialog
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
            viewModel.onEvent(SupplierListUiEvent.OnAddSupplierClick)
        }
    }

    override fun setupObservers() {

        collectFlow(viewModel.uiState) { state ->
            adapter.submitList(state.visibleSupplier) // show derived list here
        }

        collectEffect(viewModel.uiEffect) { effect ->
            when (effect) {
                SupplierListUiEffect.NavToAddSupplierForm -> {
                    val action =
                        SupplierListFragmentDirections.actionSuppliersFragmentToSupplierFormBottomSheetFragment()
                    findNavController().navigate(action)
                }

                is SupplierListUiEffect.OpenOptionDialog -> {
                    showEditDeleteDialog(effect.supplierId)
                }

                is SupplierListUiEffect.ShowMessage -> {
                    showToast(effect.message)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = SupplierListAdapter { supplierId ->
            viewModel.onEvent(SupplierListUiEvent.OnSupplierItemClick(supplierId))
            true
        }
        binding.rvSupplier.adapter = adapter
    }

    private fun showEditDeleteDialog(supplierId: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Action")
            .setItems(arrayOf("Edit", "Delete")) { dialog, which ->
                when (which) {
                    0 -> {
                        openEditSupplier(supplierId)
                    }

                    1 -> confirmDeleteSupplier(supplierId)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun openEditSupplier(supplierId: String) {
        val action =
            SupplierListFragmentDirections.actionSuppliersFragmentToSupplierFormBottomSheetFragment(
                supplierId
            )
        findNavController().navigate(action)
    }

    private fun confirmDeleteSupplier(supplierId: String) {

        showDeleteActionDialog {
            viewModel.onEvent(SupplierListUiEvent.OnDeleteSupplierClick(supplierId))
        }
    }
}

