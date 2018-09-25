package ru.ifmo.rain.Abramov.parallelism;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//java info.kgeorgiy.java.advanced.concurrent.Tester list ru.ifmo.rain.Abramov.parallelism.IterativeParallelism
//java -cp .;..\..\..\lib\*;..\..\..\artifacts\IterativeParallelismTest.jar  info.kgeorgiy.java.advanced.concurrent.Tester list ru.ifmo.rain.Abramov.parallelism.IterativeParallelism

public class IterativeParallelism implements ListIP {
    private final ParallelMapper mapper;

    public IterativeParallelism() {
        mapper = null;
    }

    public IterativeParallelism(ParallelMapper mapp) {
        mapper = mapp;
    }

    @Override
    public String join(int count, List<?> list) throws InterruptedException {
        return parallelFunc(count, list,
                var -> var.map(Object::toString).collect(Collectors.joining()),
                var -> var.collect(Collectors.joining()));
    }

    @Override
    public <T> List<T> filter(int count, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return parallelFunc(count, list,
                var -> var.filter(predicate).collect(Collectors.toList()),
                var -> var.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T, U> List<U> map(int count, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        return parallelFunc(count, list,
                var -> var.map(function).collect(Collectors.toList()),
                var -> var.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T> T maximum(int count, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<Stream<? extends T>, ? extends T> maximumFunc = var -> var.max(comparator).get();
        return parallelFunc(count, list, maximumFunc, maximumFunc);
    }

    @Override
    public <T> T minimum(int count, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<Stream<? extends T>, ? extends T> minimumFunc = var -> var.min(comparator).get();
        return parallelFunc(count, list, minimumFunc, minimumFunc);
    }

    @Override
    public <T> boolean all(int count, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return parallelFunc(count, list,
                var -> var.allMatch(predicate),
                var -> var.allMatch(x -> x == true));
    }

    @Override
    public <T> boolean any(int count, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return parallelFunc(count, list,
                var -> var.anyMatch(predicate),
                var -> var.anyMatch(x -> x == true));
    }

    private <T, R> R parallelFunc(int count, final List<? extends T> list,
                                         final Function<Stream<? extends T>, ? extends R> mapFunc,
                                         final Function<Stream<? extends R>, ? extends R> collectFunc) throws InterruptedException {
        if (list == null || count <= 0 || list.isEmpty()) {
            throw new IllegalArgumentException("Argument must be not null");
        }
        count = Math.min(count, list.size());
        List<Stream<? extends T>> sublist = new ArrayList<>();
        int size = list.size() / count;
        int ost = list.size() % count;
        int leftBorder = 0, rightBorder = size;
        for (int i = 0; i < count; ++i) {
            int l = leftBorder;
            int r = i < ost ? rightBorder + 1 : rightBorder;
            sublist.add(list.subList(l, r).stream());
            leftBorder = r;
            rightBorder = leftBorder + size;
        }
        List<R> ans;
        if (mapper != null) {
            ans = mapper.map(mapFunc, sublist);
        } else {
            List<Thread> threads = new ArrayList<>();
            ans = new ArrayList<>(Collections.nCopies(count, null));
            for (int i = 0; i < count; ++i) {
                final int ind = i;
                threads.add(new Thread(() -> ans.set(ind, mapFunc.apply(sublist.get(ind)))));
            }
            threads.forEach(Thread::start);
            boolean except = false;
            InterruptedException ex = new InterruptedException();
            for (Thread fin : threads) {
                try {
                    fin.join();
                } catch (InterruptedException e) {
                    except = true;
                    ex.addSuppressed(e);
                }
            }
            if (except) {
                throw ex;
            }
        }
        return collectFunc.apply(ans.stream());

        /*count = Math.min(count, list.size());
        List<Thread> threads = new ArrayList<>();
        List<R> ans = new ArrayList<>(Collections.nCopies(count, null));
        int size = list.size() / count;
        int ost = list.size() % count;
        int leftBorder = 0, rightBorder = size;
        for (int i = 0; i < count; ++i) {
            int l = leftBorder;
            int r = i < ost ? rightBorder + 1 : rightBorder;
            final int i1 = i;
            threads.add(new Thread(() -> ans.set(i1, mapFunc.apply(list.subList(l, r).stream()))));
            leftBorder = r;
            rightBorder = leftBorder + size;
        }
        threads.forEach(Thread::start);
        boolean except = false;
        InterruptedException ex = new InterruptedException();
        for (Thread fin : threads) {
            try {
                fin.join();
            } catch (InterruptedException e) {
                except = true;
                ex.addSuppressed(e);
            }
        }
        if (except) {
            throw ex;
        }
        return collectFunc.apply(ans.stream());*/
    }
}
