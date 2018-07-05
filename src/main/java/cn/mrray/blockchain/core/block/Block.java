package cn.mrray.blockchain.core.block;

import cn.hutool.crypto.digest.DigestUtil;

/**
 * 区块
 */
public class Block {
    /**
     * 区块头
     */
    private BlockHeader blockHeader = new BlockHeader();
    private String hash;
    /**
     * 区块body
     */
    private BlockBody blockBody;
    private BlockEnd blockEnd;

    /**
     * 根据该区块所有属性计算sha256
     *
     * @return sha256hex
     */
    private String calculateHash() {
        return DigestUtil.sha256Hex(
                blockHeader.toString() + blockBody.toString()
        );
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public BlockHeader getBlockHeader() {
        return blockHeader;
    }

    public void setBlockHeader(BlockHeader blockHeader) {
        this.blockHeader = blockHeader;
    }

    public BlockBody getBlockBody() {
        return blockBody;
    }

    public void setBlockBody(BlockBody blockBody) {
        this.blockBody = blockBody;
    }

    public BlockEnd getBlockEnd() {
        return blockEnd;
    }

    public void setBlockEnd(BlockEnd blockEnd) {
        this.blockEnd = blockEnd;
    }

    @Override
    public String toString() {
        return "Block{" +
                "blockHeader=" + blockHeader +
                ", blockBody=" + blockBody +
                ", blockEnd=" + blockEnd +
                '}';
    }
}
