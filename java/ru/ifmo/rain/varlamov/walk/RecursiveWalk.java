package ru.ifmo.rain.varlamov.walk;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.stream.Stream;

public class RecursiveWalk {
    public static void main(String[] args) {
        RecursiveWalk walk = new RecursiveWalk();
        if (args == null || args.length < 2 || args[0] == null || args[1] == null) {
            System.err.println("Invalid arguments");
        } else {
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(args[0]), Charset.forName("UTF-8"))) {
                try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(args[1]), Charset.forName("UTF-8"))) {
                    walk.walk(reader, writer);
                } catch (InvalidPathException e) {
                    System.err.println("Second argument isn't path");
                } catch (IOException e) {
                    System.err.println("No access to output file");
                }
            } catch (InvalidPathException e) {
                System.err.println("First argument isn't path");
            } catch (IOException e) {
                System.err.println("No access to input file");
            }
        }
    }

    private void walk(BufferedReader reader, BufferedWriter writer) {
        String line;
        String ans;
        Path path;
        try {
            while ((line = reader.readLine()) != null) {
                ans = "";
                try {
                    path = Paths.get(line);
                    try {
                        if (Files.isDirectory(path)) {
                            try (Stream<Path> stream = Files.walk(path)) {
                                stream.forEach(
                                        p -> {
                                            if (!p.toFile().isDirectory()) {
                                                try {
                                                    writer.write(findHash(p.toString()));
                                                    writer.newLine();
                                                } catch (IOException e) {
                                                    System.err.println("An error occurred while writing answer");
                                                }
                                            }
                                        }
                                );
                            }
                        } else {
                            ans = findHash(line);
                        }
                    } catch (SecurityException e) {
                        System.err.println("No access to directory");
                    } catch (IOException e) {
                        ans = findHash(line);
                    }
                } catch (InvalidPathException e) {
                    ans = findHash(line);
                }
                if (!ans.isEmpty()) {
                    try {
                        writer.write(ans);
                        writer.newLine();
                    } catch (IOException e) {
                        System.err.println("An error occurred while writing answer");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("An error occurred while reading the line");
        }
    }

    private String findHash(String str) {
        int h = 0x811c9dc5;
        byte[] b = new byte[1024];
        int c;
        try (InputStream reader = Files.newInputStream(Paths.get(str))) {
            while ((c = reader.read(b)) >= 0) {
                h = hash(b, h, c);
            }
        } catch (IOException | InvalidPathException e) {
            h = 0;
        }
        return String.format("%08x", h) + " " + str;
    }

    private int hash(final byte[] bytes, int h, int c) {
        for (int i = 0; i < c; ++i) {
            h = (h * 0x01000193) ^ (bytes[i] & 0xff);
        }
        return h;
    }
}
//  java info.kgeorgiy.java.advanced.walk.Tester RecursiveWalk ru.ifmo.rain.varlamov.walk.RecursiveWalk