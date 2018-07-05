package cn.mrray.blockchain.core.gossip.utils;

import cn.mrray.blockchain.core.gossip.member.LocalMember;
import cn.mrray.blockchain.core.gossip.member.Member;

import java.util.UUID;

public abstract class MemberUtils {

    public static String uid() {
        return UUID.randomUUID().toString();
    }


    public static Member toMember(LocalMember member) {
        return new Member(member.getId(), member.getHost(), member.getPort(), member.getHeartbeat(), member.getCluster());
    }

    public static LocalMember toLocalMember(Member member) {
        return new LocalMember(member.getId(), member.getHost(), member.getPort(), member.getHeartbeat(), member.getCluster());
    }

}
