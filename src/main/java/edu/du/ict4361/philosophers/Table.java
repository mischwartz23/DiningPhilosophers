package edu.du.ict4361.philosophers;

////////////////////////////////////
// Table.java
// Author: M Schwartz
//
// Table represents a round table with the same number of Philosophers and Forks
// Table holds the Philosophers and the Forks, and associates them.
// The Table creates and assigns the Philosophers to a fixed array.
// The Table creates and assigns the Forks to a fixed array.
// The Table creates and maintains a thread for each Philosopher
//     (so it can shut them down later)
// The Table uses the array positions to represent seats at the table,
//     and the association of left and right Forks to each Philosopher
//
// Some statistical functions are added to the Table for a summary report.
////////////////////////////////////
import java.util.Arrays;

/**
 * The table is the coordinating class for the dining philosophers.
 * It holds the philosophers (and their threads) and the forks
 * @author michael
 *
 */
public class Table {
    private final Philosopher[] philosophers;
    private final Fork[] forks;
    private final Thread[] philosopherThreads;
    
    // Volatile boolean on whether to keep going
    private volatile boolean partyIsOn = true;
    
    private static int pauseTime = 90;

    // Question: Will thread priorities matter?
    public Table(int numberOfSeats) {
        // Must have at least 2 philosophers
        if (numberOfSeats >= 2) {
            philosophers = new Philosopher[numberOfSeats];
            forks = new Fork[numberOfSeats];
            philosopherThreads = new Thread[numberOfSeats];
        } else {
            throw new IllegalArgumentException("Must have at least 2 philosophers");
        }
    }
    
    // Helper function to cause a thread to sleeps and ignore
    // InterruptedException events
    private void waitTime(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            // do nothing but report
            System.out.println("    Wait interrupted.");
        }
    }
    
    public void startTable() {
        for (Thread thread: philosopherThreads)  {
            thread.start();
            waitTime(pauseTime); // Pause briefly to avoid out-of-the-gate deadlock (if > forkTime)
        }
    }

    public void populateTable() {
        for (int i = 0; i < philosophers.length; i++) {
            forks[i] = new Fork("Fork " + (i + 1));
            philosophers[i] = new Philosopher("Phil " + (i + 1), this);
            philosophers[i].setSeatNumber(i);
            philosopherThreads[i] = new Thread(philosophers[i]);
        }
    }

    public Philosopher getPhilosopher(int seat) {
        if (seat >= 0 && seat < philosophers.length) {
            return philosophers[seat];
        }
        return null;
    }

    public Fork getLeftFork(int seat) {
        if (seat >= 0 && seat < philosophers.length) {
            return forks[seat];
        }
        return null;
    }

    public Fork getRightFork(int seat) {
        seat = (seat + 1) % philosophers.length;
        if (seat >= 0 && seat < philosophers.length) {
            return forks[seat];
        }
        return null;
    }

    /// Reporting functions 
    public String getTableStatus() {
        if (!isPartyIsOn()) {
            return "Party is over";
        }
        StringBuffer sb = new StringBuffer();
        sb.append("Table status: table has ");
        sb.append(philosophers.length);
        sb.append(" seats");
        sb.append("\n");
        for (Philosopher phil : philosophers) {
            sb.append("  ");
            sb.append(phil.getStatus());
            sb.append("\n");
        }
        return sb.toString();
    }

    /// Getters and setters
    
    public static void setPauseTime(int amt) {
        pauseTime = amt;
    }
    public static int getPauseTime() {
        return pauseTime;
    }

    public int getNumberOfPhilosophers() {
        return philosophers.length;
    }
    
    public boolean isPartyIsOn() {
        return partyIsOn;
    }

    public void setPartyIsOn(boolean partyIsOn) {
        this.partyIsOn = partyIsOn;
        if (partyIsOn == false) {
            System.out.println("Table: Shutting down. Interrupting all philosophers.");
            for (Thread thread: philosopherThreads) {
                thread.interrupt();
            }
            for (Fork f : forks) {
                f.putDown(-2); // Special "seat number" for shutdown
            }
        }
    }

    public Integer getTotalMeals() {
        return Arrays.stream(philosophers).map(item -> item.getNumberOfTimesEating()).reduce((a, b) -> a + b)
                .orElse(-1);
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
    
    ///  Statistics-related functions
    public int getMaximumMeals() {
        int max = 0;
        for (int i=0; i<philosophers.length; i++) {
            if ( philosophers[i].getNumberOfTimesEating() > max ) {
                max = philosophers[i].getNumberOfTimesEating();
            }
        }
        return max;
    }
    public int getMinimumMeals() {
        int min = Integer.MAX_VALUE;
        for (int i=0; i<philosophers.length; i++) {
            if ( philosophers[i].getNumberOfTimesEating() < min ) {
                min = philosophers[i].getNumberOfTimesEating();
            }
        }
        return min;
    }
    public double getAverageMeals() {
        int sum = 0;
        for (int i=0; i<philosophers.length; i++) {
                sum += philosophers[i].getNumberOfTimesEating();
        }
        return (double)sum / philosophers.length;
    }
}
