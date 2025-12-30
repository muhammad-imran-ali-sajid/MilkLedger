package com.miassolutions.milkledger.presentation.customerandsales.sales.saleslist

import android.os.Bundle
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
import com.miassolutions.milkledger.presentation.supplier.balancehistory.BalanceHistory
import com.miassolutions.milkledger.presentation.supplier.balancehistory.BalanceHistoryAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CustomerBalanceHistoryBottomSheet : BottomSheetDialogFragment() {

    private val viewModel by viewModels<SalesViewModel>()
    private lateinit var adapter: BalanceHistoryAdapter

    private var onSelected: ((BalanceHistory) -> Unit)? = null

    fun setOnSelectedListener(listener: (BalanceHistory) -> Unit) {
        onSelected = listener
    }

    companion object {
        private const val ARG_CUSTOMER_ID = "customer_id"
        private const val ARG_CUSTOMER_NAME = "customer_name"

        fun newInstance(customerId: String, customerName: String): CustomerBalanceHistoryBottomSheet {
            return CustomerBalanceHistoryBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_CUSTOMER_ID, customerId)
                    putString(ARG_CUSTOMER_NAME, customerName)
                }
            }
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

        if (customerId.isNullOrEmpty()) return

        adapter = BalanceHistoryAdapter { item ->
            onSelected?.invoke(item)
            dismiss()
        }

        binding.rvBalanceHistory.adapter = adapter

//        viewModel.loadBalanceHistory(customerId)
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.balanceHistory.collect { list ->
//
//                    val totalBalance = list.sumOf { it.balance }
//
//                    binding.tvBalance.text = numberFormat(totalBalance)
//                    binding.tvBalance.setTextColor(textColor(totalBalance))
//
//                    binding.tvTitle.text = "$customerName\nBalance History"
//
//                    adapter.submitList(list)
//                }
//            }
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}