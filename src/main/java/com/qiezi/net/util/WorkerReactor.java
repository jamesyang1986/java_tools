package com.qiezi.net.util;

import java.io.IOException;
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
import java.util.concurrent.atomic.AtomicLong;

public class WorkerReactor extends Thread {
    private static Selector selector;

    private static final int SELECTOR_WAIT_TIME_MS = 10;

    private static AtomicLong counter = new AtomicLong(0);

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
        super("worker-" + counter.getAndIncrement());
    }

    public void register(SocketChannel socketChannel) {
        try {
            socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT);
        } catch (ClosedChannelException e) {
            throw new RuntimeException(e);
        }
    }

    public void register(Connection connection) {
        try {
            connection.getSocketChannel().register(selector,
                    SelectionKey.OP_READ | SelectionKey.OP_WRITE
                            | SelectionKey.OP_CONNECT);
        } catch (ClosedChannelException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                selector.select(SELECTOR_WAIT_TIME_MS);
                Set<SelectionKey> keySet = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = keySet.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    handleEvent(key);

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }


    private void handleEvent(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.configureBlocking(false);

        Connection conn = (Connection) key.attachment();
        if (conn != null) {
            conn.setKey(key);
        }
        if (key.isReadable()) {
            conn.read();
        } else if (key.isConnectable()) {
            //handle client socket connect event

        } else if (key.isWritable()) {
            conn.doWrite();
        }
    }

}
