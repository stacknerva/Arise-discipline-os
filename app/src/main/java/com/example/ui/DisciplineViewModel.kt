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
        }
    }

    fun skipToday() {
        viewModelScope.launch {
            val dateStr = _currentDate.value
            val report = repository.getReportForDate(dateStr) ?: return@launch
            if (!report.isSubmitted) {
                repository.insertReport(report.copy(isSkipped = true, status = "skipped", isSubmitted = true))
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
        }
    }
    
    fun addRoutineTemplate(template: RoutineTemplateEntity) {
        viewModelScope.launch { 
            repository.insertTemplate(template) 
            // Sync today's tasks by just dropping and recreating? No, let's keep it simple.
            // A clean way is just deleting today's tasks and letting checkAndInitializeDay recreate.
            repository.deleteTasksForDate(_currentDate.value)
            checkAndInitializeDay(_currentDate.value)
        }
    }

    fun deleteRoutineTemplate(id: Int) {
        viewModelScope.launch { 
            repository.deleteTemplate(id)
            repository.deleteTasksForDate(_currentDate.value)
            checkAndInitializeDay(_currentDate.value)
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
}
