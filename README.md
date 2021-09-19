# Dining Philosophers implementations with deadlocks

This set of sources represents the dining philosophers problem with each philosopher set up an as independent task, and each fork (or chopstick) set up as a lockable resources.

The main purpose is to demonstrate that without careful thought, a protocol can go wrong.

For the purpose of exploring ways to avoid deadlock, those mechanisms can be injected into the solutions.

## Languages used

These implementations are done in Python, Node/JavaScript, and Java.

The Python and Node implementation are based on the work of others, and modified to my purposes.

JavaScript implementation: Fedor Sheremetyev: https://gist.github.com/sheremetyev, Downloaded 6/13/2017

Python implementation: Daniel Underwood (and Dr. John Baugh), https://github.com/djunderw/dining-philosophers

The code intent is to be clear and easily modifiable. To that end, I'm afraid not all coding conventions were closely adhered to.
