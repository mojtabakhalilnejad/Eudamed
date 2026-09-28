package com.openregulatory.eudamedsearch.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Helpers for the "similar" matching the user asked for on date fields too: EUDAMED dates are
 * sometimes only known to the month or year (a certificate's `issueDate` vs `startingValidityDate`,
 * for instance), so exact-date filtering is too strict. Instead, date filters in this app are
 * *ranges* ("from" / "to", both optional) — which is itself the natural way to express "on or
 * around this date" — evaluated with day-level granularity once a raw ISO date/date-time string
 * from the API is parsed.
 */
object FuzzyDate {

    private val isoDate = DateTimeFormatter.ISO_LOCAL_DATE

    /** Parses a raw API date string that may be a plain date ("2023-05-17") or a date-time
     *  ("2023-05-17T14:15:22Z"); returns null if it can't be parsed at all. */
    fun parseApiDate(raw: String?): LocalDate? {
        if (raw.isNullOrBlank()) return null
        val datePart = raw.substringBefore('T')
        return try {
            LocalDate.parse(datePart, isoDate)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    /** True if [date] falls within `[from, to]`; a null [date] (unknown) is never excluded by a
     *  range filter, since we'd rather over-show than silently hide devices with missing dates. */
    fun inRange(date: LocalDate?, from: LocalDate?, to: LocalDate?): Boolean {
        if (date == null) return true
        if (from != null && date.isBefore(from)) return false
        if (to != null && date.isAfter(to)) return false
        return true
    }

    fun format(date: LocalDate?): String = date?.format(isoDate) ?: ""
}
