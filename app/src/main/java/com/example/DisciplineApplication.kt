package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.DisciplineRepository
import com.example.data.QuoteEntity
import com.example.data.RoutineTemplateEntity
import com.example.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DisciplineApplication : Application() {
    lateinit var database: AppDatabase
    lateinit var repository: DisciplineRepository
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "discipline_db"
        )
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
        
        repository = DisciplineRepository(database)
        settingsRepository = SettingsRepository(this)
        
        CoroutineScope(Dispatchers.IO).launch {
            val templates = repository.getAllTemplates().first()
            if (templates.isEmpty()) {
                val initialRoutines = listOf(
                    RoutineTemplateEntity(title = "Wake Up", startTimeStr = "04:00", endTimeStr = "04:00", orderIndex = 0),
                    RoutineTemplateEntity(title = "Physics Lecture", startTimeStr = "07:00", endTimeStr = "09:00", orderIndex = 1),
                    RoutineTemplateEntity(title = "Chemistry Lecture", startTimeStr = "09:00", endTimeStr = "11:00", orderIndex = 2),
                    RoutineTemplateEntity(title = "Mathematics Lecture", startTimeStr = "11:00", endTimeStr = "13:00", orderIndex = 3),
                    RoutineTemplateEntity(title = "Lunch", startTimeStr = "13:00", endTimeStr = "14:00", orderIndex = 4),
                    RoutineTemplateEntity(title = "Mathematics Lecture", startTimeStr = "14:00", endTimeStr = "16:00", orderIndex = 5),
                    RoutineTemplateEntity(title = "Self Study", startTimeStr = "16:30", endTimeStr = "20:00", orderIndex = 6),
                    RoutineTemplateEntity(title = "Sleep", startTimeStr = "21:00", endTimeStr = "04:00", orderIndex = 7)
                )
                initialRoutines.forEach { repository.insertTemplate(it) }
            } else {
                // Fix for existing Sleep routines with wrong end time
                val sleepRoutine = templates.find { it.title == "Sleep" && it.startTimeStr == "21:00" && it.endTimeStr == "21:00" }
                if (sleepRoutine != null) {
                    repository.updateTemplate(sleepRoutine.copy(endTimeStr = "04:00"))
                }
            }
        }
    }
}
