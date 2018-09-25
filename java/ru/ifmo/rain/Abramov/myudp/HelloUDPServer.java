package ru.ifmo.rain.Abramov.myudp;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//java -cp .;..\..\..\lib\*;..\..\..\artifacts\HelloUDPTest.jar  info.kgeorgiy.java.advanced.hello.Tester server-i18n ru.ifmo.rain.Abramov.myudp.HelloUDPServer

public class HelloUDPServer implements HelloServer {
    private ExecutorService threads;
    private DatagramSocket socket;

    public static void main(String[] args) {
        try {
            final int port = Integer.parseInt(args[0]);
            final int threadNum = Integer.parseInt(args[1]);

            new HelloUDPServer().start(port, threadNum);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.err.println("Wrong arguments! Try insert 'port', 'thread_numbers'\n");
        }
    }

    @Override
    public void start(int port, int threadNum) {
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            System.err.print("UDP error: ");
            e.printStackTrace();
            return;
        }

        threads = Executors.newFixedThreadPool(threadNum);
        byte[] buf = new byte[0];
        try {
            buf = new byte[socket.getReceiveBufferSize()];
        } catch (SocketException ignored) {
        }
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        Runnable task = () -> {
            byte answer[] = ("Hello, " + new String(
                    packet.getData(), packet.getOffset(), packet.getLength(), Charset.forName("UTF-8"))
            ).getBytes(Charset.forName("UTF-8"));
            DatagramPacket ans = new DatagramPacket(answer, answer.length, packet.getSocketAddress());
            try {
                socket.send(ans);
            } catch (IOException ignored) {
            }
        };

        threads.submit(() -> {
            while (!Thread.currentThread().isInterrupted() || !socket.isClosed()) {
                try {
                    socket.receive(packet);
                } catch (IOException ignored) {
                }

                if (threadNum != 1) {
                    threads.submit(task);
                } else {
                    task.run();
                }
            }
        });
    }

    @Override
    public void close() {
        if (socket != null) {
            socket.close();
        }
        if (threads != null) {
            threads.shutdownNow();
        }
    }
}
