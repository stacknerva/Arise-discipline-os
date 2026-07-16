package com.example.data

import kotlinx.coroutines.flow.Flow

class DisciplineRepository(private val db: AppDatabase) {
    fun getAllTemplates() = db.routineDao().getAllTemplates()
    suspend fun insertTemplate(template: RoutineTemplateEntity) = db.routineDao().insertTemplate(template)
    suspend fun updateTemplate(template: RoutineTemplateEntity) = db.routineDao().updateTemplate(template)
    suspend fun deleteTemplate(id: Int) = db.routineDao().deleteTemplateById(id)

    fun getTasksForDate(date: String) = db.dailyTaskDao().getTasksForDate(date)
    suspend fun insertTasks(tasks: List<DailyTaskEntity>) = db.dailyTaskDao().insertTasks(tasks)
    suspend fun updateTask(task: DailyTaskEntity) = db.dailyTaskDao().updateTask(task)
    suspend fun deleteTasksForDate(date: String) = db.dailyTaskDao().deleteTasksForDate(date)

    fun getQuoteByIdFlow(id: String) = db.quoteDao().getQuoteByIdFlow(id)
    suspend fun getRandomQuote() = db.quoteDao().getRandomQuote()
    suspend fun checkQuoteExists(id: String) = db.quoteDao().checkExists(id)
    suspend fun insertQuote(quote: QuoteEntity) = db.quoteDao().insertQuote(quote)

    fun getReportForDateFlow(date: String) = db.dailyReportDao().getReportForDateFlow(date)
    suspend fun getReportForDate(date: String) = db.dailyReportDao().getReportForDate(date)
    fun getAllReportsFlow() = db.dailyReportDao().getAllReportsFlow()
    suspend fun insertReport(report: DailyReportEntity) = db.dailyReportDao().insertReport(report)
}
