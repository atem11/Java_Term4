package ru.ifmo.rain.Abramov.parallelism;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.lang.reflect.Array;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        IterativeParallelism a = new IterativeParallelism();
        ArrayList<Integer> l = new ArrayList<>();
        l.add(5);
        l.add(10);
        l.add(2);
        l.add(5);
        l.add(100);
        l.add(-2);

        try {
            Integer ans = a.maximum(3, l, null);
            System.out.println(ans.toString());
        } catch (InterruptedException e) {
            System.err.println("ERROR");
        }
    }
}
