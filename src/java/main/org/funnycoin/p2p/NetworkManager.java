package org.funnycoin.p2p;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.funnycoin.FunnycoinCache;
import org.funnycoin.blocks.Block;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NetworkManager {
    public static List<Peer> peers = new ArrayList<Peer>();

    public NetworkManager() throws Exception {
        init();
        System.out.println("init complete");
    }

    public void broadcast(String message) throws IOException {
        for(Peer peer : peers) {
            if(peer.peerIsOnline()) {
                BufferedWriter stream  = new BufferedWriter(new OutputStreamWriter(peer.socket.getOutputStream()));
                stream.write(message);
                stream.close();
            }
        }
    }

    private void init() throws Exception {
        File peersFile = new File("peers.json");
        if(peersFile.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(peersFile));
            String tempLine;
            StringBuilder builder = new StringBuilder();
            while((tempLine = reader.readLine()) != null) {
                builder.append(tempLine);
            }
            String peers_json = builder.toString();
            if(peers_json.length() > 1) {
                System.out.println("Loaded from file!");
                JsonParser chainParser = new JsonParser();
                JsonArray blockChainArray = (JsonArray) chainParser.parse(peers_json);
                Gson gson = new Gson();
                Peer[] peersArray = gson.fromJson(blockChainArray, Peer[].class);
                peers = Arrays.asList(peersArray);
                for(Peer p : peers) {
                    if(p.address == FunnycoinCache.ip) {
                        System.out.println("silly goose thats you!");
                        peers.remove(p);
                    }
                }
                boolean j = false;
            while(true) {
                for (Peer peer : peers) {
                    if (peer.peerIsOnline()) {
                        System.out.println("online");
                        peer.connectToPeer();
                        j = true;
                    }
                }
                if(j) {
                    break;
                }
            }

            } else {
                List<Peer> peers_local = new ArrayList<Peer>();
                Gson gson = new Gson();
                String peers_json_generated = gson.toJson(peers_local);
                BufferedWriter writer = new BufferedWriter(new FileWriter(peersFile));
                writer.write(peers_json_generated);
                writer.close();
                System.out.println("init invoke done");
            }
        } else {
            System.exit(0);
        }
    }

}
