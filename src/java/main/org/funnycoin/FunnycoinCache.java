package org.funnycoin;

import org.funnycoin.blocks.Block;

import java.util.ArrayList;
import java.util.List;

public class FunnycoinCache {
    public static List<Block> blockChain = new ArrayList<>();

    public static int getCirculation() {
        return blockChain.size();
    }

    public static int getReward() {
        return 50;
    }

    public static int getInputReward() {
        return blockChain.size() < 10000 ? 50 : 40;
    }
}
