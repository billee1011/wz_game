package util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

public class WeakHashSet<E> extends AbstractSet<E> implements Set<E> {

    private final transient WeakHashMap<E, Object> map;

    private static final Object kDefaultValue = new Object();

    public WeakHashSet() {
        map = new WeakHashMap<E, Object>();
    }

    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean add(E e) {
        return map.put(e, kDefaultValue) == null;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) != null;
    }

    @Override
    public Object[] toArray() {
        return map.keySet().toArray();
    }

}
