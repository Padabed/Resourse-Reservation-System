package com.example.javafx;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Objects;

public class Destination {

    private Integer id;
    private InetAddress ip;
    private Integer port;
    private Integer listenPort;

    public Destination() {
        this.id = null;
        this.ip = null;
        this.port = null;
        this.listenPort = null;
    }

    public Destination(Integer id, InetAddress ip, Integer port) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.listenPort = null;
    }

    public Destination(InetAddress ip, Integer port) {
        this.ip = ip;
        this.port = port;
        this.listenPort = null;
    }

    public Destination(InetAddress ip, Integer port, Integer listenPort) {
        this.ip = ip;
        this.port = port;
        this.listenPort = listenPort;
    }

    public Destination(String gateway) throws UnknownHostException {
        this.ip = InetAddress.getByName(gateway.split(":")[0]);
        this.port = Integer.parseInt(gateway.split(":")[1]);
        this.listenPort = null;
    }

    public Destination(Integer id, Integer port) {
        this.id = id;
        this.ip = null;
        this.port = port;
        this.listenPort = null;
    }

    public void forwardAllocationRequest(AllocationRequest request) throws IOException {
        Socket socket = new Socket(ip, port);
        TCPHandler.sendMessage(request.buildProtocol(false), socket);
    }

    public Integer getId() {
        return id;
    }

    public InetAddress getIp() {
        return ip;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getListenPort() {
        return listenPort;
    }

    public void setListenPort(Integer listenPort) {
        this.listenPort = listenPort;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }


    @Override
    public boolean equals(Object obj) {

        if (obj == null || obj.getClass() != NetworkNode.class && obj.getClass() != Destination.class)
            return false;

        Destination d = (Destination) obj;

        return Objects.equals(this.getIp().getHostAddress(), d.getIp().getHostAddress()) &&
                Objects.equals(this.getPort(), d.getPort());
    }

    @Override
    public String toString() {
        if (listenPort == null)
            return ip.getHostAddress() + ":" + port;
        return ip.getHostAddress() + ":" + port + ":" + listenPort;
    }

    public String getStringForProtocol() {
        if (listenPort == null)
            return ip.getHostAddress() + ":" + port;
        return ip.getHostAddress() + ":" + port + ":" + listenPort;
    }
}
