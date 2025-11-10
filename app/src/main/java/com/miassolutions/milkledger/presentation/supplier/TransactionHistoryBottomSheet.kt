package com.miassolutions.milkledger.presentation.supplier

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.databinding.BottomsheetBalanceHistoryBinding
import com.miassolutions.milkledger.presentation.supplier.purchase.PurchaseViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransactionHistoryBottomSheet : BottomSheetDialogFragment() {

    private val viewModel by viewModels<PurchaseViewModel>()
    private lateinit var adapter: BalanceHistoryAdapter

    companion object {
        const val ARG_SUPPLIER_ID = "supplier_id"

        fun newInstance(supplierId: String): TransactionHistoryBottomSheet {
            val args = Bundle().apply {
                putString(ARG_SUPPLIER_ID, supplierId)
            }

            val fragment = TransactionHistoryBottomSheet()
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: BottomsheetBalanceHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetBalanceHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val supplierId = arguments?.getString(ARG_SUPPLIER_ID)

        if (supplierId.isNullOrEmpty()) {
            return
        }

        adapter = BalanceHistoryAdapter()
        binding.rvBalanceHistory.adapter = adapter

        viewModel.setBalanceSupplierId(supplierId)
        Log.d("TransactionHistory", supplierId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.balanceHistory.collect {
                    Log.d("TransactionHistory", "$it")
                    adapter.submitList(it)
                }
            }
        }

    }
}