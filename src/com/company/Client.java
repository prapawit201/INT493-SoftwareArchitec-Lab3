
package com.company;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {

    public static void main(String[] args) throws Exception {
        Selector selector = Selector.open();
        Scanner sc = new Scanner(System.in);
        SocketChannel clientCh = SocketChannel.open();
        clientCh.configureBlocking(false);
        clientCh.register(selector, SelectionKey.OP_CONNECT);
        ExecutorService executorService = Executors.newSingleThreadExecutor(Executors.defaultThreadFactory());
        System.out.print("Connecting port : ");
        int port = Integer.parseInt(sc.nextLine());
        clientCh.connect(new InetSocketAddress("127.0.0.1", port));

        while (true) {
            System.out.print("msg : ");
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> it = keys.iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                if (key.isConnectable()) {
                    SocketChannel ch = (SocketChannel) key.channel();
                    ch.finishConnect();
//                    if (!ch.finishConnect()) {
//                        ch.close();
//                        continue;
//                    }
                    ByteBuffer buffer = ByteBuffer.allocate(20);
                    buffer.flip();
                    ch.write(buffer);

                        executorService.submit(() -> {

                            while (true) {
                                buffer.clear();
                                String msg = sc.nextLine();
                                buffer.put(msg.getBytes());
                                buffer.flip();
                                ch.write(buffer);
                            }
                        });
                    System.out.println("Connecting with " + ch.getLocalAddress() + " to "
                            + ch.getRemoteAddress());

                    ch.configureBlocking(false);
                    ch.register(selector, SelectionKey.OP_READ);
                }

                if (key.isReadable()) {
                    SocketChannel ch = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(20);
                    int n = ch.read(buffer);
                    if (n == -1) {
                        ch.close();
                        continue;
                    }
                    buffer.flip();
                    String msg = new String(buffer.array());
                    System.out.println(ch.getLocalAddress()+" says :"+msg.trim());
//                    if (msg.trim().equalsIgnoreCase("Connected")) {
//                        new Thread(() -> {
//                            try {
//                                while (true) {
//                                    SocketChannel ch2 = (SocketChannel) key.channel();
//                                    ByteBuffer buf2 = ByteBuffer.allocate(1024);
//                                    System.out.print(ch2.getLocalAddress() + " : ");
//                                    String message = sc.nextLine();
//                                    buf2.put(message.getBytes());
//                                    buf2.flip();
//                                    ch2.write(buf2);
//
//                                    ch.read(buf2);
//                                    buf2.flip();
//                                    System.out.println(new String(buf2.array()));
//                                }
//                            } catch (Exception e) {
//
//                            }
//                        }).start();
//                    }
                }

                it.remove();
            }
        }
    }
}