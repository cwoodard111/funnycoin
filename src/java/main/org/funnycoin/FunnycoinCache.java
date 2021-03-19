package org.funnycoin;

public class FunnycoinCache {
  public static List<Block> blockChain = new ArrayList<Block>();
  
  public static int getCirculation() {
    return blockChain.size();
  }
  
  public static int getReward() {
    return blockChain.size() 
  }
  
  public static int getInputReward() {
    return blockChain.size() < 10000 ? true : false;
  }
