package org.leibnizcenter.nfa.util;

import java.util.*;
import java.util.stream.Stream;

/**
 * Extra util methods for collections
 * <p>
 * Created by Maarten on 2016-04-03.
 */
@SuppressWarnings("unused")
public class Collections3 {
    /**
     * Runs in O(1), but the stream may run longer of course.
     *
     * @return A stream of pairs of elements taken from stream A and stream B
     */
    public static <A, B> Stream<Pair<A, B>> zip(Stream<A> as, Stream<B> bs) {
        final Iterator<A> i = as.iterator();
        return bs.filter(x -> i.hasNext()).map(b -> new Pair<>(i.next(), b));
    }

    /**
     * Runs in O(1).
     *
     * @param coll Given collection
     * @return Whether the given collection is null or {@link Collection#isEmpty() empty}
     */
    public static <T> boolean isNullOrEmpty(Collection<T> coll) {
        return coll == null || coll.isEmpty();
    }

    /**
     * Runs in O(k), where k is the element at which the parameter stopAfter occurs (or the length of the given iterable if it does not occur)
     *
     * @param stopAfter Element after which to stop traversing the deque
     * @return Given iterable as a {@link LinkedList linked list}, up to but not including stopAfter
     */
    public static <K> LinkedList<K> upToAndIncluding(Iterable<K> iterable, K stopAfter) {
        final LinkedList<K> returnObj = new LinkedList<>();
        for (K el : iterable) {
            returnObj.add(el);
            if (Objects.equals(el, stopAfter)) break;
        }
        return returnObj;
    }

    /**
     * @return Given list, or immutable empty list if null
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> orEmpty(List<T> list) {
        return (list == null) ? Collections.EMPTY_LIST : list;
    }

    /**
     * @return Given list, or immutable empty list if null
     */
    @SuppressWarnings("unchecked")
    public static <R> Set<R> orEmpty(Set<R> set) {
        return set == null ? Collections.EMPTY_SET : set;
    }

    /**
     * @return 0 if collection is null
     */
    public static int size(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    /**
     * @return Last element in list, of null if not existing
     */
    public static <R> R last(List<R> list) {
        return (list == null || list.size() <= 0) ? null : list.get(list.size() - 1);
    }

    /**
     * @return Sub-list starting at start (inclusive), ending at list end
     */
    public static <T> List<T> subList(List<T> l, int start) {
        List<T> newList = new ArrayList<>(l.size() - start);
        for (int i = start; i < l.size(); i++) newList.add(l.get(i));
        return newList;
    }

    /**
     * @param set   May be null
     * @param toAdd Element to add
     * @return Given set with element added (or new HashSet if given set was null)
     */
    public static <T> Set<T> add(Set<T> set, T toAdd) {
        if (set == null) set = new HashSet<>();
        set.add(toAdd);
        return set;
    }
}
