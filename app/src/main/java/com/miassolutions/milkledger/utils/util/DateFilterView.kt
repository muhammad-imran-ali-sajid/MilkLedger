package com.miassolutions.milkledger.utils.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.util.Pair
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.databinding.ViewDateFilterBinding
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toMillis
import java.time.Instant
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DateFilterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding: ViewDateFilterBinding =
        ViewDateFilterBinding.inflate(LayoutInflater.from(context), this, true)

    private var onDateRangeSelected: ((Long, Long, String) -> Unit)? = null
    private var fragmentManager: FragmentManager? = null

    // State Management
    private var currentMode = FilterMode.ALL
    private var selectedDate: LocalDate = LocalDate.now()

    enum class FilterMode { ALL, DAY, MONTH, YEAR, CUSTOM }

    init {
        setupChipListeners()
        setupNavListeners()

        // 🔥 Click Listener for Date/Month Text
        binding.tvCurrentRange.setOnClickListener {
            if (currentMode == FilterMode.DAY) {
                openSingleDatePicker(isMonthMode = false)
            } else if (currentMode == FilterMode.MONTH) {
                openSingleDatePicker(isMonthMode = true)
            }
        }

        refreshUI()
    }

    fun setup(fm: FragmentManager, listener: (Long, Long, String) -> Unit) {
        this.fragmentManager = fm
        this.onDateRangeSelected = listener
        emitCurrentState() // Initial Call (Load All)
    }

    private fun setupChipListeners() = with(binding) {
        chipAll.setOnClickListener {
            currentMode = FilterMode.ALL
            refreshUI()
            emitCurrentState()
        }

        chipDay.setOnClickListener {
            currentMode = FilterMode.DAY
            selectedDate = LocalDate.now()
            refreshUI()
            emitCurrentState()
        }

        chipMonth.setOnClickListener {
            currentMode = FilterMode.MONTH
            selectedDate = LocalDate.now()
            refreshUI()
            emitCurrentState()
        }

        chipYear.setOnClickListener {
            currentMode = FilterMode.YEAR
            selectedDate = LocalDate.now()
            refreshUI()
            emitCurrentState()
        }

        chipCustom.setOnClickListener {
            currentMode = FilterMode.CUSTOM
            refreshUI()
            openDateRangePicker()
        }
    }

    private fun setupNavListeners() = with(binding) {
        btnPrev.setOnClickListener { moveDate(-1) }
        btnNext.setOnClickListener { moveDate(1) }
    }

    private fun moveDate(amount: Long) {
        selectedDate = when (currentMode) {
            FilterMode.DAY -> selectedDate.plusDays(amount)
            FilterMode.MONTH -> selectedDate.plusMonths(amount)
            FilterMode.YEAR -> selectedDate.plusYears(amount)
            else -> selectedDate
        }
        refreshUI()
        emitCurrentState()
    }

    // ✅ Helper to Pick Date or Month
    private fun openSingleDatePicker(isMonthMode: Boolean) {
        val title = if (isMonthMode) "Select Month (Pick any date in month)" else "Select Date"

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(
                selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            .build()

        picker.addOnPositiveButtonClickListener { selectionMillis ->
            val newDate = Instant.ofEpochMilli(selectionMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            // Update State
            selectedDate = newDate
            refreshUI()
            emitCurrentState()
        }

        fragmentManager?.let { picker.show(it, "SingleDatePicker") }
    }

    private fun openDateRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Custom Range")
            .setSelection(
                Pair(
                    MaterialDatePicker.todayInUtcMilliseconds(),
                    MaterialDatePicker.todayInUtcMilliseconds()
                )
            )
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val start = selection.first
            val end = selection.second

            val startStr = LocalDate.ofEpochDay(start / 86400000).toDisplayDate()
            val endStr = LocalDate.ofEpochDay(end / 86400000).toDisplayDate()

            binding.chipCustom.text = "Custom"
            onDateRangeSelected?.invoke(start, end, "$startStr - $endStr")
        }

        picker.addOnNegativeButtonClickListener {
            binding.chipAll.isChecked = true
            currentMode = FilterMode.ALL
            refreshUI()
        }

        fragmentManager?.let { picker.show(it, "RangePicker") }
    }

    private fun refreshUI() = with(binding) {
        // Show Navigation only for Day or Month
        layoutNavigation.isVisible =
            (currentMode == FilterMode.DAY || currentMode == FilterMode.MONTH || currentMode == FilterMode.YEAR)

        // Text Update Logic
        tvCurrentRange.text = when (currentMode) {
            FilterMode.DAY -> selectedDate.toDisplayDate() // "08 Jan 2026"
            FilterMode.MONTH -> selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")) // "January 2026"
            FilterMode.YEAR -> selectedDate.year.toString()
            else -> ""
        }

        // Dropdown Icon Visibility
        val showIcon = (currentMode == FilterMode.DAY || currentMode == FilterMode.MONTH)
        val endDrawable = if (showIcon) R.drawable.ic_arrow_pick else 0
        tvCurrentRange.setCompoundDrawablesWithIntrinsicBounds(0, 0, endDrawable, 0)
    }

    private fun emitCurrentState() {
        var start = 0L
        var end = Long.MAX_VALUE
        var label = "All History"

        when (currentMode) {
            FilterMode.ALL -> {} // Default 0 to Max

            FilterMode.DAY -> {
                start = selectedDate.atStartOfDay().toMillis()
                end = selectedDate.plusDays(1).atStartOfDay().toMillis() - 1
                label = selectedDate.toDisplayDate()
            }

            FilterMode.MONTH -> {
                val yearMonth = YearMonth.from(selectedDate)
                // Month start: 1st day 00:00
                start = yearMonth.atDay(1).atStartOfDay().toMillis()
                // Month end: Last day 23:59:59
                end = yearMonth.atEndOfMonth().plusDays(1).atStartOfDay().toMillis() - 1
                label = selectedDate.format(DateTimeFormatter.ofPattern("MMM yyyy"))
            }

            FilterMode.YEAR -> {
                val year = selectedDate.year

                start = LocalDate.of(year, 1, 1)
                    .atStartOfDay()
                    .toMillis()

                end = LocalDate.of(year + 1, 1, 1)
                    .atStartOfDay()
                    .toMillis() - 1

                label = year.toString()
            }

            FilterMode.CUSTOM -> return // Handled by Picker Callback

        }

        onDateRangeSelected?.invoke(start, end, label)
    }
}