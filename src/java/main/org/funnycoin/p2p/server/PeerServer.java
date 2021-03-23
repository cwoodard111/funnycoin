package org.funnycoin.p2p.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerServer {
    ServerSocket s;
    Socket socket;

    public PeerServer() {
        try {
            s = new ServerSocket(51241);
            socket = s.accept();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message) throws IOException {
        ObjectOutputStream stream = new ObjectOutputStream(socket.getOutputStream());
        stream.writeObject(message);
        stream.close();
    }
}
