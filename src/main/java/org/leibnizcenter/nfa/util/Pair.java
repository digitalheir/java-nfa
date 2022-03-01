package org.leibnizcenter.nfa.util;

import java.util.Map;

/**
 * A pair of elements
 * <p>
 * Created by Maarten on 2016-04-03.
 */
public class Pair<K, V> implements Map.Entry<K, V> {
    private V v;
    private K k;

    public Pair(K key, V value) {
        v = value;
        k = key;
    }

    @Override
    public K getKey() {
        return k;
    }

    @Override
    public V getValue() {
        return v;
    }

    @Override
    public V setValue(V value) {
        V oldV = v;
        v = value;
        return oldV;
    }

    @SuppressWarnings("unused")
    public K setKey(K key) {
        K oldKey = k;
        k = key;
        return oldKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Map.Entry)) return false;

        Map.Entry<?, ?> pair = (Map.Entry<?, ?>) o;

        return (v != null ? v.equals(pair.getValue()) : pair.getValue() == null)
                && (k != null ? k.equals(pair.getKey()) : pair.getKey() == null);
    }

    @Override
    public int hashCode() {
        return 31 * (v != null ? v.hashCode() : 0) + (k != null ? k.hashCode() : 0);
    }
}
