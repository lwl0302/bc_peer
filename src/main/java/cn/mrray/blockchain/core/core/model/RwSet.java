package cn.mrray.blockchain.core.core.model;

import java.io.Serializable;
import java.util.List;

public class RwSet implements Serializable {

    private String chainCode;

    private List<RwSetRead> reads;

    private List<RwSetWrite> writes;

    public String getChainCode() {
        return chainCode;
    }

    public void setChainCode(String chainCode) {
        this.chainCode = chainCode;
    }

    public List<RwSetRead> getReads() {
        return reads;
    }

    public void setReads(List<RwSetRead> reads) {
        this.reads = reads;
    }

    public List<RwSetWrite> getWrites() {
        return writes;
    }

    public void setWrites(List<RwSetWrite> writes) {
        this.writes = writes;
    }
}
