package org.funnycoin.p2p.server;

import com.codebrig.beam.Communicator;
import com.codebrig.beam.handlers.BeamHandler;
import com.codebrig.beam.messages.BasicMessage;
import com.codebrig.beam.messages.BeamMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
            List<Block> tempChain = new ArrayList<>();
            tempChain.add(gson.fromJson(msg,Block.class));
            try {
                if(isChainValid(tempChain)) {
                    RequestParams.interrupted = true;
                    System.out.println("chain is valid.");
                    FunnycoinCache.blockChain = tempChain;
                    FunnycoinCache.syncBlockchainFile();
                } else {
                    System.out.println("the chain is not valid");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(beamMessage.get("event").toLowerCase().contains("nodejoin")) {
            System.out.println("A node has logged on.");
            String address = beamMessage.get("address");
            int port = Integer.parseInt(beamMessage.get("port"));
            System.out.println(address + ":" + port);
            Peer p = new Peer(address,port);
            for(Peer j : FunnycoinCache.peerLoader.peers) {
                if(!j.address.equals(p.address) && p.port != j.port) {
                    p.connectToPeer();
                    FunnycoinCache.peerLoader.peers.add(p);
                    try {
                        FunnycoinCache.peerLoader.syncPeerFile();
                        System.out.println("added peer to peers.");
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.err.println("couldn't write to peer file for some reason:" + e.getMessage());
                    }
                }
            }
        }
        return null;
    }
    private boolean isChainValid(List<Block> blockChain) throws Exception {
        for(int i = 0; i < blockChain.size(); i++) {
            String difficultyTarget = new String(new char[FunnycoinCache.getDifficulty()]).replace('\0','0');
            Block currentBlock = blockChain.get(i);
            if(blockChain.size() > 1) {
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
                if(!currentBlock.hash.substring(0,FunnycoinCache.getDifficulty()).equals(difficultyTarget)) {
                    System.out.println("The block does not have a Proof-Of-Work attached to it.");
                    return false;
                }
                /**
                 * Checking transactions for validity or errors.
                 */
                for(int j = 0; j < currentBlock.getTransactions().size(); j++) {
                    Transaction currentTransaction = currentBlock.transactions.get(j);

                    if(!currentTransaction.verify(currentTransaction.getHash(),currentTransaction.signature, SignageUtils.getPublicKey(currentTransaction.getOwnerKey()))) {
                        if(currentTransaction.getOwnerKey().toLowerCase().equals("coinbase")) {
                            return true;
                        }
                    }
                }


            } else {
                /**
                 * We are going to just say it's correct because it's at genesis.
                 */
                return true;
            }
        }
        return false;
    }
}
