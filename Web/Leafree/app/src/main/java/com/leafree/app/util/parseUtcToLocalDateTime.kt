package com.leafree.app.util

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * ISO 8601 UTC 형식 문자열을 LocalDateTime으로 변환
 * 예: "2025-05-19T07:01:38.846Z" → LocalDateTime
 */
fun parseUtcToLocalDateTime(utcString: String): LocalDateTime {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    val localDateTime = LocalDateTime.parse(utcString, formatter)
    return localDateTime.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
}

/**
 * LocalDateTime에서 시간만 "오전/오후 h:mm" 형식으로 포맷
 */
fun formatTimeKorean(localDateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREA)
    return localDateTime.toLocalTime().format(formatter)
}

fun formatDateKorean(localDateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREA)
    return localDateTime.toLocalDate().format(formatter)
}
