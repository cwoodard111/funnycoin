package org.funnycoin.p2p;

import com.dosse.upnp.UPnP;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.funnycoin.FunnycoinCache;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PeerLoader {
    public List<Peer> peers;
    public PeerLoader() {
        peers = new ArrayList<>();
    }

    public void syncPeerFile() throws IOException {
        File peersf = new File("peers.json");
        BufferedWriter writer = new BufferedWriter(new FileWriter(peersf));
        writer.write(new Gson().toJson(FunnycoinCache.peerLoader.peers));
        writer.close();
    }

    public void init() {
        File peersf = new File("peers.json");
        StringBuilder peerBuilder = new StringBuilder();
        try {
            String bufs;
            BufferedReader buf = new BufferedReader(new InputStreamReader(new FileInputStream(peersf)));
            while((bufs = buf.readLine()) != null) {
                peerBuilder.append(bufs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Peer[] peersArray = new Gson().fromJson(peerBuilder.toString(),Peer[].class);
        for(Peer p : peersArray) {
            System.out.println(p.address + " null");
            if(p.peerIsOnline()) {
                try {
                    if (!(p.address.contains(FunnycoinCache.getIp()))) {
                        peers.add(p);
                        p.connectToPeer();
                    }
                } catch (Exception e ) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("SIZE: " + peers.size());
        if(peers.size() == 0) {
            while (peers.size() == 0) {
                for (Peer p : peersArray) {
                    if (p.peerIsOnline()) {
                        try {
                            if (!(p.address.contains(FunnycoinCache.getIp()))) {
                                peers.add(p);
                                p.connectToPeer();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
