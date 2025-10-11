package com.miassolutions.milkledger.presentation.purchase

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentPurchasesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PurchaseFragment :
    BaseFragment<FragmentPurchasesBinding>(FragmentPurchasesBinding::inflate) {


    private val viewModel: PurchaseViewModel by viewModels()
    private lateinit var purchaseAdapter: PurchaseAdapter

    override fun setupViews() {
        setToolbarTitle(getString(R.string.purchases))
        showBottomNav(true)
        setupRecyclerView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeUiState()
    }

    // ----------------------------------------------------------
    // 🧩 Setup RecyclerView
    // ----------------------------------------------------------
    private fun setupRecyclerView() {
        purchaseAdapter = PurchaseAdapter(
            onSupplierClick = { supplierId ->
                viewModel.onEvent(PurchaseUiEvent.OnSupplierSelected(supplierId))
            },
            onVolumeChanged = { entryId, volume ->
                viewModel.onEvent(PurchaseUiEvent.OnVolumeChanged(entryId, volume))
            },
            onFatChanged = { entryId, fat ->
                viewModel.onEvent(PurchaseUiEvent.OnFatChanged(entryId, fat))
            },
            onLrChanged = { entryId, lr ->
                viewModel.onEvent(PurchaseUiEvent.OnLrChanged(entryId, lr))
            },
            onNotesChanged = { entryId, notes ->
                viewModel.onEvent(PurchaseUiEvent.OnNotesChanged(entryId, notes))
            },
            onPaidChanged = {entryId, paid ->
                viewModel.onEvent(PurchaseUiEvent.OnPaidChanged(entryId, paid))
            }

        )

        binding.rvPurchases.apply {
            adapter = purchaseAdapter
            itemAnimator = null
            layoutManager = LinearLayoutManager(requireContext())
            setRecyclerListener { holder ->
                holder.itemView.findFocus()?.clearFocus()
            }
            setHasFixedSize(true)
        }
    }

    // ----------------------------------------------------------
    // 👀 Observe UI State
    // ----------------------------------------------------------
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                Log.d("PurchaseFragment", "${state.purchasesForDate}")

                // Update RecyclerView list
                binding.rvPurchases.setItemViewCacheSize(state.purchasesForDate.size)
                purchaseAdapter.submitList(state.purchasesForDate)

                // Update total text
                binding.tvGrandTotalForDate.text =
                    getString(R.string.price_format, state.grandTotalForDate)

                // Update date text
                binding.tvDate.text = getString(
                    R.string.date_format,
                    state.currentDate.dayOfMonth,
                    state.currentDate.monthValue,
                    state.currentDate.year
                )

                // Handle navigation
                state.navigateToLedgerForSupplierId?.let { supplierId ->
                    navigateToLedger(supplierId)
                    viewModel.onLedgerNavigated()
                }
            }
        }
    }

    // ----------------------------------------------------------
    // 🧭 Navigate to Supplier Ledger
    // ----------------------------------------------------------
    private fun navigateToLedger(supplierId: String) {
        // TODO: Replace with actual navigation direction when you create the ledger screen
        // Example if using Navigation Component:
        // val action = PurchaseFragmentDirections.actionPurchaseFragmentToSupplierLedgerFragment(supplierId)
        // findNavController().navigate(action)
    }
}
