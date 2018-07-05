package cn.mrray.blockchain.core.block;

/**
 * 区块头
 */
public class BlockHeader {
    private int size;
    /**
     * 上一区块的hash
     */
    private String hashPreviousBlock;
    /**
     * merkle tree根节点hash
     */
    //private String hashMerkleRoot;
    /**
     * 生成该区块的公钥
     */
    //private String publicKey;
    /**
     * 区块的序号
     */
    private int number;
    /**
     * 时间戳
     */
    private long timeStamp;
    /**
     * 32位随机数
     */
    private String nonce;

    /**
     * 该区块里每条交易信息的hash集合，按顺序来的，通过该hash集合能算出根节点hash
     */
    //private List<String> hashList;
    @Override
    public String toString() {
        return "BlockHeader{" +
                "hashPreviousBlock='" + hashPreviousBlock + "'" +
                ",size'" + size + "'" +
                //", hashMerkleRoot='" + hashMerkleRoot + '\'' +
                //", publicKey='" + publicKey + "'" +
                ", number=" + number +
                ", timeStamp=" + timeStamp +
                ", nonce=" + nonce +
                //", hashList=" + hashList +
                '}';
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getHashPreviousBlock() {
        return hashPreviousBlock;
    }

    public void setHashPreviousBlock(String hashPreviousBlock) {
        this.hashPreviousBlock = hashPreviousBlock;
    }


    //public String getPublicKey() {
    //    return publicKey;
    //}
    //
    //public void setPublicKey(String publicKey) {
    //    this.publicKey = publicKey;
    //}

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }


}
