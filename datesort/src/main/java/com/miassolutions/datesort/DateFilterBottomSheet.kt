package com.miassolutions.datesort

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.miassolutions.datesort.databinding.BottomsheetDateFilterBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

class DateFilterBottomSheet(
    private val callback: OnDateRangeSelected
) : BottomSheetDialogFragment() {

    private var _binding: BottomsheetDateFilterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DateFilterViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomsheetDateFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.uiState.collectLatest { state ->
                binding.tvRange.text = state.formattedRange
            }
        }

        binding.btnPrev.setOnClickListener { viewModel.onEvent(DateFilterUiEvent.OnPreviousClicked) }
        binding.btnNext.setOnClickListener { viewModel.onEvent(DateFilterUiEvent.OnNextClicked) }

        binding.btnCustom.setOnClickListener { showCustomPicker() }

        binding.radioGroup.setOnCheckedChangeListener { _, id ->
            val type = when (id) {
                binding.rbDay.id -> DateRangeType.DAY
                binding.rbWeek.id -> DateRangeType.WEEK
                binding.rbMonth.id -> DateRangeType.MONTH
                binding.rbYear.id -> DateRangeType.YEAR
                else -> DateRangeType.DAY
            }
            viewModel.onEvent(DateFilterUiEvent.OnTypeSelected(type))
        }

        binding.btnApply.setOnClickListener {
            val state = viewModel.uiState.value
            callback.onDateRangeSelected(state.startDate, state.endDate, state.type)
            dismiss()
        }
    }

    private fun showCustomPicker() {
        val today = LocalDate.now()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            val start = LocalDate.of(y, m + 1, d)
            DatePickerDialog(requireContext(), { _, y2, m2, d2 ->
                val end = LocalDate.of(y2, m2 + 1, d2)
                viewModel.onEvent(DateFilterUiEvent.OnCustomRangeSelected(start, end))
            }, today.year, today.monthValue - 1, today.dayOfMonth).show()
        }, today.year, today.monthValue - 1, today.dayOfMonth).show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}