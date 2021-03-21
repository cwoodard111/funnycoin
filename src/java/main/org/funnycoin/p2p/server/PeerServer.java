package org.funnycoin.p2p.server;

import org.funnycoin.FunnycoinCache;
import org.funnycoin.blocks.Block;
import org.funnycoin.transactions.Transaction;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerServer {
    public static PeerServer INSTANCE;

    static {
        try {
            INSTANCE = new PeerServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ServerSocket s;
    Socket socket;

    public PeerServer() throws Exception {
        s = new ServerSocket(3182);
        socket = s.accept();
    }


    public void broadcast(String message) throws IOException {
        ObjectOutputStream stream = (ObjectOutputStream) socket.getOutputStream();
        stream.writeObject(message);
        stream.close();
    }
}
