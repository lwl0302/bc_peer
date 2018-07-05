package cn.mrray.blockchain.core.gossip.message;

import cn.mrray.blockchain.core.block.Block;

import java.util.ArrayList;
import java.util.List;

public class BlockSyncMessage extends Message implements Comparable<BlockSyncMessage> {

    /**
     * 区块高度
     */
    private int blockHeight;

    /**
     * 交易数量
     */
    private int txNum;

    /**
     * 差的交易数
     */
    private List<Block> txs = new ArrayList<>();

    public BlockSyncMessage() {
    }

    public BlockSyncMessage(int blockHeight, int txNum, List txs) {
        this.blockHeight = blockHeight;
        this.txNum = txNum;
//        this.txs = txs;
    }

    public int getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(int blockHeight) {
        this.blockHeight = blockHeight;
    }

    public int getTxNum() {
        return txNum;
    }

    public void setTxNum(int txNum) {
        this.txNum = txNum;
    }

    public List<Block> getTxs() {
        return txs;
    }

    public void setTxs(List<Block> txs) {
        this.txs = txs;
    }

    @Override
    public int compareTo(BlockSyncMessage o) {
        int diff = this.blockHeight - o.blockHeight;
        if (diff != 0) {
            return diff;
        }

        diff = this.txNum - o.txNum;
        if (diff != 0) {
            return diff;
        }

        return 0;
    }
}
