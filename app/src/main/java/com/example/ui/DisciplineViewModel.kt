package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)

class DisciplineViewModel(
    private val repository: DisciplineRepository,
    private val settingsRepository: SettingsRepository,
    private val syncManager: CloudSyncManager = CloudSyncManager(repository, settingsRepository),
    private val notificationHelper: NotificationHelper,
    private val apiService: QuoteApiService,
    private val timeApiService: WorldTimeApiService
) : ViewModel() {

    val currentStreak = settingsRepository.currentStreak
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _currentDate = MutableStateFlow(getCurrentDateStr())
    val currentDate = _currentDate.asStateFlow()
    
    private val _currentTime = MutableStateFlow(Calendar.getInstance())
    val currentTime = _currentTime.asStateFlow()

    val allTemplates = repository.getAllTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayTasks = _currentDate.flatMapLatest { date ->
        repository.getTasksForDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val currentReport = _currentDate.flatMapLatest { date ->
        repository.getReportForDateFlow(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    

    val showCloudImportDialog = MutableStateFlow(false)
    val isUserSignedIn = MutableStateFlow(syncManager.isUserSignedIn())

    private fun triggerCloudSync() {
        if (syncManager.isUserSignedIn()) {
            viewModelScope.launch {
                syncManager.syncToCloud()
            }
        }
    }

    private suspend fun backupGuestData() {
        val templates = repository.getAllTemplatesSync()
        val tasks = repository.getAllTasksSync()
        val reports = repository.getAllReportsSync()
        val streak = settingsRepository.currentStreak.first()
        val dateStr = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault()).format(Date())
        val backup = AppBackup(templates, tasks, reports, streak, dateStr)
        val moshi = com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
        val json = moshi.adapter(AppBackup::class.java).toJson(backup)
        settingsRepository.setGuestBackup(json)
    }

    fun handleSignIn(context: android.content.Context) {
        isUserSignedIn.value = true
        viewModelScope.launch {
            if (syncManager.hasCloudData()) {
                backupGuestData()
                val success = syncManager.fetchFromCloud()
                if (success) {
                    val now = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                    setLastSyncTime("Today at $now")
                }
            } else {
                showCloudImportDialog.value = true
            }
        }
    }

    fun handleSignOut() {
        isUserSignedIn.value = false
        viewModelScope.launch {
            val json = settingsRepository.guestBackup.first()
            if (json != null) {
                val moshi = com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
                val backup = moshi.adapter(AppBackup::class.java).fromJson(json)
                if (backup != null) {
                    repository.deleteAllTemplates()
                    repository.deleteAllTasks()
                    repository.deleteAllReports()
                    backup.templates.forEach { repository.insertTemplate(it) }
                    repository.insertTasks(backup.tasks)
                    backup.reports.forEach { repository.insertReport(it) }
                    settingsRepository.setCurrentStreak(backup.currentStreak)
                }
            }
        }
    }

    fun onImportCloudData(import: Boolean) {
        showCloudImportDialog.value = false
        viewModelScope.launch {
            if (import) {
                syncManager.syncToCloud()
                val now = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                setLastSyncTime("Today at $now")
            } else {
                backupGuestData()
                repository.deleteAllTemplates()
                repository.deleteAllTasks()
                repository.deleteAllReports()
                settingsRepository.setCurrentStreak(0)
                syncManager.syncToCloud()
                val now = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                setLastSyncTime("Today at $now")
            }
        }
    }

    @Suppress("DEPRECATION")
    fun manualSync(context: android.content.Context) {
        viewModelScope.launch {
            if (syncManager.isUserSignedIn()) {
                val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
                val activeNetwork = cm.activeNetworkInfo
                val isConnected = activeNetwork?.isConnectedOrConnecting == true
                
                if (isConnected) {
                    syncManager.syncToCloud()
                    syncManager.fetchFromCloud()
                    val now = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                    setLastSyncTime("Today at $now")
                    android.widget.Toast.makeText(context, "Sync completed.", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(context, "No internet connection. Your changes will sync automatically when you're back online.", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val isRoutineExpanded = settingsRepository.isRoutineExpanded.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val lastSyncTime = settingsRepository.lastSyncTime.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val notificationSoundMode = settingsRepository.notificationSoundMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "default")
    val notificationSoundUri = settingsRepository.notificationSoundUri.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setRoutineExpanded(expanded: Boolean) {
        viewModelScope.launch { settingsRepository.setRoutineExpanded(expanded)
            triggerCloudSync() }
    }
    
    fun setLastSyncTime(time: String) {
        viewModelScope.launch { settingsRepository.setLastSyncTime(time) }
    }

    fun setNotificationSoundMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setNotificationSoundMode(mode)
            val uri = settingsRepository.notificationSoundUri.first()
            notificationHelper.recreateNotificationChannel(mode, uri)
            triggerCloudSync()
        }
    }

    fun setNotificationSoundUri(uri: String?) {
        viewModelScope.launch {
            settingsRepository.setNotificationSoundUri(uri)
            notificationHelper.recreateNotificationChannel("custom", uri)
            triggerCloudSync()
        }
    }
    
    val quoteOfTheDay = settingsRepository.currentQuoteId.flatMapLatest { id ->
        if (id.isNullOrEmpty()) flowOf(null) else repository.getQuoteByIdFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    val allReports = repository.getAllReportsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isReportAvailable = combine(currentTime, currentReport) { cal, report ->
        if (report?.isSubmitted == true || report?.isSkipped == true) return@combine false
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        (hour == 20 && minute >= 30) || (hour == 21 && minute == 0) // 20:30 to 21:00
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private var timeOffsetMs: Long? = null

    fun syncTimeNow() {
        viewModelScope.launch {
            try {
                Log.d("AriseApp", "Attempting internet time sync on resume...")
                val timeResponse = timeApiService.getCurrentTime()
                val internetTimeMs = timeResponse.unixtime * 1000L
                timeOffsetMs = internetTimeMs - System.currentTimeMillis()
                Log.d("AriseApp", "Time offset calculated: $timeOffsetMs ms")
            } catch (e: Exception) {
                Log.e("AriseApp", "Time sync failed, using device time", e)
                timeOffsetMs = 0L
            }
        }
    }

    init {
        viewModelScope.launch {
            while (true) {
                try {
                    Log.d("AriseApp", "Attempting internet time sync...")
                    val timeResponse = timeApiService.getCurrentTime()
                    val internetTimeMs = timeResponse.unixtime * 1000L
                    timeOffsetMs = internetTimeMs - System.currentTimeMillis()
                    Log.d("AriseApp", "Time offset calculated: $timeOffsetMs ms")
                } catch (e: Exception) {
                    Log.e("AriseApp", "Time sync failed, using device time", e)
                    timeOffsetMs = 0L
                }
                delay(15 * 60 * 1000L) // every 15 minutes
            }
        }
        
        viewModelScope.launch {
            while (true) {
                val realTimeMs = System.currentTimeMillis() + (timeOffsetMs ?: 0L)
                val realCal = Calendar.getInstance().apply { timeInMillis = realTimeMs }
                _currentTime.value = realCal
                
                val currentStr = getCurrentDateStr()
                if (_currentDate.value != currentStr) {
                    _currentDate.value = currentStr
                    checkAndInitializeDay(currentStr)
                }
                
                val savedQuoteDate = settingsRepository.currentQuoteDate.first()
                if (savedQuoteDate != _currentDate.value) {
                    syncQuoteForToday(_currentDate.value)
                }
                
                delay(60000)
            }
        }
        
        viewModelScope.launch {
            checkAndInitializeDay(_currentDate.value)
            triggerCloudSync()
            val savedQuoteDate = settingsRepository.currentQuoteDate.first()
            if (savedQuoteDate != _currentDate.value) {
                syncQuoteForToday(_currentDate.value)
            }
        }
        
        viewModelScope.launch {
            combine(todayTasks, allTemplates) { tasks, templates ->
                Pair(tasks, templates)
            }.collect { (tasks, templates) ->
                notificationHelper.scheduleAlarmsForTasks(tasks, templates)
            }
        }
    }

    private suspend fun syncQuoteForToday(dateStr: String) {
        try {
            Log.d("AriseApp", "Attempting to sync quote for $dateStr")
            val response = apiService.getQuotes()
            val quotes = response.quotes
            Log.d("AriseApp", "Successfully parsed ${quotes.size} quotes from JSON backend.")

            val approvedSources = setOf(
                "Marcus Aurelius", "Seneca", "Epictetus", "Aristotle", "Socrates",
                "Friedrich Nietzsche", "Arthur Schopenhauer", "Confucius", "Chanakya",
                "Baltasar Gracián", "Niccolò Machiavelli", "Viktor Frankl", "Carl Jung",
                "Sun Tzu", "Miyamoto Musashi", "Dwight D. Eisenhower", "Theodore Roosevelt",
                "George S. Patton", "James Mattis", "Admiral James Stockdale",
                "Richard Feynman", "Benjamin Franklin", "James Clear", "Cal Newport",
                "Naval Ravikant", "Bruce Lee", "Julius Caesar", "Alexander the Great",
                "Guts", "Guts (Berserk)", "Askeladd", "Thorfinn", "Fang Yuan", 
                "Bruce Wayne", "Batman", "Alfred Pennyworth", "Sherlock Holmes", 
                "Tony Stark", "Uncle Iroh", "Johan Liebert", "Thomas Shelby"
            )

            val rejectedCategories = setOf(
                "motivation", "generic", "social media", "wealth", "entrepreneurship",
                "romance", "romantic", "spiritual", "science", "trivia"
            )

            val filteredQuotes = quotes.filter { q ->
                val isApprovedSource = approvedSources.any { 
                    it.equals(q.author, ignoreCase = true) || 
                    it.equals(q.source, ignoreCase = true) || 
                    q.author.contains(it, ignoreCase = true) 
                }
                val isRejectedCategory = rejectedCategories.any { q.category.contains(it, ignoreCase = true) }
                isApprovedSource && !isRejectedCategory
            }

            if (filteredQuotes.isNotEmpty()) {
                val shuffled = filteredQuotes.shuffled()
                var newQuote: QuoteDto? = null
                for (q in shuffled) {
                    val uniqueId = "${q.author}_${q.quote.hashCode()}"
                    if (repository.checkQuoteExists(uniqueId) == null) {
                        newQuote = q
                        break
                    }
                }
                
                if (newQuote == null) {
                    newQuote = shuffled.firstOrNull()
                }
                
                if (newQuote != null) {
                    val uniqueId = "${newQuote.author}_${newQuote.quote.hashCode()}"
                    val entity = QuoteEntity(
                        uniqueId = uniqueId,
                        text = newQuote.quote,
                        author = newQuote.author,
                        source = newQuote.source,
                        category = newQuote.category,
                        dateDownloaded = dateStr
                    )
                    Log.d("AriseApp", "Inserting new unique quote: ${entity.text}")
                    repository.insertQuote(entity)
                    settingsRepository.setCurrentQuoteId(uniqueId)
                    settingsRepository.setCurrentQuoteDate(dateStr)
                }
            } else {
                Log.d("AriseApp", "No approved quotes found. Picking random from local database.")
                val randomDbQuote = repository.getRandomQuote()
                if (randomDbQuote != null) {
                    settingsRepository.setCurrentQuoteId(randomDbQuote.uniqueId)
                    settingsRepository.setCurrentQuoteDate(dateStr)
                }
            }
        } catch (e: Exception) {
            Log.e("AriseApp", "Failed to fetch quotes from backend. Previous quote remains active.", e)
            // No internet or API failed, keep previous quote and DO NOT set currentQuoteDate.
            // Loop will retry every minute.
        }
    }

    private suspend fun checkAndInitializeDay(dateStr: String) {
        val tasks = repository.getTasksForDate(dateStr).first()
        if (tasks.isEmpty()) {
            val templates = repository.getAllTemplates().first()
            val newTasks = templates.map { temp ->
                DailyTaskEntity(
                    date = dateStr,
                    templateId = temp.id,
                    title = temp.title,
                    startTimeStr = temp.startTimeStr,
                    endTimeStr = temp.endTimeStr,
                    isMissed = false,
                    orderIndex = temp.orderIndex
                )
            }
            repository.insertTasks(newTasks)
        }
        
        val report = repository.getReportForDate(dateStr)
        if (report == null) {
            repository.insertReport(DailyReportEntity(date = dateStr, status = ""))
        }
        
        // Check if yesterday was missed (no report submitted and not skipped)
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val date = format.parse(dateStr)
            if (date != null) {
                val yesterdayCal = Calendar.getInstance().apply {
                    time = date
                    add(Calendar.DAY_OF_YEAR, -1)
                }
                val yesterdayStr = format.format(yesterdayCal.time)
                val yesterdayReport = repository.getReportForDate(yesterdayStr)
                if (yesterdayReport != null && !yesterdayReport.isSubmitted && !yesterdayReport.isSkipped) {
                    // Missed report -> streak resets
                    settingsRepository.setCurrentStreak(0)
                    repository.insertReport(yesterdayReport.copy(status = "missed"))
                }
            }
        } catch (e: Exception) {
            Log.e("AriseApp", "Error parsing date", e)
        }
    }

    fun toggleTaskMissed(task: DailyTaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isMissed = !task.isMissed))
            triggerCloudSync()
        }
    }

    fun submitReport(missedCount: Int, totalCount: Int, reason: String?, otherReason: String?) {
        viewModelScope.launch {
            val dateStr = _currentDate.value
            val report = repository.getReportForDate(dateStr) ?: return@launch
            
            val status = if (missedCount == 0) "perfect" else "partial"
            val updatedReport = report.copy(
                isSubmitted = true,
                missedTasksCount = missedCount,
                totalTasksCount = totalCount,
                reason = reason,
                otherReason = otherReason,
                status = status
            )
            repository.insertReport(updatedReport)
            
            val current = settingsRepository.currentStreak.first()
            settingsRepository.setCurrentStreak(current + 1)
            triggerCloudSync()
        }
    }

    fun skipToday() {
        viewModelScope.launch {
            val dateStr = _currentDate.value
            val report = repository.getReportForDate(dateStr) ?: return@launch
            if (!report.isSubmitted) {
                repository.insertReport(report.copy(isSkipped = true, status = "skipped", isSubmitted = true))
            triggerCloudSync()
            }
        }
    }

    fun updateRoutineTemplate(template: RoutineTemplateEntity) {
        viewModelScope.launch { 
            repository.updateTemplate(template) 
            // We should sync today's tasks if we update a template
            val tasks = repository.getTasksForDate(_currentDate.value).first()
            val taskToUpdate = tasks.find { it.templateId == template.id }
            if (taskToUpdate != null) {
                repository.updateTask(taskToUpdate.copy(
                    title = template.title,
                    startTimeStr = template.startTimeStr,
                    endTimeStr = template.endTimeStr,
                    orderIndex = template.orderIndex
                ))
            }
            triggerCloudSync()
        }
    }
    
    fun addRoutineTemplate(template: RoutineTemplateEntity) {
        viewModelScope.launch { 
            repository.insertTemplate(template) 
            // Sync today's tasks by just dropping and recreating? No, let's keep it simple.
            // A clean way is just deleting today's tasks and letting checkAndInitializeDay recreate.
            repository.deleteTasksForDate(_currentDate.value)
            checkAndInitializeDay(_currentDate.value)
            triggerCloudSync()
        }
    }

    fun deleteRoutineTemplate(id: Int) {
        viewModelScope.launch { 
            repository.deleteTemplate(id)
            repository.deleteTasksForDate(_currentDate.value)
            checkAndInitializeDay(_currentDate.value)
            triggerCloudSync()
        }
    }

    private fun getCurrentDateStr(): String {
        val realTimeMs = System.currentTimeMillis() + (timeOffsetMs ?: 0L)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(realTimeMs))
    }
    
    fun getTasksForDateFlow(date: String) = repository.getTasksForDate(date)

    fun setCurrentDate(date: String) {
        _currentDate.value = date
    }
    fun exportBackup(context: android.content.Context, uri: android.net.Uri, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val templates = repository.getAllTemplatesSync()
                val tasks = repository.getAllTasksSync()
                val reports = repository.getAllReportsSync()
                val streak = settingsRepository.currentStreak.first()
                val dateStr = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault()).format(Date())
                
                val backup = AppBackup(templates, tasks, reports, streak, dateStr)
                
                val moshi = com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
                val adapter = moshi.adapter(AppBackup::class.java)
                val json = adapter.toJson(backup)
                
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(json.toByteArray())
                }
                onResult(true, null)
            } catch (e: Exception) {
                Log.e("Backup", "Error exporting backup", e)
                onResult(false, e.message)
            }
        }
    }
    
    fun importBackup(context: android.content.Context, uri: android.net.Uri, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                }
                
                if (json != null) {
                    val moshi = com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
                    val adapter = moshi.adapter(AppBackup::class.java)
                    val backup = adapter.fromJson(json)
                    
                    if (backup != null) {
                        repository.deleteAllTemplates()
                        repository.deleteAllTasks()
                        repository.deleteAllReports()
                        
                        for (t in backup.templates) {
                            repository.insertTemplate(t)
                        }
                        repository.insertTasks(backup.tasks)
                        for (r in backup.reports) {
                            repository.insertReport(r)
                        }
                        settingsRepository.setCurrentStreak(backup.currentStreak)
                        triggerCloudSync()
                        onResult(true, null)
                        return@launch
                    }
                }
                onResult(false, "Invalid backup format")
            } catch (e: Exception) {
                Log.e("Backup", "Error importing backup", e)
                onResult(false, e.message)
            }
        }
    }
}
