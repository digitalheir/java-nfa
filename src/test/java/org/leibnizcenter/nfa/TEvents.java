package org.leibnizcenter.nfa;

/**
 * Created by maarten on 16-6-16.
 */
public final class TEvents {
    public static final Event eventC = new Event() {
        @Override
        public void run() {
        }

        @Override
        public String toString() {
            return "[c]";
        }
    };
    public static final Event eventB = new Event() {
        @Override
        public void run() {
        }

        @Override
        public String toString() {
            return "[b]";
        }
    };
    public static final Event eventA = new Event() {
        @Override
        public void run() {
        }

        @Override
        public String toString() {
            return "[a]";
        }
    };
}
