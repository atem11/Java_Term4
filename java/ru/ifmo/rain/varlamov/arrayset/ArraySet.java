package ru.ifmo.rain.varlamov.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E> {
    private List<E> array;
    private Comparator<? super E> comparator;

    public ArraySet() {
        array = new ArrayList<>();
        comparator = null;
    }

    public ArraySet(Collection<? extends E> array, Comparator<? super E> comparator) {
        TreeSet<E> buf = new TreeSet<>(comparator);
        buf.addAll(array);
        this.array = new ArrayList<>(buf);
        this.comparator = comparator;
    }

    public ArraySet(Collection<? extends E> array) {
        this.array = new ArrayList<>(new TreeSet<>(array));
        comparator = null;
    }

    private ArraySet(List<E> array, Comparator<? super E> comparator) {
        this.array = array;
        this.comparator = comparator;
    }

    public Iterator<E> iterator() {
        return Collections.unmodifiableList(array).iterator();
    }

    public int size() {
        return array.size();
    }

    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(array, (E) o, comparator) >= 0;
    }

    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, toElement, false);
    }

    private SortedSet<E> subSet(E fromElement, E toElement, boolean includeLast) {
        if (array.isEmpty())
            return new ArraySet<E>();
        int from = find(fromElement);
        int to = find(toElement);
        if (includeLast)
            ++to;
        return new ArraySet<E>(array.subList(from, to), comparator);
    }

    public SortedSet<E> headSet(E toElement) {
        try {
            return subSet(first(), toElement);
        } catch (NoSuchElementException e) {
            return new ArraySet<E>();
        }
    }

    public SortedSet<E> tailSet(E fromElement) {
        try {
            return subSet(fromElement, last(), true);
        } catch (NoSuchElementException e) {
            return new ArraySet<E>();
        }
    }

    public E first() {
        if (array.isEmpty()) {
            throw new NoSuchElementException("Set is empty");
        }
        return array.get(0);
    }

    public E last() {
        if (array.isEmpty()) {
            throw new NoSuchElementException("Set is empty");
        }
        return array.get(array.size() - 1);
    }

    private int find(E element) {
        int a = Collections.binarySearch(array, element, comparator);
        if (a >= 0)
            return a;
        else return Math.abs(a + 1);
    }
}
//  java info.kgeorgiy.java.advanced.arrayset.Tester SortedSet ru.ifmo.rain.varlamov.arrayset.ArraySet