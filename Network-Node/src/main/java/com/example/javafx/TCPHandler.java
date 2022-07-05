package com.example.javafx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


public class TCPHandler implements Runnable {

    Socket socket;
    NetworkNode node;

    public TCPHandler(Socket socket, NetworkNode node) {
        this.socket = socket;
        this.node = node;
    }

    @Override
    public void run() {

        if (NetworkNode.DEBUG_INFO) System.out.println("\n\nRUN!");

        BufferedReader inFromClient;
        PrintWriter outToClient;

        try {

            if (node.getIp() == null) node.setIp(socket.getLocalAddress());


            inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outToClient = new PrintWriter(socket.getOutputStream(), true);

            List<String[]> in = new ArrayList<>();
            in.add(inFromClient.readLine().split(" "));
            AllocationRequest request;
            List<String[]> out;

            if ("TERMINATE".equals(in.get(0)[0])) {

                if (NetworkNode.DEBUG_INFO) System.out.println("PROTOCOL CONTENT:");
                if (NetworkNode.DEBUG_INFO) in.forEach(tab -> System.out.println(Arrays.asList(tab)));

                if (NetworkNode.DEBUG_INFO) System.out.println("TERMINATION BEGAN");


                node.getResourceManager().getAllocationRequestsExecutor().shutdownNow();

                if (NetworkNode.DEBUG_INFO) System.out.println("EXECUTOR did SHUTDOWN");

                inFromClient.close();
                outToClient.close();
                socket.close();

                node.getEveryNeighbour().stream().filter(Objects::nonNull)
                        .forEach(destination -> {
                            try {
                                Socket terminationSocket = new Socket(destination.getIp(), destination.getPort());
                                sendMessage("TERMINATE\n", terminationSocket);
                                terminationSocket.close();
                            } catch (IOException ignored) {
                                System.exit(0);
                            }
                        });

                System.exit(0);

            } else if ("HELLO".equals(in.get(0)[0])) {


                if (NetworkNode.DEBUG_INFO) System.out.println("PROTOCOL CONTENT:");
                if (NetworkNode.DEBUG_INFO) in.forEach(tab -> System.out.println(Arrays.asList(tab)));

                try {
                    node.getChildrenNodes().add(new Destination(
                            Integer.parseInt(in.get(0)[1].split(":")[0]),
                            InetAddress.getByName(in.get(0)[1].split(":")[1]),
                            Integer.parseInt(in.get(0)[1].split(":")[2])));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }

                if (NetworkNode.DEBUG_INFO) System.out.println(node.getChildrenNodes().toString());

            } else if (in.get(0)[0].matches("\\d+.*")) {


                if (NetworkNode.DEBUG_INFO) System.out.println("PROTOCOL CONTENT:");
                if (NetworkNode.DEBUG_INFO) in.forEach(tab -> System.out.println(Arrays.asList(tab)));

                Integer clientId = Integer.parseInt(in.get(0)[0]);

                if (NetworkNode.DEBUG_INFO) System.out.println("allocation request received...");

                request = node.getResourceManager()
                        .requestAllocation(new AllocationRequest(clientId, in, node))
                        .get();

                if (node.equals(request.getComNode()) && request.getAllocationStatus().equals(AllocationStatus.ALLOCATE)) {
                    if (NetworkNode.DEBUG_INFO) System.out.println("managing ComNode...");
                    ServerSocket listenSocket = request.getServerSocket();
                    while (request.getAllocationStatus().equals(AllocationStatus.ALLOCATE)) {
                        request = new AllocationRequest(getMessage(listenSocket.accept()), node);
                        if (request.getAllocationStatus().equals(AllocationStatus.ALLOCATE))
                            request = node.getResourceManager().requestAllocation(request).get();
                    }
                }

                if (NetworkNode.DEBUG_INFO) {
                    System.out.println(request.getAllocationStatus());
                    System.out.println(request.buildProtocol(true));
                    System.out.println("DUPADUPA");
                }

                if (NetworkNode.DEBUG_INFO) System.out.println(request.buildProtocol(true));


                if (NetworkNode.DEBUG_INFO) System.out.println("Allocation process done. Sending info to Client");

                if (NetworkNode.DEBUG_INFO) {
                    System.out.println(request.buildProtocol(true));
                }

                outToClient.println(request.buildProtocol(true));

            } else {

                String line;
                while ((line = inFromClient.readLine()) != null) {
                    if (!line.equals("")) in.add(line.split(" "));
                }

                if (NetworkNode.DEBUG_INFO) System.out.println("PROTOCOL CONTENT:");
                if (NetworkNode.DEBUG_INFO) in.forEach(tab -> System.out.println(Arrays.asList(tab)));

                node.getResourceManager().requestAllocation(new AllocationRequest(in, node)).get();

            }

            inFromClient.close();
            outToClient.close();
            socket.close();
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static List<String[]> getMessage(Socket socket) {
        ArrayList<String[]> res = new ArrayList<>();
        try (BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line;

            if (NetworkNode.DEBUG_INFO) System.out.println("Received lines (" + socket.getPort() + "):");
            while ((line = inFromClient.readLine()) != null) {
                if (NetworkNode.DEBUG_INFO) System.out.println(line);
                res.add(line.split(" "));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    public static Boolean sendMessage(List<String[]> msg, Socket socket) {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            if (NetworkNode.DEBUG_INFO)
                System.out.println("sendMessage" + "(" + socket.getPort() + "): \n" + String.join("\n", convertInputListToStrList(msg)));
            out.println(String.join("\n", convertInputListToStrList(msg)));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static Boolean sendMessage(String msg, Socket socket) {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            if (NetworkNode.DEBUG_INFO) System.out.println("sendMessage" + "(" + socket.getPort() + "): \n" + msg);

            if (msg.charAt(msg.length() - 1) == '\n') out.print(msg);
            else out.println(msg);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static List<String> convertInputListToStrList(List<String[]> in) {
        return in.stream().map(strTab -> String.join(" ", strTab)).collect(Collectors.toList());
    }

}
