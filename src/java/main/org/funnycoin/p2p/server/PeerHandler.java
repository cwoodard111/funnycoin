package org.funnycoin.p2p.server;

import com.codebrig.beam.Communicator;
import com.codebrig.beam.handlers.BeamHandler;
import com.codebrig.beam.messages.BasicMessage;
import com.codebrig.beam.messages.BeamMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.funnycoin.Funnycoin;
import org.funnycoin.FunnycoinCache;
import org.funnycoin.blocks.Block;
import org.funnycoin.p2p.Peer;
import org.funnycoin.p2p.RequestParams;
import org.funnycoin.transactions.Transaction;
import org.funnycoin.wallet.SignageUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PeerHandler extends BeamHandler {
    public PeerHandler() {
        super(0);
    }


    @Override
    public BeamMessage messageReceived(Communicator communicator, BeamMessage beamMessage) {
        System.out.println("message recieved");
        String msg = beamMessage.get("message");
        Gson gson = new Gson();
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
            String difficultyTarget = new String(new char[FunnycoinCache.getBlockDifficulty(i)]).replace('\0','0');
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
                if(!currentBlock.hash.substring(0,FunnycoinCache.getDifficulty).equals(difficultyTarget)) {
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
