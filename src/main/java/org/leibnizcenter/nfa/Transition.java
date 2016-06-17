package org.leibnizcenter.nfa;

public class Transition {
    public final Event event;
    public final State from;
    public final State to;
    public final boolean isFinal;

    public Transition(Event event, State from, State to) {
        this.event = event;
        this.from = from;
        this.to = to;
        this.isFinal = false;
    }

    public Transition(Event event, State from, State to, boolean isFinal) {
        this.event = event;
        this.from = from;
        this.to = to;
        this.isFinal = isFinal;
    }

    public Transition(State from, Event event, State to) {
        this.event = event;
        this.from = from;
        this.to = to;
        this.isFinal = false;
    }


    public Event getEvent() {
        return event;
    }

    public State getFrom() {
        return from;
    }

    public State getTo() {
        return to;
    }

    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public String toString() {
        return from+"-["+ event +
                "]->" + to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transition that = (Transition) o;

        return isFinal == that.isFinal && event.equals(that.event) && from.equals(that.from) && to.equals(that.to);

    }

    @Override
    public int hashCode() {
        int result = event.hashCode();
        result = 31 * result + from.hashCode();
        result = 31 * result + to.hashCode();
        result = 31 * result + (isFinal ? 1 : 0);
        return result;
    }

    public static FromHolder from(State from) {
        return new FromHolder(from);
    }


    public static class FromHolder {
        private final State from;

        public FromHolder(State from) {
            this.from = from;
        }

        public ThroughHolder through(Event event) {
            return new ThroughHolder(from, event);
        }
    }

    public static class ThroughHolder {
        private final Event event;
        private final State from;

        public ThroughHolder(State from, Event event) {
            this.event = event;
            this.from = from;
        }

        public Transition to(State to) {
            return new Transition(from, event, to);
        }
    }
}