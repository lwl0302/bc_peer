package cn.mrray.blockchain.core.gossip.message.handler;

import cn.mrray.blockchain.core.ApplicationContextProvider;
import cn.mrray.blockchain.core.block.Block;
import cn.mrray.blockchain.core.core.manager.DbBlockManager;
import cn.mrray.blockchain.core.gossip.Gossiper;
import cn.mrray.blockchain.core.gossip.member.Member;
import cn.mrray.blockchain.core.gossip.message.Message;
import cn.mrray.blockchain.core.gossip.message.udp.UdpBlockSyncMessage;
import cn.mrray.blockchain.core.gossip.message.udp.UdpBlockVersionMessage;
import cn.mrray.blockchain.core.gossip.utils.MemberUtils;
import cn.mrray.blockchain.core.socket.body.RpcSimpleBlockBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockVersionMessageHandler implements MessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockVersionMessageHandler.class);

    @Override
    public boolean handle(Gossiper gossiper, Message base) {

        UdpBlockVersionMessage message = (UdpBlockVersionMessage) base;

        LOGGER.debug("received one UdpBlockVersionMessage {}", message);

        Member sender = message.getSender();
        // 和本地比较
        RpcSimpleBlockBody blockInfo = ApplicationContextProvider.getBean(DbBlockManager.class).getLastBlockInfo();

        // get local block height
        int localHeight = blockInfo.getNumber();
        // get remote block height
        int remoteHeight = message.getBlockHeight();

        // get local last block txNum
        int localTxNum = blockInfo.getSort();
        // get remote last block txNum
        int remoteTxNum = message.getTxNum();

        // 相等的情况不发消息
        if (localHeight == remoteHeight && localTxNum == remoteTxNum) {
            return true;
        }
        Member me = MemberUtils.toMember(gossiper.getSelf());
        RpcSimpleBlockBody body = new RpcSimpleBlockBody();
        body.setNumber(remoteHeight);
        body.setSort(remoteTxNum);
        Block block = ApplicationContextProvider.getBean(DbBlockManager.class).getBlockByBlockInfo(body);
        if (block != null) {
            UdpBlockSyncMessage reply = new UdpBlockSyncMessage();
            reply.setSender(me);
            reply.setUid(message.getUid());
            reply.setBlockHeight(localHeight);
            reply.setTxNum(localTxNum);
            reply.getTxs().add(block);
            gossiper.sendOneWay(reply, MemberUtils.toLocalMember(sender));

            return true;
        }

        UdpBlockVersionMessage reply = new UdpBlockVersionMessage();
        reply.setSender(me);
        reply.setUid(message.getUid());
        reply.setBlockHeight(localHeight);
        reply.setTxNum(localTxNum);

        // 回一条消息
        gossiper.sendOneWay(reply, MemberUtils.toLocalMember(sender));

        return true;
    }

}
