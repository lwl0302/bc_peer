package cn.mrray.blockchain.core.socket.handler.server;

import cn.mrray.blockchain.core.ApplicationContextProvider;
import cn.mrray.blockchain.core.block.Block;
import cn.mrray.blockchain.core.common.AppId;
import cn.mrray.blockchain.core.core.manager.DbBlockManager;
import cn.mrray.blockchain.core.socket.base.AbstractBlockHandler;
import cn.mrray.blockchain.core.socket.body.RpcBlockBody;
import cn.mrray.blockchain.core.socket.body.RpcSimpleBlockBody;
import cn.mrray.blockchain.core.socket.packet.BlockPacket;
import cn.mrray.blockchain.core.socket.packet.PacketBuilder;
import cn.mrray.blockchain.core.socket.packet.PacketType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Aio;
import org.tio.core.ChannelContext;

/**
 * 请求别人某个区块的信息
 */
public class FetchBlockRequestHandler extends AbstractBlockHandler<RpcSimpleBlockBody> {
    private Logger logger = LoggerFactory.getLogger(FetchBlockRequestHandler.class);

    @Override
    public Class<RpcSimpleBlockBody> bodyClass() {
        return RpcSimpleBlockBody.class;
    }

    @Override
    public Object handler(BlockPacket packet, RpcSimpleBlockBody rpcBlockBody, ChannelContext channelContext) {
        logger.info("收到来自于<" + rpcBlockBody.getAppId() + "><请求该Block>消息，block hash为[" + rpcBlockBody.getHash() + "] 高度为[" + rpcBlockBody.getNumber() + "] 序号为[" + rpcBlockBody.getSort() + "]");
        if (AppId.value.equalsIgnoreCase(rpcBlockBody.getAppId())) {
            return null;
        }
        Block block = ApplicationContextProvider.getBean(DbBlockManager.class).getBlockByBlockInfo(rpcBlockBody);
        if (block != null) {
            BlockPacket blockPacket = new PacketBuilder<>().setType(PacketType.FETCH_BLOCK_INFO_RESPONSE).setBody(new RpcBlockBody(block)).build();
            Aio.send(channelContext, blockPacket);
        }
        return null;
    }
}
