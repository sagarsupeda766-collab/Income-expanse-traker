package com.example.util

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    val KOLKATA_ZONE: ZoneId = ZoneId.of("Asia/Kolkata")

    private val ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.US)
    private val READABLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.US)
    private val MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)
    private val YEAR_MONTH_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM", Locale.US)
    private val SHORT_DAY_MONTH_FORMATTER = DateTimeFormatter.ofPattern("dd MMM", Locale.US)

    fun today(): LocalDate {
        return LocalDate.now(KOLKATA_ZONE)
    }

    fun todayIso(): String {
        return today().format(ISO_DATE_FORMATTER)
    }

    fun formatToIso(date: LocalDate): String {
        return date.format(ISO_DATE_FORMATTER)
    }

    fun parseIso(isoString: String): LocalDate {
        return try {
            LocalDate.parse(isoString, ISO_DATE_FORMATTER)
        } catch (e: Exception) {
            today()
        }
    }

    fun formatToDisplay(date: LocalDate): String {
        return date.format(DISPLAY_DATE_FORMATTER)
    }

    fun formatIsoToDisplay(isoString: String): String {
        return formatToDisplay(parseIso(isoString))
    }

    fun formatIsoToReadable(isoString: String): String {
        val date = parseIso(isoString)
        val today = today()
        val yesterday = today.minusDays(1)

        return when (date) {
            today -> "Today, ${date.format(READABLE_DATE_FORMATTER)}"
            yesterday -> "Yesterday, ${date.format(READABLE_DATE_FORMATTER)}"
            else -> date.format(READABLE_DATE_FORMATTER)
        }
    }

    fun formatIsoToShort(isoString: String): String {
        return parseIso(isoString).format(SHORT_DAY_MONTH_FORMATTER)
    }

    fun formatMonthYear(yearMonth: YearMonth): String {
        return yearMonth.format(MONTH_YEAR_FORMATTER)
    }

    fun formatToYearMonthKey(date: LocalDate): String {
        return date.format(YEAR_MONTH_KEY_FORMATTER)
    }

    fun formatYearMonthKey(yearMonth: YearMonth): String {
        return yearMonth.format(YEAR_MONTH_KEY_FORMATTER)
    }

    fun parseYearMonthKey(key: String): YearMonth {
        return try {
            YearMonth.parse(key, YEAR_MONTH_KEY_FORMATTER)
        } catch (e: Exception) {
            YearMonth.now(KOLKATA_ZONE)
        }
    }

    fun currentYearMonth(): YearMonth {
        return YearMonth.now(KOLKATA_ZONE)
    }

    fun getStartAndEndOfWeek(date: LocalDate): Pair<LocalDate, LocalDate> {
        val dayOfWeek = date.dayOfWeek.value // 1 (Monday) to 7 (Sunday)
        val start = date.minusDays((dayOfWeek - 1).toLong())
        val end = start.plusDays(6)
        return Pair(start, end)
    }

    fun getStartAndEndOfMonth(yearMonth: YearMonth): Pair<LocalDate, LocalDate> {
        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()
        return Pair(start, end)
    }

    fun millisToIso(millis: Long): String {
        val instant = Instant.ofEpochMilli(millis)
        return instant.atZone(KOLKATA_ZONE).toLocalDate().format(ISO_DATE_FORMATTER)
    }

    fun isoToMillis(isoString: String): Long {
        val date = parseIso(isoString)
        return date.atStartOfDay(KOLKATA_ZONE).toInstant().toEpochMilli()
    }
}
