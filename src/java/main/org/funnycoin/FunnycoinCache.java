package org.funnycoin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.funnycoin.blocks.Block;
import org.funnycoin.p2p.NetworkManager;
import org.funnycoin.p2p.server.PeerServer;
import org.funnycoin.transactions.Transaction;
import org.funnycoin.wallet.Wallet;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FunnycoinCache {
    public static List<Block> blockChain = new ArrayList<>();

    public static int getCirculation() {
        return blockChain.size();
    }

    public static int getReward() {
        return 50;
    }

    public static int getInputReward() {
        return blockChain.size() < 10000 ? 50 : 40;
    }

    public static Block getCurrentBlock() {
        if(FunnycoinCache.blockChain.size() == 0) {
            return new Block("genesis");
        }
        return FunnycoinCache.blockChain.get(FunnycoinCache.blockChain.size() - 1);
    }

    public static PeerServer peerServer = new PeerServer();

    public static NetworkManager manager;

    static {
        try {
            manager = new NetworkManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Wallet wallet;

    static {
        try {
            wallet = new Wallet();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void syncBlockchainFile() {
        try {
            BufferedWriter chainWriter = new BufferedWriter(new FileWriter(new File("blockchain.json")));
            Gson gson = new Gson();
            System.out.println(gson.toJson(FunnycoinCache.blockChain));
            chainWriter.write(gson.toJson(FunnycoinCache.blockChain));
            chainWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadBlockChain() throws IOException {
        File blockChainFile = new File("blockchain.json");

        BufferedReader chainReader = new BufferedReader(new FileReader(blockChainFile));
        StringBuilder chainBuilder = new StringBuilder();
        String line;
        while((line = chainReader.readLine()) != null) {
            chainBuilder.append(line);
        }
        String json = chainBuilder.toString();
        JsonParser chainParser = new JsonParser();
        JsonArray blockChainArray = (JsonArray) chainParser.parse(json);
        Gson gson = new Gson();
        Block[] blockChain = gson.fromJson(blockChainArray, Block[].class);

        FunnycoinCache.blockChain = Arrays.asList(blockChain);
    }

    public static int getDifficulty() {
        return 6;
    }
}
