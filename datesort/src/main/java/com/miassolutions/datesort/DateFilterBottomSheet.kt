package com.miassolutions.datesort

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import androidx.lifecycle.lifecycleScope
import com.miassolutions.datesort.databinding.BottomsheetDateFilterBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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

        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.tvRange.text = state.formattedRange
            }
        }

        // Radio group handling
        binding.radioGroup.setOnCheckedChangeListener { _, id ->
            val type = when (id) {
                binding.rbDay.id -> DateRangeType.DAY
                binding.rbMonth.id -> DateRangeType.MONTH
                binding.rbYear.id -> DateRangeType.YEAR
                else -> DateRangeType.DAY
            }
            viewModel.onEvent(DateFilterUiEvent.OnTypeSelected(type))
        }

        binding.btnPrev.setOnClickListener { viewModel.onEvent(DateFilterUiEvent.OnPreviousClicked) }
        binding.btnNext.setOnClickListener { viewModel.onEvent(DateFilterUiEvent.OnNextClicked) }

        binding.btnCustom.setOnClickListener { showMaterialRangePicker() }

        binding.btnApply.setOnClickListener {
            val state = viewModel.uiState.value
            callback.onDateRangeSelected(state.startDate, state.endDate, state.type)
            dismiss()
        }
    }

    private fun showMaterialRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Custom Range")
            .setTheme(R.style.ThemeOverlay_App_DatePicker)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val startMillis = selection.first ?: return@addOnPositiveButtonClickListener
            val endMillis = selection.second ?: return@addOnPositiveButtonClickListener

            val start = Instant.ofEpochMilli(startMillis).atZone(ZoneId.systemDefault()).toLocalDate()
            val end = Instant.ofEpochMilli(endMillis).atZone(ZoneId.systemDefault()).toLocalDate()

            viewModel.onEvent(DateFilterUiEvent.OnCustomRangeSelected(start, end))
        }

        picker.show(childFragmentManager, "MaterialDatePicker")
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
