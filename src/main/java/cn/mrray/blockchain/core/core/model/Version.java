package cn.mrray.blockchain.core.core.model;

public class Version {
    private int blockNum;
    private int txNum;

    public int getBlockNum() {
        return blockNum;
    }

    public void setBlockNum(int blockNum) {
        this.blockNum = blockNum;
    }

    public int getTxNum() {
        return txNum;
    }

    public void setTxNum(int txNum) {
        this.txNum = txNum;
    }
}
