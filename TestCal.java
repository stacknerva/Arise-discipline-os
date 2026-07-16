import java.util.Calendar;
public class TestCal {
    public static void main(String[] args) {
        Calendar currentCal = Calendar.getInstance();
        currentCal.set(Calendar.HOUR_OF_DAY, 17);
        currentCal.set(Calendar.MINUTE, 0);
        currentCal.set(Calendar.SECOND, 0);
        
        Calendar startCal = (Calendar) currentCal.clone();
        startCal.set(Calendar.HOUR_OF_DAY, 21);
        
        Calendar endCal = (Calendar) currentCal.clone();
        endCal.set(Calendar.HOUR_OF_DAY, 4);
        
        Calendar cutoffCal = (Calendar) endCal.clone();
        cutoffCal.add(Calendar.HOUR_OF_DAY, 4);
        
        if (currentCal.before(cutoffCal)) {
            startCal.add(Calendar.DAY_OF_YEAR, -1);
        } else {
            endCal.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        boolean isPast = !currentCal.before(endCal);
        boolean isCurrent = !currentCal.before(startCal) && currentCal.before(endCal);
        
        System.out.println("startCal: " + startCal.getTime());
        System.out.println("endCal: " + endCal.getTime());
        System.out.println("current: " + currentCal.getTime());
        System.out.println("isPast: " + isPast + ", isCurrent: " + isCurrent);
    }
}
