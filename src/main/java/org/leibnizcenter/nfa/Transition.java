package org.leibnizcenter.nfa;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Transition<StateType extends State, EventType extends Event> {
    public final EventType event;
    public final StateType from;
    public final StateType to;
    public final boolean isFinal;

    public Transition(EventType event, StateType from, StateType to) {
        this.event = event;
        this.from = from;
        this.to = to;
        this.isFinal = false;
    }

    public Transition(EventType event, StateType from, StateType to, boolean isFinal) {
        this.event = event;
        this.from = from;
        this.to = to;
        this.isFinal = isFinal;
    }

    public Transition(StateType from, EventType event, StateType to) {
        this.event = event;
        this.from = from;
        this.to = to;
        this.isFinal = false;
    }

    public static <StateType extends State, EventType extends Event>
    FromHolder<StateType, EventType> from(StateType from) {
        return new FromHolder<>(from);
    }

    public EventType getEvent() {
        return event;
    }

    public StateType getFrom() {
        return from;
    }

    public StateType getTo() {
        return to;
    }

    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public String toString() {
        return from + "-[" + event +
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

    public static class FromHolder<StateType extends State, EventType extends Event> {
        private final StateType from;

        public FromHolder(StateType from) {
            this.from = from;
        }

        public ThroughHolder through(EventType event) {
            return new ThroughHolder<>(from, event);
        }
    }

    public static class ThroughHolder<StateType extends State, EventType extends Event> {
        private final EventType event;
        private final StateType from;

        public ThroughHolder(StateType from, EventType event) {
            this.event = event;
            this.from = from;
        }

        public Transition to(StateType to) {
            return new Transition<>(from, event, to);
        }
    }
}