#!/usr/bin/python

'''
Created on Mar 20, 2011
@author: Daniel Underwood (and Dr. John Baugh)
@author: Michael Schwartz (modification)
see https://github.com/djunderw/dining-philosophers
'''


import sys
import threading
import time
import random

class ChopStick():

    def __init__(self, number):
        self.number = number           # chop stick ID
        self.user = -1                 # keep track of philosopher using it
        self.lock = threading.Condition(threading.Lock())
        self.taken = False

    def take(self, user):         # used for synchronization
        with self.lock:
            while self.taken:     # testing for True
                self.lock.wait()
            self.user = user
            self.taken = True
            sys.stdout.write(f'    p[{user}] took c[{self.number}]\n')
            self.lock.notifyAll()

    def drop(self, user):         # used for synchronization
        with self.lock:
            while self.taken is False:
                self.lock.wait()
            self.user = -1
            self.taken = False
            sys.stdout.write(f'    p[{user}] dropped c[{self.number}]\n')
            self.lock.notifyAll()


class Philosopher (threading.Thread):

    def __init__(self, number, left, right):
        threading.Thread.__init__(self)
        self.number = number            # philosopher number
        self.left = left
        self.right = right
        self.eaten = 0
        self.complete = False

    def run(self):
        # Adding the randomness can stop some of the deadlocks
        my_pause = random.random() * 1.5 + 1.85
        # sys.stdout.write("  DEBUG: p[%d] think interval %f (vs 2.8)\n" % (self.number, my_pause) )
        for _ in range(5):
            time.sleep(my_pause)            # think
            self.left.take(self.number)     # pickup left chopstick
            time.sleep(0.8)                 # (yield makes deadlock more likely)
            self.right.take(self.number)    # pickup right chopstick
            time.sleep(2.0)                 # eat
            self.eaten += 1                 # record eating
            self.right.drop(self.number)    # drop right chopstick
            self.left.drop(self.number)     # drop left chopstick
            sys.stdout.write(f'  p[{self.number}] thought and ate\n')
        self.complete = True
        sys.stdout.write(f' p[{self.number}] finished thinking and eating ({self.eaten})\n')

class Reporter (threading.Thread):

    def __init__(self, philosophers, interval):
        threading.Thread.__init__(self)
        self.philosophers = philosophers
        self.interval = interval

    def run(self):
        for _ in range(8):
            total = 0
            complete = True
            time.sleep(self.interval)
            for phil in self.philosophers:
                total += phil.eaten
                sys.stdout.write(f'p[{phil.number}] has eaten {phil.eaten} times\n')
                if not phil.complete:
                    complete = False
            if total == 0:
                sys.stdout.write("Deadlock detected!\n")
                break
            if complete:
                sys.stdout.write("All philosophers have completed their meals\n")
                break
        # sys.stdout.write("Done reporting\n")

def main():
    # number of philosophers / chop sticks
    n = 5

    # list of chopsticks
    c = [ChopStick(i) for i in range(n)]

    # list of philsophers
    p = [Philosopher(i, c[i], c[(i+1) % n]) for i in range(n)]

    r = Reporter(p, 8.0)
    r.start()

    for i in range(n):
        p[i].start()


if __name__ == "__main__":
    main()
