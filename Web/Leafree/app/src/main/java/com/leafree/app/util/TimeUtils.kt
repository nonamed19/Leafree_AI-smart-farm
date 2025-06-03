package com.leafree.app.util

fun formatMinutesToDayHourMinute(totalMinutes: Int): String {
    val days = totalMinutes / (24 * 60)
    val hours = (totalMinutes % (24 * 60)) / 60
    val minutes = totalMinutes % 60

    return buildString {
        if (days > 0) append("${days}일 ")
        if (hours > 0) append("${hours}시간 ")
        append("${minutes}분")
    }.trim()
}
