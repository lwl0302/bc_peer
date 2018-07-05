package cn.mrray.blockchain.core.chaincode.model;

import java.io.Serializable;
import java.util.List;

public class Transaction implements Serializable {
    private List<ReadModel> reads;
    private List<ModificationModel> writes;
    private String invoke;
    private String chainCode;
    private String channelName;

    public List<ReadModel> getReads() {
        return reads;
    }

    public Transaction setReads(List<ReadModel> reads) {
        this.reads = reads;
        return this;
    }

    public List<ModificationModel> getWrites() {
        return writes;
    }

    public Transaction setWrites(List<ModificationModel> writes) {
        this.writes = writes;
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

    public String getChannelName() {
        return channelName;
    }

    public Transaction setChannelName(String channelName) {
        this.channelName = channelName;
        return this;
    }
}
