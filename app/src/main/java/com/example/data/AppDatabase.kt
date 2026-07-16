package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        RoutineTemplateEntity::class,
        DailyTaskEntity::class,
        QuoteEntity::class,
        DailyReportEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao
    abstract fun dailyTaskDao(): DailyTaskDao
    abstract fun quoteDao(): QuoteDao
    abstract fun dailyReportDao(): DailyReportDao
}
