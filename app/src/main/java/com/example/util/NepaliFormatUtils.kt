package com.example.util

import com.example.data.model.SystemLanguage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object NepaliFormatUtils {

    private val NEPALI_MONTHS = arrayOf(
        "जनवरी", "फेब्रुअरी", "मार्च", "अप्रिल", "मे", "जुन",
        "जुलाई", "अगस्ट", "सेप्टेम्बर", "अक्टोबर", "नोभेम्बर", "डिसेम्बर"
    )

    private val NEPALI_WEEKDAYS = arrayOf(
        "आइतबार", "सोमबार", "मंगलबार", "बुधबार", "बिहीबार", "शुक्रबार", "शनिबार"
    )

    fun toDevanagariDigits(text: String): String {
        val out = StringBuilder(text.length)
        for (ch in text) {
            if (ch in '0'..'9') {
                out.append(('०'.code + (ch - '0')).toChar())
            } else {
                out.append(ch)
            }
        }
        return out.toString()
    }

    fun formatNumber(value: Int, language: SystemLanguage): String =
        if (language == SystemLanguage.NEPALI) DevanagariNumberUtils.toDevanagari(value) else value.toString()

    fun formatLong(value: Long, language: SystemLanguage): String =
        if (language == SystemLanguage.NEPALI) toDevanagariDigits(value.toString()) else value.toString()

    fun formatDecimal(value: Float, decimals: Int = 2, language: SystemLanguage): String {
        val raw = String.format(Locale.US, "%.${decimals}f", value)
        return if (language == SystemLanguage.NEPALI) toDevanagariDigits(raw) else raw
    }

    fun formatPercent(percent: Int, language: SystemLanguage): String =
        if (language == SystemLanguage.NEPALI) "${DevanagariNumberUtils.toDevanagari(percent)}%" else "$percent%"

    fun formatSpeed(speed: Float, language: SystemLanguage): String {
        val raw = "${speed}x"
        return if (language == SystemLanguage.NEPALI) toDevanagariDigits(raw) else raw
    }

    fun formatBytes(bytes: Long, language: SystemLanguage): String {
        if (language != SystemLanguage.NEPALI) {
            return when {
                bytes <= 0L -> "0 KB"
                bytes < 1024L * 1024L -> "${(bytes / 1024L).coerceAtLeast(1L)} KB"
                else -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
            }
        }
        return when {
            bytes <= 0L -> "० केबी"
            bytes < 1024L * 1024L -> "${DevanagariNumberUtils.toDevanagari(((bytes / 1024L).coerceAtLeast(1L)).toInt())} केबी"
            else -> "${formatDecimal((bytes / (1024.0 * 1024.0)).toFloat(), 2, SystemLanguage.NEPALI)} एमबी"
        }
    }

    fun formatDateTime(timestampMs: Long, language: SystemLanguage): String {
        if (language != SystemLanguage.NEPALI) {
            return SimpleDateFormat("MMM d, h:mm a", Locale.US).format(Date(timestampMs))
        }
        val cal = Calendar.getInstance().apply { timeInMillis = timestampMs }
        val monthName = NEPALI_MONTHS[cal.get(Calendar.MONTH).coerceIn(0, 11)]
        val day = DevanagariNumberUtils.toDevanagari(cal.get(Calendar.DAY_OF_MONTH))
        val hour24 = cal.get(Calendar.HOUR_OF_DAY)
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        val minute = cal.get(Calendar.MINUTE).toString().padStart(2, '0')
        val period = when (hour24) {
            in 4..11 -> "बिहान"
            in 12..16 -> "दिउँसो"
            in 17..19 -> "साँझ"
            else -> "राति"
        }
        return "$day $monthName, $period ${DevanagariNumberUtils.toDevanagari(hour12)}:${toDevanagariDigits(minute)}"
    }

    fun formatFullDate(timestampMs: Long, language: SystemLanguage): String {
        if (language != SystemLanguage.NEPALI) {
            return SimpleDateFormat("EEE, MMM d, yyyy", Locale.US).format(Date(timestampMs))
        }
        val cal = Calendar.getInstance().apply { timeInMillis = timestampMs }
        val weekday = NEPALI_WEEKDAYS[(cal.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)]
        val month = NEPALI_MONTHS[cal.get(Calendar.MONTH).coerceIn(0, 11)]
        val day = DevanagariNumberUtils.toDevanagari(cal.get(Calendar.DAY_OF_MONTH))
        val year = DevanagariNumberUtils.toDevanagari(cal.get(Calendar.YEAR))
        return "$weekday, $day $month $year"
    }
}
