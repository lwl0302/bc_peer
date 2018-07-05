package cn.mrray.blockchain.core.gossip.message.udp;


import cn.mrray.blockchain.core.gossip.member.Member;
import cn.mrray.blockchain.core.gossip.message.BlockVersionMessage;

public class UdpBlockVersionMessage extends BlockVersionMessage implements Trackable {

    // 发送者
    private Member sender;

    private String uid;

    @Override
    public Member getSender() {
        return sender;
    }

    @Override
    public void setSender(Member sender) {
        this.sender = sender;
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "UdpBlockVersionMessage{" +
                "sender=" + sender +
                ", uid='" + uid + '\'' +
                '}';
    }
}
