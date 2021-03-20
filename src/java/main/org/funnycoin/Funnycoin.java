package org.funnycoin;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.funnycoin.blocks.Block;
import org.funnycoin.transactions.Transaction;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Funnycoin {

    Funnycoin(NodeType type) throws IOException {
        if(type == NodeType.MINER) {
            File blockChainFile = new File("blockchain.json");
            BufferedReader chainReader = new BufferedReader(new FileReader(blockChainFile));
            StringBuilder chainBuilder = new StringBuilder();
            String line;
            while((line = chainReader.readLine()) != null) {
                chainBuilder.append(line);
            }
            String json = chainBuilder.toString();
            if(json.length() < 1) {
                System.out.println("genesis block time: TESTING WITH PUBLIC KEYS THAT DONT EXIST");
                Transaction transaction = new Transaction("blaze","blaze2",50f);
                List<Transaction> transactions = new ArrayList<>();
                transactions.add(transaction);
                Block genesisBlock = new Block(transactions);
                FunnycoinCache.blockChain.add(genesisBlock);
                Gson gson = new Gson();
                String newJson = gson.toJson(FunnycoinCache.blockChain);
                BufferedWriter writer = new BufferedWriter(new FileWriter(new File("blockchain.json")));
                writer.write(newJson);
                writer.close();
            } else {
                JsonParser chainParser = new JsonParser();
                JsonArray blockChainArray = (JsonArray) chainParser.parse(json);
                Gson gson = new Gson();
                Block[] blockChain = gson.fromJson(blockChainArray, Block[].class);
                FunnycoinCache.blockChain = Arrays.asList(blockChain);
                Block currentBlock = FunnycoinCache.blockChain.get(FunnycoinCache.blockChain.size() - 1);
                System.out.println("waiting for block");
                System.out.println("owner bal: " + getBalanceFromChain("blaze2"));
            }
        }
    }
    Funnycoin(NodeType type, String publicKey) throws IOException {
        if(type == NodeType.WALLET) {
            loadChainWallet();
            float balance = getBalanceFromChain(publicKey);
            System.out.println("BALANCE: " + balance);
        }
    }

    private float getBalanceFromMiningNodes(String publicKey) {
        //TODO add p2p network to give the client it's balance but for now we're just going to require everyone to download the chain.
        return 0.0f;
    }

    private void loadChainWallet() throws IOException {
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
    private float getBalanceFromChain(String publicKey) {
        float balance = 0.0f;
        for(Block block : FunnycoinCache.blockChain) {
            for(Transaction transaction : block.getTransactions()) {
                if((transaction.getOutputKey().contains(publicKey))) {
                    balance += transaction.getAmount();
                }
            }
        }
        return balance;
    }

    public static void main(String[] args) throws IOException {
        if(args.length == 2) {
            if(args[0].toLowerCase().equals("miner")) {
                new Funnycoin(NodeType.MINER);
            } else if(args[0].toLowerCase().contains("wallet")) {
                new Funnycoin(NodeType.WALLET,args[1]);
            }
        }
    }
    public enum NodeType {
        MINER,
        VOTER,
        WALLET
    }
}
