package ru.ifmo.rain.varlamov.arrayset;

import java.util.Arrays;

public class Start {
    public static void main(String[] args) {
        Integer h[] = {-1766832224, -1015982160, 775253861, -186421961, -200021685, -195556633};
        ArraySet<Integer> a = new ArraySet<>(Arrays.asList(h)), b = new ArraySet<>();
        b.headSet(123);
    }
}
