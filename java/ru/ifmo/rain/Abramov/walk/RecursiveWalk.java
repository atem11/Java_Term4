package ru.ifmo.rain.Abramov.walk;

import java.io.*;
import java.nio.file.*;
import java.util.stream.Stream;

import static java.lang.String.format;

public class RecursiveWalk {
    public static void main(String args[]) {
        RecursiveWalk doit = new RecursiveWalk();
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("!Wrong arguments!");
        } else {
            doit.returnHash(args[0], args[1]);
        }
    }

    private void returnHash(String src, String dst) {
        try {
            Path out = Paths.get(dst);
            if (out.getParent() != null) {
                Files.createDirectories(out.getParent());
            }
        } catch (InvalidPathException | IOException e) {
            System.err.println("Wrong dst path");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(dst));
             BufferedReader reader = Files.newBufferedReader(Paths.get(src))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    File pth = new File(line);
                    if (pth.isDirectory()) {
                        try (Stream<Path> dir = Files.walk(pth.toPath())) {
                            dir.forEach(
                                    p -> {
                                        if (!p.toFile().isDirectory()) {
                                            try {
                                                writer.write(hash(p.toString()) +
                                                        " " + p.toString() + System.lineSeparator());
                                            } catch (IOException e) {
                                                System.err.println("!Writer is broken!");
                                            }
                                        }
                                    }
                            );
                        }
                    } else {
                        try {
                            writer.write(hash(pth.getPath()) +
                                    " " + line +  System.lineSeparator());
                        } catch (IOException er) {
                            System.err.println("!Writer is broken!");
                        }
                    }
                } catch (IOException e) {
                    try {
                        writer.write("00000000 " + line +  System.lineSeparator());
                    } catch (IOException er) {
                        System.err.println("!Writer is broken!");
                    }
                }
            }
        } catch (IOException | InvalidPathException e) {
            System.err.println("!Wrong arguments path!");
        }
    }


    private String hash(String path) {
        int h = 0x811c9dc5;
        try (FileInputStream file = new FileInputStream(path)) {
            byte buff[] = new byte[2048];
            int b;
            while ((b = file.read(buff)) != -1) {
                for (int i = 0; i < b; i++) {
                    h = (h * 0x01000193) ^ (buff[i] & 0xff);
                }
            }
        } catch (IOException | InvalidPathException e) {
            h = 0;
        }
        return format("%08x", h);
    }

}
//java info.kgeorgiy.java.advanced.walk.Tester RecursiveWalk ru.ifmo.rain.Abramov.walk.RecursiveWalk
