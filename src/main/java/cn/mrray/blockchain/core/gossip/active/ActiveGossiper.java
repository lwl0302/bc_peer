package cn.mrray.blockchain.core.gossip.active;

import cn.mrray.blockchain.core.gossip.Gossiper;
import cn.mrray.blockchain.core.gossip.member.LocalMember;
import cn.mrray.blockchain.core.gossip.message.ShutdownMessage;
import cn.mrray.blockchain.core.gossip.message.udp.UdpActiveGossipMessage;
import cn.mrray.blockchain.core.gossip.utils.MemberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

public abstract class ActiveGossiper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveGossiper.class);

    protected final Gossiper gossiper;
    private final Random random;

    protected ActiveGossiper(Gossiper gossiper) {
        this.gossiper = gossiper;
        this.random = new Random();
    }

    public void active() {

    }

    public void shutdown() {

    }

    protected void sendMembershipList(LocalMember sender, LocalMember dest) {
        if (dest == null || sender.getId().equals(dest.getId())) {
            return;
        }
        sender.setHeartbeat(System.nanoTime());

        // 发送节点列表
        UdpActiveGossipMessage message = new UdpActiveGossipMessage();
        message.setSender(MemberUtils.toLocalMember(sender));
        message.setUid(MemberUtils.uid());

        // 将 sender 放在第一位
        message.getMembers().add(sender);
        for (LocalMember member : gossiper.getMembers().keySet()) {
            message.getMembers().add(MemberUtils.toMember(member));
        }

        gossiper.sendOneWay(message, dest);
    }


    public final void sendShutdownMessage(LocalMember sender, LocalMember dest) {
        if (dest == null || sender.getId().equals(dest.getId())) {
            return;
        }
        ShutdownMessage message = new ShutdownMessage(sender.getId(), System.nanoTime());
        gossiper.sendOneWay(message, dest);
    }

    protected LocalMember selectPartner(List<LocalMember> memberList) {
        LocalMember member = null;
        if (memberList.size() > 0) {
            int randomNeighborIndex = random.nextInt(memberList.size());
            member = memberList.get(randomNeighborIndex);
        }
        return member;
    }
}
