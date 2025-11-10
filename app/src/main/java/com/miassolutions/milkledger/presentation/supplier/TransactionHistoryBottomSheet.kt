package com.miassolutions.milkledger.presentation.supplier

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.databinding.BottomsheetBalanceHistoryBinding

class TransactionHistoryBottomSheet : BottomSheetDialogFragment() {

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
        val list = List<BalanceHistory>(20) { BalanceHistory("Date $it", "Balance $it") }
        val adapter = BalanceHistoryAdapter(list)
        binding.rvBalanceHistory.adapter = adapter
    }
}