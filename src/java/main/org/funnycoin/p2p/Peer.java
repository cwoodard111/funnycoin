package org.funnycoin.p2p;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.funnycoin.FunnycoinCache;
import org.funnycoin.blocks.Block;
import org.funnycoin.transactions.Transaction;
import org.funnycoin.wallet.SignageUtils;

import static org.funnycoin.p2p.RequestParams.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Peer {
    public String address;
    public Socket socket;

    public Peer(String address) {
        this.address = address;
    }

    public void connectToPeer() throws Exception {
        if(peerIsOnline()) {
            System.out.println("The peer is online.");
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
            t.start();
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

    private boolean isChainValid(List<Block> blockChain) throws Exception {
        for(int i = 0; i < blockChain.size(); i++) {
            String difficultyTarget = new String(new char[FunnycoinCache.getDifficulty()]).replace('\0','0');
            Block currentBlock = blockChain.get(i);
            if(blockChain.size() >= 1) {
                Block previousBlock = blockChain.get(i - 1);
                /**
                 * Performing checks on the temporary blockchain to see if it's valid.
                 */
                if(!currentBlock.hash.equals(currentBlock.getHash())) {
                    System.out.println("The hash of the block is not equal to the calculated value.");
                    return false;
                }
                if(!previousBlock.hash.equals(currentBlock.getHash())) {
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

    public void startListener() throws Exception {
        System.out.println("starting listener");
            while(true) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String tmp;
                if((tmp = reader.readLine()) != null) {
                    System.out.println(tmp);
                    JsonObject eventObject = JsonParser.parseString(tmp).getAsJsonObject();
                    String event = eventObject.get("event").getAsString();
                    if(event.toLowerCase().contains("blocksAfter")) {
                        int height = eventObject.get("startingHeight").getAsInt();
                        if(requestingBlocks) if(blockHeight == height) {
                            Gson gson = new Gson();
                            List<Block> tempChain = new ArrayList<>(FunnycoinCache.blockChain);
                            JsonArray blockArray = eventObject.getAsJsonArray("blocks");
                            Block[] blocksArray = gson.fromJson(blockArray, Block[].class);
                            List<Block> incomingBlocks = Arrays.asList(blocksArray);
                            for(Block b : incomingBlocks) {
                                tempChain.add(b);
                            }

                            if(isChainValid(tempChain)) {
                                FunnycoinCache.blockChain = tempChain;
                                FunnycoinCache.syncBlockchainFile();
                            } else {
                                System.out.println("Denied blockchain, invalid.");
                            }
                            blockHeight = 0;
                            requestingBlocks = false;
                        }
                    }
                }
            }
        }
    }
