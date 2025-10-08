package com.miassolutions.milkledger.presentation.suppliers

import androidx.fragment.app.viewModels
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentSuppliersBinding
import com.miassolutions.milkledger.presentation.customers.CustomerListAdapter
import com.miassolutions.milkledger.presentation.customers.CustomerListViewModel
import com.miassolutions.milkledger.presentation.forms.CustomerFormBottomSheetFragment
import com.miassolutions.milkledger.presentation.forms.SupplierFormBottomSheetFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SupplierListFragment :
    BaseFragment<FragmentSuppliersBinding>(FragmentSuppliersBinding::inflate) {

    private val viewModel by viewModels<SupplierListViewModel>()
    private lateinit var adapter: SupplierListAdapter


    override fun setupViews() {
        setToolbarTitle(getString(R.string.suppliers))
        showBottomNav(false)

        setupRecyclerView()

        viewModel.uiState.collectState { state ->
            adapter.submitList(state.suppliers)
        }

        binding.fabAddSupplier.setOnClickListener {
            SupplierFormBottomSheetFragment(
                supplier = null
            ) {
                viewModel.saveSupplier(it)

            }.show(parentFragmentManager, null)
        }

    }

    private fun setupRecyclerView() {
        adapter = SupplierListAdapter()
        binding.rvSupplier.adapter = adapter
    }


}