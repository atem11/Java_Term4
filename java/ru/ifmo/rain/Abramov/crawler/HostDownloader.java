package ru.ifmo.rain.Abramov.crawler;

import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.URLUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HostDownloader implements Downloader {
    private static final byte[] OK_MARKER = {'+'};
    private static final byte[] FAIL_MARKER = {'-'};

    static Set<String> allBooks = ConcurrentHashMap.newKeySet();

    private final Path directory;
    private final String host;
    private final Predicate<String> startURL = url -> url.contains("e.lanbook.com/book/") ||
            url.contains("e.lanbook.com/books/917") ||
            url.contains("e.lanbook.com/books/918") ||
            url.contains("e.lanbook.com/books/1537");
    private final Predicate<String> startBook = url -> url.contains("e.lanbook.com/book/");


    public HostDownloader(final String dir, String host) throws IOException {
        this.host = host;

        this.directory = Paths.get(dir);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        if (!Files.isDirectory(directory)) {
            throw new IOException(directory + " is not a directory");
        }
    }

    private boolean checkPage(String url) {
        try {
            return URLUtils.getHost(url).equals(host) && startURL.test(url);
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private boolean checkBook(String html) {
        if (html.contains("matematika_0\">Математика<") ||
                html.contains("fizika_0\">Физика<") ||
                html.contains("informatika_0\">Информатика<")) {
            for (int year = 2014; year <= 2018; ++year) {
                if (html.contains("<dt>Год:</dt><dd>" + Integer.toString(year) + "</dd>")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Document download(final String url) throws IOException {
        if (!checkPage(url)) {
            throw new IOException("Wrong page");
        }
        final URI uri = URLUtils.getURI(url);
        final Path file = directory.resolve(URLEncoder.encode(uri.toString(), "UTF-8"));
        if (Files.notExists(file)) {
            System.out.println("Downloading\t" + url);
            try {
                try (final InputStream is = uri.toURL().openStream()) {
                    Files.copy(new SequenceInputStream(new ByteArrayInputStream(OK_MARKER), is), file);
                }
            } catch (final IOException e) {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                out.write(FAIL_MARKER);
                try (ObjectOutputStream oos = new ObjectOutputStream(out)) {
                    oos.writeObject(e);
                }
                Files.copy(new ByteArrayInputStream(out.toByteArray()), file);
                throw e;
            }
            System.out.println("Downloaded\t" + uri);
        } else {
            //System.out.println("Already downloaded\t" + url);
            try (final InputStream is = Files.newInputStream(file)) {
                if (is.read() == FAIL_MARKER[0]) {
                    try (ObjectInputStream ois = new ObjectInputStream(is)) {
                        throw (IOException) ois.readObject();
                    } catch (final ClassNotFoundException e) {
                        throw new AssertionError(e);
                    }
                }
            }
        }

        if (startBook.test(url)) {
            try (final BufferedReader is = Files.newBufferedReader(file)) {
                String pref = "\"bibliographic_record\">";
                if (!(is.read() == FAIL_MARKER[0])) {
                    String html = is.lines().collect(Collectors.joining());
                    html = html.replaceAll("\\p{javaWhitespace}+", "");
                    if (checkBook(html)) {
                        int left = html.indexOf(pref);
                        int right = html.indexOf("</div>", left);
                        allBooks.add(html.substring(left + pref.length(), right));
                    } else {
                        throw new IOException("Wrong book");
                    }
                }
            }
        }


        return () -> {
            try (final InputStream is = Files.newInputStream(file)) {
                return is.read() == FAIL_MARKER[0] ? Collections.emptyList()
                        : URLUtils.extractLinks(uri, is).stream().filter(this::checkPage)
                        .map(link -> {
                            int quest = link.indexOf('?');
                            if (quest == -1) {
                                return link;
                            } else {
                                String[] flags = link.substring(quest + 1).split("&");
                                for (String fl : flags) {
                                    if (fl.matches("page=\\d+")) {
                                        return link.substring(0, quest) + "?" + fl;
                                    }
                                }
                                return link.substring(0, quest);
                            }
                        })
                        .collect(Collectors.toList());
            }
        };
    }
}
