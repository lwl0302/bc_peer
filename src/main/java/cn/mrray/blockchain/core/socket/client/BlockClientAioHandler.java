package cn.mrray.blockchain.core.socket.client;

import cn.mrray.blockchain.core.ApplicationContextProvider;
import cn.mrray.blockchain.core.kafka.QueueTimmer;
import cn.mrray.blockchain.core.socket.base.AbstractAioHandler;
import cn.mrray.blockchain.core.socket.distruptor.base.BaseEvent;
import cn.mrray.blockchain.core.socket.distruptor.base.MessageProducer;
import cn.mrray.blockchain.core.socket.packet.BlockPacket;
import cn.mrray.blockchain.core.socket.packet.NextBlockPacketBuilder;
import org.tio.client.intf.ClientAioHandler;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;

public class BlockClientAioHandler extends AbstractAioHandler implements ClientAioHandler {

    @Override
    public BlockPacket heartbeatPacket() {
        //心跳包的内容就是隔一段时间向别的节点获取一次下一步区块（带着自己的最新Block获取别人的next Block）
        if (QueueTimmer.getLastAdd() + 2000 < System.currentTimeMillis()) {
            return NextBlockPacketBuilder.build();
        } else {
            return null;
        }
        //return null;
    }

    /**
     * server端返回的响应会先进到该方法，将消息全丢到Disruptor中
     */
    @Override
    public void handler(Packet packet, ChannelContext channelContext) {
        BlockPacket blockPacket = (BlockPacket) packet;

        //使用Disruptor来publish消息。所有收到的消息都进入Disruptor，同BlockServerAioHandler
        ApplicationContextProvider.getBean(MessageProducer.class).publish(new BaseEvent(blockPacket, channelContext));
    }
}
