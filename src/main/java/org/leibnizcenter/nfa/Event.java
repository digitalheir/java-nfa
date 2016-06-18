package org.leibnizcenter.nfa;

import java.util.function.BiConsumer;

/**
 * Created by maarten on 16-6-16.
 */
public interface Event<S extends State> extends BiConsumer<S, S> {
}
