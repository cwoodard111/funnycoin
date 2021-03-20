package org.funnycoin.blocks;

import org.funnycoin.transactions.Transaction;

import java.util.List;

public class Block {
    List<Transaction> transactions;

    public Block(List<Transaction> transactions) {
        this.transactions = transactions;
    }


    public List<Transaction> getTransactions() {
        return transactions;
    }
}
