package com.miassolutions.milkledger.presentation.customer

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
import com.miassolutions.milkledger.core.helper.numberFormat
import com.miassolutions.milkledger.core.helper.textColor
import com.miassolutions.milkledger.databinding.BottomsheetBalanceHistoryBinding
import com.miassolutions.milkledger.presentation.customer.sales.SalesViewModel
import com.miassolutions.milkledger.presentation.supplier.BalanceHistoryAdapter
import com.miassolutions.milkledger.presentation.supplier.purchase.PurchaseViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CustomerBalanceHistoryBottomSheet : BottomSheetDialogFragment() {

    private val viewModel by viewModels<SalesViewModel>()
    private lateinit var adapter: BalanceHistoryAdapter

    companion object {
        const val ARG_CUSTOMER_ID = "customer_id"
        const val ARG_CUSTOMER_NAME = "supplier_name"

        fun newInstance(customerId: String, customerName: String): CustomerBalanceHistoryBottomSheet {
            val args = Bundle().apply {
                putString(ARG_CUSTOMER_ID, customerId)
                putString(ARG_CUSTOMER_NAME, customerName)
            }

            val fragment = CustomerBalanceHistoryBottomSheet()
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

        val customerId = arguments?.getString(ARG_CUSTOMER_ID)
        val customerName = arguments?.getString(ARG_CUSTOMER_NAME)

        if (customerId.isNullOrEmpty()) {
            return
        }

        adapter = BalanceHistoryAdapter()
        binding.rvBalanceHistory.adapter = adapter

//        viewModel.setBalanceCustomerId(customerId)
        viewModel.getBalanceHistory(customerId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.balanceHistory.collect {
                    Log.d("TransactionHistory", "$it")

                    val totalBalance = it.sumOf { balanceHistory -> balanceHistory.balance }

                    binding.tvBalance.text = numberFormat(totalBalance)
                    binding.tvBalance.setTextColor(textColor(totalBalance))

                    binding.tvTitle.text = "$customerName\nBalance History"
                    adapter.submitList(it)
                }
            }
        }


    }
}