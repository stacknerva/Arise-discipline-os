package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppBackup(
    val templates: List<RoutineTemplateEntity>,
    val tasks: List<DailyTaskEntity>,
    val reports: List<DailyReportEntity>,
    val currentStreak: Int,
    val backupDate: String
)
