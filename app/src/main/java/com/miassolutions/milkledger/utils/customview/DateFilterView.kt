package com.miassolutions.milkledger.utils.customview

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.util.Pair
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.databinding.ViewDateFilterBinding
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toMillis
import java.time.Instant
import java.time.LocalDate
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

    var currentMode = FilterMode.DAY
        private set

    var selectedDate: LocalDate = LocalDate.now()
        private set

    private var customStartMillis: Long = 0L
    private var customEndMillis: Long = 0L
    private var customRangeLabel: String = ""

    enum class FilterMode { ALL, DAY, MONTH, YEAR, CUSTOM }

    init {
        orientation = VERTICAL
        setupChipListeners()
        setupNavListeners()

        binding.tvCurrentRange.setOnClickListener {
            when (currentMode) {
                FilterMode.DAY -> openSingleDatePicker(isMonthMode = false)
                FilterMode.MONTH -> openSingleDatePicker(isMonthMode = true)
                FilterMode.CUSTOM -> openDateRangePicker()
                else -> {}
            }
        }

        binding.chipDay.isChecked = true
        refreshUI()
    }

    fun setup(fm: FragmentManager, listener: (Long, Long, String) -> Unit) {
        this.fragmentManager = fm
        this.onDateRangeSelected = listener
        emitCurrentState()
    }

    fun restoreFilterState(mode: FilterMode, date: LocalDate, customLabel: String = "") {
        this.currentMode = mode
        this.selectedDate = date
        this.customRangeLabel = customLabel

        when(mode) {
            FilterMode.ALL -> binding.chipAll.isChecked = true
            FilterMode.DAY -> binding.chipDay.isChecked = true
            FilterMode.MONTH -> binding.chipMonth.isChecked = true
            FilterMode.YEAR -> binding.chipYear.isChecked = true
            FilterMode.CUSTOM -> binding.chipCustom.isChecked = true
        }

        refreshUI()
    }

    private fun setupChipListeners() = with(binding) {
        chipAll.setOnClickListener { switchMode(FilterMode.ALL) }
        chipDay.setOnClickListener {
            if (currentMode != FilterMode.DAY) selectedDate = LocalDate.now()
            switchMode(FilterMode.DAY)
        }
        chipMonth.setOnClickListener {
            if (currentMode != FilterMode.MONTH) selectedDate = LocalDate.now()
            switchMode(FilterMode.MONTH)
        }
        chipYear.setOnClickListener {
            if (currentMode != FilterMode.YEAR) selectedDate = LocalDate.now()
            switchMode(FilterMode.YEAR)
        }
        chipCustom.setOnClickListener {
            currentMode = FilterMode.CUSTOM
            refreshUI()
            openDateRangePicker()
        }
    }

    private fun switchMode(mode: FilterMode) {
        currentMode = mode
        refreshUI()
        emitCurrentState()
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

    private fun openSingleDatePicker(isMonthMode: Boolean) {
        val title = if (isMonthMode) "Select Month" else "Select Date"
        val utcSelection = selectedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(utcSelection)
            .build()

        picker.addOnPositiveButtonClickListener { selectionMillis ->
            selectedDate = Instant.ofEpochMilli(selectionMillis)
                .atZone(ZoneId.of("UTC")) // Perfectly handles UTC mismatch
                .toLocalDate()

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
            // 🔥 FIX 1: MaterialDatePicker ke raw UTC selection ko Local Date me parse karen
            val startLocal = Instant.ofEpochMilli(selection.first).atZone(ZoneId.of("UTC")).toLocalDate()
            val endLocal = Instant.ofEpochMilli(selection.second).atZone(ZoneId.of("UTC")).toLocalDate()

            // 🔥 FIX 2: DB k saath align karne k liye wahi `toMillis()` extension use karen.
            // (endLocal me 1 din plus kr k minus 1 is liye kiya ta k raat 23:59:59 tak ka data cover ho sakay)
            customStartMillis = startLocal.toMillis()
            customEndMillis = endLocal.plusDays(1).toMillis() - 1

            customRangeLabel = "${startLocal.toCompleteDateFormat()} to ${endLocal.toCompleteDateFormat()}"

            refreshUI()
            onDateRangeSelected?.invoke(customStartMillis, customEndMillis, customRangeLabel)
        }

        picker.addOnNegativeButtonClickListener {
            binding.chipDay.isChecked = true
            switchMode(FilterMode.DAY)
        }

        fragmentManager?.let { picker.show(it, "RangePicker") }
    }

    private fun refreshUI() = with(binding) {
        val isNavigable = (currentMode == FilterMode.DAY || currentMode == FilterMode.MONTH || currentMode == FilterMode.YEAR)
        val isCustom = (currentMode == FilterMode.CUSTOM)

        layoutNavigation.isVisible = isNavigable || isCustom
        btnPrev.isVisible = isNavigable
        btnNext.isVisible = isNavigable

        tvCurrentRange.text = when (currentMode) {
            FilterMode.DAY -> selectedDate.toDisplayDate()
            FilterMode.MONTH -> selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
            FilterMode.YEAR -> selectedDate.year.toString()
            FilterMode.CUSTOM -> customRangeLabel.ifEmpty { "Select Range" }
            else -> ""
        }

        val showIcon = isNavigable || isCustom
        val endDrawable = if (showIcon) R.drawable.ic_arrow_pick else 0
        tvCurrentRange.setCompoundDrawablesWithIntrinsicBounds(0, 0, endDrawable, 0)
    }

    private fun emitCurrentState() {
        var start = 0L
        var end = Long.MAX_VALUE
        var label = "All History"

        // 🔥 FIX 3: .atStartOfDay() ko hata dia gaya hai.
        // Ab hum direct `selectedDate.toMillis()` use kar rahe hain taake filter wahi
        // timestamp banaye jo hum Database mein record save krty wqt banaty hain.
        when (currentMode) {
            FilterMode.ALL -> {}
            FilterMode.DAY -> {
                start = selectedDate.toMillis()
                end = selectedDate.plusDays(1).toMillis() - 1
                label = selectedDate.toDisplayDate()
            }
            FilterMode.MONTH -> {
                val yearMonth = YearMonth.from(selectedDate)
                start = yearMonth.atDay(1).toMillis()
                end = yearMonth.atEndOfMonth().plusDays(1).toMillis() - 1
                label = selectedDate.format(DateTimeFormatter.ofPattern("MMM yyyy"))
            }
            FilterMode.YEAR -> {
                val year = selectedDate.year
                start = LocalDate.of(year, 1, 1).toMillis()
                end = LocalDate.of(year + 1, 1, 1).toMillis() - 1
                label = year.toString()
            }
            FilterMode.CUSTOM -> {
                start = customStartMillis
                end = customEndMillis
                label = customRangeLabel
            }
        }
        onDateRangeSelected?.invoke(start, end, label)
    }

    // =========================================================================
    // 4. State Preservation Implementation (Rotation / Process Death)
    // =========================================================================

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.mode = currentMode.ordinal
        savedState.dateEpochDay = selectedDate.toEpochDay()
        savedState.customLabel = customRangeLabel
        savedState.customStart = customStartMillis
        savedState.customEnd = customEndMillis
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            currentMode = FilterMode.entries.toTypedArray()[state.mode]
            selectedDate = LocalDate.ofEpochDay(state.dateEpochDay)
            customRangeLabel = state.customLabel
            customStartMillis = state.customStart
            customEndMillis = state.customEnd

            when (currentMode) {
                FilterMode.ALL -> binding.chipAll.isChecked = true
                FilterMode.DAY -> binding.chipDay.isChecked = true
                FilterMode.MONTH -> binding.chipMonth.isChecked = true
                FilterMode.YEAR -> binding.chipYear.isChecked = true
                FilterMode.CUSTOM -> binding.chipCustom.isChecked = true
            }
            refreshUI()
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    internal class SavedState : BaseSavedState {
        var mode: Int = 0
        var dateEpochDay: Long = 0
        var customLabel: String = ""
        var customStart: Long = 0
        var customEnd: Long = 0

        constructor(superState: Parcelable?) : super(superState)

        constructor(source: Parcel) : super(source) {
            mode = source.readInt()
            dateEpochDay = source.readLong()
            customLabel = source.readString() ?: ""
            customStart = source.readLong()
            customEnd = source.readLong()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(mode)
            out.writeLong(dateEpochDay)
            out.writeString(customLabel)
            out.writeLong(customStart)
            out.writeLong(customEnd)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState = SavedState(source)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }
}