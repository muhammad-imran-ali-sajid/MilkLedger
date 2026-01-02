package com.miassolutions.milkledger.features.purchase.balancehistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.databinding.BottomsheetBalanceHistoryBinding
import com.miassolutions.milkledger.features.purchase.ui.list.PurchaseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SupplierBalanceHistoryBottomSheet : BottomSheetDialogFragment() {

    private val viewModel by viewModels<PurchaseViewModel>()
    private lateinit var adapter: BalanceHistoryAdapter

    companion object {
        const val ARG_SUPPLIER_ID = "supplier_id"
        const val ARG_SUPPLIER_NAME = "supplier_name"

        fun newInstance(supplierId: String, supplierName: String): SupplierBalanceHistoryBottomSheet {
            val args = Bundle().apply {
                putString(ARG_SUPPLIER_ID, supplierId)
                putString(ARG_SUPPLIER_NAME, supplierName)
            }

            val fragment = SupplierBalanceHistoryBottomSheet()
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


//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//
//        val supplierId = arguments?.getString(ARG_SUPPLIER_ID)
//        val supplierName = arguments?.getString(ARG_SUPPLIER_NAME)
//
//        if (supplierId.isNullOrEmpty()) {
//            return
//        }
//
//        adapter = BalanceHistoryAdapter()
//        binding.rvBalanceHistory.adapter = adapter
//
//        viewModel.setBalanceSupplierId(supplierId)
//        Log.d("TransactionHistory", supplierId)
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.balanceHistory.collect {
//                    Log.d("TransactionHistory", "$it")
//
//                    val totalBalance = it.sumOf { balanceHistory -> balanceHistory.balance }
//
//                    binding.tvBalance.text = numberFormat(totalBalance)
//                    binding.tvBalance.setTextColor(textColor(totalBalance))
//
//                    binding.tvTitle.text = "$supplierName\nBalance History"
//                    adapter.submitList(it)
//                }
//            }
//        }
//
//
//    }
}