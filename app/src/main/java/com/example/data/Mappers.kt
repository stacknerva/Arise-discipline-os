package com.example.data

fun RoutineTemplateEntity.toMap(): Map<String, Any> = mapOf(
    "id" to id,
    "title" to title,
    "startTimeStr" to startTimeStr,
    "endTimeStr" to endTimeStr,
    "notificationOffsetMins" to notificationOffsetMins,
    "orderIndex" to orderIndex
)

fun mapToRoutineTemplate(map: Map<String, Any>): RoutineTemplateEntity = RoutineTemplateEntity(
    id = (map["id"] as? Number)?.toInt() ?: 0,
    title = map["title"] as? String ?: "",
    startTimeStr = map["startTimeStr"] as? String ?: "",
    endTimeStr = map["endTimeStr"] as? String ?: "",
    notificationOffsetMins = (map["notificationOffsetMins"] as? Number)?.toInt() ?: 5,
    orderIndex = (map["orderIndex"] as? Number)?.toInt() ?: 0
)

fun DailyTaskEntity.toMap(): Map<String, Any> = mapOf(
    "id" to id,
    "date" to date,
    "templateId" to templateId,
    "title" to title,
    "startTimeStr" to startTimeStr,
    "endTimeStr" to endTimeStr,
    "isMissed" to isMissed,
    "orderIndex" to orderIndex
)

fun mapToDailyTask(map: Map<String, Any>): DailyTaskEntity = DailyTaskEntity(
    id = (map["id"] as? Number)?.toInt() ?: 0,
    date = map["date"] as? String ?: "",
    templateId = (map["templateId"] as? Number)?.toInt() ?: 0,
    title = map["title"] as? String ?: "",
    startTimeStr = map["startTimeStr"] as? String ?: "",
    endTimeStr = map["endTimeStr"] as? String ?: "",
    isMissed = map["isMissed"] as? Boolean ?: false,
    orderIndex = (map["orderIndex"] as? Number)?.toInt() ?: 0
)

fun DailyReportEntity.toMap(): Map<String, Any> = mapOf(
    "date" to date,
    "isSubmitted" to isSubmitted,
    "isSkipped" to isSkipped,
    "missedTasksCount" to missedTasksCount,
    "totalTasksCount" to totalTasksCount,
    "reason" to (reason ?: ""),
    "otherReason" to (otherReason ?: ""),
    "status" to status
)

fun mapToDailyReport(map: Map<String, Any>): DailyReportEntity = DailyReportEntity(
    date = map["date"] as? String ?: "",
    isSubmitted = map["isSubmitted"] as? Boolean ?: false,
    isSkipped = map["isSkipped"] as? Boolean ?: false,
    missedTasksCount = (map["missedTasksCount"] as? Number)?.toInt() ?: 0,
    totalTasksCount = (map["totalTasksCount"] as? Number)?.toInt() ?: 0,
    reason = (map["reason"] as? String).takeIf { !it.isNullOrBlank() },
    otherReason = (map["otherReason"] as? String).takeIf { !it.isNullOrBlank() },
    status = map["status"] as? String ?: "skipped"
)
