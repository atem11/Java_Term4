package ru.ifmo.rain.Abramov.arraySet;

import java.util.Arrays;

public class Tests {
    public static void main(String[] args) {
        Integer h[] = {-1766832224, -1015982160, 775253861, -186421961, -200021685, -195556633};
        ArraySet<Integer> a = new ArraySet<>(Arrays.asList(h)), b = new ArraySet<>();
        System.out.println(Boolean.toString(a.contains(-1766832224)));
        System.out.println(b.isEmpty());
    }
}
