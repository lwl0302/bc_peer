package cn.mrray.blockchain.core.core.model;

import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.util.List;

public class Transaction implements Serializable {

    /**
     * 交易的ID
     */
    private long txId = System.currentTimeMillis();

    /**
     * 调用的方法
     */
    private String invoke;
    private String chainCode;
    private List<RwSetWrite> writes;
    private List<RwSetRead> reads;
    private Version version;
    private String channelName;
    private long offset;

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public Version getVersion() {
        return version;
    }

    public Transaction setVersion(Version version) {
        this.version = version;
        return this;
    }

    public long getTxId() {
        return txId;
    }

    public Transaction setTxId(long txId) {
        this.txId = txId;
        return this;
    }

    public String getInvoke() {
        return invoke;
    }

    public Transaction setInvoke(String invoke) {
        this.invoke = invoke;
        return this;
    }

    public String getChainCode() {
        return chainCode;
    }

    public Transaction setChainCode(String chainCode) {
        this.chainCode = chainCode;
        return this;
    }

    public List<RwSetWrite> getWrites() {
        return writes;
    }

    public Transaction setWrites(List<RwSetWrite> writes) {
        this.writes = writes;
        return this;
    }

    public List<RwSetRead> getReads() {
        return reads;
    }

    public Transaction setReads(List<RwSetRead> reads) {
        this.reads = reads;
        return this;
    }

    public String getHash() {
        return DigestUtil.sha256Hex(String.format("%s:%s:%s:%s:%s", txId, invoke, chainCode, JSON.toJSONString(reads), JSON.toJSONString(writes)));
    }

    public String getChannelName() {
        return channelName;
    }

    public Transaction setChannelName(String channelName) {
        this.channelName = channelName;
        return this;
    }
}
