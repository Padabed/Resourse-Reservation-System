package com.example.javafx;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NetworkNode extends Destination {

    public static Boolean DEBUG_INFO = false;
    public static Boolean REPORT_RESOURCES_LEFT = true;

    private ServerSocket comSocket;
    private Destination parentNode;
    private final List<Destination> childrenNodes;
    private final ResourceManager resourceManager;

    public NetworkNode(Integer id, Integer clientComPort, String parentGateway, ResourceManager resourceManager) {
        super(id, clientComPort);
        childrenNodes = new ArrayList<>();

        this.resourceManager = resourceManager;


        try {
            if (DEBUG_INFO) System.out.println(parentGateway);
            this.parentNode = parentGateway != null ? new Destination(parentGateway) : null;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        if (DEBUG_INFO) System.out.println(this);

        try {
            comSocket = new ServerSocket(clientComPort);

            if (parentNode != null) {

                Socket helloSocket = new Socket(parentNode.getIp(), parentNode.getPort());
                List<String[]> helloMessage = new ArrayList<>();
                helloMessage.add(new String[]{"HELLO", this.getId() + ":"
                        + helloSocket.getLocalAddress().getHostAddress() + ":" + this.getPort()});
                TCPHandler.sendMessage(helloMessage, helloSocket);
            }

            establishServerSocket(comSocket, 8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void establishServerSocket(ServerSocket comSocket, int numberOfThreads) {
        try {
            ExecutorService threadPool = Executors.newFixedThreadPool(numberOfThreads);
            if (DEBUG_INFO) System.out.println(this);
            while (!threadPool.isShutdown()) {
                threadPool.submit(new TCPHandler(comSocket.accept(), this));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Destination getParentNode() {
        return parentNode;
    }

    public void setParentNode(Destination parentNode) {
        this.parentNode = parentNode;
    }

    public List<Destination> getChildrenNodes() {
        return childrenNodes;
    }

    public List<Destination> getEveryNeighbour() {
        return Stream.concat(childrenNodes.stream(), Stream.of(parentNode))
                .collect(Collectors.toList());
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public static void main(String[] args) {


        int ident = -1;
        int tcpPort = -1;
        String gateway = null;
        StringBuilder resourceList = null;


        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-ident":
                    ident = Integer.parseInt(args[++i]);
                    break;
                case "-tcpport":
                    tcpPort = Integer.parseInt(args[++i]);
                    break;
                case "-gateway":
                    gateway = args[++i];
                    break;
                default:
                    if (resourceList == null) resourceList = new StringBuilder(args[i]);
                    else resourceList.append(" ").append(args[i]);
            }
        }

        try {
            if (resourceList != null)
                new NetworkNode(ident, tcpPort, gateway,
                        new ResourceManager(Arrays.asList(resourceList.toString().split(" "))));
            else
                new NetworkNode(ident, tcpPort, gateway, new ResourceManager(new ArrayList<>()));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    @Override
    public String toString() {
        return "NetworkNode{" +
                "id=" + getId() +
                ", ip=" + getIp() +
                ", port=" + getPort() +
                ", listenPort=" + getListenPort() +
                ", comSocket=" + comSocket +
                ", parentNode=" + parentNode +
                ", childrenNodes=" + childrenNodes +
                ", resourceManager=" + resourceManager +
                ", everyNeighbour=" + getEveryNeighbour() +
                "} ";
    }
}
