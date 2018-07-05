package cn.mrray.blockchain.core.gossip.message.handler;

import cn.mrray.blockchain.core.ApplicationContextProvider;
import cn.mrray.blockchain.core.block.Block;
import cn.mrray.blockchain.core.core.event.NewTransactionEvent;
import cn.mrray.blockchain.core.core.manager.DbBlockManager;
import cn.mrray.blockchain.core.gossip.Gossiper;
import cn.mrray.blockchain.core.gossip.message.Message;
import cn.mrray.blockchain.core.gossip.message.udp.UdpBlockSyncMessage;
import cn.mrray.blockchain.core.socket.body.RpcSimpleBlockBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockSyncMessageHandler implements MessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockSyncMessageHandler.class);

    @Override
    public boolean handle(Gossiper gossiper, Message base) {

        UdpBlockSyncMessage message = (UdpBlockSyncMessage) base;

        LOGGER.debug("received one UdpBlockSyncMessage {}", message);

        DbBlockManager blockManager = ApplicationContextProvider.getBean(DbBlockManager.class);
        RpcSimpleBlockBody blockInfo = blockManager.getLastBlockInfo();
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

        RpcSimpleBlockBody body = new RpcSimpleBlockBody();
        body.setNumber(remoteHeight);
        body.setSort(remoteTxNum);
        Block block = blockManager.getBlockByBlockInfo(body);
        if (block == null) {
            message.getTxs().forEach(b -> ApplicationContextProvider.publishEvent(new NewTransactionEvent(b)));
        }

        return true;
    }
}
