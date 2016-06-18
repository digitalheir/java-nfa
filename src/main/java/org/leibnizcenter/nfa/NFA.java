package org.leibnizcenter.nfa;

import com.github.krukow.clj_ds.PersistentList;
import com.google.common.collect.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Immutable NFA
 * <p>
 * Created by maarten on 15-6-16.
 */
@SuppressWarnings("WeakerAccess")
public class NFA<S extends State, E extends Event> {
    public final Map<S, Multimap<E, Transition<S, E>>> transitions;
    public final Set<S> states;
    public final Multimap<E, S> statesThatAllowEvent;

    private NFA(Builder<S, E> builder) {
        this.states = ImmutableSet.copyOf(builder.states);

        ImmutableMap.Builder<S, Multimap<E, Transition<S, E>>> immTransitions = new ImmutableMap.Builder<>();

        // O(transitions.numberOfBranches())
        builder.transitions.forEach((state, eventToTransitionMap) -> {
            final ImmutableMultimap.Builder<E, Transition<S, E>> eventToTransitionMapBuilder = new ImmutableMultimap.Builder<>();
            eventToTransitionMap.forEach(eventToTransitionMapBuilder::putAll);
            immTransitions.put(state, eventToTransitionMapBuilder.build());
        });
        this.transitions = immTransitions.build();

        // O(transitions.numberOfBranches())
        ImmutableMultimap.Builder<E, S> immStatesThatAllowEvent = new ImmutableMultimap.Builder<>();
        builder.transitions.forEach((state, eventMap) ->
                eventMap.forEach((event, transitions) ->
                        immStatesThatAllowEvent.put(event, state)
                )
        );
        statesThatAllowEvent = immStatesThatAllowEvent.build();

        //
        // Sanity check:
        //

        // O(transitions.numberOfBranches())
        builder.transitions.forEach((state, eventMap) -> {
                    assert (eventMap.values().stream()
                            .flatMap(Collection::stream)
                            .map(Transition::getFrom)
                            .filter(st4t3 -> !state.equals(st4t3))
                            .limit(1).count() == 0) : "Error in map from state " + state + " to transitions. This is a bug.";
                }
        );
    }

    @SuppressWarnings("unused")
    public PossibleStateTransitionPaths<S, E> getTransitions(S start, LinkedList<E> events) {
        final List<E> events1 = ImmutableList.copyOf(events); // O(n)
        return precomputePaths(events)
                .get(start)
                .get(events1);
    }

    public Stream<State> apply(S start, LinkedList<E> events) {
        final PossibleStateTransitionPaths<S, E> transitions = getTransitions(start, events);
        return transitions.applyRecursive();
    }

    /**
     * O(path.numberOfBranches() * states.numberOfBranches() * transitions.numberOfBranches())
     *
     * @param event Input events to use for computing all possible paths along the NFA
     * @return A map from starting states to a map of input events to an enumeration of possible branches
     */
    public Map<S, Map<List<E>, PossibleStateTransitionPaths<S, E>>> precomputePaths(LinkedList<E> event) {
        PersistentList<E> postFixPath = com.github.krukow.clj_lang.PersistentList.create((Iterable<? extends E>) new ArrayList<E>(0)); // O(1)
        Map<S, Map<List<E>, PossibleStateTransitionPaths<S, E>>> precomputedPaths = new HashMap<>(states.size());// O(1)

        while (event.size() > 0) { // O(path.numberOfBranches()) *
            E lastEvent = event.removeLast(); // O(1)

            postFixPath = postFixPath.plus(lastEvent); // O(1)

            // TODO filter only those states that *can* be reached through the previous action
            for (S state : statesThatAllowEvent.get(lastEvent)) { // O(states.numberOfBranches()) *
                Map<List<E>, PossibleStateTransitionPaths<S, E>> pathsForEvents = precomputedPaths.getOrDefault(state, new HashMap<>());//O(1)
                final Collection<Transition<S, E>> possibleTransitions = getTransitions(state, lastEvent); // O(1)

                PossibleStateTransitionPaths<S, E> possibleBranches;
                if (postFixPath.size() == 1) {
                    possibleBranches = new PossibleStateTransitionPaths<>(state, possibleTransitions, postFixPath, null); // O(1)
                } else {
                    final PersistentList<E> furtherEvents = postFixPath.minus();
                    ImmutableMap.Builder<S, PossibleStateTransitionPaths<S, E>> restPaths = ImmutableMap.builder(); // O(1)
                    possibleTransitions.stream() // O(possibleTransitions.numberOfBranches())
                            .map(Transition::getTo)
                            .distinct()
                            .forEach(possibleTargetState -> {
                                PossibleStateTransitionPaths<S, E> restBranches = precomputedPaths.get(possibleTargetState).get(furtherEvents);
                                assert restBranches != null : "Possible branches must have been calculated for state " + possibleTargetState;
                                restPaths.put(possibleTargetState, restBranches);
                            });
                    possibleBranches = new PossibleStateTransitionPaths<>(
                            state,
                            possibleTransitions,
                            postFixPath,
                            restPaths.build()
                    ); // O(possibleTransitions.numberOfBranches())
                }
                assert !pathsForEvents.containsKey(postFixPath) : "Already computed possible paths for " + postFixPath + "?!";
                pathsForEvents.put(postFixPath, possibleBranches); // O(1)
                precomputedPaths.putIfAbsent(state, pathsForEvents);// O(1)
            }
        }
        return precomputedPaths;
    }

