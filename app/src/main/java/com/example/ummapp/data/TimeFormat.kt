package com.example.ummapp.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Short, human-friendly time for lists: "Just now", "5 min ago", "Today, 14:30", "Yesterday, 09:15", "28 Oct, 09:00". */
fun formatTime(time: Long, now: Long = System.currentTimeMillis()): String {
    val minutes = (now - time) / 60_000
    if (minutes < 1) return "Just now"
    if (minutes < 60) return "$minutes min ago"

    val then = Calendar.getInstance().apply { timeInMillis = time }
    val today = Calendar.getInstance().apply { timeInMillis = now }
    val clock = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time))
    val daysApart = today.get(Calendar.DAY_OF_YEAR) - then.get(Calendar.DAY_OF_YEAR)
    val sameYear = today.get(Calendar.YEAR) == then.get(Calendar.YEAR)

    return when {
        sameYear && daysApart == 0 -> "Today, $clock"
        sameYear && daysApart == 1 -> "Yesterday, $clock"
        sameYear -> SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(time))
        else -> SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(time))
    }
}

/** Full date and time for detail views: "Wed 30 Oct 2026, 14:30". */
fun fullTime(time: Long): String = SimpleDateFormat("EEE dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(time))
