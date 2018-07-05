package cn.mrray.blockchain.core.socket.client;

import cn.mrray.blockchain.core.block.Block;
import cn.mrray.blockchain.core.core.event.AddBlockEvent;
import cn.mrray.blockchain.core.socket.body.RpcBlockBody;
import cn.mrray.blockchain.core.socket.packet.BlockPacket;
import cn.mrray.blockchain.core.socket.packet.PacketBuilder;
import cn.mrray.blockchain.core.socket.packet.PacketType;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 本地新生成区块后，需要通知所有group内的节点
 */
@Component
public class BlockGeneratedListener {
    @Resource
    private PacketSender packetSender;

    //@Order(2)
    //@EventListener(AddBlockEvent.class)
    public void blockGenerated(AddBlockEvent addBlockEvent) {
        Block block = (Block) addBlockEvent.getSource();
        BlockPacket blockPacket = new PacketBuilder<>().setType(PacketType.GENERATE_COMPLETE_REQUEST).setBody(new RpcBlockBody(block)).build();
        //广播给其他人做验证
        packetSender.sendGroup(blockPacket);
    }
}
