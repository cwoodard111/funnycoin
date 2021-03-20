package org.funnycoin.transactions;


import java.security.MessageDigest;

public class Transaction {
    String ownerKey;
    String outputKey;
    float amount;
    public String transactionId;

    public Transaction(String ownerKey, String outputKey, float amount) {
        this.outputKey = outputKey;
        this.ownerKey = ownerKey;
        this.amount = amount;
    }

    public String getOwnerKey() {
        return ownerKey;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public float getAmount() {
        return amount;
    }

    private String getHash() {
        return applySha256(ownerKey + outputKey);
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
}
