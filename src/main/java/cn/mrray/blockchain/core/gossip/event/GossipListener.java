package cn.mrray.blockchain.core.gossip.event;

import cn.mrray.blockchain.core.gossip.GossipState;
import cn.mrray.blockchain.core.gossip.member.Member;

public interface GossipListener {
    void gossipEvent(Member member, GossipState state);
}

