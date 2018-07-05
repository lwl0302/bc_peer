package cn.mrray.blockchain.core.socket.body;

public class LeaderHeartBeatBody extends BaseBody {
    private int term;
    private String ip;

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public String getIp() {
        return ip;
    }

    public LeaderHeartBeatBody() {
        super();
    }

    public LeaderHeartBeatBody(String ip, int term) {
        super();
        this.ip = ip;
        this.term = term;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
