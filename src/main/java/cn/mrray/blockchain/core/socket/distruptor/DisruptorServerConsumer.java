package cn.mrray.blockchain.core.socket.distruptor;

import cn.mrray.blockchain.core.socket.base.AbstractBlockHandler;
import cn.mrray.blockchain.core.socket.distruptor.base.BaseEvent;
import cn.mrray.blockchain.core.socket.distruptor.base.MessageConsumer;
import cn.mrray.blockchain.core.socket.handler.server.*;
import cn.mrray.blockchain.core.socket.packet.BlockPacket;
import cn.mrray.blockchain.core.socket.packet.PacketType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 所有client发来的消息都在这里处理
 */
@Component
public class DisruptorServerConsumer implements MessageConsumer {

    private static Map<Byte, AbstractBlockHandler<?>> handlerMap = new HashMap<>();

    static {
        handlerMap.put(PacketType.GENERATE_COMPLETE_REQUEST, new GenerateCompleteRequestHandler());
        handlerMap.put(PacketType.GENERATE_BLOCK_REQUEST, new GenerateBlockRequestHandler());
        //handlerMap.put(PacketType.TOTAL_BLOCK_INFO_REQUEST, new TotalBlockInfoRequestHandler());
        handlerMap.put(PacketType.FETCH_BLOCK_INFO_REQUEST, new FetchBlockRequestHandler());
        //handlerMap.put(PacketType.HEART_BEAT, new HeartBeatHandler());
        //handlerMap.put(PacketType.NEXT_BLOCK_INFO_REQUEST, new NextBlockRequestHandler());
        handlerMap.put(PacketType.PBFT_VOTE, new PbftVoteHandler());

        handlerMap.put(PacketType.LEADER_HEART_BEAT, new LeaderHeartBeatHandler());
        handlerMap.put(PacketType.NEW_LEADER_VOTE, new NewLeaderVoteHandler());
        handlerMap.put(PacketType.BECOME_NEW_LEADER, new BecomeNewLeaderHandler());
        handlerMap.put(PacketType.NEW_TRANSACTION, new NewTransactionHandler());
        //handlerMap.put(PacketType.SORT_TRANSACTION, new SortTransactionHandler());
    }

    @Override
    public void receive(BaseEvent baseEvent) throws Exception {
        BlockPacket blockPacket = baseEvent.getBlockPacket();
        Byte type = blockPacket.getType();
        AbstractBlockHandler<?> handler = handlerMap.get(type);
        if (handler == null) {
            return;
        }
        handler.handler(blockPacket, baseEvent.getChannelContext());
    }
}
