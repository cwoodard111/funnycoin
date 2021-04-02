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

    public void broadcast(String message) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.println(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
