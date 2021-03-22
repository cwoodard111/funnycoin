package org.funnycoin;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.funnycoin.blocks.Block;
import org.funnycoin.p2p.NetworkManager;
import org.funnycoin.p2p.server.PeerServer;
import org.funnycoin.transactions.Transaction;
import org.funnycoin.wallet.SignageUtils;
import org.funnycoin.wallet.Wallet;

import java.io.*;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Funnycoin {

    Funnycoin(NodeType type) throws Exception {
        if(type == NodeType.MINER) {
            System.out.println("loaded mining system ");
            File blockChainFile = new File("blockchain.json");
            BufferedReader chainReader = new BufferedReader(new FileReader(blockChainFile));
            StringBuilder chainBuilder = new StringBuilder();
            String line;
            while((line = chainReader.readLine()) != null) {
                chainBuilder.append(line);
            }
            String json = chainBuilder.toString();
            /**
             *
             */
            if(json.length() < 1) {
                Block genesis = new Block("first block ever");
                ArrayList<Transaction> transactionList = new ArrayList<>();
                transactionList.add(new Transaction("coinbase","MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAK4cjvdZ1XfjAcK2s3ysMrSiVEIi9hNl3il79DAFmKg3tyet7l5i8gRy2j2qvfe8KkQGBO9EAOWjfrKWazDKLqMCAwEAAQ==",10000f,"null"));
                genesis.transactions = transactionList;
                FunnycoinCache.blockChain.add(genesis);
                BufferedWriter chainWriter = new BufferedWriter(new FileWriter(blockChainFile));
                Gson gson = new Gson();
                chainWriter.write(gson.toJson(FunnycoinCache.blockChain));
                chainWriter.close();
            } else {
                JsonParser chainParser = new JsonParser();
                JsonArray blockChainArray = (JsonArray) chainParser.parse(json);
                Gson gson = new Gson();
                Block[] blockChain = gson.fromJson(blockChainArray, Block[].class);
                FunnycoinCache.blockChain = Arrays.asList(blockChain);
            }
            System.out.println("Loading peerserver:");
            PeerServer server = FunnycoinCache.peerServer;
            System.out.println("we genned it i think? thatll be funny");
            server.initListeners();
            Thread runner = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        NetworkManager manager = new NetworkManager();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            System.out.println("ok and?");



            //TODO: CHECK IF BLOCK VALID ON SEND WITH P2P NETWORK

        } else if(type == NodeType.WALLET) {
            System.out.println("pre nw");
            NetworkManager manager = new NetworkManager();
            System.out.println("tetatst1243ing");
            Wallet myWallet = new Wallet();
            System.out.println("hallo wallet");
            while(true) {
                Scanner scanner = new Scanner(System.in);
                String cmd = scanner.nextLine();
                String[] args = cmd.split(" ");
                for(String arg : args) {
                    System.out.println(arg);
                }
                String command = args[0];

                if(command.toLowerCase().contains("send")) {
                    System.out.println("yeasss");
                    String ownerKey = myWallet.getBase64Key(myWallet.publicKey);
                    String outputKey = args[1];
                    float amount = Float.parseFloat(args[2]);

                    /**
                     * Setup transaction json for sending to remote nodes :)
                     */
                    JsonObject transactionData = new JsonObject();
                    transactionData.addProperty("event","transaction");
                    transactionData.addProperty("ownerKey",ownerKey);
                    transactionData.addProperty("recipientKey",outputKey);
                    transactionData.addProperty("amount",amount);
                    System.out.println(transactionData.toString());
                    // creating signature now hold up lemme find the method
                    System.out.println(SignageUtils.applySha256(ownerKey + outputKey + amount));
                    String signatureData = SignageUtils.sign(SignageUtils.applySha256(ownerKey + outputKey + amount), myWallet.privateKey);
                    System.out.println("signed: " + signatureData);
                    transactionData.addProperty("signatureData",transactionData.toString());
                    manager.broadcast(transactionData.toString());
                }

            }
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

    public static void main(String[] args) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        for(String arg : args) {
            System.out.println(arg);
        }
        if(args.length == 1) {
            if(args[0].toLowerCase().equals("miner")) {
                new Funnycoin(NodeType.MINER);
            } else if(args[0].toLowerCase().contains("wallet")) {
                new Funnycoin(NodeType.WALLET);
            }
        }
    }





    public enum NodeType {
        MINER,
        VOTER,
        WALLET
    }
}
