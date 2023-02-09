package com.qiezi.net.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class MainReactor implements Runnable {

    private static Selector selector;
    private ServerSocketChannel ss;

    private WorkerReactor[] workerReactors;

    private static final int SELECTOR_WAIT_MS_INTERVAL = 100;

    private AtomicLong counter = new AtomicLong(0);

    static {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MainReactor(int port, int workerNum) {
        try {
            ss = ServerSocketChannel.open();
            ss.bind(new InetSocketAddress("0.0.0.0", port));
            register(ss);
            workerReactors = new WorkerReactor[workerNum];
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void register(ServerSocketChannel serverSocketChannel) {
        try {
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (ClosedChannelException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                selector.select(SELECTOR_WAIT_MS_INTERVAL);
                Set<SelectionKey> keySet = selector.selectedKeys();
                Iterator<SelectionKey> keysIterator = keySet.iterator();
                while (keysIterator.hasNext()) {
                    SelectionKey key = keysIterator.next();
                    if (key.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        Connection conn = Connection.buildConnection(socketChannel, key);
                        key.attach(conn);
                        getWorker().register(conn);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private WorkerReactor getWorker() {
        int index = (int) (counter.getAndIncrement()
                % workerReactors.length);
        return workerReactors[index];
    }
}
