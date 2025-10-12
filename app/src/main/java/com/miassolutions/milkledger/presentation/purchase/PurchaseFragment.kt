package com.miassolutions.milkledger.presentation.purchase

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.managers.PasswordManager
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentPurchasesBinding
import com.miassolutions.milkledger.presentation.misc.EnterPasswordDialogFragment
import com.miassolutions.milkledger.presentation.purchase.dialogs.SetPasswordDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@AndroidEntryPoint
class PurchaseFragment :
    BaseFragment<FragmentPurchasesBinding>(FragmentPurchasesBinding::inflate) {

    private var isEditable = true // keep track of edit mode
    private lateinit var passwordManager: PasswordManager

    private val viewModel: PurchaseViewModel by viewModels()
    private lateinit var purchaseAdapter: PurchaseAdapter

    override fun setupViews() {
        setToolbarTitle(getString(R.string.purchases))
        passwordManager = PasswordManager(requireContext())
        setupRecyclerView()

        binding.btnToggleEdit.setOnClickListener {
            if (!isEditable) {
                if (!passwordManager.isPasswordSet()) {
                    // Show set password dialog
                    SetPasswordDialogFragment(passwordManager) {
                        enableEditMode()
                    }.show(childFragmentManager, "setPassword")
                } else {
                    // Show enter password dialog with forgot option
                    EnterPasswordDialogFragment(passwordManager, {
                        enableEditMode()
                    }, {
                        // Forgot password clicked - clear saved password and force reset
                        passwordManager.clearPassword()
                        Toast.makeText(
                            requireContext(),
                            "Password reset. Please set a new password.",
                            Toast.LENGTH_LONG
                        ).show()
                        SetPasswordDialogFragment(passwordManager) {
                            enableEditMode()
                        }.show(childFragmentManager, "setPassword")
                    }).show(childFragmentManager, "enterPassword")
                }
            } else {
                disableEditMode()
            }
        }

    }

    private fun enableEditMode() {
        isEditable = true
        purchaseAdapter.isEditable = true
        Toast.makeText(requireContext(), "Edit mode enabled", Toast.LENGTH_SHORT).show()
    }

    private fun disableEditMode() {
        isEditable = false
        purchaseAdapter.isEditable = false
        Toast.makeText(requireContext(), "Edit mode disabled", Toast.LENGTH_SHORT).show()
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
            onPaidChanged = { entryId, paid ->
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
                    getString(R.string.rs, state.grandTotalForDate.roundToInt())

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
