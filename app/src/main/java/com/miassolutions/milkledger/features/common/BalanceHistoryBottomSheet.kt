package com.miassolutions.milkledger.features.common


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

    private val viewModel: BalanceHistoryViewModel by viewModels()
    private var _binding: FragmentBalanceHistorySheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBalanceHistorySheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val accountId = arguments?.getString("accountId") ?: return
        val accountName = arguments?.getString("accountName") ?: "History"

        // 🔥 Date Argument Retrieve karein
        val dateLimit = if (arguments?.containsKey("dateLimit") == true) {
            arguments?.getLong("dateLimit")
        } else {
            null // Null ka matlab "Abhi tak ka sara data"
        }

        binding.tvSheetTitle.text = "$accountName - Balance History"

        val adapter = BalanceHistoryAdapter()
        binding.rvHistory.layoutManager = LinearLayoutManager(context)
        binding.rvHistory.adapter = adapter

        // 🔥 ViewModel ko Date pass karein
        viewModel.loadHistory(accountId, dateLimit)

        lifecycleScope.launch {
            viewModel.historyFlow.collectLatest { list ->
                adapter.submitList(list)
            }
        }

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
        // 🔥 Updated newInstance: Accepts optional dateLimit
        fun newInstance(
            accountId: String,
            accountName: String,
            dateLimit: Long? = null // Optional Parameter
        ): BalanceHistoryBottomSheet {
            val fragment = BalanceHistoryBottomSheet()
            val args = Bundle()
            args.putString("accountId", accountId)
            args.putString("accountName", accountName)

            // Agar date provided hai to add karein
            if (dateLimit != null) {
                args.putLong("dateLimit", dateLimit)
            }

            fragment.arguments = args
            return fragment
        }
    }
}