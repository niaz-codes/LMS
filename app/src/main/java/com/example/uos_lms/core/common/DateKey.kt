package com.example.uos_lms.core.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val DATE_KEY_PATTERN = "yyyy-MM-dd"
private const val DISPLAY_PATTERN = "dd MMM yyyy"

/** Today's date as a plain "yyyy-MM-dd" key — the unit attendance sessions are keyed by. */
fun todayDateKey(): String = SimpleDateFormat(DATE_KEY_PATTERN, Locale.US).format(Date())

fun dateKeyToDisplay(dateKey: String): String = try {
    val parsed = SimpleDateFormat(DATE_KEY_PATTERN, Locale.US).parse(dateKey)
    if (parsed != null) SimpleDateFormat(DISPLAY_PATTERN, Locale.US).format(parsed) else dateKey
} catch (e: Exception) {
    dateKey
}

fun dateKeyToMillis(dateKey: String): Long = try {
    SimpleDateFormat(DATE_KEY_PATTERN, Locale.US).parse(dateKey)?.time ?: System.currentTimeMillis()
} catch (e: Exception) {
    System.currentTimeMillis()
}

fun millisToDisplay(millis: Long): String =
    SimpleDateFormat(DISPLAY_PATTERN, Locale.US).format(Date(millis))
