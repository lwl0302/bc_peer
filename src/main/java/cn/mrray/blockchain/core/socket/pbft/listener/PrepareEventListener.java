package cn.mrray.blockchain.core.socket.pbft.listener;

import cn.mrray.blockchain.core.common.AppId;
import cn.mrray.blockchain.core.socket.body.PbftVoteBody;
import cn.mrray.blockchain.core.socket.client.PacketSender;
import cn.mrray.blockchain.core.socket.packet.BlockPacket;
import cn.mrray.blockchain.core.socket.packet.PacketBuilder;
import cn.mrray.blockchain.core.socket.packet.PacketType;
import cn.mrray.blockchain.core.socket.pbft.VoteType;
import cn.mrray.blockchain.core.socket.pbft.event.MsgPrepareEvent;
import cn.mrray.blockchain.core.socket.pbft.msg.VoteMsg;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class PrepareEventListener {
    @Resource
    private PacketSender packetSender;

    /**
     * block已经开始进入Prepare状态
     *
     * @param msgPrepareEvent msgIsPrepareEvent
     */
    @EventListener
    public void msgIsPrepare(MsgPrepareEvent msgPrepareEvent) {
        VoteMsg voteMsg = (VoteMsg) msgPrepareEvent.getSource();
        voteMsg.setVoteType(VoteType.PREPARE);
        voteMsg.setAppId(AppId.value);

        //感觉可以去掉block

        //群发消息，通知别的节点，我已对该Block Prepare
        BlockPacket blockPacket = new PacketBuilder<>().setType(PacketType.PBFT_VOTE).setBody(new
                PbftVoteBody(voteMsg)).build();

        //广播给所有人我已Prepare
        packetSender.sendGroup(blockPacket);
    }
}
