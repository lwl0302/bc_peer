package cn.mrray.blockchain.core.block;

public class BlockEnd {
    /**
     * 交易数
     */
    private int size;
    /**
     * merkle根值
     */
    private String merkleTreeRoot;
    /**
     * 该区块的hash
     */
    //private String hash;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getMerkleTreeRoot() {
        return merkleTreeRoot;
    }

    public void setMerkleTreeRoot(String merkleTreeRoot) {
        this.merkleTreeRoot = merkleTreeRoot;
    }

    //public String getHash() {
    //    return hash;
    //}
    //
    //public void setHash(String hash) {
    //    this.hash = hash;
    //}
}
