import java.util.Calendar

fun main() {
    val times = listOf(
        "02:00", "03:59", "04:01", "08:01", "17:00", "22:00"
    )
    for (time in times) {
        val currentCal = Calendar.getInstance()
        val parts = time.split(":")
        currentCal.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
        currentCal.set(Calendar.MINUTE, parts[1].toInt())
        currentCal.set(Calendar.SECOND, 0)
        
        val startCal = currentCal.clone() as Calendar
        startCal.set(Calendar.HOUR_OF_DAY, 21)
        startCal.set(Calendar.MINUTE, 0)
        
        val endCal = currentCal.clone() as Calendar
        endCal.set(Calendar.HOUR_OF_DAY, 4)
        endCal.set(Calendar.MINUTE, 0)
        
        if (endCal.before(startCal) || endCal == startCal) {
            val cutoffCal = endCal.clone() as Calendar
            cutoffCal.add(Calendar.HOUR_OF_DAY, 4)
            if (currentCal.before(cutoffCal)) {
                startCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                endCal.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        val isCurrent = !currentCal.before(startCal) && currentCal.before(endCal)
        val isPast = !currentCal.before(endCal)
        
        println("Time $time -> Past: $isPast, Current: $isCurrent")
    }
}
