package cn.mrray.blockchain.core.gossip.message.udp;

import cn.mrray.blockchain.core.gossip.member.Member;
import cn.mrray.blockchain.core.gossip.message.ActiveGossipOkMessage;
import com.alibaba.fastjson.annotation.JSONType;

@JSONType(typeName = "UdpActiveGossipOk")
public class UdpActiveGossipOkMessage extends ActiveGossipOkMessage implements Trackable {

    private Member sender;

    // uid 为消息的唯一标识可以跟踪
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
}
