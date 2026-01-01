package com.miassolutions.milkledger.core.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.miassolutions.milkledger.databinding.BottomsheetCustomDateRangeBinding
import java.time.Instant
import java.time.ZoneId

/**
 * A BottomSheetDialogFragment to allow the user to select a custom date range
 * using the Material Date Range Picker.
 */
class CustomDateRangeBottomSheet : BottomSheetDialogFragment() {

    // Define constants for the Fragment Result API
    companion object {
        const val REQUEST_KEY = "custom_date_range_request"
        const val BUNDLE_START_DATE = "start_date"
        const val BUNDLE_END_DATE = "end_date"
    }

    private var _binding: BottomsheetCustomDateRangeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetCustomDateRangeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSelectCustomRange.setOnClickListener {
            showMaterialRangePicker()
        }
    }

    /**
     * Launches the Material Date Range Picker.
     */
    private fun showMaterialRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Custom Range")
            .setTheme(R.style.ThemeOverlay_Material3_MaterialCalendar) // Use the standard M3 theme
//            .setTheme(com.google.android.material.R.style.ThemeOverlay_Material3_MaterialCalendar_Fullscreen)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val startMillis = selection.first ?: return@addOnPositiveButtonClickListener
            val endMillis = selection.second ?: return@addOnPositiveButtonClickListener

            // Convert milliseconds to LocalDate
            val start = Instant.ofEpochMilli(startMillis).atZone(ZoneId.systemDefault()).toLocalDate()
            val end = Instant.ofEpochMilli(endMillis).atZone(ZoneId.systemDefault()).toLocalDate()

            // 1. Package the results into a Bundle
            val result = Bundle().apply {
                // Since LocalDate is not parcelable/serializable, pass the date strings
                putString(BUNDLE_START_DATE, start.toString())
                putString(BUNDLE_END_DATE, end.toString())
            }

            // 2. Send the result using the Fragment Result API
            setFragmentResult(REQUEST_KEY, result)
            dismiss()
        }

        picker.show(childFragmentManager, "MaterialDatePicker")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}