    public Collection<Transition<S, E>> getTransitions(S from, E event) {
        final Multimap<E, Transition<S, E>> eventTransitionMultimap = transitions.get(from);
        if (eventTransitionMultimap != null) return eventTransitionMultimap.get(event);
        else return Collections.emptySet();
    }

    public Set<S> getStates() {
        return states;
    }

    public StateContainer start(S state) {
        return new StateContainer(Collections.singletonList(state));
    }

    @SuppressWarnings("unused")
    public Collection<S> getStatesThatAllowEvent(E e) {
        return statesThatAllowEvent.get(e);
    }

    public static class Builder<S extends State, E extends Event> {
        private final Set<S> states;
        private final Map<S, Map<E, Set<Transition<S, E>>>> transitions;

        public Builder() {
            this.states = new HashSet<>(50);
            transitions = new HashMap<>(50);
        }

        @SuppressWarnings("unused")
        public Builder<S, E> addStates(Collection<S> states) {
            this.states.addAll(states);
            return this;
        }

        public Builder<S, E> addState(S states) {
            this.states.add(states);
            return this;
        }

        /**
         * Will automatically add states if they've not been added separately.
         *
         * @param from  From state
         * @param event Transition event
         * @param to    To state
         * @return This builder
         */
        public Builder<S, E> addTransition(S from, E event, S to) {
            states.add(from);
            states.add(to);

            addTransition(new Transition<>(event, from, to), from, event);

            return this;
        }

        /**
         * Will automatically add states if they've not been added separately.
         *
         * @param transitionsToAdd List of transitions. Implicit states will be added if necessary.
         * @return This builder
         */
        public Builder<S, E> addTransitions(Collection<Transition> transitionsToAdd) {
            transitionsToAdd.forEach(this::addTransition);
            return this;
        }

        /**
         * Will automatically add states if they've not been added separately.
         *
         * @param transition Implicit states will be added if necessary.
         * @return This builder
         */
        public Builder<S, E> addTransition(Transition<S, E> transition) {
            S from = transition.from;
            S to = transition.to;
            E event = transition.event;
            states.add(from);
            states.add(to);
            addTransition(transition, from, event);
            return this;
        }

        private void addTransition(Transition<S, E> transition, S from, E event) {
            Map<E, Set<Transition<S, E>>> eventsForState = transitions.getOrDefault(from, new HashMap<>());
            Set<Transition<S, E>> transitionsForEvent = eventsForState.getOrDefault(event, new HashSet<>());
            transitionsForEvent.add(transition);
            eventsForState.putIfAbsent(event, transitionsForEvent);
            transitions.putIfAbsent(from, eventsForState);
        }


        public NFA<S, E> build() {
            return new NFA<>(this);
        }
    }

    public class StateContainer {
        public Collection<S> states;

        public StateContainer(Collection<S> ses) {
            states = ses;
        }

        public StateContainer andThen(E e) {
            states = states.stream()
                    .flatMap(from -> getTransitions(from, e).stream().map(transition -> {//noinspection unchecked
                        e.accept(transition.getFrom(), transition.getTo());
                        return transition;
                    })).map(Transition::getTo).collect(Collectors.toList());
            return this;
        }

        public Stream<S> getState() {
            return states.stream();
        }
    }
}
