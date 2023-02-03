package com.qiezi.net.util;

public class WritePacket {
    private Connection conn;
    private byte[] data;

    public WritePacket(Connection conn, byte[] data) {
        this.conn = conn;
        this.data = data;
    }

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "[Connection:" + conn.getEndPoint()
                + " len:" + (data == null ? 0 : data.length)
                + "]";
    }
}
