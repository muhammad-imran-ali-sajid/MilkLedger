package com.miassolutions.milkledger.presentation.datefilter

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.filterdata.CustomDateRangeBottomSheet
import com.miassolutions.milkledger.databinding.DateFilterLayoutBinding
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class DateFilterController(
    private val fragment: Fragment,
    private val binding: DateFilterLayoutBinding,
    private val callback: DateFilterCallback
) {

    private var currentPeriod: DatePeriod = DatePeriod.Daily(LocalDate.now())

    fun init() {
        setupToggleGroup()
        setupPrevNext()
        setupCustomRangeListener()
        setupTvSelectedClick()

        // Set Daily as default selection
        binding.togglePeriod.check(R.id.btnAll)

        render()
        callback.onPeriodChanged(currentPeriod)
    }


    /** Toggle Group for Daily, Weekly, Monthly, Yearly, All */
    private fun setupToggleGroup() = with(binding) {
        togglePeriod.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            currentPeriod = when (checkedId) {
                R.id.btnDaily -> DatePeriod.Daily(LocalDate.now())
                R.id.btnWeekly -> {
                    val now = LocalDate.now()
                    val start = now.with(DayOfWeek.MONDAY)
                    val end = now.with(DayOfWeek.SUNDAY)
                    DatePeriod.Weekly(start, end)
                }
                R.id.btnMonthly -> DatePeriod.Monthly(YearMonth.now())
                R.id.btnYearly -> DatePeriod.Yearly(LocalDate.now().year)
                R.id.btnAll -> DatePeriod.All
                else -> currentPeriod
            }

            render()
            callback.onPeriodChanged(currentPeriod)
        }
    }

    /** Prev/Next buttons for Daily, Weekly, Monthly, Yearly */
    private fun setupPrevNext() = with(binding) {
        btnPrevDate.setOnClickListener {
            currentPeriod = shift(-1)
            render()
            callback.onPeriodChanged(currentPeriod)
        }

        btnNextDate.setOnClickListener {
            currentPeriod = shift(+1)
            render()
            callback.onPeriodChanged(currentPeriod)
        }
    }

    /** Listener for receiving custom range from Bottom Sheet */
    private fun setupCustomRangeListener() {
        fragment.setFragmentResultListener(CustomDateRangeBottomSheet.REQUEST_KEY) { _, bundle ->
            val startString = bundle.getString(CustomDateRangeBottomSheet.BUNDLE_START_DATE)
            val endString = bundle.getString(CustomDateRangeBottomSheet.BUNDLE_END_DATE)

            if (!startString.isNullOrEmpty() && !endString.isNullOrEmpty()) {
                val start = LocalDate.parse(startString)
                val end = LocalDate.parse(endString)

                currentPeriod = DatePeriod.Custom(start, end)
                hidePrevNext(true)
                render()
                callback.onPeriodChanged(currentPeriod)
            }
        }
    }

    /** Click listener on tvSelectedDate to open custom range picker */
    private fun setupTvSelectedClick() {
        binding.tvSelectedDate.setOnClickListener {
            CustomDateRangeBottomSheet().show(fragment.parentFragmentManager, "custom_range")
        }
    }

    /** Shift period by step for Prev/Next buttons */
    private fun shift(step: Int): DatePeriod = when (currentPeriod) {
        is DatePeriod.Daily -> {
            val d = (currentPeriod as DatePeriod.Daily).date.plusDays(step.toLong())
            DatePeriod.Daily(d)
        }
        is DatePeriod.Weekly -> {
            val p = currentPeriod as DatePeriod.Weekly
            DatePeriod.Weekly(p.start.plusWeeks(step.toLong()), p.end.plusWeeks(step.toLong()))
        }
        is DatePeriod.Monthly -> {
            val m = (currentPeriod as DatePeriod.Monthly).month.plusMonths(step.toLong())
            DatePeriod.Monthly(m)
        }
        is DatePeriod.Yearly -> {
            val y = (currentPeriod as DatePeriod.Yearly).year + step
            DatePeriod.Yearly(y)
        }
        else -> currentPeriod
    }

    /** Update UI based on currentPeriod */
    private fun render() = with(binding) {
        when (currentPeriod) {
            is DatePeriod.Daily -> {
                hidePrevNext(false)
                tvSelectedDate.text = (currentPeriod as DatePeriod.Daily)
                    .date.format(fmt())
            }
            is DatePeriod.Weekly -> {
                hidePrevNext(false)
                val p = currentPeriod as DatePeriod.Weekly
                tvSelectedDate.text = "${p.start.format(fmt())} → ${p.end.format(fmt())}"
            }
            is DatePeriod.Monthly -> {
                hidePrevNext(false)
                val m = (currentPeriod as DatePeriod.Monthly).month
                tvSelectedDate.text = "${m.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${m.year}"
            }
            is DatePeriod.Yearly -> {
                hidePrevNext(false)
                tvSelectedDate.text = (currentPeriod as DatePeriod.Yearly).year.toString()
            }
            DatePeriod.All -> {
                hidePrevNext(true)
                tvSelectedDate.text = "All Records"
            }
            is DatePeriod.Custom -> {
                hidePrevNext(true)
                val p = currentPeriod as DatePeriod.Custom
                tvSelectedDate.text = "${p.start.format(fmt())} → ${p.end.format(fmt())}"
            }
        }
    }

    /** Show/hide prev/next buttons */
    private fun hidePrevNext(hide: Boolean) = with(binding) {
        btnPrevDate.visibility = if (hide) View.INVISIBLE else View.VISIBLE
        btnNextDate.visibility = if (hide) View.INVISIBLE else View.VISIBLE
    }

    private fun fmt() = DateTimeFormatter.ofPattern("dd MMM yyyy")
}
