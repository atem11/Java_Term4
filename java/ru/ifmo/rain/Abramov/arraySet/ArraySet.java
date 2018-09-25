package ru.ifmo.rain.Abramov.arraySet;

import java.util.*;
//java info.kgeorgiy.java.advanced.arrayset.Tester NavigableSet ru.ifmo.rain.Abramov.arraySet.ArraySet

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private List<E> data;
    private Comparator<? super E> comparator;

    public ArraySet() {
        data = Collections.emptyList();
        //data = new ArrayList<>();
    }

    public ArraySet(Collection<? extends E> other) {
        data = new ArrayList<>(new TreeSet<>(other));
    }

    public ArraySet(Comparator<? super E> comp) {
        data = Collections.emptyList();
        comparator = comp;
    }

    public ArraySet(Collection<? extends E> other, Comparator<? super E> comp) {
        TreeSet<E> tree = new TreeSet<>(comp);
        tree.addAll(other);
        data = new ArrayList<>(tree);
        comparator = comp;
    }

    private ArraySet(List<E> other, Comparator<? super E> comp) {
        data = other;
        comparator = comp;
    }

    private int binSearch(int incl, int rev, E t) {
        int pos = Collections.binarySearch(data, t, comparator);
        if (pos < 0) {
            pos = -pos - 1;
            pos += rev;
        } else {
            pos += incl;
        }
        return 0 <= pos && pos < data.size() ? pos : -1;
    }

    private E myGet(int pos) {
        if (pos >= 0) {
            return data.get(pos);
        }
        return null;
    }

    @Override
    public E lower(E t) {
        return myGet(binSearch(-1, -1, t));
    }

    @Override
    public E floor(E t) {
        return myGet(binSearch(0, -1, t));
    }

    @Override
    public E ceiling(E t) {
        return myGet(binSearch(0, 0, t));
    }

    @Override
    public E higher(E t) {
        return myGet(binSearch(1, 0, t));
    }

    @Override
    public E pollFirst() {
        E ans = first();
        remove(ans);
        return ans;
    }

    @Override
    public E pollLast() {
        E ans = last();
        remove(ans);
        return ans;
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        int left = binSearch(1, 0, fromElement), right = binSearch(-1, -1, toElement);
        if (fromInclusive) {
            left = binSearch(0, 0, fromElement);
        }
        if (toInclusive) {
            right = binSearch(0, -1, toElement);
        }
        if (right == -1 || left == -1 || left > right) {
            return Collections.emptyNavigableSet();
        }
        return new ArraySet<>(data.subList(left, right + 1), comparator);
    }

    private boolean myCheck() {
        return !data.isEmpty();
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return myCheck() ? subSet(first(), true, toElement, inclusive) : Collections.emptyNavigableSet();
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return myCheck() ? subSet(fromElement, inclusive, last(), true) : Collections.emptyNavigableSet();
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(data).iterator();
    }

    private class MyList<T> extends AbstractList<T> {
        private List<T> datas;
        private boolean rev;

        public MyList(List<T> other) {
            if (other instanceof MyList) {
                rev = !((MyList) other).rev;
                datas = ((MyList<T>) other).datas;
            } else {
                rev = true;
                datas = other;
            }

        }

        @Override
        public T get(int i) {
            if (rev) {
                i = datas.size() - i - 1;
            }
            return datas.get(i);
        }

        @Override
        public int size() {
            return datas.size();
        }
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(new MyList<>(data), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(data, (E) o, comparator) >= 0;
    }

    @Override
    public E first() {
        if (data.isEmpty()) {
            throw new NoSuchElementException();
        }
        return data.get(0);
    }

    @Override
    public E last() {
        if (data.isEmpty()) {
            throw new NoSuchElementException();
        }
        return data.get(data.size() - 1);
    }

    @Override
    public int size() {
        return data.size();
    }
}
