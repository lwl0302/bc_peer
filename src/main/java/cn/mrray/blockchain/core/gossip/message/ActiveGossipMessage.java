package cn.mrray.blockchain.core.gossip.message;

import cn.mrray.blockchain.core.gossip.member.Member;
import com.alibaba.fastjson.annotation.JSONType;

import java.util.ArrayList;
import java.util.List;

@JSONType(typeName = "ActiveGossipMessage")
public class ActiveGossipMessage extends Message {

    private List<Member> members = new ArrayList<>();

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }
}
