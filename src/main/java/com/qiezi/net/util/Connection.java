package com.qiezi.net.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class Connection {
    private SocketChannel socketChannel;
    private InetSocketAddress remoteAddress;

    public String ip;
    public int port;

    private WorkerReactor worker;

    public Connection(SocketChannel socketChannel, InetSocketAddress remoteAddress) {
        this.socketChannel = socketChannel;
        this.remoteAddress = remoteAddress;
        this.ip = this.remoteAddress.getAddress().getHostAddress();
        this.port = this.remoteAddress.getPort();
    }

    public static Connection buildConnection(SocketChannel socketChannel) {
        try {
            InetSocketAddress socketAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
            return new Connection(socketChannel, socketAddress);
        } catch (IOException e) {
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
