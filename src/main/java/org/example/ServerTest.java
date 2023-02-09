package org.example;

import com.qiezi.net.util.TcpNioServer;

import java.io.IOException;

public class ServerTest {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        TcpNioServer server = TcpNioServer.TcpNioServerBuilder
                .builder()
                .withMainNum(1)
                .withWorkerNum(10)
                .withPort(9090)
                .build();
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}