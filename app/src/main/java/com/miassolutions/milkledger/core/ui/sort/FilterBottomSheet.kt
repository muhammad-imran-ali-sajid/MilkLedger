package com.miassolutions.milkledger.core.ui.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.core.ui.sort.FilterOptions
import com.miassolutions.milkledger.core.ui.sort.FilterSharedViewModel
import com.miassolutions.milkledger.core.ui.sort.SortOrder
import com.miassolutions.milkledger.databinding.BottomsheetFilterBinding

class FilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomsheetFilterBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: FilterSharedViewModel by activityViewModels()

    private lateinit var adapter: CategoryAdapter
    private var selectedCategory: String? = null
    private var selectedSortOrder = SortOrder.NONE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupCategoryList()
        setupSortChips()
        setupButtons()
    }

    private fun setupCategoryList() {
        adapter = CategoryAdapter { category ->
            selectedCategory = category
        }
        binding.rvCategories.adapter = adapter
        adapter.submitList(listOf("Date", "Net Milk"))
    }

    private fun setupSortChips() {
        binding.sortChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedSortOrder = when {
                checkedIds.contains(binding.chipAscending.id) -> SortOrder.ASCENDING
                checkedIds.contains(binding.chipDescending.id) -> SortOrder.DESCENDING
                else -> SortOrder.NONE
            }
        }
    }

    private fun setupButtons() {
        binding.btnApply.setOnClickListener {
            sharedViewModel.applyFilter(
                FilterOptions(
                    sortOrder = selectedSortOrder,
                    category = selectedCategory
                )
            )
            dismiss()
        }

        binding.btnReset.setOnClickListener {
            selectedSortOrder = SortOrder.NONE
            selectedCategory = null
            binding.sortChipGroup.clearCheck()
            adapter.setSelected(null)
            sharedViewModel.reset()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
