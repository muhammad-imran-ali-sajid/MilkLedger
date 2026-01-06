package com.miassolutions.milkledger.features.milk.balancehistory

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.databinding.BottomSheetCustomerHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CustomerHistoryBottomSheet : BottomSheetDialogFragment() {

    // Arguments se Customer ID aur Name len
    private val args: CustomerHistoryBottomSheetArgs by navArgs()

    // ViewModel (Simple Repository Call k liye)
    private val viewModel: CustomerHistoryViewModel by viewModels()

    private var _binding: BottomSheetCustomerHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetCustomerHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // SAFE + CLEAN
        binding.tvCustomerName.text =
            "${args.customerName}'s History"


        val adapter = BalanceHistoryAdapter()
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter

        lifecycleScope.launch {
            viewModel.getDailyClosingBalance(args.customerId).collect { list ->
                adapter.submitList(list)

                // Optional: Agar list empty hai to empty view dikhayen
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}