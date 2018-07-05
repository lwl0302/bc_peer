package cn.mrray.blockchain.core.gossip.message.handler;

import cn.mrray.blockchain.core.gossip.GossipState;
import cn.mrray.blockchain.core.gossip.Gossiper;
import cn.mrray.blockchain.core.gossip.member.Member;
import cn.mrray.blockchain.core.gossip.message.Message;
import cn.mrray.blockchain.core.gossip.message.udp.UdpActiveGossipOkMessage;
import cn.mrray.blockchain.core.gossip.utils.MemberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActiveGossipOkMessageHandler implements MessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveGossipOkMessageHandler.class);

    @Override
    public boolean handle(Gossiper gossiper, Message base) {

        // 收到回复了
        UdpActiveGossipOkMessage msg = (UdpActiveGossipOkMessage) base;

        LOGGER.debug("we receive one UdpActiveGossipOk {}", msg);

        Member sender = msg.getSender();

        gossiper.getMembers().put(MemberUtils.toLocalMember(sender), GossipState.UP);

        return true;
    }
}
