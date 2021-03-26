package org.funnycoin.p2p.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;

public class PeerServer {
    ServerSocket s;
    Socket socket;

    public PeerServer() {
        try {
            s = new ServerSocket(51341);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void init() throws IOException {
        socket = s.accept();
    }

    public void broadcast(String message) throws IOException {
        System.out.println("SENDING " + message);
        PrintWriter writer = new PrintWriter(socket.getOutputStream(),true);
        writer.println(message + "\n");
    }
}
