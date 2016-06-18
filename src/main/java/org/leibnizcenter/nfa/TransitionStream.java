//package org.leibnizcenter.nfa;
//
//import java.util.Comparator;
//import java.util.Iterator;
//import java.util.Optional;
//import java.util.Spliterator;
//import java.util.function.*;
//import java.util.stream.*;
//
///**
// * Wrapper for stream to house some utility methods for NFAs
// * <p>
// * Created by maarten on 18-6-16.
// */
//@SuppressWarnings("unused")
//public class TransitionStream<S extends State, E extends Event> {
//    private final NFA<S, E> nfa;
//    private Stream<Transition<S, E>> stream;
//
//    public TransitionStream(NFA<S, E> nfa, Stream<Transition<S, E>> stream) {
//        this.stream = stream;
//        this.nfa = nfa;
//    }
//
//    public TransitionStream<S, E> andThen(E event) {
//        stream = stream
//
//        ;
//        return this;
//    }
//
//
//    public Stream<S> getState() {
//
//    }
//}
