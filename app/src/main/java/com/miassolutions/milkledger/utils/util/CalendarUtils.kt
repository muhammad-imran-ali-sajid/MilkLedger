package com.miassolutions.milkledger.utils.util

import android.os.Parcel
import android.os.Parcelable
import com.google.android.material.datepicker.CalendarConstraints
import java.util.Calendar
import java.util.TimeZone
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
        val today = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
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














