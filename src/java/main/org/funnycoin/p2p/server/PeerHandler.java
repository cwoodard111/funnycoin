package org.funnycoin.p2p.server;

import com.codebrig.beam.Communicator;
import com.codebrig.beam.handlers.BeamHandler;
import com.codebrig.beam.messages.BasicMessage;
import com.codebrig.beam.messages.BeamMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.funnycoin.Funnycoin;
import org.funnycoin.FunnycoinCache;
import org.funnycoin.blocks.Block;
import org.funnycoin.p2p.Peer;
import org.funnycoin.p2p.RequestParams;
import org.funnycoin.transactions.Transaction;
import org.funnycoin.wallet.SignageUtils;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


public class PeerHandler extends BeamHandler {
    public PeerHandler() {
        super(0);
    }

    private float getBalanceFromChain(String publicKey, String token) {
        token = token.toUpperCase();
        float balance = 0.0f;
        for(Block block : FunnycoinCache.blockChain) {
            for(Transaction transaction : block.getTransactions()) {
                if((transaction.getOutputKey().contains(publicKey)) && transaction.getToken().equals(token)) {
                    balance += transaction.getAmount();
                }
            }
        }
        return balance;
    }
    public String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(input.getBytes("UTF-8"));

            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BeamMessage messageReceived(Communicator communicator, BeamMessage beamMessage) {
        System.out.println("message recieved");
        String msg = beamMessage.get("message");
        Gson gson = new Gson();
        if(beamMessage.get("event").toLowerCase().contains("newTransaction")) {
            JsonParser parser = new JsonParser();
            JsonObject obj = (JsonObject) parser.parse(beamMessage.get("message"));
            if(getBalanceFromChain(obj.get("ownerWallet").getAsString(),obj.get("token").getAsString()) < obj.get("amount").getAsFloat()) {
                // Think it works.
                RequestParams.interrupted = true;
                String ownerWallet = obj.get("ownerWallet").getAsString();
                String targetWallet = obj.get("targetWallet").getAsString();
                float amount = obj.get("amount").getAsFloat();
                String token = obj.get("token").getAsString();
                int version = obj.get("version").getAsInt();
                String signature = obj.get("signature").getAsString();

                String txHash = applySha256(ownerWallet + targetWallet + amount + token + version);

                Transaction b = new Transaction(obj.get("ownerWallet").getAsString(),obj.get("targetWallet").getAsString(),obj.get("amount").getAsFloat(),signature,token);
                try {
                    if(b.verify(txHash,signature, SignageUtils.getPublicKey(ownerWallet))) {
                         /*
                         Transaction was signed and is valid and is not double spending.
                         */
                        FunnycoinCache.getNextBlock().transactions.add(b);
                        RequestParams.interrupted = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if(beamMessage.get("event").toLowerCase().contains("newblock")) {
            List<Block> tempChain = new ArrayList<>(FunnycoinCache.blockChain);
            tempChain.add(gson.fromJson(msg,Block.class));
            try {
                if(isChainValid(tempChain)) {
                    RequestParams.interrupted = true;
                    System.out.println("chain is valid.");
                    FunnycoinCache.blockChain = tempChain;
                    FunnycoinCache.syncBlockchainFile();
                    System.out.println("synced blockchain file");
                } else {
                    System.out.println("the chain is not valid");
                }
            } catch (Exception e) {
                System.out.println("EXCEPTION BRO");
                e.printStackTrace();
            }
        } else if(beamMessage.get("event").toLowerCase().contains("nodejoin")) {
            System.out.println("A node has logged on.");
            String address = beamMessage.get("address");
            int port = Integer.parseInt(beamMessage.get("port"));
            System.out.println(address + ":" + port);
            Peer p = new Peer(address, port);
            if (FunnycoinCache.peerLoader.peers.size() < 1) {
                for(Peer peer : FunnycoinCache.peerLoader.peers) {
                    if (!(peer.address == p.address && peer.port == p.port)) {
                    }
                    try {
                        p.connectToPeer();
                        FunnycoinCache.peerLoader.peers.add(p);
                        FunnycoinCache.peerLoader.syncPeerFile();
                        System.out.println("added peer to peers.");
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.err.println("couldn't write to peer file for some reason:" + e.getMessage());
                    }
                }
            } else {
                for (int k = 0; k < FunnycoinCache.peerLoader.peers.size(); k++) {
                    Peer j = FunnycoinCache.peerLoader.peers.get(k);
                    if (p.port != j.port) {
                        try {
                            FunnycoinCache.peerLoader.peers.add(p);
                            FunnycoinCache.peerLoader.syncPeerFile();
                            System.out.println("added peer to peers.");
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.println("couldn't write to peer file for some reason:" + e.getMessage());
                        }
                        FunnycoinCache.peerLoader.peers.get(FunnycoinCache.peerLoader.peers.size()).connectToPeer();
                    }
                }
            }
            FunnycoinCache.peerLoader.removeDuplicates();
        }
        return beamMessage.emptyResponse();
    }


    private boolean isChainValid(List<Block> blockChain) throws Exception {
        boolean valid = false;
        for(int i = 0; i < blockChain.size(); i++) {
            Block currentBlock = blockChain.get(i);
            if(blockChain.size() > 1 && i != 0) {
                Block previousBlock = blockChain.get(i - 1);
                /**
                 * Performing checks on the temporary blockchain to see if it's valid.
                 */
                if(!currentBlock.hash.equals(currentBlock.getHash())) {
                    System.out.println("The hash of the block is not equal to the calculated value.");
                    return false;
                }
                if(!previousBlock.hash.equals(previousBlock.getHash())) {
                    System.out.println("The hash of the previous block is not equal to the calculated value.");
                    return false;
                }
                int difficulty = 6;
                String difficultyTarget = new String(new char[difficulty]).replace('\0','0');
                System.out.println(difficultyTarget + " " + currentBlock.hash);

                if(!currentBlock.hash.substring(0,difficulty).equals(difficultyTarget)) {
                    System.out.println("The block does not have a Proof-Of-Work attached to it.");
                    return false;
                }
                /**
                 * Checking transactions for validity or errors.
                 */
                for(int j = 0; j < currentBlock.getTransactions().size(); j++) {
                    Transaction currentTransaction = currentBlock.transactions.get(j);
                    System.out.println("Transaction sender,reciever:" + currentTransaction);
                    if(currentTransaction.getOwnerKey().toLowerCase().equals("coinbase")) {
                        valid = true;
                        continue;
                    }
                    if(!currentTransaction.verify(currentTransaction.getHash(),currentTransaction.signature, SignageUtils.getPublicKey(currentTransaction.getOwnerKey()))) {
                        return false;
                    }
                }


            } else {
                /**
                 * We are going to just say it's correct because it's at genesis.
                 */
                valid = true;
            }
        }
        return valid;
    }
}
