package cn.mrray.blockchain.core.gossip.message.handler;

import cn.mrray.blockchain.core.gossip.GossipState;
import cn.mrray.blockchain.core.gossip.Gossiper;
import cn.mrray.blockchain.core.gossip.member.LocalMember;
import cn.mrray.blockchain.core.gossip.message.Message;
import cn.mrray.blockchain.core.gossip.message.ShutdownMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class ShutdownMessageHandler implements MessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownMessageHandler.class);

    @Override
    public boolean handle(Gossiper gossiper, Message base) {

        ShutdownMessage msg = (ShutdownMessage) base;

        LOGGER.debug("received one ShutdownMessage {}", msg);

        // 找到指定的Member 状态修改为DOWN
        ConcurrentSkipListMap<LocalMember, GossipState> members = gossiper.getMembers();
        for (Map.Entry<LocalMember, GossipState> entry : members.entrySet()) {
            LocalMember member = entry.getKey();
            if (member.getId().equals(msg.getNodeId())) {
                members.put(member, GossipState.DOWN);
                break;
            }
        }

        return true;
    }

}
