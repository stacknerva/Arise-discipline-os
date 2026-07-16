package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routine_templates ORDER BY orderIndex ASC, startTimeStr ASC")
    fun getAllTemplates(): Flow<List<RoutineTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: RoutineTemplateEntity)

    @Update
    suspend fun updateTemplate(template: RoutineTemplateEntity)

    @Query("DELETE FROM routine_templates WHERE id = :id")
    suspend fun deleteTemplateById(id: Int)

    @Query("SELECT * FROM routine_templates")
    suspend fun getAllTemplatesSync(): List<RoutineTemplateEntity>

    @Query("DELETE FROM routine_templates")
    suspend fun deleteAllTemplates()
}

@Dao
interface DailyTaskDao {
    @Query("SELECT * FROM daily_tasks WHERE date = :date ORDER BY orderIndex ASC, startTimeStr ASC")
    fun getTasksForDate(date: String): Flow<List<DailyTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<DailyTaskEntity>)

    @Update
    suspend fun updateTask(task: DailyTaskEntity)
    
    @Query("DELETE FROM daily_tasks WHERE date = :date")
    suspend fun deleteTasksForDate(date: String)

    @Query("SELECT * FROM daily_tasks")
    suspend fun getAllTasksSync(): List<DailyTaskEntity>

    @Query("DELETE FROM daily_tasks")
    suspend fun deleteAllTasks()
}

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes WHERE uniqueId = :id LIMIT 1")
    fun getQuoteByIdFlow(id: String): Flow<QuoteEntity?>

    @Query("SELECT * FROM quotes ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuote(): QuoteEntity?

    @Query("SELECT * FROM quotes WHERE uniqueId = :id LIMIT 1")
    suspend fun checkExists(id: String): QuoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteEntity)
}

@Dao
interface DailyReportDao {
    @Query("SELECT * FROM daily_reports WHERE date = :date")
    fun getReportForDateFlow(date: String): Flow<DailyReportEntity?>

    @Query("SELECT * FROM daily_reports WHERE date = :date")
    suspend fun getReportForDate(date: String): DailyReportEntity?

    @Query("SELECT * FROM daily_reports ORDER BY date DESC")
    fun getAllReportsFlow(): Flow<List<DailyReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: DailyReportEntity)

    @Query("SELECT * FROM daily_reports")
    suspend fun getAllReportsSync(): List<DailyReportEntity>

    @Query("DELETE FROM daily_reports")
    suspend fun deleteAllReports()
}
