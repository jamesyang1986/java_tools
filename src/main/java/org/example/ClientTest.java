package org.example;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientTest {
    public static int magicNum = 0xAABB;

    public static void main(String[] args) {
        ExecutorService es = Executors.newFixedThreadPool(10);
        int taskNum = 10;
        CountDownLatch countDownLatch = new CountDownLatch(taskNum);
        for (int i = 0; i < taskNum; i++) {
            final int clientTag = i;
            es.submit(new Runnable() {
                @Override
                public void run() {
                    connectServer(clientTag);
                    countDownLatch.countDown();
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


    private static void connectServer(int clientTag) {
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", 9090);
            socket.setTcpNoDelay(true);

            for (int i = 0; i < 100; i++) {
                String testStr = "client:" + clientTag + "hello:" + i;
                int len = testStr.length();

                byte[] data = genHeader(0xAABB, len);
                socket.getOutputStream().write(data);
                socket.getOutputStream().write(testStr.getBytes());
                socket.getOutputStream().flush();

                byte[] header = new byte[4];
                socket.getInputStream().read(header);

                ByteBuffer byteBuffer = ByteBuffer.wrap(header);
                checkMagic(byteBuffer);

                len = (byteBuffer.get() & 0xFF << 8 | byteBuffer.get() & 0xFF) & 0xFFFF;
                byte[] returnData = new byte[len];
                socket.getInputStream().read(returnData);
                String body = new String(returnData);
                System.out.println(body);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static byte[] genHeader(int magicNum, int len) {
        byte[] data = new byte[4];
        data[0] = (byte) ((magicNum << 16) >> 24 & 0xFF);
        data[1] = (byte) ((magicNum << 24) >> 24 & 0xFF);
        data[2] = (byte) ((len << 16) >> 24 & 0xFF);
        data[3] = (byte) ((len << 24) >> 24 & 0xFF);
        return data;
    }

    public static void checkMagic(ByteBuffer header) {
        byte magic1 = header.get();
        byte magic2 = header.get();
        int checkMagic = (magic1 & 0xFF) << 8 | (magic2 & 0xFF);
        if (checkMagic != magicNum) {
            throw new RuntimeException("wrong magic header.");
        }
    }
}
