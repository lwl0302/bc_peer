package cn.mrray.blockchain.core.gossip.message.handler;

import cn.mrray.blockchain.core.gossip.Gossiper;
import cn.mrray.blockchain.core.gossip.member.LocalMember;
import cn.mrray.blockchain.core.gossip.member.Member;
import cn.mrray.blockchain.core.gossip.message.Message;
import cn.mrray.blockchain.core.gossip.message.udp.UdpActiveGossipMessage;
import cn.mrray.blockchain.core.gossip.message.udp.UdpActiveGossipOkMessage;
import cn.mrray.blockchain.core.gossip.utils.MemberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ActiveGossipMessageHandler implements MessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveGossipMessageHandler.class);

    @Override
    public boolean handle(Gossiper gossiper, Message base) {

        UdpActiveGossipMessage msg = (UdpActiveGossipMessage) base;

        LOGGER.debug("receive message: {}", msg);

        LocalMember self = gossiper.getSelf();

        // 将发送者加入Member和更新状态
        Member sender = msg.getSender();

        if (!self.getCluster().equals(sender.getCluster())) {
            // 不是同一个cluster
            return true;
        }

        // 回复一条消息
        UdpActiveGossipOkMessage okMessage = new UdpActiveGossipOkMessage();
        okMessage.setSender(self);
        okMessage.setUid(MemberUtils.uid());
        gossiper.sendOneWay(okMessage, MemberUtils.toLocalMember(sender));

        // 合并Member
        List<Member> remoteMembers = msg.getMembers();
        gossiper.mergeMembers(sender, remoteMembers);

        return true;
    }
}
