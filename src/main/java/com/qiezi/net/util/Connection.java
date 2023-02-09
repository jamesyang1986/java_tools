package com.qiezi.net.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class Connection {
    private SocketChannel socketChannel;
    private InetSocketAddress remoteAddress;
    private SelectionKey key;
    public String ip;
    public int port;

    private WorkerReactor worker;

    private BlockingDeque<ByteBuffer> writeQ = new LinkedBlockingDeque<ByteBuffer>();

    protected int magicNum = 0xAABB;

    public WorkerReactor getWorker() {
        return worker;
    }

    public void setWorker(WorkerReactor worker) {
        this.worker = worker;
    }

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

    public SelectionKey getKey() {
        return key;
    }

    public void setKey(SelectionKey key) {
        this.key = key;
    }

    public String getEndPoint() {
        return ip + ":" + port;
    }

    public void read() {
        this.key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        ByteBuffer header = ByteBuffer.allocate(4);
        try {
            while (true) {
                this.socketChannel.read(header);
                if (!header.hasRemaining()) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        header.flip();

        checkMagic(header);
        int len = (header.get() & 0xFF << 8 | header.get() & 0xFF)
                & 0xFFFF;
        ByteBuffer data = ByteBuffer.allocate(len);
        try {
            while (data.hasRemaining()) {
                this.socketChannel.read(data);
            }
            data.flip();
            String body = new String(data.array());
            System.out.println("receive body:" + body);

            this.key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            this.key.selector().wakeup();

            send(body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void send(String body) throws IOException {
        String returnVal = "return:" + body + " worker:"
                + this.getWorker().getName();
        int returnLen = returnVal.getBytes().length;
        byte[] sendHeader = genHeader(magicNum,
                returnLen);

        ByteBuffer toSendData = ByteBuffer.allocate(sendHeader.length + returnLen);
        toSendData.put(sendHeader);
        toSendData.put(returnVal.getBytes());
        toSendData.flip();
        writeQ.add(toSendData);

//        int writedBytes = this.socketChannel.write(toSendData);
//        if (writedBytes == 0 || toSendData.hasRemaining()) {
//            System.out.println("fail to write data.");
//        }
    }


    protected void checkMagic(ByteBuffer header) {
        byte magic1 = header.get();
        byte magic2 = header.get();

        int checkMagic = (magic1 & 0xFF) << 8 | (magic2 & 0xFF);
        if (checkMagic != magicNum) {
            throw new RuntimeException("wrong magic header.");
        }
    }

    public void doWrite() {
        this.key.interestOps(SelectionKey.OP_READ);
        while (writeQ.size() != 0) {
            ByteBuffer toSendData = writeQ.peek();
            int writedBytes = 0;
            try {
                if (!this.socketChannel.isConnected()) {
                    break;
                }

                writedBytes = this.socketChannel.write(toSendData);
                if (writedBytes == 0 || toSendData.hasRemaining()) {
                    System.out.println("fail to write data.");
                    this.key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                    this.key.selector().wakeup();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        this.key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        this.key.selector().wakeup();
    }

    protected static byte[] genHeader(int magicNum, int len) {
        byte[] data = new byte[4];
        data[0] = (byte) ((magicNum << 16) >> 24 & 0xFF);
        data[1] = (byte) ((magicNum << 24) >> 24 & 0xFF);
        data[2] = (byte) ((len << 16) >> 24 & 0xFF);
        data[3] = (byte) ((len << 24) >> 24 & 0xFF);
        return data;
    }
}
