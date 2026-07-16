package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "routine_templates")
@JsonClass(generateAdapter = true)
data class RoutineTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val startTimeStr: String, // HH:mm
    val endTimeStr: String, // HH:mm
    val notificationOffsetMins: Int = 5,
    val orderIndex: Int = 0
)

@Entity(tableName = "daily_tasks")
@JsonClass(generateAdapter = true)
data class DailyTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val templateId: Int, // Maps back to the routine template
    val title: String,
    val startTimeStr: String,
    val endTimeStr: String,
    val isMissed: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(tableName = "quotes")
@JsonClass(generateAdapter = true)
data class QuoteEntity(
    @PrimaryKey val uniqueId: String,
    val text: String,
    val author: String,
    val source: String,
    val category: String,
    val dateDownloaded: String
)

@Entity(tableName = "daily_reports")
@JsonClass(generateAdapter = true)
data class DailyReportEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val isSubmitted: Boolean = false,
    val isSkipped: Boolean = false,
    val missedTasksCount: Int = 0,
    val totalTasksCount: Int = 0,
    val reason: String? = null,
    val otherReason: String? = null,
    val status: String // "perfect", "partial", "skipped"
)
