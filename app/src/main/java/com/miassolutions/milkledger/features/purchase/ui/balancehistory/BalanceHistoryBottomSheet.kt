package com.miassolutions.milkledger.features.purchase.ui.balancehistory


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.databinding.FragmentBalanceHistorySheetBinding
import com.miassolutions.milkledger.utils.extensions.setBalanceWithColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BalanceHistoryBottomSheet : BottomSheetDialogFragment() {

    // Is fragment ka ViewModel (Neeche defined hai)
    private val viewModel: BalanceHistoryViewModel by viewModels()

    private var _binding: FragmentBalanceHistorySheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBalanceHistorySheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Argument se Account ID lein
        val accountId = arguments?.getString("accountId") ?: return
        val accountName = arguments?.getString("accountName") ?: "History"

        // Setup UI
        binding.tvSheetTitle.text = "$accountName - Balance"

        val adapter = BalanceHistoryAdapter()
        binding.rvHistory.layoutManager = LinearLayoutManager(context)
        binding.rvHistory.adapter = adapter

        // Fetch Data
        viewModel.loadHistory(accountId)

        // Observe List
        lifecycleScope.launch {
            viewModel.historyFlow.collectLatest { list ->
                adapter.submitList(list)
            }
        }

        // Observe Current Balance
        lifecycleScope.launch {
            viewModel.balanceFlow.collectLatest { balance ->
                binding.tvCurrentBalance.setBalanceWithColor(balance)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(accountId: String, accountName: String): BalanceHistoryBottomSheet {
            val fragment = BalanceHistoryBottomSheet()
            val args = Bundle()
            args.putString("accountId", accountId)
            args.putString("accountName", accountName)
            fragment.arguments = args
            return fragment
        }
    }
}