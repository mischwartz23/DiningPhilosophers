package edu.du.ict4361.philosophers;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

/**
 * SimpleDinner is a version of Dinner with no command line parameters needed.
 * Also, no deadlock detection.
 * @author michael
 *
 */
public class SimpleDinner {

    private final Table table;

    public SimpleDinner(int seats) {
        table = new Table(seats);
    }

    private static int getRandomNumber(int min, int max ) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }
    
    private static int numberOfPhilosophers=5;
    private static int thinkTime=1000;
    private static int eatTime=1000;
    private static int forkTime=1000;
    private static int statusTime=6000;
    private static int iterations = 7;
    // To avoid early deadlock, waitTime > forkTime (perhaps waitTime = forktime + 10)
    private static int waitTime = forkTime + getRandomNumber(-8,10);
    
    // Time measurement
    private static Instant startTime;
    
    public static void main(String[] args) {
        SimpleDinner dinner = new SimpleDinner(numberOfPhilosophers);
        SimpleDinner.setThinkTime(thinkTime);
        SimpleDinner.setEatTime(eatTime);
        SimpleDinner.setForkTime(forkTime);
        Table.setPauseTime(waitTime);
        
        dinner.table.populateTable();
        
        dinner.printConfiguration();

        startTime = Instant.now();

        dinner.table.startTable();
        
        // Run the table for a number of status iterations
        for (int i = 0; i < iterations; i++) {
            try {
                Thread.sleep(statusTime);
            } catch (Exception e) {
                System.out.println("     Dinner interrupted");
            }
            System.out.println(dinner.table.getTableStatus());
        }
        dinner.table.setPartyIsOn(false); // Release locks so Philosophers can exit
        dinner.printSummaryResult();
    }

    public static int getThinkTime() {
        return Philosopher.getThinkTime();
    }

    public static void setThinkTime(int thinkTime) {
        Philosopher.setThinkTime(thinkTime);
    }

    public static int getEatTime() {
        return Philosopher.getEatTime();
    }

    public static void setEatTime(int eatTime) {
        Philosopher.setEatTime(eatTime);
    }

    public static int getForkTime() {
        return Philosopher.getForkTime();
    }

    public static void setForkTime(int forkTime) {
        Philosopher.setForkTime(forkTime);
    }
    
    // Print the configuration of the Dinner
    public void printConfiguration() {
        StringBuffer sb = new StringBuffer();
        sb.append("Configuration:");
        sb.append("\n");
        sb.append("  Number of philosophers: "+table.getNumberOfPhilosophers());
        sb.append("\n");
        sb.append("  Think time:             "+getThinkTime()+" milliseconds");
        sb.append("\n");
        sb.append("  Eat time:               "+getEatTime()+" milliseconds");
        sb.append("\n");
        sb.append("  Fork time:              "+getForkTime()+" milliseconds");
        sb.append("\n");
        sb.append("  Inter-launch wait time: "+Table.getPauseTime()+" milliseconds");
        sb.append("\n");
        sb.append("  Status interval:        "+statusTime+" milliseconds");
        sb.append("\n");
        sb.append("  Status iterations:      "+iterations+" (about "
                                              + (iterations * statusTime / 1000 )
                                              + " seconds)");
        System.out.println(sb);
    }
    
    // Print the summary of the dinner
    public void printSummaryResult() {
        StringBuffer sb = new StringBuffer() ;
        sb.append("Results:");
        sb.append("\n");
        sb.append("  Total number of eatings: "+table.getTotalMeals());
        sb.append("\n");
        sb.append("  Maximum meals:           "+table.getMaximumMeals());
        sb.append("\n");
        sb.append("  Minimum meals:           "+table.getMinimumMeals());
        sb.append("\n");
        sb.append("  Average meals:           "+String.format("%.2f",table.getAverageMeals()));
        sb.append("\n");
        sb.append("Elapsed time: " + Duration.between(startTime, Instant.now()));
        System.out.println(sb);
    }
}
