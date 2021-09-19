/**
 * Dining Philosophers js
 * Starting with
 * Fedor Sheremetyev: gist.github.com/sheremetyev, downloaded 6/13/2017
 */
 "use strict";

var eatingCounts = {};
var iterations = 0;
const MAX_ITERATIONS=5;
var doExit = false;

// Report function added to check for "fairness" of eating.
function report() {
    // Just dump the object name/value pairs
    var stats = Object.getOwnPropertyNames(eatingCounts).map( (i) => i + "->" + eatingCounts[i]).join(", ");
    var sum = Object.getOwnPropertyNames(eatingCounts).reduce( (i,j) => eatingCounts[j] + i , 0);

    console.error(Date()+": "+stats+": Iterations: "+iterations+" Portions eaten: "+sum);
    if ( iterations > 3 && sum === 0 ) {
        console.error("Deadlock detected. Philosophers are starving!");
        doExit = true;
        return;
    }
    if ( iterations > MAX_ITERATIONS ) {
        console.error("Maximum iterations reached. Philosophers are done eating.");
        doExit = true;
        return;
    }

    iterations += 1;
    setTimeout(report, 8000);
}

function Phil(me, left, right) {
    // Philosophers name is the me.
    eatingCounts[me] = 0;

    var myPause = Math.random() * 1100 + 1500; // Adding the randomness can stop some of the deadlocks
    // var myPause = 1000; // Fixed, one-second pauses will allow deadlocks

    var run = function() {
        sequential([
            500, // pause
            function() { console.log(me + ' sits'); }, // reports
            myPause, // "random" pause to prevent initial deadlock, and reduce deadlock opportunities
            left, // channel
            function() { console.log(me + ' picked left fork'); }, // reports
            500,
            right, // channel
            function() { console.log(me + ' picked right fork'); }, // reports
            500,
            function() { console.log(me + ' eats'); eatingCounts[me]+=1; }, // reports
            500,
            left,
            function() { console.log(me + ' dropped left fork'); }, // reports
            500,
            right,
            function() { console.log(me + ' dropped right fork'); }, // reports
            500,
            function() { setTimeout(run, 0); }
        ]);
    };
    setTimeout(run, 1000); // Start the thread
}

function Fork(me, left, right) {
    var run = function() {
        rendezvous(
            left, function() {
                rendezvous(left, function() {
                    setTimeout(run, 0);
                });
            },
            right, function() {
                rendezvous(right, function() {
                    setTimeout(run,0);
                });
            }
        );
    };
    setTimeout(run, 0);
}

function sequential(steps) {
    if (steps.length == 0 || doExit ) {
        return;
    }

    var first = steps[0];
    var rest  = steps.slice(1);
    if (first instanceof channel) {
       //  console.log ("first is a channel. Passing on");
        rendezvous(first, function() { sequential(rest); } );
    } else if (first > 0 ) { // Test first to see if it is a number
        // console.log ("first is a number, paused before continuing");
        setTimeout(function() { sequential(rest); }, first);
    } else { // It is a function
        //  console.log("First is a function. Calling it and passing on.")
        first();
        sequential(rest);
    }
}

function rendezvous() {
    var args = arguments;
    var request = [];
    for (var i = 0; i < args.length / 2; i += 1) {
        request.push( { chan: args[i*2], func: args[i*2+1] } );
    }

    for (var i = 0; i < request.length; i += 1) {
        request[i].chan.add(request);
    }

    for (var i = 0; i < request.length; i += 1) {
        if (request[i].chan.match()) {
            break;
        }
    }
}

function channel() {
    var self = this;
    var requests = [];

    self.add = function(req) {
        requests.push(req);
    }

    self.remove = function(req) {
        for (var i = 0; i < requests.length; i += 1) {
            if (requests[i] == req) {
                requests.splice(i, 1);
                break;
            }
        }
    }

    self.match = function() {
        if (requests.length != 2) {
            return false;
        }

        // copy array since it will be modified by 'remove' method call
        var reqs = requests.slice(0);
        var funcs = [];

        for (var i = 0; i < 2; i += 1) {
            var req = reqs[i];
            for (var j = 0; j < req.length; j += 1) {
                if (req[j].chan == self) {
                    funcs.push(req[j].func);
                    break;
                }
            }

            for (var j = 0; j < req.length; j += 1) {
                req[j].chan.remove(req);
            }
        }

        // assert(funcs.length == 2);
        // assert(requests.length == 0);

        for (var i = 0; i < funcs.length; i += 1) {
            funcs[i]();
        }
    }
}

var philToLeftFork = [];
var philToRightFork = [];

var N = 5;

for (var i = 0; i < N; i += 1) {
    philToLeftFork.push(new channel());
    philToRightFork.push(new channel());
}

for (var i = 0; i < N; i += 1) {
    Phil("Philosopher-"+i, philToRightFork[(i+1)%N], philToLeftFork[i]);
    Fork(i, philToLeftFork[i], philToRightFork[i]);
}

console.log("Started");
setTimeout(report,1000);
