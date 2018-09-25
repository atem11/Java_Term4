package ru.ifmo.rain.Abramov.implementor;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.nio.file.Paths;
import static java.lang.StrictMath.min;

public class Test {

    public static void main(String[] args) {
        Implementor a = new Implementor();
        try {
            a.implementJar(ListIP.class, Paths.get("test00_defaultConstructorClasses\\info.kgeorgiy.java.advanced.implementor.standard.full.ПриветInterface.jar"));
        } catch (ImplerException e) {
            System.err.println(e.getMessage());
        }
    }
}
