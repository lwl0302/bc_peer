package cn.mrray.blockchain.core.socket.body;

public class NewLeaderBody extends BaseBody {
    private String ip;
    private int term;

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public NewLeaderBody(String ip, int term) {
        super();
        this.ip = ip;
        this.term = term;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
