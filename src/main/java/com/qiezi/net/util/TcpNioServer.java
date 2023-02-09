package com.qiezi.net.util;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

public class TcpNioServer {
    private MainReactor mainReactor;
    private ServerSocketChannel ss;
    private int mainNum;
    private int workerNum;
    private int port;

    public static final class TcpNioServerBuilder {
        private int mainNum;
        private int workerNum;
        private int port;

        private TcpNioServerBuilder() {

        }

        public static TcpNioServerBuilder builder() {
            return new TcpNioServerBuilder();
        }

        public TcpNioServerBuilder withMainNum(int mainNum) {
            this.mainNum = mainNum;
            return this;
        }

        public TcpNioServerBuilder withWorkerNum(int workerNum) {
            this.workerNum = workerNum;
            return this;
        }

        public TcpNioServerBuilder withPort(int port) {
            this.port = port;
            return this;
        }

        public TcpNioServer build() {
            TcpNioServer tcpNioServer = new TcpNioServer();
            tcpNioServer.workerNum = this.workerNum;
            tcpNioServer.port = this.port;
            tcpNioServer.mainNum = this.mainNum;
            return tcpNioServer;
        }
    }

    public void start() throws IOException {
        this.ss = ServerSocketChannel.open();
        this.ss.bind(new InetSocketAddress("0.0.0.0", this.port));
        this.ss.configureBlocking(false);
        WorkerReactor[] reactors = new WorkerReactor[workerNum];

        //start the main reactor thread
        this.mainReactor = new MainReactor(ss, reactors);
        this.mainReactor.start();

        //start the worker reactor threads
        for (int i = 0; i < reactors.length; i++) {
            reactors[i] = new WorkerReactor();
            reactors[i].start();
        }
    }
}
