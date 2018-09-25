package ru.ifmo.rain.Abramov.myudp;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

//java -cp .;..\..\..\lib\*;..\..\..\artifacts\HelloUDPTest.jar  info.kgeorgiy.java.advanced.hello.Tester client-i18n ru.ifmo.rain.Abramov.myudp.HelloUDPClient

public class HelloUDPClient implements HelloClient {

    private final int TIME = 60;

    public static void main(String[] args) {
        try {
            new HelloUDPClient().run(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.err.println("Wrong input!!");
        }
    }

    @Override
    public void run(String host, int port, String prefix, int threadNum, int reqNum) {
        InetSocketAddress serverAddress = new InetSocketAddress(host, port);
        ExecutorService threads = Executors.newFixedThreadPool(threadNum);
        //System.out.println(threadNum + "___" + reqNum);
        try {
            for (int i = 0; i < threadNum; i++) {
                int threadInd = i;
                //DatagramSocket socket = new DatagramSocket();

                threads.submit(() -> {
                    try (DatagramSocket socket = new DatagramSocket()) {
                        socket.setSoTimeout(TIME);
                        DatagramPacket requestPack = new DatagramPacket(new byte[socket.getReceiveBufferSize()], socket.getReceiveBufferSize());
                        DatagramPacket returned = new DatagramPacket(new byte[socket.getReceiveBufferSize()], socket.getReceiveBufferSize());
                        for (int reqInd = 0; reqInd < reqNum; ++reqInd) {
                            String request = prefix + threadInd + "_" + reqInd;
                            byte message[] = request.getBytes(Charset.forName("UTF-8"));
                            requestPack.setData(message, 0, message.length);
                            requestPack.setSocketAddress(serverAddress);
                            message = new byte[socket.getReceiveBufferSize()];
                            returned.setData(message, 0, message.length);
                            while (!socket.isClosed()) {
                                try {
                                    //System.out.println(threadNum + "___" + reqNum + "   " +threadInd + "____" + reqInd);
                                    socket.send(requestPack);
                                    socket.receive(returned);
                                    String returnedStr = new String(returned.getData(),
                                            returned.getOffset(),
                                            returned.getLength(),
                                            Charset.forName("UTF-8"));
                                    if (returnedStr.endsWith(request) ||
                                            returnedStr.matches(".*" + Pattern.quote(request) + "(|\\p{Space}.*)")) {
                                       // System.out.println(returnedStr);
                                        break;
                                    }
                                } catch (IOException ignored) {
                                }
                            }
                        }
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                });
            }
            threads.shutdown();
            threads.awaitTermination(threadNum * TIME, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            threads.shutdownNow();
        }
    }
}
