package edu.du.ict4361.philosophers;

/////////////////////////////////////
// Fork.java
// Author: M Schwartz
//
// This implementation of a Fork is a semaphore.
// Threads (Philosophers) trying to acquire the fork may wait if the semaphore is in use.
// Threads are expected to release the fork, and thus the semaphore, when done.
//
// When a semaphore is acquired, the caller blocks until it is available--or the acquisition is interrupted.
// Shutting down our Philosopher's Diner interrupts all the "pickUp"s.
//
// The current holder is noted by hir seat number at the table.
//   -1 means no one is holding the fork
//   -2 means shutdown is in progress.
/////////////////////////////////////

import java.util.concurrent.Semaphore;

public class Fork {
    private Semaphore semaphore = new Semaphore(1); // or Semaphore(1,true);
    private final String name;
    private int currentHolder = -1;
    
    public Fork(String name) {
        this.name = name;
    }
    
    public void pickUp(int seatNumber) {
        if (currentHolder != seatNumber ) {
          try {
              semaphore.acquire();
              currentHolder = seatNumber;
          } catch ( InterruptedException ie) {
              System.err.println("      Fork: "+name+" was interrupted in pickUp");
          }
        } else { // Can't pick up the fork if I have already picked it up.
            System.err.println("Thread "+name+": attempt to pick up fork already picked up!"+
                    " ("+currentHolder+" != "+seatNumber+" )");
        }
    }
    
    public void putDown(int seatNumber) {
        if ( seatNumber == -2 ) { // Special value for shutdown. Put it down.
            currentHolder = -2;
            semaphore.release();
        } else if ( currentHolder == seatNumber ) { // I current have it. Put it down.
            currentHolder = -1;
            semaphore.release();
        } else if ( currentHolder == -1 ) {  // No one has it. That's an error.
            System.err.println("Thread "+name+": attempt to put down fork never picked up!"+
                    " ("+currentHolder+" != "+seatNumber+" )");            
        }
    }
    
    public boolean isInUse() {
        return currentHolder != -1;
    }

    public String getName() {
        return name;
    }
    
    public int getCurrentHolder() {
        return currentHolder;
    }

}
