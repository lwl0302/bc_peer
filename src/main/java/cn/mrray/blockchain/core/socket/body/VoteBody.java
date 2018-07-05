package cn.mrray.blockchain.core.socket.body;

import cn.mrray.blockchain.core.core.model.Version;

public class VoteBody extends BaseBody {
    private String ip;
    private int term;
    private Version version;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }
}
