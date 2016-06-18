package org.leibnizcenter.nfa;

/**
 * Created by maarten on 16-6-16.
 */
public final class TEvents implements Event<TStates> {
    public static final TEvents eventC = new TEvents("c");
    public static final TEvents eventB = new TEvents("b");
    public static final TEvents eventA = new TEvents("a");

    private final String name;

    public TEvents(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "[" + name + "]";
    }

    @Override
    public void accept(TStates tStates, TStates tStates2) {
    }
}
