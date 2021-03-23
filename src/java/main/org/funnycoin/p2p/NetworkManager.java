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
                JsonParser chainParser = new JsonParser();
                JsonArray blockChainArray = (JsonArray) chainParser.parse(peers_json);
                Gson gson = new Gson();
                Peer[] peersArray = gson.fromJson(blockChainArray, Peer[].class);
                peers = Arrays.asList(peersArray);

                for(Peer peer : peers) {
                    peer.connectToPeer();
                }

            } else {
                List<Peer> peers_local = new ArrayList<Peer>();
                Peer test = new Peer("104.254.247.125");
                peers_local.add(test);
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
