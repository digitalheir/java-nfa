package org.leibnizcenter.nfa;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.leibnizcenter.nfa.TEvents.eventA;
import static org.leibnizcenter.nfa.TEvents.eventB;
import static org.leibnizcenter.nfa.TStates.S0;
import static org.leibnizcenter.nfa.TStates.S1;

/**
 * Created by maarten on 16-6-16.
 */
public class NFATest {

    public static final Transition<TStates, TEvents> TRANSITION_S0_A_S0 = new Transition<>(S0, eventA, S0);
    public static final Transition<TStates, TEvents> TRANSITION_S1_A_S1 = new Transition<>(S1, eventA, S1);
    public static final Transition<TStates, TEvents> TRANSITION_S0_A_S1 = new Transition<>(S0, eventA, S1);

    @Test
    public void getPathsForInput() throws Exception {
        NFA.Builder<TStates, TEvents> b = new NFA.Builder<>();

        b.addTransition(S0, eventA, S0);
        b.addTransition(S1, eventA, S1);
        b.addTransition(S0, eventA, S1);
        final NFA<TStates, TEvents> nfa = b.build();

        final LinkedList<TEvents> events = new LinkedList<>(Lists.newLinkedList(Lists.newArrayList(
                eventA,
                eventA,
                eventA,
                eventA,
                eventA,
                eventA,
                eventA,
                eventA,
                eventA,
                eventA,
                eventA,
                eventA,
                eventA
        )));
        List<Event> events1 = ImmutableList.copyOf(events);
        Map<TStates, Map<List<TEvents>, PossibleStateTransitionPaths<TStates, TEvents>>> possiblePaths = nfa.precomputePaths(events);
//        for (Map.Entry<List<Event>, PossibleBranches> entry : possiblePaths.get(S0).entrySet()) {
//            System.out.println(entry.getKey());
//            System.out.println(events1);
//            System.out.println(events1.equals(entry.getKey()));
//            System.out.println(entry.getKey().equals(events1));
//        }

        final PossibleStateTransitionPaths<TStates, TEvents> transitions = possiblePaths.get(S0).get(events1);
        assertEquals(transitions.numberOfBranches(), 14);
        final int transitionNumber = 104;
        assertEquals(transitions.size(), transitionNumber);
        final int[] i = {0};

        transitions.parallelStream().forEach(ignored -> i[0]++);
        assertEquals(i[0], transitionNumber);
        i[0] = 0;
        transitions.stream().forEach(ignored -> i[0]++);
        assertEquals(i[0], transitionNumber);
        i[0] = 0;
        for (Transition<TStates, TEvents> ignored : transitions) i[0]++;
        assertEquals(i[0], transitionNumber);
    }

    @Test
    public void getTransitions() throws Exception {
        NFA.Builder<TStates, TEvents> b = new NFA.Builder<>();

        b.addTransition(TRANSITION_S0_A_S0);
        b.addTransition(TRANSITION_S1_A_S1);
        b.addTransition(TRANSITION_S0_A_S1);
        final NFA<TStates, TEvents> nfa = b.build();

        //noinspection unchecked
        assertEquals(new HashSet<>(nfa.getTransitions(S0, eventA)), Sets.newHashSet(new Transition<>(S0, eventA, S0), new Transition<>(S0, eventA, S1)));
        assertEquals(new HashSet<>(nfa.getTransitions(S0, eventB)), Sets.newHashSet());
        assertEquals(new HashSet<>(nfa.getTransitions(S1, eventB)), Sets.newHashSet());
    }


}