package org.funnycoin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.funnycoin.blocks.Block;
import org.funnycoin.p2p.PeerLoader;
import org.funnycoin.p2p.server.PeerServer;
import org.funnycoin.wallet.Wallet;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
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


    public static int getBlockDifficulty(int height) {
        int difficulty = FunnycoinCache.getDifficulty;
        if(FunnycoinCache.blockChain.size() > 3) {
            Block p = FunnycoinCache.blockChain.get(height - 1);
            Block p2 = FunnycoinCache.blockChain.get(height - 2);

            long b = p.timeStamp / 1000;
            long b2 = p2.timeStamp / 1000;
            long difference = b - b2;
            if(difference > 160) {
                difficulty = 6;
            } else if(difference > 130) {
                difficulty = 7;
            } else if(difference > 110) {
                difficulty = 9;
            } else if(difference > 90) {
                difficulty = 9;
            } else if(difference > 70) {
                difficulty = 11;
            } else if(difference > 50) {
                difficulty = 8;
            } else if(difference > 30) {
                difficulty = 12;
            } else if(difference > 9) {
                difficulty = 16;
            } else if(difference > 5) {
                difficulty = 16;
            }
        }
        getDifficulty = difficulty;
        return difficulty;
    }


    public static PeerServer peerServer = new PeerServer();

    public static Block getNextBlock() {
        return new Block(getCurrentBlock().getHash());
    }

    public static PeerLoader peerLoader = new PeerLoader();


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
        if(chainBuilder.toString().length() > 1) {
            String json = chainBuilder.toString();
            JsonParser chainParser = new JsonParser();
            JsonArray blockChainArray = (JsonArray) chainParser.parse(json);
            Gson gson = new Gson();
            Block[] blockChain = gson.fromJson(blockChainArray, Block[].class);
            List<Block> blocksList = Arrays.asList(blockChain);
            FunnycoinCache.blockChain.addAll(blocksList);
        }
    }

    public static int getDifficulty = 8;

    public static List<List<Block>> gatheredBlocks = new ArrayList<>();

    public static String ip;

    static {
        try {
            ip = getIp();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getIp() throws IOException {
        URL url = new URL("http://checkip.amazonaws.com/");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        return reader.readLine();
    }

    public static JsonParser parser = new JsonParser();

}
