package org.funnycoin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.funnycoin.blocks.Block;
import org.funnycoin.p2p.NetworkManager;
import org.funnycoin.transactions.Transaction;
import org.funnycoin.wallet.SignageUtils;
import org.funnycoin.wallet.Wallet;

import java.io.IOException;
import java.security.Security;
import java.util.Scanner;

import static org.funnycoin.FunnycoinCache.*;

public class Funnycoin {
    private int difficulty = 6;

    private void getBlocksAfter(int height) {
        try {
            JsonObject requestObject = new JsonObject();
            requestObject.addProperty("event","getBlocksAfter");
            peerServer.broadcast(requestObject.toString());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    Funnycoin(NodeType type) throws Exception {
        Gson gson = new Gson();
        if(type == NodeType.MINER) {
            System.out.println("selected type: miner");
            /**
             * Later on in the class, i define a method that asks other nodes to send their blockchain starting after a block height of what i currently have.
             */
            if(blockChain.size() == 0) {
                Block genesis = FunnycoinCache.getCurrentBlock();
                genesis.transactions.add(new Transaction("coinbase",wallet.getBase64Key(wallet.publicKey),50.0f,"null"));
                genesis.mine(difficulty);
                blockChain.add(genesis);
                syncBlockchainFile();
                /**
                 * We have set the local blockchain since we know our own block is valid!
                 * we are going to send the block to other people now.
                 */
                JsonObject object = new JsonObject();
                object.addProperty("event","newBlock");
                object.addProperty("block",gson.toJson(genesis));
                peerServer.broadcast(object.toString());
            } else {

            }




















        } else if(type == NodeType.WALLET) {
            NetworkManager manager = new NetworkManager();
            Wallet myWallet = new Wallet();
            while(true) {
                Scanner scanner = new Scanner(System.in);
                String cmd = scanner.nextLine();
                String[] args = cmd.split(" ");
                for(String arg : args) {
                    System.out.println(arg);
                }
                String command = args[0];

                if(command.toLowerCase().contains("send")) {
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
                    // creating signature now hold up lemme find the method
                    String signatureData = SignageUtils.sign(SignageUtils.applySha256(ownerKey + outputKey + amount), myWallet.privateKey);
                    transactionData.addProperty("signatureData",signatureData);
                    manager.broadcast(transactionData.toString());
                }

            }
        }
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
