package org.leibnizcenter.nfa;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.leibnizcenter.nfa.TStates.S0;
import static org.leibnizcenter.nfa.TStates.S1;

/**
 * Created by maarten on 16-6-16.
 */
public class BuilderTest {

    @Test
    public void addState() {
        NFA.Builder<TStates, TEvents> b = new NFA.Builder<>();
        b.addState(S1);
        assertEquals(Sets.newHashSet(S1), b.build().getStates());
    }

    @Test
    public void addTransition() {
        NFA.Builder<TStates, TEvents> b = new NFA.Builder<>();
        final TEvents event = TEvents.eventC;
        b.addTransition(S0, event, S0);
        b.addTransition(S1, event, S1);
        b.addTransition(S0, event, S1);
        final NFA<TStates, TEvents> nfa = b.build();
        assertEquals(Sets.newHashSet(S1, S0), nfa.getStates());
        assertEquals(2, nfa.getTransitions(S0, event).size());
    }

    @Test
    public void addTransitions() {
        NFA.Builder<TStates, Event<TStates>> b = new NFA.Builder<>();
        final TEvents event = TEvents.eventC;
        Transition<TStates, Event<TStates>> e1 = new Transition<>(event, S0, S1);
        Transition<TStates, Event<TStates>> e2 = new Transition<>(S0, event, S0);
        Transition<TStates, Event<TStates>> e3 = Transition.from(S1).through(event).to(S1);
        b.addTransitions(Arrays.asList(
                e1,
                e2,
                e3
        ));
        final NFA<TStates, Event<TStates>> nfa = b.build();
        assertEquals(Sets.newHashSet(S1, S0), nfa.getStates());
        assertEquals(2, nfa.getTransitions(S0, event).size());
    }


}