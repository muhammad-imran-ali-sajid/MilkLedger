package com.miassolutions.milkledger.core.util

import android.os.Parcel
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * ==============================================================================
 * 1. CORE UTILITY CLASS: GET TODAY'S DATE
 * ==============================================================================
 */

/**
 * Utility object to provide consistent, UTC-based date/time values for MaterialDatePicker.
 */
object CalendarUtils {
    /**
     * Returns the timestamp for the current day at midnight UTC, which is used
     * to prevent selection of any future dates.
     */
    fun getTodayUtcTimestampAtMidnight(): Long {
        val today = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        // Normalize time to midnight (00:00:00) so 'today' is selectable, but tomorrow is not.
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        // Add one day and subtract one millisecond to ensure that 'today' up until 23:59:59 is still selectable.
        // A simpler approach is just to use today's timestamp and let the MaterialDatePicker handle the maxDate boundary.
        // However, for strict 'no future dates', we often use a custom validator or set MaxDate to the end of today.
        // We will use the standard 'now' and rely on the date picker's behavior for MaxDate.
        return today.timeInMillis
    }
}

/**
 * ==============================================================================
 * 2. CUSTOM DATE VALIDATOR
 * ==============================================================================
 */

/**
 * A custom Material Date Picker validator that enforces two rules:
 * 1. No future dates can be selected (Always enforced via maxDate/internal logic).
 * 2. Past dates are only valid if 'isAuthorized' is true.
 *
 * @property isAuthorized If true, past dates are allowed. If false, only today is allowed.
 * @property todayTimestampUtc The timestamp for the current moment (used to check against future dates).
 */
class ConditionalPastDateValidator(
    private val isAuthorized: Boolean,
    private val todayTimestampUtc: Long
) : CalendarConstraints.DateValidator {

    // Mandated field for Parcelable implementation
    @JvmField
    val CREATOR: Parcelable.Creator<ConditionalPastDateValidator> =
        object : Parcelable.Creator<ConditionalPastDateValidator> {
            override fun createFromParcel(parcel: Parcel): ConditionalPastDateValidator {
                return ConditionalPastDateValidator(parcel)
            }

            override fun newArray(size: Int): Array<ConditionalPastDateValidator?> {
                return arrayOfNulls(size)
            }
        }

    // Secondary constructor for Parcelable
    private constructor(parcel: Parcel) : this(
        isAuthorized = parcel.readByte() != 0.toByte(),
        todayTimestampUtc = parcel.readLong()
    )

    /**
     * This is the core logic. It returns true if a date is valid for selection.
     */
    override fun isValid(dateInMillis: Long): Boolean {
        // --- RULE 1: NO FUTURE DATES (Applies to everyone) ---
        // We ensure that the date cannot be tomorrow or beyond.
        // We use a slight adjustment to today's timestamp to be more permissive for the current day.
        val tomorrowStartUtc =
            CalendarUtils.getTodayUtcTimestampAtMidnight() + TimeUnit.DAYS.toMillis(1)
        if (dateInMillis >= tomorrowStartUtc) {
            return false // Date is in the future
        }

        // --- RULE 2: CONDITIONAL PAST DATES ---
        val todayStartUtc = CalendarUtils.getTodayUtcTimestampAtMidnight()

        return if (isAuthorized) {
            // Authorized user: Allow any date up to today (past dates are valid)
            true
        } else {
            // Unauthorized user: Only allow today's date, or dates in the future (which are already rejected by Rule 1).
            // Effectively, only the current day is selectable if the user is unauthorized.
            dateInMillis >= todayStartUtc
        }
    }

    // Required methods for Parcelable
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isAuthorized) 1 else 0)
        parcel.writeLong(todayTimestampUtc)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConditionalPastDateValidator) return false

        if (isAuthorized != other.isAuthorized) return false
        if (todayTimestampUtc != other.todayTimestampUtc) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isAuthorized.hashCode()
        result = 31 * result + todayTimestampUtc.hashCode()
        return result
    }
}


/**
 * ==============================================================================
 * 3. LOGIC FOR CONSTRAINTS GENERATION
 * ==============================================================================
 */

/**
 * Helper class to generate the correct CalendarConstraints builder.
 */
class DatePickerLogic {

