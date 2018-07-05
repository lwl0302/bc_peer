package cn.mrray.blockchain.core.chaincode.model;

import java.io.Serializable;

/**
 * 读取对象
 */
public class ReadModel implements Serializable {

    private String key;//唯一标识
    private Version version;

    public String getKey() {
        return key;
    }

    public ReadModel setKey(String key) {
        this.key = key;
        return this;
    }

    public Version getVersion() {
        return version;
    }

    public ReadModel setVersion(Version version) {
        this.version = version;
        return this;
    }
}
