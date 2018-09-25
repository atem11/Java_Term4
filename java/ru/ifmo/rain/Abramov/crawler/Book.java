package ru.ifmo.rain.Abramov.crawler;

import info.kgeorgiy.java.advanced.crawler.URLUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Book {
    public static void main(String[] args) {
        String host;
        try {
            host = URLUtils.getHost("https://e.lanbook.com/book/45675?category_pk=917#authors");
        } catch (MalformedURLException e) {
            return;
        }

        try {
            HostDownloader downloader = new HostDownloader("Books2013-2018", host);
            try (WebCrawler bookCrawler = new WebCrawler(downloader, 8, 8, 8)) {
                bookCrawler.download("https://e.lanbook.com/books/917", 30);
                System.out.println(HostDownloader.allBooks.size());
                try (BufferedWriter out = Files.newBufferedWriter(Paths.get("Books2014-2018.txt"))) {
                    for (String s : HostDownloader.allBooks) {
                        out.write(s + System.lineSeparator() + System.lineSeparator());
                    }
                } catch (IOException e) {
                    System.err.println("Can't create file");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
