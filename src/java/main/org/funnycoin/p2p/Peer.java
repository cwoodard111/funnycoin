package org.funnycoin.p2p;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.funnycoin.FunnycoinCache;
import org.funnycoin.blocks.Block;
import org.funnycoin.p2p.server.PeerServer;
import org.funnycoin.transactions.Transaction;
import org.funnycoin.wallet.SignageUtils;

import java.io.*;
import java.net.*;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class Peer {
    public String address;
    public Socket socket;

    public Peer(String address) {
        this.address = address;
    }

    public void connectToPeer() throws Exception {
        if(peerIsOnline()) {
            System.out.println("yea");
            socket = new Socket(address, 51241);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        startListener();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public boolean peerIsOnline() {
        String hostName = address;
        int port = 51241;
        boolean isAlive = false;
        SocketAddress socketAddress = new InetSocketAddress(hostName, port);
        Socket socketL = new Socket();
        int timeout = 2000;

        try {
            socketL.connect(socketAddress, timeout);
            socketL.close();
            isAlive = true;

        } catch (SocketTimeoutException exception) {
            System.out.println("SocketTimeoutException " + hostName + ":" + port + ". " + exception.getMessage());
        } catch (IOException exception) {
            System.out.println(
                    "IOException - Unable to connect to " + hostName + ":" + port + ". " + exception.getMessage());
        }
        System.out.println(isAlive);
        return isAlive;
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

    public boolean startListener() throws Exception {
        System.out.println("starting listener");
        while(true) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("kkkkkkjj");
            String templine;
            if((templine = reader.readLine()) != null) {
                System.out.println(templine + " YAY");
                JsonObject json = JsonParser.parseString(templine).getAsJsonObject();
                String event = json.get("event").getAsString();
                if(event.toLowerCase().contains("transaction")) {
                    JsonObject transactionData = json.getAsJsonObject("transactionData");
                    JsonElement temporaryElement = json.remove("signatureData");
                    String rawData = temporaryElement.toString();
                    String ownerKey = transactionData.get("ownerKey").getAsString();
                    String recipientKey = transactionData.get("recipientKey").getAsString();
                    String signatureRaw = transactionData.get("signatureData").getAsString();
                    float amount = transactionData.get("amount").getAsFloat();
                    Transaction transaction = new Transaction(ownerKey,recipientKey,amount,signatureRaw);
                    List<Block> blockChainTemp = new ArrayList<>(FunnycoinCache.blockChain);
                    Block currentBlockTemp = blockChainTemp.get(FunnycoinCache.blockChain.size() - 1);
                    currentBlockTemp.transactions.add(transaction);
                    blockChainTemp.set(FunnycoinCache.blockChain.size() - 1, currentBlockTemp);
                    boolean chainValid = true;
                    String hashTarget = new String(new char[3]).replace('\0', '0');
                    for(int j = 0; j < blockChainTemp.size(); j++) {
                        /**
                         * Performing various checks on the temporary blockchain
                         */
                        Block loopBlock = blockChainTemp.get(j);
                        Block previousBlockLoop = blockChainTemp.get(j - 1);

                        if(!loopBlock.hash.equals(loopBlock.getHash())) {
                            System.out.println("Current hash is not equal");
                            chainValid = false;
                        }
                        if(!previousBlockLoop.hash.equals(loopBlock.getPreviousHash())) {
                            System.out.println("The previous hash of this block is not correct");
                            chainValid = false;
                        }

                        for(int i = 0; i < loopBlock.transactions.size();i++) {
                            Transaction loopTransaction = loopBlock.transactions.get(i);

                            if(!loopTransaction.verify(rawData,loopTransaction.signature, (PublicKey) SignageUtils.getPublicKey(loopTransaction.getOwnerKey()))) {
                                System.out.println("Looks like it wasn't signed by the owner!");
                                chainValid = false;
                            }

                            if(transaction.getAmount() > getBalanceFromChain(transaction.getOwnerKey())) {
                                System.out.println("Balance insufficient");
                                chainValid = false;
                            }
                        }
                    if(chainValid) {
                        if(!loopBlock.hash.substring(0,3).equals(hashTarget)) {
                            System.out.println("Block has not been mined!");
                            loopBlock.mine(3);
                            chainValid = true;
                        }
                    }


                    }
                    if(chainValid) {
                        Gson gson = new Gson();
                        FunnycoinCache.blockChain = blockChainTemp;
                        String newJson = gson.toJson(FunnycoinCache.blockChain);
                        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("blockchain.json")));
                        writer.write(newJson);
                        writer.close();
                        FunnycoinCache.peerServer.broadcast(templine);
                    } else {
                        System.out.println("Didn't add block to the chain, it's broken.");
                    }

                }
            }
        }
    }
}
