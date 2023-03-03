package com.zheng.zencrawler;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 The ConcurrentHashSet Class is derived from ConcurrentHashMap.
 It is used for multithreading purpose.
 @author Leon Zheng
 */
public class ConcurrentHashSet<E> {
    private final Set<E> set = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public boolean add(E e) {
        if(e==null)
            return false;
        return set.add(e);
    }

    public boolean remove(E e) {
        return set.remove(e);
    }

    public E poll() {
        Iterator<E> it = set.iterator();
        if (it.hasNext()) {
            E element = it.next();
            it.remove();
            return element;
        } else {
            return null;
        }
    }
    public boolean contains(E e) {
        return set.contains(e);
    }

    public int size() {
        return set.size();
    }
}
