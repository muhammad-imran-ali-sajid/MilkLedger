package com.miassolutions.sort_filter

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.sort_filter.databinding.BottomsheetDataSortBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DataSortBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomsheetDataSortBinding? = null
    private val binding get() = _binding!!

    private val filters by lazy { requireArguments().getParcelableArrayList<FilterOption>("filters") ?: emptyList() }
    private val sorts by lazy { requireArguments().getParcelableArrayList<SortOption>("sorts") ?: emptyList() }

    private val viewModel by viewModels<DataSortViewModel> {
        DataSortViewModelFactory(filters, sorts)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomsheetDataSortBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val filterAdapter = DataSortAdapter(filters) { id -> viewModel.onEvent(DataSortUiEvent.FilterChanged(id)) }
        val sortAdapter = DataSortAdapter(sorts) { id -> viewModel.onEvent(DataSortUiEvent.SortChanged(id)) }

        binding.rvFilter.adapter = filterAdapter
        binding.rvFilter.layoutManager = LinearLayoutManager(requireContext())

        binding.rvSort.adapter = sortAdapter
        binding.rvSort.layoutManager = LinearLayoutManager(requireContext())

        binding.btnApply.setOnClickListener {
            viewModel.onEvent(DataSortUiEvent.ApplyClicked)
            val state = viewModel.uiState.value
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply {
                    putParcelableArrayList("filters", ArrayList(state.filterOptions))
                    putParcelableArrayList("sorts", ArrayList(state.sortOptions))
                }
            )
            dismiss()
        }

        binding.btnReset.setOnClickListener { viewModel.onEvent(DataSortUiEvent.ResetClicked) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                filterAdapter.submitList(state.filterOptions)
                sortAdapter.submitList(state.sortOptions)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val REQUEST_KEY = "data_sort_request"

        fun newInstance(filters: List<FilterOption>, sorts: List<SortOption>) =
            DataSortBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("filters", ArrayList(filters))
                    putParcelableArrayList("sorts", ArrayList(sorts))
                }
            }
    }
}