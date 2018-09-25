package ru.ifmo.rain.Abramov.walk;

import java.io.File;
import java.lang.reflect.Modifier;

public class Qqq {
    public static void main(String[] args) {

        Class myToken = Qqq.class;
        System.out.println(Modifier.toString(myToken.getModifiers()) + " " +
                "kek" + (myToken.isInterface() ? " implements " : " extends ") +
                myToken.getSimpleName() + " {" + System.lineSeparator()
        );
    }
}