    /**
     * Generates the CalendarConstraints for the MaterialDatePicker.
     * @param isAuthorized The authorization status of the user.
     * @return The configured CalendarConstraints.
     */
    fun buildConstraints(isAuthorized: Boolean): CalendarConstraints {
        val today = CalendarUtils.getTodayUtcTimestampAtMidnight()

        val constraintsBuilder = CalendarConstraints.Builder()

        // 1. Set the MAX DATE constraint: Always TODAY. This is the simplest way to disable all future dates.
        // We set the max date to the current timestamp.
        constraintsBuilder.setEnd(today)

        // 2. Set the custom DateValidator that controls past date selection
        val validator = ConditionalPastDateValidator(isAuthorized, today)

        constraintsBuilder.setValidator(validator)

        // For authorized users, we allow selection of dates far in the past.
        // If not authorized, the validator handles restricting to today.
        if (isAuthorized) {
            // Allow the calendar to scroll back to a reasonable date (e.g., 100 years ago)
            val pastDate = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                add(Calendar.YEAR, -100)
            }.timeInMillis
            constraintsBuilder.setStart(pastDate)
        } else {
            // For unauthorized users, we only allow scrolling back to the current month.
            constraintsBuilder.setStart(today)
        }

        return constraintsBuilder.build()
    }
}

// ==============================================================================
// 4. DATE PICKER INTEGRATION & USAGE (The fix for your query)
// ==============================================================================

/**
 * This function handles the creation and display of the Material Date Picker
 * and the conversion of the selected timestamp into a LocalDate.
 *
 * @param activity The host FragmentActivity (or Fragment) to get the FragmentManager.
 * @param isAuthorized The user's authorization status (passed to the constraints).
 * @param initialDate The date to pre-select in the picker.
 * @param onPicked A lambda function to execute when a date is successfully selected.
 */
fun Fragment.showExpenseDatePicker(

    isAuthorized: Boolean,
    initialDate: LocalDate,
    onPicked: (LocalDate) -> Unit
) {
    // 1. Get the Constraints
    val constraints = DatePickerLogic().buildConstraints(isAuthorized)

    // 2. Convert LocalDate to a UTC timestamp for pre-selection
    val initialTimestamp = initialDate
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    // 3. Build the Material Date Picker
    val datePicker = MaterialDatePicker.Builder.datePicker()
//        .setTheme(com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialCalendar) // Use a standard theme
        .setTitleText("Select Expense Date")
        .setSelection(initialTimestamp)
        .setCalendarConstraints(constraints)
        .build()

    // 4. Handle the positive button click (Selected Date)
    datePicker.addOnPositiveButtonClickListener { selectedTimestamp: Long ->
        // CONVERSION STEP: Convert the UTC timestamp (Long) back to LocalDate
        val selectedDate = Instant.ofEpochMilli(selectedTimestamp)
            .atZone(ZoneId.systemDefault()) // Convert to the device's time zone
            .toLocalDate()

        // 5. Call the user's callback with the converted LocalDate
        onPicked(selectedDate)
    }

    // 6. Show the picker
    datePicker.show(parentFragmentManager, "EXPENSE_DATE_PICKER_TAG")
}

/**
 * ==============================================================================
 * 4. SIMULATED USAGE EXAMPLE (How to integrate in your Android code)
 * ==============================================================================
 */
//fun main() {
//    val datePickerLogic = DatePickerLogic()
//
//    // --- SCENARIO 1: UNAUTHORIZED USER (Cannot select past or future) ---
//    val unauthorizedConstraints = datePickerLogic.buildConstraints(isAuthorized = false)
//    println("--- UNAUTHORIZED USER CONSTRAINTS ---")
//    println("Max Date (Set End) Constraint: ${unauthorizedConstraints.end} (Today)")
//    // The validator will only return true for 'today' (up to the current time)
//    // The calendar's start date is also set to today for scrolling limitation.
//
//    // --- SCENARIO 2: AUTHORIZED USER (Can select past, cannot select future) ---
//    val authorizedConstraints = datePickerLogic.buildConstraints(isAuthorized = true)
//    println("\n--- AUTHORIZED USER CONSTRAINTS ---")
//    println("Max Date (Set End) Constraint: ${authorizedConstraints.end} (Today)")
//    // The validator will return true for all past dates.
//    // The calendar's start date will be set far in the past to allow scrolling.
//
//    // --- HOW TO USE IN AN ANDROID FRAGMENT/ACTIVITY ---
//    println("\n--- ANDROID INTEGRATION GUIDE ---")
//    println("To use the constraints, you would build the MaterialDatePicker like this:")
//    println("val constraints = datePickerLogic.buildConstraints(isUserAdminOrAuthorized)")
//    println("val datePicker = MaterialDatePicker.Builder.datePicker()")
//    println("    .setCalendarConstraints(constraints)")
//    println("    .setTitleText(\"Select a Date (Authorized: \$isUserAdminOrAuthorized)\")")
//    println("    .build()")
//    println("// datePicker.show(supportFragmentManager, \"DATE_PICKER_TAG\")")
//}
