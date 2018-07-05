package cn.mrray.blockchain.core.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class RwSetRead implements Serializable {
    @JsonProperty
    private String key;
    @JsonProperty
    private Version version;
    @JsonIgnore
    public String getKey() {
        return key;
    }
    @JsonIgnore
    public void setKey(String key) {
        this.key = key;
    }
    @JsonIgnore
    public Version getVersion() {
        return version;
    }
    @JsonIgnore
    public void setVersion(Version version) {
        this.version = version;
    }
}
