package org.leibnizcenter.nfa;

import com.github.krukow.clj_ds.PersistentList;
import com.google.common.collect.*;

import java.util.*;

/**
 * Immutable NFA
 * <p>
 * Created by maarten on 15-6-16.
 */
public class NFA {
    public final Map<State, Multimap<Event, Transition>> transitions;
    public final Set<State> states;
    public final Multimap<Event, State> statesThatAllowEvent;

    private NFA(Builder builder) {
        this.states = ImmutableSet.copyOf(builder.states);

        ImmutableMap.Builder<State, Multimap<Event, Transition>> immTransitions = new ImmutableMap.Builder<>();

        // O(transitions.numberOfBranches())
        builder.transitions.forEach((state, eventToTransitionMap) -> {
            final ImmutableMultimap.Builder<Event, Transition> eventToTransitionMapBuilder = new ImmutableMultimap.Builder<>();
            eventToTransitionMap.forEach(eventToTransitionMapBuilder::putAll);
            immTransitions.put(state, eventToTransitionMapBuilder.build());
        });
        this.transitions = immTransitions.build();

        // O(transitions.numberOfBranches())
        ImmutableMultimap.Builder<Event, State> immStatesThatAllowEvent = new ImmutableMultimap.Builder<>();
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

    public PossibleStateTransitionPaths getPathsForInput(State start, LinkedList<Event> events) {
        final List<Event> events1 = ImmutableList.copyOf(events); // O(n)
        return precomputePaths(events)
                .get(start)
                .get(events1);
    }

    /**
     * O(path.numberOfBranches() * states.numberOfBranches() * transitions.numberOfBranches())
     *
     * @param event Input events to use for computing all possible paths along the NFA
     * @return A map from starting states to a map of input events to an enumeration of possible branches
     */
    public Map<State, Map<List<Event>, PossibleStateTransitionPaths>> precomputePaths(LinkedList<Event> event) {
        PersistentList<Event> postFixPath = com.github.krukow.clj_lang.PersistentList.create(); // O(1)
        Map<State, Map<List<Event>, PossibleStateTransitionPaths>> precomputedPaths = new HashMap<>(states.size());// O(1)

        while (event.size() > 0) { // O(path.numberOfBranches()) *
            Event lastEvent = event.removeLast(); // O(1)

            postFixPath = postFixPath.plus(lastEvent); // O(1)

            // TODO filter only those states that *can* be reached through the previous action
            for (State state : statesThatAllowEvent.get(lastEvent)) { // O(states.numberOfBranches()) *
                Map<List<Event>, PossibleStateTransitionPaths> pathsForEvents = precomputedPaths.getOrDefault(state, new HashMap<>());//O(1)
                final Collection<Transition> possibleTransitions = getTransitions(state, lastEvent); // O(1)

                PossibleStateTransitionPaths possibleBranches;
                if (postFixPath.size() == 1) {
                    possibleBranches = new PossibleStateTransitionPaths(state, possibleTransitions, postFixPath, null); // O(1)
                } else {
                    final PersistentList<Event> furtherEvents = postFixPath.minus();
                    ImmutableMap.Builder<State, PossibleStateTransitionPaths> restPaths = ImmutableMap.builder(); // O(1)
                    possibleTransitions.stream() // O(possibleTransitions.numberOfBranches())
                            .map(Transition::getTo)
                            .distinct()
                            .forEach(possibleTargetState -> {
                                PossibleStateTransitionPaths restBranches = precomputedPaths.get(possibleTargetState).get(furtherEvents);
                                assert restBranches != null : "Possible branches must have been calculated for state " + possibleTargetState;
                                restPaths.put(possibleTargetState, restBranches);
                            });
                    possibleBranches = new PossibleStateTransitionPaths(
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

    public Collection<Transition> getTransitions(State from, Event event) {
        final Multimap<Event, Transition> eventTransitionMultimap = transitions.get(from);

        if (eventTransitionMultimap != null) return eventTransitionMultimap.get(event);
        else return Collections.emptySet();
    }

    public Set<State> getStates() {
        return states;
    }

    public static class Builder {
        private final Set<State> states;
        private final Map<State, Map<Event, Set<Transition>>> transitions;

        public Builder() {
            this.states = new HashSet<>(50);
            transitions = new HashMap<>(50);
        }

        public Builder addStates(Collection<State> states) {
            this.states.addAll(states);
            return this;
        }

        public Builder addState(State states) {
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
        public Builder addTransition(State from, Event event, State to) {
            states.add(from);
            states.add(to);

            addTransition(new Transition(event, from, to), from, event);

            return this;
        }

        /**
         * Will automatically add states if they've not been added separately.
         *
         * @param transitionsToAdd List of transitions. Implicit states will be added if necessary.
         * @return This builder
         */
        public Builder addTransitions(Collection<Transition> transitionsToAdd) {
            transitionsToAdd.forEach(this::addTransition);
            return this;
        }

        /**
         * Will automatically add states if they've not been added separately.
         *
         * @param transition Implicit states will be added if necessary.
         * @return This builder
         */
        public Builder addTransition(Transition transition) {
            State from = transition.from;
            State to = transition.to;
            Event event = transition.event;
            states.add(from);
            states.add(to);
            addTransition(transition, from, event);
            return this;
        }

        private void addTransition(Transition transition, State from, Event event) {
            Map<Event, Set<Transition>> eventsForState = transitions.getOrDefault(from, new HashMap<>());
            Set<Transition> transitionsForEvent = eventsForState.getOrDefault(event, new HashSet<>());
            transitionsForEvent.add(transition);
            eventsForState.putIfAbsent(event, transitionsForEvent);
            transitions.putIfAbsent(from, eventsForState);
        }


        public NFA build() {
            return new NFA(this);
        }
    }

    public Collection<State> getStatesThatAllowEvent(Event e) {
        return statesThatAllowEvent.get(e);
    }
}
