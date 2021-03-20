package org.funnycoin.blocks;

import org.funnycoin.transactions.Transaction;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Block {
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    private long timeStamp;
    public String hash;
    private String previousHash;
    private int nonce;
    String merkleRoot;

    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        hash = getHash();
    }

    public void mine(int difficulty) {
        merkleRoot = getMerkleRoot(transactions);
        String targetHash = new String(new char[difficulty]).replace('\0', '0');
        while(!hash.substring(0,difficulty).equals(targetHash)) {
            nonce++;
            hash = getHash();
        }
        System.out.println("block successfully mined");
    }

    public String getMerkleRoot(ArrayList<Transaction> transactions) {
        int count = transactions.size();

        List<String> previousTreeLayer = new ArrayList<String>();
        for(Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.transactionId);
        }
        List<String> treeLayer = previousTreeLayer;

        while(count > 1) {
            treeLayer = new ArrayList<String>();
            for(int i=1; i < previousTreeLayer.size(); i+=2) {
                treeLayer.add(applySha256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }

        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
        return merkleRoot;
    }
    public String getHash() {
        return applySha256(previousHash + Long.toString(timeStamp) + Integer.toString(nonce) + merkleRoot);
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String applySha256(String input){

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

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public boolean isFull() {
        if(transactions.size() >= 100) {
            return true;
        } else {
            return false;
        }
    }
}
