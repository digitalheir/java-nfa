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
    private int cents;

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
                .addTransition(PAID_0, drop25cents, PAID_25)
                .addTransition(PAID_0, drop50cents, PAID_50)
                .addTransition(PAID_25, drop25cents, PAID_50)
                .addTransition(PAID_25, drop50cents, PAID_75)
                .addTransition(PAID_50, drop25cents, PAID_75)
                .addTransition(PAID_50, drop50cents, PAID_0)
                .addTransition(PAID_75, drop25cents, PAID_0)
                .addTransition(PAID_75, drop50cents, PAID_0) // Paid too much... no money back!
                .build();

        // Apply action step-by-step
        Collection<State> endStates1 = nfa.start(PAID_0)
                .andThen(drop25cents)
                .andThen(drop50cents)
                .andThen(drop50cents)
                .andThen(drop25cents)
                .getState()
                .collect(Collectors.toList());

        // Or apply actions in bulk (this makes calculations of the possible paths more efficient, but it doesn't matter if we iterate over all transitions anyway)
        Collection<State> endStates2 = nfa.apply(PAID_0, new LinkedList<>(Arrays.asList(drop50cents, drop25cents, drop50cents, drop25cents)))
                .collect(Collectors.toList());

        System.out.println("Today earnings: ¢" + cents + ".");
    }

    enum PayState implements State {
        PAID_0(0), PAID_25(25), PAID_50(50), PAID_75(75);
        public final int centsValue;

        PayState(int centsValue) {
            this.centsValue = centsValue;
        }
    }

    private class CoinDrop implements Event<PayState> {
        final int centsValue;

        CoinDrop(int value) {
            this.centsValue = value;
        }

        @Override
        public void accept(PayState from, PayState to) {
            System.out.println("Bleep Bloop. Added ¢" + centsValue + " to ¢" + from.centsValue + ". ");
            if (to.centsValue <= 0 || to.centsValue >= 100) {
                System.out.println("You may park. Good day.");
            } else {
                System.out.println("You have paid ¢" + to.centsValue + " in total. Please add ¢" + (100 - to.centsValue) + " before you may park.");
            }
            System.out.println("----------------------------------------------");
            cents += this.centsValue;
        }

        @Override
        public String toString() {
            return "¢" + centsValue;
        }
    }
}