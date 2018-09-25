package ru.ifmo.rain.Abramov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;


//java -cp .;..\..\..\lib\*;..\..\..\artifacts\WebCrawlerTest.jar  info.kgeorgiy.java.advanced.crawler.Tester hard ru.ifmo.rain.Abramov.crawler.WebCrawler

public class WebCrawler implements Crawler {

    private class MySem {
        private final Phaser phaser;
        Queue<Runnable> tasks;
        int size;

        MySem(Phaser phaser) {
            this.phaser = phaser;
            tasks = new ArrayDeque<>();
            size = 0;
        }

        synchronized void add(Runnable task) {
            phaser.register();
            if (size < perHosts) {
                ++size;
                downloadPages.submit(task);
            } else {
                tasks.add(task);
            }
        }

        synchronized void poll() {
            phaser.arrive();
            Runnable task = tasks.poll();
            if (task == null) {
                --size;
            } else {
                downloadPages.submit(task);
            }
        }
    }

    private ExecutorService downloadPages;
    private ExecutorService extractLinks;
    private final Downloader downloader;
    private final Integer perHosts;
    private Map<String, MySem> hosts = new ConcurrentHashMap<>();


    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        downloadPages = Executors.newFixedThreadPool(downloaders);
        extractLinks = Executors.newFixedThreadPool(extractors);
        this.downloader = downloader;
        perHosts = perHost;
    }

    private void extractLinks(Document page, int depth, Set<String> links, Map<String, IOException> excep, Phaser phaser, Set<String> duplicate) {
        try {
            page.extractLinks().stream()
                    .filter(duplicate::add)
                    .forEach(
                            link -> downloadPage(link, depth, links, excep, phaser, duplicate)
                    );
        } catch (IOException ignored) {
        }
        phaser.arrive();
    }

    private void downloadPage(String url, int depth, Set<String> links, Map<String, IOException> excep, Phaser phaser, Set<String> duplicate) {
        try {
            String host = URLUtils.getHost(url);
            MySem data = hosts.computeIfAbsent(host, x -> new MySem(phaser));
            data.add(() -> {
                try {
                    Document page = downloader.download(url);
                    links.add(url);
                    if (depth > 1) {
                        phaser.register();
                        extractLinks.submit(() -> extractLinks(page, depth - 1, links, excep, phaser, duplicate));
                    }
                } catch (IOException e) {
                    excep.put(url, e);
                }
                data.poll();
            });
        } catch (MalformedURLException e) {
            excep.put(url, e);
        }
    }

    /*private void downloadPage1(String url, int depth, Set<String> links, Map<String, IOException> excep, Phaser phaser, Set<String> duplicate) {
        MySem d = downloadPage(url, depth, links, excep, phaser, duplicate);
        if (d != null) {
            d.poll();
        }
    }*/

    @Override
    public Result download(String url, int depth) {
        Set<String> links = ConcurrentHashMap.newKeySet();
        Map<String, IOException> excep = new ConcurrentHashMap<>();
        Set<String> duplicates = ConcurrentHashMap.newKeySet();
        Phaser phaser = new Phaser(1);
        duplicates.add(url);
        downloadPage(url, depth, links, excep, phaser, duplicates);
        phaser.arriveAndAwaitAdvance();
        return new Result(new ArrayList<>(links), excep);
    }

    @Override
    public void close() {
        downloadPages.shutdownNow();
        extractLinks.shutdownNow();
    }

    public static void main(String[] args) {
        int dowloadeR;
        int extractorS;
        int perHosT;
        try {
            dowloadeR = Integer.parseInt(args[2]);
        } catch (NumberFormatException ignored) {
            dowloadeR = 3;
        }
        try {
            extractorS = Integer.parseInt(args[3]);
        } catch (NumberFormatException ignored) {
            extractorS = 2;
        }
        try {
            perHosT = Integer.parseInt(args[4]);
        } catch (NumberFormatException ignored) {
            perHosT = 2;
        }
        try (WebCrawler crawler = new WebCrawler(new CachingDownloader(), dowloadeR, extractorS, perHosT)) {
            crawler.download(args[0], Integer.parseInt(args[1]));
        } catch (IOException | NumberFormatException e) {
            System.err.println(e.getMessage());
        }
    }

}
