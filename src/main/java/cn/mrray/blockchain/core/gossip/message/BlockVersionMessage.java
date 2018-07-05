package cn.mrray.blockchain.core.gossip.message;

public class BlockVersionMessage extends Message implements Comparable<BlockVersionMessage> {

    /**
     * 区块高度
     */
    private int blockHeight;

    /**
     * 交易数量
     */
    private int txNum;

    public BlockVersionMessage() {
    }

    public BlockVersionMessage(int blockHeight, int txNum) {
        this.blockHeight = blockHeight;
        this.txNum = txNum;
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

    @Override
    public String toString() {
        return "BlockVersionMessage{" +
                "blockHeight=" + blockHeight +
                ", txNum=" + txNum +
                '}';
    }

    @Override
    public int compareTo(BlockVersionMessage o) {
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
