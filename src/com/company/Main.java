package com.company;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.*;

public class Main {
    private  static ArrayList<SocketChannel> clients = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        Selector selector = Selector.open();
        Scanner sc = new Scanner(System.in);
        ServerSocketChannel serverCh = ServerSocketChannel.open();
        serverCh.configureBlocking(false);
        System.out.print("Start server port : ");
        int port = Integer.parseInt(sc.nextLine());
        serverCh.bind(new InetSocketAddress(port));
        serverCh.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server listen port : "+ port);
        while (true) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> it = keys.iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
//                it.remove();
                if (key.isAcceptable()) {
                    ServerSocketChannel ch = (ServerSocketChannel) key.channel();
                    SocketChannel clientCh = ch.accept();
                    clientCh.configureBlocking(false);
                    System.out.println(clientCh.getRemoteAddress() + " join to server.");
                    clientCh.register(selector, SelectionKey.OP_READ);
                    clients.add(clientCh);
                }
                if (key.isReadable()) {
                    SocketChannel ch = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(20);
                    ch.configureBlocking(false);
                    buffer.clear();
                    int n = ch.read(buffer);
                    if (n == -1) {
                        ch.close();
                        continue;
                    }
                    String message = new String(buffer.array());
                    int i =1;
                    for (SocketChannel client : clients) {
                        ByteBuffer writeBuffer = ByteBuffer.allocate(20);
                        writeBuffer.put((message).getBytes());
                        System.out.println("Send msg to "+i + " " + client.getRemoteAddress()
                                + " : " + new String(writeBuffer.array()));
                        writeBuffer.flip();
                        client.write(writeBuffer);
                        i++;
                    }
                }

                it.remove();
            }
        }

    }
}