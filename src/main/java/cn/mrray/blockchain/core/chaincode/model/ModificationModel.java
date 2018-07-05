package cn.mrray.blockchain.core.chaincode.model;

import java.io.Serializable;

/**
 * 交易修改模型对象
 */
public class ModificationModel implements Serializable {
    private String key;//唯一标识key
    private String value;//值
    private boolean isDelete;//是否删除

    public String getKey() {
        return key;
    }

    public ModificationModel setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ModificationModel setValue(String value) {
        this.value = value;
        return this;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public ModificationModel setDelete(boolean delete) {
        isDelete = delete;
        return this;
    }
}
