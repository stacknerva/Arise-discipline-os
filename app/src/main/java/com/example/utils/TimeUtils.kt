package com.example.utils

fun formatTimeAmPm(time24: String): String {
    val parts = time24.split(":")
    if (parts.size != 2) return time24
    val hour = parts[0].toIntOrNull() ?: return time24
    val min = parts[1]
    
    val amPm = if (hour >= 12) "PM" else "AM"
    var hour12 = hour % 12
    if (hour12 == 0) hour12 = 12
    
    return "$hour12:$min $amPm"
}

fun parseTo24Hour(timeAmPm: String): String {
    try {
        val trimmed = timeAmPm.trim().uppercase()
        val isPm = trimmed.endsWith("PM")
        val isAm = trimmed.endsWith("AM")
        if (!isPm && !isAm) return timeAmPm // maybe already 24h
        
        val timePart = trimmed.replace("AM", "").replace("PM", "").trim()
        val parts = timePart.split(":")
        if (parts.size != 2) return timeAmPm
        var hour = parts[0].toInt()
        val min = parts[1].padStart(2, '0')
        
        if (isPm && hour != 12) hour += 12
        if (isAm && hour == 12) hour = 0
        
        return "${hour.toString().padStart(2, '0')}:$min"
    } catch (e: Exception) {
        return timeAmPm
    }
}
