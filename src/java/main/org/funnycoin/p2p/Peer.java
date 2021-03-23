package org.funnycoin.p2p;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.funnycoin.FunnycoinCache;
import org.funnycoin.blocks.Block;
import org.funnycoin.p2p.server.PeerServer;
import org.funnycoin.transactions.Transaction;
import org.funnycoin.wallet.SignageUtils;
import org.funnycoin.wallet.Wallet;

import java.io.*;
import java.net.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class Peer {
    public String address;
    public Socket socket;

    public Peer(String address) {
        this.address = address;
    }

    public void connectToPeer() throws Exception {
        if(peerIsOnline()) {
            System.out.println("The peer is online.");
            socket = new Socket(address, 51241);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        startListener();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }
    }

    public boolean peerIsOnline() {
        String hostName = address;
        int port = 51241;
        boolean isAlive = false;
        SocketAddress socketAddress = new InetSocketAddress(hostName, port);
        Socket socketL = new Socket();
        int timeout = 2000;

        try {
            socketL.connect(socketAddress, timeout);
            socketL.close();
            isAlive = true;

        } catch (SocketTimeoutException exception) {
            System.out.println("SocketTimeoutException " + hostName + ":" + port + ". " + exception.getMessage());
        } catch (IOException exception) {
            System.out.println(
                    "IOException - Unable to connect to " + hostName + ":" + port + ". " + exception.getMessage());
        }
        System.out.println(isAlive);
        return isAlive;
    }

    public void startListener() throws Exception {
        System.out.println("starting listener");
            while(true) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String tmp;
                if((tmp = reader.readLine()) != null) {
                    System.out.println(tmp);
                }
            }
        }
    }
