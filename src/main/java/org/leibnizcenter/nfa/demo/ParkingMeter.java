package org.leibnizcenter.nfa.demo;

import org.leibnizcenter.nfa.Event;
import org.leibnizcenter.nfa.NFA;
import org.leibnizcenter.nfa.State;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static org.leibnizcenter.nfa.demo.ParkingMeter.PayState.*;

public class ParkingMeter {
    /**
     * How much money is in the machine
     */
    private int ¢¢¢;

    public static void main(String... args) {
        // Run example
        new ParkingMeter().run();
    }

    private void run() {
        // Say we can buy parking for 100 cents

        // Define some actions
        CoinDrop drop25cents = new CoinDrop(25);
        CoinDrop drop50cents = new CoinDrop(50);

        // Define our NFA
        NFA<PayState, CoinDrop> nfa = new NFA.Builder<PayState, CoinDrop>()
                .addTransition(PAYED_0, drop25cents, PAYED_25)
                .addTransition(PAYED_0, drop50cents, PAYED_50)
                .addTransition(PAYED_25, drop25cents, PAYED_50)
                .addTransition(PAYED_25, drop50cents, PAYED_75)
                .addTransition(PAYED_50, drop25cents, PAYED_75)
                .addTransition(PAYED_50, drop50cents, PAYED_0)
                .addTransition(PAYED_75, drop25cents, PAYED_0)
                .addTransition(PAYED_75, drop50cents, PAYED_0) // Payed too much... no money back!
                .build();

        // Apply action step-by-step
        Collection<State> endStates1 = nfa.start(PAYED_0)
                .andThen(drop25cents)
                .andThen(drop50cents)
                .andThen(drop50cents)
                .andThen(drop25cents)
                .getState().collect(Collectors.toList());

        // Or apply actions in bulk (this makes calculations of the possible paths more efficient, but it doesn't matter if we iterate over all transitions anyway)
        Collection<State> endStates2 = nfa.apply(PAYED_0, new LinkedList<>(Arrays.asList(drop50cents, drop25cents, drop50cents, drop25cents)))
                .collect(Collectors.toList());

        System.out.println("Today earnings: ¢" + ¢¢¢ + ".");
    }

    enum PayState implements State {
        PAYED_0(0), PAYED_25(25), PAYED_50(50), PAYED_75(75);
        public int ¢;

        PayState(int ¢) {
            this.¢ = ¢;
        }
    }

    private class CoinDrop implements Event<PayState> {
        final int ¢;

        CoinDrop(int value) {
            this.¢ = value;
        }

        @Override
        public void accept(PayState from, PayState to) {
            System.out.println("Bleep Bloop. Added ¢" + ¢ + " to ¢" + from.¢ + ". ");
            if (to.¢ <= 0 || to.¢ >= 100) System.out.println("You may park. Good day.");
            else
                System.out.println("You have paid ¢" + to.¢ + " in total. Please add ¢" + (100 - to.¢) + " before you may park.");
            System.out.println("----------------------------------------------");
            ¢¢¢ += this.¢;
        }

        @Override
        public String toString() {
            return "¢" + ¢;
        }
    }
}