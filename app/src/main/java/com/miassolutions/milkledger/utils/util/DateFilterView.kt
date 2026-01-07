package com.miassolutions.milkledger.utils.util

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.util.Pair
import androidx.fragment.app.FragmentManager
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.databinding.ViewDateFilterBinding
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toMillis
import java.time.LocalDate
import java.time.YearMonth

class DateFilterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewDateFilterBinding =
        ViewDateFilterBinding.inflate(LayoutInflater.from(context), this, true)

    // Callback Listener (StartMillis, EndMillis, Label)
    private var onDateRangeSelected: ((Long, Long, String) -> Unit)? = null

    // Fragment Manager needed for Dialog
    private var fragmentManager: FragmentManager? = null

    init {
        setupListeners()
    }

    fun setup(fm: FragmentManager, listener: (Long, Long, String) -> Unit) {
        this.fragmentManager = fm
        this.onDateRangeSelected = listener
    }

    private fun setupListeners() = with(binding) {
        chipAll.setOnClickListener { emitRange(0L, Long.MAX_VALUE, "All History") }

        chipToday.setOnClickListener {
            val today = LocalDate.now()
            val start = today.atStartOfDay().toMillis()
            val end = today.plusDays(1).atStartOfDay().toMillis() - 1
            emitRange(start, end, "Today")
        }

        chipMonth.setOnClickListener {
            val now = LocalDate.now()
            val start = now.withDayOfMonth(1).atStartOfDay().toMillis()
            val end = now.plusMonths(1).withDayOfMonth(1).atStartOfDay().toMillis() - 1
            emitRange(start, end, "This Month")
        }

        chipPrevMonth.setOnClickListener {
            val lastMonth = YearMonth.now().minusMonths(1)
            val start = lastMonth.atDay(1).atStartOfDay().toMillis()
            val end = lastMonth.atEndOfMonth().plusDays(1).atStartOfDay().toMillis() - 1
            emitRange(start, end, "Last Month")
        }

        chipCustom.setOnClickListener {
            openDateRangePicker()
        }
    }

    private fun openDateRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Date Range")
            .setTheme(com.miassolutions.datesort.R.style.ThemeOverlay_App_DatePicker)
            .setSelection(
                Pair(MaterialDatePicker.todayInUtcMilliseconds(), MaterialDatePicker.todayInUtcMilliseconds())
            )
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val start = selection.first
            val end = selection.second

            // UX Polish: Chip ka text change karein taake user ko pata chale kya select kia
            val startStr = LocalDate.ofEpochDay(start / 86400000).toDisplayDate() // Rough conversion or use proper utils
            val endStr = LocalDate.ofEpochDay(end / 86400000).toDisplayDate()

            binding.chipCustom.text = "$startStr - $endStr"
            emitRange(start, end, "Custom Range")
        }

        // Agar user cancel kare to wapis "All" ya previous selection par jayein logic yahan lag sakti hai
        picker.addOnNegativeButtonClickListener {
            binding.chipAll.isChecked = true // Revert to default
        }

        fragmentManager?.let { picker.show(it, "RangePicker") }
    }

    private fun emitRange(start: Long, end: Long, label: String) {
        // Reset Custom Chip text if not custom
        if (label != "Custom Range") {
            binding.chipCustom.text = "Custom"
        }
        onDateRangeSelected?.invoke(start, end, label)
    }
}