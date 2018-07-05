package cn.mrray.blockchain.core.block;

import java.util.List;

public class VoteBlock {
    private BlockHeader blockHeader;
    private String hash;
    private List<String> txHash;

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    public List<String> getTxHash() {
        return txHash;
    }

    public void setTxHash(List<String> txHash) {
        this.txHash = txHash;
    }

    public BlockHeader getBlockHeader() {
        return blockHeader;
    }

    public void setBlockHeader(BlockHeader blockHeader) {
        this.blockHeader = blockHeader;
    }
}
