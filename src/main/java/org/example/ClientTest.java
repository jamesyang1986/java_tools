package org.example;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketOption;

public class ClientTest {
    public static void main(String [] args){
        try {
            Socket socket = new Socket("127.0.0.1", 9090);
            socket.setTcpNoDelay(true);
            socket.getOutputStream().write("hello".getBytes());
            socket.getInputStream().read(new byte[1024]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
