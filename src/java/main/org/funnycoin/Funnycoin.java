package org.funnycoin;

import com.codebrig.beam.messages.BeamMessage;
import com.dosse.upnp.UPnP;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.funnycoin.blocks.Block;
import org.funnycoin.p2p.Peer;
import org.funnycoin.transactions.Transaction;

import java.io.*;
import java.security.Security;

import static org.funnycoin.FunnycoinCache.*;
import static org.funnycoin.p2p.RequestParams.*;

public class Funnycoin {
    private int difficulty = 6;


    private void mine() throws IOException {
        Block mine = FunnycoinCache.getNextBlock();
        mine.transactions.add(new Transaction("coinbase", wallet.getBase64Key(wallet.publicKey),50.f,"null"));
        if(mine.mine(getDifficulty())) {
            Gson gson = new Gson();
            String json = gson.toJson(mine);
            peerServer.broadcast(json, "newBlock");
            blockChain.add(mine);
            syncBlockchainFile();
            interrupted = false;
        }
    }

    private void loadConfig() {
        try {
            final File config = new File("config.json");
            final StringBuilder builder = new StringBuilder();
            final BufferedReader reader = new BufferedReader(new FileReader(config));
            String tmp;
            while((tmp = reader.readLine()) != null) builder.append(tmp);
            JsonParser parser = new JsonParser();
            JsonObject obj = (JsonObject) parser.parse(builder.toString());
            int port = obj.get("port").getAsInt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Funnycoin(NodeType type) throws Exception {
        Gson gson = new Gson();
        if(type == NodeType.MINER) {
            System.out.println("selected type: miner");
            // NetworkManager manager = new NetworkManager();
            System.out.println("connecting to other people");
            loadBlockChain();
            /**
             * Later on in the class, i define a method that asks other nodes to send their blockchain starting after a block height of what i currently have.
             */
            if(blockChain.size() == 0) {
                peerServer.init();
                peerLoader.init();
                for(Peer p : peerLoader.peers) {
                    BeamMessage message = new BeamMessage();
                    message.set("event","nodejoin");
                    message.set("address", getIp());
                    message.set("port","45800");
                    System.out.println("sending: " + getIp());
                    p.socket.queueMessage(message);
                }
                System.out.println("Blockchain empty.");
                Block genesis = FunnycoinCache.getCurrentBlock();
                genesis.transactions.add(new Transaction("coinbase",wallet.getBase64Key(wallet.publicKey),50.0f,"null"));
                if(genesis.mine(getDifficulty())) {
                    blockChain.add(genesis);
                    syncBlockchainFile();
                    /**
                     * We have set the local blockchain since we know our own block is valid at genesis. who else would make a fraudulent block when they don't know the chain exists;
                     * we are going to send the block to other people now;
                     */
                    JsonObject object = new JsonObject();
                    object.addProperty("event", "newBlock");
                    object.addProperty("block", gson.toJson(genesis));
                    while (true) {
                        mine();
                    }
                }
            } else {
                peerServer.init();
                peerLoader.init();
                for(Peer p : peerLoader.peers) {
                    BeamMessage message = new BeamMessage();
                    message.set("event","nodejoin");
                    message.set("address", getIp());
                    message.set("port","45800");
                    p.socket.queueMessage(message);
                }

                // getBlocksAfter(getCurrentBlock().height);
                while(true) {
                    mine();
                }
            }
        } else if(type == NodeType.WALLET) {
            FunnycoinCache.loadBlockChain();
            System.out.print(getBalanceFromChain(wallet.getBase64Key(wallet.publicKey)));
            peerLoader.init();
            while(true) {
                Thread.sleep(1000);
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
        if(args.length == 1) {
            if(args[0].toLowerCase().contains("miner")) {
                new Funnycoin(NodeType.MINER);
            } else if(args[0].toLowerCase().contains("wallet")) {
                new Funnycoin(NodeType.WALLET);
            }
        }
    }





    public enum NodeType {
        MINER,
        WALLET
    }
}
