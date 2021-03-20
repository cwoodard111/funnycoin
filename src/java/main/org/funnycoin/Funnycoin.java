package org.funnycoin;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.funnycoin.blocks.Block;
import org.funnycoin.transactions.Transaction;
import org.funnycoin.wallet.Wallet;

import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Funnycoin {

    Funnycoin(NodeType type) throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        if(type == NodeType.MINER) {
            File blockChainFile = new File("blockchain.json");
            BufferedReader chainReader = new BufferedReader(new FileReader(blockChainFile));
            StringBuilder chainBuilder = new StringBuilder();
            String line;
            while((line = chainReader.readLine()) != null) {
                chainBuilder.append(line);
            }
            String json = chainBuilder.toString();
            Wallet wallet1 = new Wallet();
            Wallet wallet2 = new Wallet();

            if(json.length() < 1) {
                Wallet genesisWallet = new Wallet();
                genesisWallet.sendGenesis(wallet2.publicKey,50.f);
            } else {
                JsonParser chainParser = new JsonParser();
                JsonArray blockChainArray = (JsonArray) chainParser.parse(json);
                Gson gson = new Gson();
                Block[] blockChain = gson.fromJson(blockChainArray, Block[].class);
                FunnycoinCache.blockChain = Arrays.asList(blockChain);
            }
                Block currentBlock = FunnycoinCache.blockChain.get(FunnycoinCache.blockChain.size() - 1);

                System.out.println(wallet1.getBase64Key(wallet1.publicKey) + " wallet2:" + wallet2.getBase64Key(wallet2.publicKey));
                while(true) {
                    Scanner scanner = new Scanner(System.in);
                    String linew;
                    if((linew = scanner.nextLine()).toLowerCase().contains("send")) {
                        String[] args = linew.split(" ");
                        String reciever = args[1];
                        float amount = Float.parseFloat(args[2]);
                        wallet2.send(reciever,amount);
                    }

                    System.out.println(wallet1.getBalanceFromChain(wallet1.publicKey) + " " + wallet2.getBalanceFromChain(wallet2.publicKey));
            }
        }
    }
    Funnycoin(NodeType type, String publicKey) throws IOException {
        if(type == NodeType.WALLET) {
            loadChainWallet();
            float balance = getBalanceFromChain(publicKey);
        }
    }

    private float getBalanceFromMiningNodes(String publicKey) {
        //TODO add p2p network to give the client it's balance but for now we're just going to require everyone to download the chain for testing.
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

    public static void main(String[] args) throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
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
