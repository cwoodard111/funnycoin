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
        if(peerBuilder.toString().length() > 1) {
            for (int k = 0; k < peersArray.length; k++) {
                Peer p = peersArray[k];
                if (p.peerIsOnline()) {
                    try {
                        System.out.println(p.address + ":" + p.port);
                            System.out.println("connecting");
                            peers.add(p);
                            p.connectToPeer();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("SIZE: " + peers.size());
        if(peers.size() == 0) {
            System.out.println("we dont have any peers unfortunately, gonna wait till someone tries to connect to someone i guess.");
            while (peers.size() == 0) {
                for (int k = 0; k < peers.size(); k++) {
                    Peer p = peers.get(k);
                    if (p.peerIsOnline()) {
                        try {
                            if ((p.port != FunnycoinCache.peerServer.port)) {
                                peers.add(p);
                                p.connectToPeer();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            System.out.println("peers added successfully!");
        }
    }
    public void removeDuplicates() {
        for(Peer p : peers) {
            peers.removeIf(j -> j.address == p.address);
        }
    }

}
