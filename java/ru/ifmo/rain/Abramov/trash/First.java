package ru.ifmo.rain.Abramov.trash;

import java.util.*;


public class First {

    private static List<Character> alph = new ArrayList<>();
    static List<Character> ans = new ArrayList<>();
    private static List<Boolean> used = new ArrayList<>();
    private static String original, encoded;

    private static boolean check () {
        boolean f = true;

        for (int i = 0; i < original.length(); ++i) {
            char ch = original.charAt(i);
            if (!Character.isLetter(ch)) {
                continue;
            }
            int ind;
            for (ind = 0; ind < 26; ++ind) {
                if (ch == alph.get(ind)) {
                    break;
                }
            }
            ind += 2;
            ind = (ind % 26);
            if (alph.get(ind) != encoded.charAt(i)) {
                f = false;
                break;
            }
        }

        return f;
    }

    public static void gen(int deep) {
        for (int i = 0; i < 9; ++i) {
            if (!used.get(i)) {
                used.set(i, true);
                alph.add((char)((int)('R') + i));

                if (deep == 8) {
                    if (check()) {
                        ans.addAll(alph);
                        return;
                    }
                } else {
                    gen(deep + 1);
                }

                alph.remove(alph.size() - 1);
                used.set(i, false);
            }
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String[] line = in.nextLine().split("::");
        original = line[0];
        encoded = line[1];
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < original.length(); ++i) {
            tmp.append(Character.toUpperCase(original.charAt(i)));
        }
        original = tmp.toString();
        for (int i = 0; i < 17; ++i) {
            alph.add((char)((int)('A') + i));
        }
        for (int i = 0; i < 9; ++i) {
            used.add(false);
        }
        gen(0);
        for (char A : ans) {
            System.out.print(A);
        }
    }
}
