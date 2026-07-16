import java.util.Calendar

fun main() {
    val currentCal = Calendar.getInstance()
    currentCal.set(Calendar.HOUR_OF_DAY, 17)
    currentCal.set(Calendar.MINUTE, 0)
    currentCal.set(Calendar.SECOND, 0)
    
    val startCal = currentCal.clone() as Calendar
    startCal.set(Calendar.HOUR_OF_DAY, 21)
    
    val endCal = currentCal.clone() as Calendar
    endCal.set(Calendar.HOUR_OF_DAY, 4)
    
    val cutoffCal = endCal.clone() as Calendar
    cutoffCal.add(Calendar.HOUR_OF_DAY, 4)
    
    if (currentCal.before(cutoffCal)) {
        startCal.add(Calendar.DAY_OF_YEAR, -1)
    } else {
        endCal.add(Calendar.DAY_OF_YEAR, 1)
    }
    
    val isPast = !currentCal.before(endCal)
    val isCurrent = !currentCal.before(startCal) && currentCal.before(endCal)
    
    println("startCal: ${startCal.time}")
    println("endCal: ${endCal.time}")
    println("current: ${currentCal.time}")
    println("isPast: $isPast, isCurrent: $isCurrent")
}
