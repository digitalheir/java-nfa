package org.leibnizcenter.nfa;

import org.jetbrains.annotations.NotNull;
import org.leibnizcenter.nfa.util.Pair;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A collection of all possible paths of state transitions, starting from a given state and a given input token
 * Created by maarten on 17-6-16.
 */
@SuppressWarnings("WeakerAccess")
public class PossibleStateTransitionPaths<S extends State, E extends Event<S>> implements Collection<Transition<S, E>> {
    public final List<E> path;
    public final Collection<Transition<S, E>> possibleTransitions;
    public final Map<S, PossibleStateTransitionPaths<S, E>> furtherPaths;
    public final int numberOfBranches;
    public final int numberOfTransitions;
    public final E e;
    public final S from;

    /**
     * This constructor has a complexity of O(possibleTransitions.size()) because it has to count the number of paths diverging
     *
     * @param possibleTransitions Collection of possible transition from a fixed state through a fixed event
     * @param path                Further string events, including the one that spawns possibleTransitions at the head
     * @param furtherPaths        Further possible paths from result states of possibleTransitions along the events of the tail of path
     */
    public PossibleStateTransitionPaths(S from, Collection<Transition<S, E>> possibleTransitions, List<E> path, Map<S, PossibleStateTransitionPaths<S, E>> furtherPaths) {
        this.path = path;
        this.possibleTransitions = possibleTransitions;
        this.furtherPaths = furtherPaths;
        this.e = path.get(0);
        this.from = from;

        // TODO don't run the sanity checks because they add complexity
        sanityChecks(possibleTransitions, path, furtherPaths);

        int furthBranchNumber = 0;
        int numberOfTransitions = possibleTransitions.size();
        if (furtherPaths != null) for (Transition<S, E> transition : possibleTransitions) {
            furthBranchNumber += furtherPaths.get(transition.getTo()).numberOfBranches();
            numberOfTransitions += furtherPaths.get(transition.getTo()).size();
        }
        else {
            furthBranchNumber += possibleTransitions.size();
        }
        this.numberOfBranches = furthBranchNumber;
        this.numberOfTransitions = numberOfTransitions;
//        System.out.println(this.numberOfTransitions);
    }

    public int numberOfBranches() {
        return numberOfBranches;
    }

    public void sanityChecks(Collection<Transition<S, E>> possibleTransitions, List<E> path, Map<S, PossibleStateTransitionPaths<S, E>> furtherPaths) {
        if (possibleTransitions.stream().map(Transition::getFrom).collect(Collectors.toSet()).size() != 1)
            throw new Error();
        if (possibleTransitions.stream().map(Transition::getEvent).collect(Collectors.toSet()).size() != 1)
            throw new Error();
        if (path.size() > 1) {
            if (furtherPaths == null) throw new NullPointerException();
        } else if (furtherPaths != null) throw new NullPointerException();

        if (furtherPaths != null) {
            possibleTransitions.forEach(transition -> {
                final PossibleStateTransitionPaths<S, E> possibleFurtherBranches = furtherPaths.get(transition.getTo());
                        if (possibleFurtherBranches.path.size() != path.size() - 1) throw new Error();
                        possibleFurtherBranches.possibleTransitions.forEach(t -> {
                                    if (!t.getFrom().equals(transition.getTo())) throw new Error();
                                }
                        );
                    }
            );
        }

        // possibleTransitions should be unique
        assert possibleTransitions.stream().distinct().count() == possibleTransitions.size();
    }

    public int size() {
        return numberOfTransitions;
    }

    @Override
    public boolean isEmpty() {
        return numberOfTransitions == 0;
    }

    /**
     * Runs in O(n), for n is the length of the path
     *
     * @param o Possible input path. Must be instance of {@see Iterable}.
     * @return Whether the given branch is contained in this one
     */
    @SuppressWarnings("rawtypes")
    @Override
    public boolean contains(Object o) {
        if (o instanceof Iterable) {
            if (o instanceof Collection && ((Collection) o).size() != size())
                return false;

            Iterable iterable = ((Iterable) o);
            Iterator itThat = iterable.iterator();
            Iterator<Transition<S, E>> itThis = iterator();
            while (itThat.hasNext() && itThis.hasNext())
                if (!itThis.next().equals(itThat.next()))
                    return false;
            return itThis.hasNext() == itThat.hasNext();
        }
        return false;
    }

