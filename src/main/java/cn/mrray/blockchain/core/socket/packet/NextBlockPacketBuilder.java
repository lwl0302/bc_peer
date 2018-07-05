package cn.mrray.blockchain.core.socket.packet;


import cn.mrray.blockchain.core.ApplicationContextProvider;
import cn.mrray.blockchain.core.core.manager.DbBlockManager;
import cn.mrray.blockchain.core.socket.body.RpcSimpleBlockBody;

/**
 * 构建向别的节点请求next block的builder.带着自己最后一个block的hash
 */
public class NextBlockPacketBuilder {
    public static BlockPacket build() {
        return build(null);
    }

    public static BlockPacket build(String responseId) {
        RpcSimpleBlockBody rpcBlockBody = ApplicationContextProvider.getBean(DbBlockManager.class).getLastBlockInfo();
        if (rpcBlockBody != null) {
            rpcBlockBody.setResponseMsgId(responseId);
            BlockPacket blockPacket = new PacketBuilder<>().setType(PacketType.FETCH_BLOCK_INFO_REQUEST).setBody(rpcBlockBody).build();
            //发布client请求事件
            //ApplicationContextProvider.publishEvent(new ClientRequestEvent(blockPacket));
            return blockPacket;
        } else {
            return null;
        }
    }
}
