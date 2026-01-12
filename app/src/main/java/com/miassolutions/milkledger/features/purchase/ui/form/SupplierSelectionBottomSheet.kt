package com.miassolutions.milkledger.features.purchase.ui.form

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.databinding.BottomSheetSupplierSelectionBinding
import com.miassolutions.milkledger.features.account.domain.Account
import com.miassolutions.milkledger.features.purchase.model.SupplierDropDownUiModel
import androidx.recyclerview.widget.LinearLayoutManager

class SupplierSelectionBottomSheet(
    private val suppliersList: List<SupplierDropDownUiModel>,
    private val onSupplierSelected: (Account) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSupplierSelectionBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SupplierSelectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSupplierSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
    }

    private fun setupRecyclerView() {
        adapter = SupplierSelectionAdapter { uiModel ->
            onSupplierSelected(uiModel.account)
            dismiss()
        }
        binding.rvSuppliers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSuppliers.adapter = adapter
        adapter.submitList(suppliersList)
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                val filtered = suppliersList.filter {
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
        const val TAG = "SupplierSelectionSheet"
    }
}