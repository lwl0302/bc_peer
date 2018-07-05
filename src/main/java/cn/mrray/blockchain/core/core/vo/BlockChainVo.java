package cn.mrray.blockchain.core.core.vo;

import java.io.Serializable;

/**
 * @author weijun
 * @date 2018/6/11 14:41
 */
public class BlockChainVo implements Serializable {

    private String id;
    private String address;
    private String networkMode;

    public String getId() {
        return id;
    }

    public BlockChainVo setId(String id) {
        this.id = id;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public BlockChainVo setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getNetworkMode() {
        return networkMode;
    }

    public BlockChainVo setNetworkMode(String networkMode) {
        this.networkMode = networkMode;
        return this;
    }
}