    @NotNull
    @Override
    public Iterator<Transition<S, E>> iterator() {
        return StreamSupport.stream(spliterator(), false)
                .iterator();
    }

//    @NotNull
    @SuppressWarnings("unchecked")
    @Override
    public Transition<S, E>[] toArray() {
        Transition<S, E>[] arr = new Transition[numberOfTransitions];
        Iterator<Transition<S, E>> iterator = iterator();
        for (int i = 0; i < numberOfTransitions; i++) arr[i] = iterator.next();
        return arr;
    }

//    @NotNull
    @Override
    public <T> T[] toArray(T[] a) {
        //if (!(Transition.class.isInstance(new Class<T>()))) throw new InvalidParameterException();
        Iterator<Transition<S, E>> iterator = iterator();
        for (int i = 0; i < a.length; i++) {
            //noinspection unchecked
            a[i] = (T) iterator.next();
            if (!iterator.hasNext()) break;
        }
        return a;
    }

    @Override
    public boolean add(Transition<S, E> transition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return c.stream()
                .filter(this::contains)
                .count() == c.size();
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Transition<S, E>> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Spliterator<Transition<S, E>> spliterator() {
        return new BranchesSpliterator<>(this);
    }

    public Stream<State> applyRecursive() {
        return possibleTransitions.stream().flatMap(t -> {
            t.getEvent().accept(t.getFrom(), t.getTo());
            if (furtherPaths == null) return Stream.of(t.getTo());
            else return furtherPaths.get(t.getTo()).applyRecursive();
        });
    }

    private static class BranchesSpliterator<S extends State, E extends Event<S>> implements Spliterator<Transition<S, E>> {
        private final LinkedList<Pair<PossibleStateTransitionPaths<S, E>, Iterator<Transition<S, E>>>> iteratorState;

        public BranchesSpliterator(PossibleStateTransitionPaths<S, E> transitionz) {
            this.iteratorState = new LinkedList<>();
            iteratorState.add(new Pair<>(transitionz, transitionz.possibleTransitions.iterator()));
        }

        public BranchesSpliterator(Pair<PossibleStateTransitionPaths<S, E>, Iterator<Transition<S, E>>> state) {
            this.iteratorState = new LinkedList<>();
            iteratorState.add(state);
        }

        @Override
        public boolean tryAdvance(Consumer<? super Transition<S, E>> action) {
            synchronized (iteratorState) {
                if (iteratorState.size() <= 0) return false;
                final Iterator<Transition<S, E>> iteratorToUse = iteratorState.peek().getValue();
                Transition<S, E> transition = iteratorToUse.next();

                if (!iteratorToUse.hasNext()) {
                    // Exhausted this iteratorState: go on to possible next states
                    final PossibleStateTransitionPaths<S, E> rip = iteratorState.pop().getKey();
                    if (rip.furtherPaths != null) rip.furtherPaths.forEach((_state, branches) ->
                            iteratorState.push(new Pair<>(branches, branches.possibleTransitions.iterator()))
                    );
                }
                action.accept(transition);
            }
            return true;
        }

        @Override
        public void forEachRemaining(Consumer<? super Transition<S, E>> action) {
            //noinspection StatementWithEmptyBody
            while (tryAdvance(action)) {
            }
        }


        @Override
        public Spliterator<Transition<S, E>> trySplit() { // O(1)
            synchronized (iteratorState) {
                if (iteratorState.size() > 1)
                    return new BranchesSpliterator<>(iteratorState.removeLast());
                else return null;
            }
        }

        @Override
        public long estimateSize() {
            return iteratorState.stream()
                    .map(Pair::getKey)
                    .mapToInt(PossibleStateTransitionPaths::size)
                    .sum();
        }

        @Override
        public long getExactSizeIfKnown() {
            return estimateSize();
        }

        @Override
        public int characteristics() {
            return SIZED | SUBSIZED
                    | NONNULL
                    | IMMUTABLE;
            // |ORDERED
            // |DISTINCT // NOTE: only if we include the state history...
            // |SORTED
            // |CONCURRENT
        }
    }
}