import re

with open("app/src/main/java/com/example/ui/DisciplineViewModel.kt", "r") as f:
    content = f.read()

target = """    init {"""
replacement = """    fun syncTimeNow() {
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

    init {"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/DisciplineViewModel.kt", "w") as f:
        f.write(content)
    print("Success")
else:
    print("Not found")
