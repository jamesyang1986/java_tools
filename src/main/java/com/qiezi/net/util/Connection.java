package com.qiezi.net.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Connection {
    private SocketChannel socketChannel;
    private InetSocketAddress remoteAddress;

    private SelectionKey key;

    public String ip;
    public int port;

    private WorkerReactor worker;

    public Connection(SocketChannel socketChannel, SelectionKey key) throws IOException {
        this.socketChannel = socketChannel;
        this.key = key;
        this.remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
        this.ip = this.remoteAddress.getAddress().getHostAddress();
        this.port = this.remoteAddress.getPort();
    }

    public static Connection buildConnection(SocketChannel socketChannel, SelectionKey key) {
        try {
            return new Connection(socketChannel, key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getEndPoint() {
        return ip + ":" + port;
    }
}
