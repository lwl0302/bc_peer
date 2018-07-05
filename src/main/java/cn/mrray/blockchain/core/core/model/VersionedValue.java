package cn.mrray.blockchain.core.core.model;

public class VersionedValue {
    private String value;
    private Version version;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }
}
