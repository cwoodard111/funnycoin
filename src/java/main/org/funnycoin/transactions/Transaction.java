package org.funnycoin.transactions;


public class Transaction {
    String ownerKey;
    String outputKey;
    float amount;

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

}
