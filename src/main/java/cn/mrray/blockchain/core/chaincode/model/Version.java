package cn.mrray.blockchain.core.chaincode.model;

import java.io.Serializable;

/**
 * 数据版本对象
 */
public class Version implements Serializable {
    private int blockNum;//区块高度
    private int txNum;//交易高度

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
