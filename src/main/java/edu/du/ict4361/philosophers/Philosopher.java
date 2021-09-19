package edu.du.ict4361.philosophers;

////////////////////////////////////
// Philosopher.java
// Author: M Schwartz
//
// Each Philosopher object is a thread.
// There are two ways to create a working thread:
// 1) Implement Runnable, implement run(), invest the object in a new Thread, and start() the Thread
//    (that's what this class is)
// 2) Extend Thread, implement run(), create a new object, and start() the object.
//
// In this implementation, each Philosopher has a name (final) and a table. 
// The Table seats them by assigning their seat number (dependency injection).
// The Philosopher's current activity is tracked in an enumeration (STATE), which changes over time.
// Each activity has an associated (fixed) time, which is injected statically into the class
//   (each Philosopher, thus, takes the same amount of time to eat, think, and manipulate a fork).
// The number of "meals" the Philosopher eats() is tracked.
// A deadlock could occur if all Philosophers have their left fork in their hand and won't relinquish it.
//   Then the poor Philosophers will starve :-(
// The state of the Philosopher uses a setter to change.
//   This allows, in the future, a way to measure how much time each Philosopher spends in each state.
////////////////////////////////////
public class Philosopher implements Runnable {
    private final String name;
    private int tableSeatNumber;
    private Table table;

    public enum STATE {
        IDLE, THINKING, EATING, PICKING_UP_FORK, PUTTING_DOWN_FORK
    };

    private volatile STATE state = STATE.IDLE;

    private static int thinkTime = 3000; // milliseconds
    private static int eatTime = 3000; // milliseconds
    private static int forkTime = 3000; // milliseconds

    private int numberOfTimesEating = 0;
    private int numberOfTimesThinking = 0;

    public Philosopher(String name, Table t) {
        this.name = name;
        table = t;
    }

    public void setSeatNumber(int seatNumber) {
        tableSeatNumber = seatNumber;
    }

    private Fork getLeftFork() {
        return table.getLeftFork(tableSeatNumber);
    }

    private Fork getRightFork() {
        return table.getRightFork(tableSeatNumber);
    }

    // Helper function to cause a thread to sleeps and ignore
    // InterruptedException events
    private void waitTime(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            System.err.println("     Philosopher: " + name + " interrupted while " + getState());
            // do nothing
        }
    }

    // Represents a delay while the philosopher thinks
    private void think() {
        setState(STATE.THINKING);
        waitTime(thinkTime);
        setState(STATE.IDLE);
        numberOfTimesThinking++;
    }

    // Represents the sequence of events for the philosopher eating.
    private void eat() {
        setState(STATE.PICKING_UP_FORK);
        getLeftFork().pickUp(tableSeatNumber);
        waitTime(forkTime);
        getRightFork().pickUp(tableSeatNumber);
        waitTime(forkTime);
        setState(STATE.EATING);

        waitTime(eatTime);

        setState(STATE.PUTTING_DOWN_FORK);
        waitTime(forkTime);
        getLeftFork().putDown(tableSeatNumber);
        waitTime(forkTime);
        getRightFork().putDown(tableSeatNumber);
        
        numberOfTimesEating++;
        
        setState(STATE.IDLE);
    }

    public int getNumberOfTimesEating() {
        return numberOfTimesEating;
    }
    
    public int getNumberOfTimesThinking() {
        return numberOfTimesThinking;
    }

    public String getStatus() {
        Fork leftFork = getLeftFork();
        Fork rightFork = getRightFork();
        int lHolder = leftFork.getCurrentHolder();
        int rHolder = rightFork.getCurrentHolder();

        StringBuffer sb = new StringBuffer();
        sb.append(name);
        sb.append(" is assigned forks ");
        sb.append("l: ");
        sb.append(leftFork.getName());
        sb.append(" and ");
        sb.append("r: ");
        sb.append(rightFork.getName());
        sb.append(" and ");
        sb.append(" has eaten ");
        sb.append(numberOfTimesEating);
        sb.append(" times, and currently is holding ");
        boolean left = (lHolder == tableSeatNumber);
        boolean right = (rHolder == tableSeatNumber);
        if (left && right) {
            sb.append("both forks");
        } else if (!left && !right) {
            sb.append("neither fork");
        } else if (left) {
            sb.append("the left fork");
            sb.append(" (" + leftFork.getName() + ") ");
        } else {
            sb.append("the right fork");
            sb.append(" (" + rightFork.getName() + ") ");
        }
        sb.append(" in state " + state);
        return sb.toString();
    }

    @Override
    public void run() {
        // Run until the Table shut down the party
        while (table.isPartyIsOn()) {
            think();
            eat();
        }
    }

    public static int getThinkTime() {
        return thinkTime;
    }

    public static void setThinkTime(int thinkTime) {
        Philosopher.thinkTime = thinkTime;
    }

    public static int getEatTime() {
        return eatTime;
    }

    public static void setEatTime(int eatTime) {
        Philosopher.eatTime = eatTime;
    }
    
    public int getEatTimeTotal() {
        return eatTime * getNumberOfTimesEating();
    }
    
    public int getThinkTimeTotal() {
        return thinkTime * getNumberOfTimesThinking();
    }

    public static int getForkTime() {
        return forkTime;
    }

    public static void setForkTime(int forkTime) {
        Philosopher.forkTime = forkTime;
    }

    public STATE getState() {
        return state;
    }

    // setState could be instrumented to report what percentage of the time is
    // spent
    // in each state.
    private void setState(STATE state) {
        this.state = state;
        Thread.yield();
    }
    
}
