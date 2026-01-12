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

    // State Management
    private var currentMode = FilterMode.ALL
    private var selectedDate: LocalDate = LocalDate.now()
    private var customRangeLabel: String = "" // Custom Range Text store karne k liye

    enum class FilterMode { ALL, DAY, MONTH, YEAR, CUSTOM }

    init {
        setupChipListeners()
        setupNavListeners()

        // Click Listener for Date/Range Text
        binding.tvCurrentRange.setOnClickListener {
            when (currentMode) {
                FilterMode.DAY -> openSingleDatePicker(isMonthMode = false)
                FilterMode.MONTH -> openSingleDatePicker(isMonthMode = true)
                FilterMode.CUSTOM -> openDateRangePicker() // Custom pr click krne se dobara picker khule
                else -> {}
            }
        }

        refreshUI()
    }

    fun setup(fm: FragmentManager, listener: (Long, Long, String) -> Unit) {
        this.fragmentManager = fm
        this.onDateRangeSelected = listener
        // Initial state emit na karein agar aap chahte hain screen load hote hi default "All" rahe
         emitCurrentState()
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

    private fun openSingleDatePicker(isMonthMode: Boolean) {
        val title = if (isMonthMode) "Select Month" else "Select Date"

        // 1. Fix Selection: Local Date ko UTC Millis me convert karen
        // Agar hum systemDefault() use karenge to ye peeche wali date pick kar lega
        val utcSelection = selectedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(utcSelection) // Updated
            .build()

        picker.addOnPositiveButtonClickListener { selectionMillis ->
            // 2. Fix Result: UTC Millis ko wapis Date me layen (UTC Zone use karke)
            // SystemDefault use karne se date shift ho jati hai
            selectedDate = Instant.ofEpochMilli(selectionMillis)
                .atZone(ZoneId.of("UTC")) // IMPORTANT: Use UTC here
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
            val startMillis = selection.first
            val endMillis = selection.second

            // Display text update
            val startLocal = Instant.ofEpochMilli(startMillis).atZone(ZoneId.systemDefault()).toLocalDate()
            val endLocal = Instant.ofEpochMilli(endMillis).atZone(ZoneId.systemDefault()).toLocalDate()

            customRangeLabel = "${startLocal.toCompleteDateFormat()} to ${endLocal.toCompleteDateFormat()}"

            // UI Refresh taake text show ho jaye
            refreshUI()

            // Callback trigger
            onDateRangeSelected?.invoke(startMillis, endMillis, customRangeLabel)
        }

        picker.addOnNegativeButtonClickListener {
            // Cancel pr wapis All pr chale jao ya jo logic apko chahiye
            binding.chipAll.isChecked = true
            currentMode = FilterMode.ALL
            refreshUI()
            emitCurrentState()
        }

        fragmentManager?.let { picker.show(it, "RangePicker") }
    }

    private fun refreshUI() = with(binding) {
        val isNavigable = (currentMode == FilterMode.DAY || currentMode == FilterMode.MONTH || currentMode == FilterMode.YEAR)
        val isCustom = (currentMode == FilterMode.CUSTOM)

        // Navigation Layout visible agar navigable ho YA custom ho
        layoutNavigation.isVisible = isNavigable || isCustom

        // Prev/Next buttons sirf tab visible jab navigation allow ho (Day/Month/Year)
        btnPrev.isVisible = isNavigable
        btnNext.isVisible = isNavigable

        // Text Update
        tvCurrentRange.text = when (currentMode) {
            FilterMode.DAY -> selectedDate.toDisplayDate()
            FilterMode.MONTH -> selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
            FilterMode.YEAR -> selectedDate.year.toString()
            FilterMode.CUSTOM -> customRangeLabel // Saved text show karein
            else -> ""
        }

        // Icon Logic (Dropdown icon agar text clickable hai)
        val showIcon = isNavigable || isCustom
        val endDrawable = if (showIcon) R.drawable.ic_arrow_down else 0 // Ensure drawable exists
        tvCurrentRange.setCompoundDrawablesWithIntrinsicBounds(0, 0, endDrawable, 0)
    }

    private fun emitCurrentState() {
        var start = 0L
        var end = Long.MAX_VALUE
        var label = "All History"

        when (currentMode) {
            FilterMode.ALL -> {}
            FilterMode.DAY -> {
                start = selectedDate.atStartOfDay().toMillis()
                end = selectedDate.plusDays(1).atStartOfDay().toMillis() - 1
                label = selectedDate.toDisplayDate()
            }
            FilterMode.MONTH -> {
                val yearMonth = YearMonth.from(selectedDate)
                start = yearMonth.atDay(1).atStartOfDay().toMillis()
                end = yearMonth.atEndOfMonth().plusDays(1).atStartOfDay().toMillis() - 1
                label = selectedDate.format(DateTimeFormatter.ofPattern("MMM yyyy"))
            }
            FilterMode.YEAR -> {
                val year = selectedDate.year
                start = LocalDate.of(year, 1, 1).atStartOfDay().toMillis()
                end = LocalDate.of(year + 1, 1, 1).atStartOfDay().toMillis() - 1
                label = year.toString()
            }
            FilterMode.CUSTOM -> return // Handled via Picker Callback
        }
        onDateRangeSelected?.invoke(start, end, label)
    }
}