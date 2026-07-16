import java.io.File

fun main() {
    val file = File("app/src/main/java/com/example/ui/DisciplineViewModel.kt")
    var content = file.readText()
    
    val initBlockOld = """    init {
        viewModelScope.launch {
            // Initial sync
            try {
                Log.d("AriseApp", "Attempting initial internet time sync...")
                val timeResponse = timeApiService.getCurrentTime()
                val internetTimeMs = timeResponse.unixtime * 1000L
                timeOffsetMs = internetTimeMs - System.currentTimeMillis()
                Log.d("AriseApp", "Time offset calculated: ${'$'}timeOffsetMs ms")
            } catch (e: Exception) {
                Log.e("AriseApp", "Initial time sync failed", e)
            }

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
        }"""
        
    val initBlockNew = """    init {
        viewModelScope.launch {
            while (true) {
                try {
                    Log.d("AriseApp", "Attempting internet time sync...")
                    val timeResponse = timeApiService.getCurrentTime()
                    val internetTimeMs = timeResponse.unixtime * 1000L
                    timeOffsetMs = internetTimeMs - System.currentTimeMillis()
                    Log.d("AriseApp", "Time offset calculated: ${'$'}timeOffsetMs ms")
                } catch (e: Exception) {
                    Log.e("AriseApp", "Time sync failed, using device time", e)
                    timeOffsetMs = 0L // Fallback to device time
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
        }"""
        
    content = content.replace(initBlockOld, initBlockNew)
    file.writeText(content)
}
