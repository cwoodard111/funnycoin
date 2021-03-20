package org.funnycoin.wallet;

import com.google.gson.Gson;
import org.funnycoin.FunnycoinCache;
import org.funnycoin.blocks.Block;
import org.funnycoin.transactions.Transaction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Wallet {
    public PrivateKey privateKey;
    public PublicKey publicKey;
    int difficulty = 3;

    public Wallet() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        generateKeyPair();
    }


    public void generateKeyPair() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
        keyGen.initialize(ecSpec,random);
        KeyPair pair = keyGen.generateKeyPair();
        privateKey = pair.getPrivate();
        publicKey = pair.getPublic();
    }


    public void send(String reciever,float amount) throws IOException {
        if(getBalanceFromChain(publicKey) < amount) {
            System.out.println("insufficient funds");
            return;
        }
        Transaction transaction = new Transaction(getBase64Key(publicKey),reciever,amount);
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);
        Block currentBlock = FunnycoinCache.blockChain.get(FunnycoinCache.blockChain.size() - 1);
        if(currentBlock.isFull()) {
            Block block = new Block(currentBlock.getHash());
            block.mine(3);
            FunnycoinCache.blockChain.add(block);
        } else {
            currentBlock.getTransactions().add(transaction);
            Gson gson = new Gson();
            String newJson = gson.toJson(FunnycoinCache.blockChain);
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("blockchain.json")));
            writer.write(newJson);
            writer.close();
        }
    }

        public String getBase64Key(PublicKey key) {
            return Base64.getEncoder().encodeToString(key.getEncoded());
        }


        public void sendGenesis(PublicKey reciever, float amount) throws IOException {

            Transaction transaction = new Transaction(getBase64Key(publicKey),getBase64Key(reciever),amount);
            Block genesisBlock = new Block("first funnycoin block");
            genesisBlock.transactions.add(transaction);
            genesisBlock.mine(3);
            FunnycoinCache.blockChain.add(genesisBlock);
            Gson gson = new Gson();
            String newJson = gson.toJson(FunnycoinCache.blockChain);
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("blockchain.json")));
            writer.write(newJson);
            writer.close();
    }

    public float getBalanceFromChain(PublicKey publicKey) {
        float balance = 0.0f;
        for(Block block : FunnycoinCache.blockChain) {
            for(Transaction transaction : block.getTransactions()) {
                if((transaction.getOutputKey().contains(getBase64Key(publicKey)))) {
                    balance += transaction.getAmount();
                } else if(transaction.getOwnerKey().contains(getBase64Key(publicKey))) {
                    balance -= transaction.getAmount();
                }
            }
        }
        return balance;
    }

}
