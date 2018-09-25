package ru.ifmo.rain.Abramov.parallelism;


import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

//java -cp .;..\..\..\lib\*;..\..\..\artifacts\ParallelMapperTest.jar  info.kgeorgiy.java.advanced.mapper.Tester list ru.ifmo.rain.Abramov.parallelism.ParallelMapperImpl,ru.ifmo.rain.Abramov.parallelism.IterativeParallelism

public class ParallelMapperImpl implements ParallelMapper {
    private final Queue<Runnable> queue;
    private List<Thread> treads;
    private final static int MAX = 1000;

    private class MyList<T> {
        private List<T> list;
        private int size;

        MyList(int sz) {
            list = new ArrayList<>(Collections.nCopies(sz, null));
            size = 0;
        }

        synchronized void set(int ind, T val) {
            list.set(ind, val);
            size++;
            if (size == list.size()) {
                notify();
            }
        }

        synchronized List<T> get() throws InterruptedException {
            while (size < list.size()) {
                wait();
            }
            return list;
        }
    }

    private void addQueue(Runnable tr) throws InterruptedException {
        synchronized (queue) {
            while (queue.size() >= MAX) {
                wait();
            }
            queue.add(tr);
            queue.notify();
        }
    }

    private void runQueue() throws InterruptedException {
        Runnable thr;
        synchronized (queue) {
            while (queue.isEmpty()) {
                queue.wait();
            }
            thr = queue.poll();
            queue.notify();
        }
        thr.run();
    }

    public ParallelMapperImpl(int threads) {
        queue = new ArrayDeque<>();
        treads = new ArrayList<>();
        for (int i = 0; i < threads; ++i) {
            treads.add(new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        runQueue();
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    Thread.currentThread().interrupt();
                }
            }));
            treads.get(i).start();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        MyList<R> res = new MyList<>(args.size());
        for (int i = 0; i < args.size(); ++i) {
            final int ind = i;
            addQueue(() -> res.set(ind, f.apply(args.get(ind))));
        }
        return res.get();
    }

    @Override
    public void close() {
        treads.forEach(Thread::interrupt);
        for (Thread fin : treads) {
            try {
                fin.join();
            } catch (InterruptedException ignored) {
            }
        }
    }
}
