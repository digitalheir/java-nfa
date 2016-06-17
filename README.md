# Nondeterministic finite state automata
This is a library that implement [nondeterminstic finite state atuomata](https://en.wikipedia.org/wiki/Nondeterministic_finite_automaton) (NFAs) in Java. You can think of NFAs as flowcharts: you are in a state, take some action, and arrive in a new state. The action can produce a side effect, such as writing a string to a tape. 

## Why?
There are already a bunch of libraries out there which work with deterministic finite state automata, and there is a well-known result in automata theory which says that for any language recognized by an NFA, we can construct a DFA which recognizes the same language.

There are two problems, however:
* Determinizing an NFA has an exponential blowup in the number of states (|stated(DFA)| = O(|states(NFA)|²)
* An NFA may have side-effects, which may be problematic with the standard way of determinizing NFAs. Indeed, [some non-deterministic finite state transducers have no equivalent deterministic finite state transducer](http://www.let.rug.nl/~vannoord/papers/preds/node22.html). 

On a side note, [Allauzen & Mohri](http://www.cs.nyu.edu/~allauzen/pdf/twins.pdf) have described efficient algorithms for determining when a transducer is in fact determinizable, and this would be a nice feature to implement.

## Current features
* Arbitrary input tokens, and arbitrary side effect to state transitions. For example we may implement a finite state transducer by taking strings as input tokens and writing some output string to tape as a side effect.
* Compute possible transition paths in polynomial time! Using a [forward-backward-like algorithm](https://en.wikipedia.org/wiki/Forward%E2%80%93backward_algorithm), we can compute all paths through automaton *A* originating from state *S*, given input *I* all possible paths in O(|*S*| * |*I*| * |*A*|).
* Transition paths can be accessed through a Spliterator: Java 8 streaming APIs can automatically branch transition paths on states where one action may lead to multiple result states.
