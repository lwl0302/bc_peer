package cn.mrray.blockchain.core.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class RwSetWrite implements Serializable {
    @JsonProperty
    private String key;
    @JsonProperty
    private String value;
    @JsonProperty
    private boolean delete;
    @JsonIgnore
    public boolean isDelete() {
        return delete;
    }
    @JsonIgnore
    public void setDelete(boolean delete) {
        this.delete = delete;
    }
    @JsonIgnore
    public String getKey() {
        return key;
    }
    @JsonIgnore
    public void setKey(String key) {
        this.key = key;
    }
    @JsonIgnore
    public String getValue() {
        return value;
    }
    @JsonIgnore
    public void setValue(String value) {
        this.value = value;
    }
}
