package com.miassolutions.milkledger.features.sale.saleform


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.databinding.BottomSheetCustomerSelectionBinding
import com.miassolutions.milkledger.features.account.domain.Account

class CustomerSelectionBottomSheet(
    private val customersList: List<CustomerDropDownUiModel>,
    private val onCustomerSelected: (Account) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCustomerSelectionBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CustomerSelectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCustomerSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
    }

    private fun setupRecyclerView() {
        adapter = CustomerSelectionAdapter { uiModel ->
            if (uiModel.isEntryDoneToday) {
                // User ko message dikhayen
                Toast.makeText(requireContext(), "Already Exist for today", Toast.LENGTH_SHORT).show()

                // Optional: Agar vibration deni ho to
                view?.performHapticFeedback(HapticFeedbackConstants.REJECT)

                return@CustomerSelectionAdapter // Aagy na jayen
            }


            onCustomerSelected(uiModel.account)
            dismiss()
        }
        binding.rvCustomers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCustomers.adapter = adapter
        adapter.submitList(customersList)
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                val filtered = customersList.filter {
                    it.account.name.lowercase().contains(query)
                }
                adapter.submitList(filtered)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CustomerSelectionSheet"
    }
}