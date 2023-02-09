package org.example;

import com.qiezi.net.util.TcpNIOServer;
import java.io.IOException;

public class ServerTest {
    public static void main(String[] args) {
        int port = 9090;
        TcpNIOServer server = TcpNIOServer.TcpNioServerBuilder
                .builder()
                .withMainNum(1)
                .withWorkerNum(5)
                .withPort(port)
                .build();
        try {
            server.start();
            System.out.println("server start to listen on " + port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}