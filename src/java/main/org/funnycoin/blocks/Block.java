package org.funnycoin.blocks;

public class Block {
  List<Transaction> transactions;
  public Block(List<Transaction> transactions) {
    this.transactions = transactions;
  }
}
