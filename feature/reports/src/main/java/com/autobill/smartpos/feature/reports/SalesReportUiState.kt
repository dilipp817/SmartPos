package com.autobill.smartpos.feature.reports

import com.autobill.smartpos.domain.model.SalesReport
import java.util.Calendar

/**
 * UI state for the Sales Report screen (Phase 8.1).
 *
 * [startDateMs] / [endDateMs] — epoch-millisecond timestamps backing the date pickers.
 *                               Converted to ISO-8601 strings when calling the API.
 * [report]         — null until the first successful load.
 * [isLoading]      — true while the API call is in-flight.
 * [errorMessage]   — non-null on failure.
 * [showStartPicker] / [showEndPicker] — controls DatePickerDialog visibility.
 */
data class SalesReportUiState(
    val startDateMs: Long = todayStartMs(),
    val endDateMs: Long = todayEndMs(),
    val report: SalesReport? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showStartPicker: Boolean = false,
    val showEndPicker: Boolean = false,
)

// ── Date helpers ─────────────────────────────────────────────────────────────

/** Start of today at 00:00:00 as epoch millis. */
internal fun todayStartMs(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

/** End of today at 23:59:59 as epoch millis. */
internal fun todayEndMs(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    return cal.timeInMillis
}

/**
 * Converts epoch-milliseconds to an ISO-8601 datetime string.
 * [endOfDay] = true  → appends "T23:59:59"
 * [endOfDay] = false → appends "T00:00:00"
 */
internal fun Long.toIsoDateString(endOfDay: Boolean = false): String {
    val cal = Calendar.getInstance().apply { timeInMillis = this@toIsoDateString }
    val y   = cal.get(Calendar.YEAR)
    val m   = cal.get(Calendar.MONTH) + 1
    val d   = cal.get(Calendar.DAY_OF_MONTH)
    val time = if (endOfDay) "23:59:59" else "00:00:00"
    return "%04d-%02d-%02dT%s".format(y, m, d, time)
}

/** Converts epoch-milliseconds to a human-readable date string e.g. "13 Apr 2026". */
internal fun Long.toDisplayDate(): String {
    val cal = Calendar.getInstance().apply { timeInMillis = this@toDisplayDate }
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val mon = arrayOf("","Jan","Feb","Mar","Apr","May","Jun",
                      "Jul","Aug","Sep","Oct","Nov","Dec")[cal.get(Calendar.MONTH) + 1]
    val yr  = cal.get(Calendar.YEAR)
    return "$day $mon $yr"
}

