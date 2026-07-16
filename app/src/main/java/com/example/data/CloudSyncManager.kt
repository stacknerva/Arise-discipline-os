package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.first
import java.util.Date

class CloudSyncManager(
    private val repository: DisciplineRepository,
    private val settingsRepository: SettingsRepository
) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun isUserSignedIn(): Boolean = auth.currentUser != null

    suspend fun hasCloudData(): Boolean {
        val user = auth.currentUser ?: return false
        return try {
            val doc = db.collection("users").document(user.uid).get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun syncToCloud() {
        val user = auth.currentUser ?: return
        
        try {
            val templates = repository.getAllTemplatesSync()
            val tasks = repository.getAllTasksSync()
            val reports = repository.getAllReportsSync()
            
            val streak = settingsRepository.currentStreak.first()
            val isExpanded = settingsRepository.isRoutineExpanded.first()
            
            val routineList = templates.map { it.toMap() }
            val calendarMap = mapOf(
                "tasks" to tasks.map { it.toMap() },
                "reports" to reports.map { it.toMap() }
            )
            
            val data = mapOf(
                "routine" to routineList,
                "calendar" to calendarMap,
                "streak" to streak,
                "settings" to mapOf("isRoutineExpanded" to isExpanded),
                "reminderSettings" to emptyMap<String, Any>(),
                "statistics" to emptyMap<String, Any>(),
                "achievements" to emptyMap<String, Any>(),
                "appPreferences" to emptyMap<String, Any>(),
                "lastSync" to Date().time
            )
            
            db.collection("users").document(user.uid)
                .set(data, SetOptions.merge())
                .await()
                
        } catch (e: Exception) {
            Log.e("CloudSync", "Error syncing to cloud", e)
        }
    }
    
    suspend fun fetchFromCloud(): Boolean {
        val user = auth.currentUser ?: return false
        
        try {
            val doc = db.collection("users").document(user.uid).get().await()
            if (!doc.exists()) return false
            
            val routineList = doc.get("routine") as? List<Map<String, Any>> ?: emptyList()
            val calendarMap = doc.get("calendar") as? Map<String, Any> ?: emptyMap()
            val tasksList = calendarMap["tasks"] as? List<Map<String, Any>> ?: emptyList()
            val reportsList = calendarMap["reports"] as? List<Map<String, Any>> ?: emptyList()
            val streak = (doc.get("streak") as? Number)?.toInt() ?: 0
            val settingsMap = doc.get("settings") as? Map<String, Any> ?: emptyMap()
            val isExpanded = settingsMap["isRoutineExpanded"] as? Boolean ?: false
            
            val newTemplates = routineList.map { mapToRoutineTemplate(it) }
            val newTasks = tasksList.map { mapToDailyTask(it) }
            val newReports = reportsList.map { mapToDailyReport(it) }
            
            repository.deleteAllTemplates()
            repository.deleteAllTasks()
            repository.deleteAllReports()
            
            newTemplates.forEach { repository.insertTemplate(it) }
            repository.insertTasks(newTasks)
            newReports.forEach { repository.insertReport(it) }
            
            settingsRepository.setCurrentStreak(streak)
            settingsRepository.setRoutineExpanded(isExpanded)
            
            return true
        } catch (e: Exception) {
            Log.e("CloudSync", "Error fetching from cloud", e)
            return false
        }
    }
}
