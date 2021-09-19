package edu.du.ict4361.philosophers;

////////////////////////////////////
// Dinner.java
// Author: M Schwartz
//
// A Dinner is an occasion with a Table at which Philosophers will sit between Forks and eat() and think().
//
// The Dinner uses dependency injection to set up the Table and Philosopher time defaults.
// The dinner populates and starts the Table.
////////////////////////////////////

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

/**
 * Runs a Dining Philosopher table with provided parameters
 * @author michael
 *
 */
public class Dinner {

    private final Table table;
    private int numberOfEatenMeals = -1;
    private Instant instant;

    public Dinner(int seats) {
        table = new Table(seats);
    }

    public boolean isDeadlocked() {
        boolean result = false;
        if (instant == null) {
            instant = Instant.now();
            numberOfEatenMeals = table.getTotalMeals();

        } else {
            int numMeals = table.getTotalMeals();
            Instant t = Instant.now();
            if ( Duration.between(instant, t).compareTo(Duration.ofSeconds(deadlockTime / 1000) ) >= 0 ) {
                int diff = numMeals - numberOfEatenMeals;
                instant = t;
                numberOfEatenMeals = numMeals;
                if (diff == 0) {
                    result = true;
                }
            }
        }
        return result;
    }
    
    // Compute a pseudo-random integer between min and max (uniform distribution)
    private static int getRandomNumber(int min, int max ) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    public static void usage() {
        System.out.println("Usage");
        System.out.println("Dinner"
                           + " --philosophers num"
                           + " --think-time ms"
                           + " --eat-time ms"
                           + " --fork-time ms"
                           + " --status-time ms"
                           + " --deadlock ms" 
                           + " --wait-time ms"
                           + " --iterations num");
        System.exit(0);
    }
    
    /*
     * -p: Number of philosophers --philosophers -t: Think time (millis)
     * --think-time -e: Eat time (millis) --eat-time -f: Time needed to pick up
     * fork (millis) --fork-time -s: Status interval (millis) --status-time -d:
     * Deadlock detection interval (millis) --deadlock-time
     */
    public static void parseArgs(String[ ] args) {
        for (int i=0; i<args.length; i++) {
            switch (args[i]) {
            case "-p":
            case "--philosophers":
                i++;
                numberOfPhilosophers = Integer.valueOf(args[i]);
                break;
            case "-t":
            case "--think-time":
                i++;
                thinkTime = Integer.valueOf(args[i]);
                break;
            case "-e":
            case "--eat-time":
                i++;
                eatTime = Integer.valueOf(args[i]);
                break;
            case "-f":
            case "--fork-time":
                i++;
                forkTime = Integer.valueOf(args[i]);
                break;
            case "-s":
            case "--status-time":
                i++;
                statusTime = Integer.valueOf(args[i]);
                break;
            case "-d":
            case "--deadlock-time":
                i++;
                deadlockTime = Integer.valueOf(args[i]);
                break;
            case "-i": case "--iterations":
                i++;
                iterations = Integer.valueOf(args[i]);
                break;
            case "-w": case "--wait-time":
                i++;
                waitTime = Integer.valueOf(args[i]);
                break;
            case "-h": case "--help":
                usage();
                break;
            default:
                System.err.println("Command line option "+args[i]+" not understood. Ignored.");
                break;
            }
        }
    }
    
    // Default values
    private static int numberOfPhilosophers=5;
    private static int thinkTime=1000;
    private static int eatTime=1000;
    private static int forkTime=1000;
    private static int statusTime=6000;
    private static int deadlockTime=3000;
    private static int iterations = 7;
    // To avoid early deadlock, waitTime > forkTime (perhaps waitTime = forktime + 10)
    private static int waitTime = forkTime + getRandomNumber(-8,10); 
    
    public static void main(String[] args) {
        parseArgs(args);
        Dinner dinner = new Dinner(numberOfPhilosophers);
        Dinner.setThinkTime(thinkTime);
        Dinner.setEatTime(eatTime);
        Dinner.setForkTime(forkTime);
        Table.setPauseTime(waitTime);
        
        dinner.table.populateTable();
        
        dinner.printConfiguration();

        dinner.table.startTable();
        
        // Run the table for a number of status iterations
        for (int i = 0; i < iterations; i++) {
            try {
                Thread.sleep(statusTime);
            } catch (Exception e) {
            }
            System.out.println(dinner.table.getTableStatus());
            if (dinner.isDeadlocked()) {
                System.err.println("Deadlock detected. Shutdown may generate error messages.");
                break;
            }
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
        sb.append("  Status iterations:      "+iterations+" (about "+(iterations*statusTime / 1000 )+" seconds)");
        System.out.println(sb);
    }
    
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
        System.out.println(sb);
    }
}
