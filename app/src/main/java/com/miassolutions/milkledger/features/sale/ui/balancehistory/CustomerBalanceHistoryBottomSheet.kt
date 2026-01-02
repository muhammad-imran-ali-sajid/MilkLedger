package com.miassolutions.milkledger.features.sale.ui.balancehistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.databinding.BottomsheetBalanceHistoryBinding
import com.miassolutions.milkledger.features.sale.domain.model.BalanceHistoryItem

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomerBalanceHistoryBottomSheet :
    BottomSheetDialogFragment() {

    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_HISTORY = "history"

        fun newInstance(
            customerName: String,
            history: List<BalanceHistoryItem>
        ) = CustomerBalanceHistoryBottomSheet().apply {
            arguments = bundleOf(
                ARG_NAME to customerName,
                ARG_HISTORY to ArrayList(history)
            )
        }
    }

    private lateinit var binding: BottomsheetBalanceHistoryBinding
    private val adapter = BalanceHistoryAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomsheetBalanceHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = requireArguments().getString(ARG_NAME)!!
        val history =
            requireArguments().getParcelableArrayList<BalanceHistoryItem>(ARG_HISTORY)!!

        binding.tvCustomerName.text = name
        binding.rvHistory.adapter = adapter

        adapter.submitList(history)
    }
}
