package com.qiezi.net.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class WorkerReactor implements Runnable {
    private static Selector selector;

    private static final int SELECTOR_WAIT_TIME_MS = 10;

    public Queue<WritePacket> writePacketQueue = new LinkedBlockingQueue<>(1024);

    private static Map<String, Connection> connMap = new ConcurrentHashMap<>();

    static {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public WorkerReactor() {
    }

    public void register(SocketChannel socketChannel) {
        try {
            socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT);
        } catch (ClosedChannelException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                int eventNum = selector.select(SELECTOR_WAIT_TIME_MS);
                Set<SelectionKey> keySet = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = keySet.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        Connection connection = Connection.buildConnection(channel);
                        handleRead(connection);
                    } else if (key.isConnectable()) {
                        //handle client socket connect event

                    } else if (key.isWritable()) {

                    }
                    keyIterator.remove();
                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void write(byte[] data, Connection connection) {

    }


    private void handleRead(Connection connection) throws IOException {
        SocketChannel channel = connection.getSocketChannel();
        InetSocketAddress socketAddress = connection.getRemoteAddress();
//        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
//        channel.read(byteBuffer);
    }

}
