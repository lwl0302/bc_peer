package cn.mrray.blockchain.core.socket.handler.server;

import cn.mrray.blockchain.core.ApplicationContextProvider;
import cn.mrray.blockchain.core.block.Block;
import cn.mrray.blockchain.core.common.AppId;
import cn.mrray.blockchain.core.core.event.NewTransactionEvent;
import cn.mrray.blockchain.core.core.model.Transaction;
import cn.mrray.blockchain.core.core.model.Version;
import cn.mrray.blockchain.core.socket.base.AbstractBlockHandler;
import cn.mrray.blockchain.core.socket.body.RpcBlockBody;
import cn.mrray.blockchain.core.socket.packet.BlockPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;

/**
 * 已生成了新区块的全网广播
 */
public class GenerateCompleteRequestHandler extends AbstractBlockHandler<RpcBlockBody> {
    private Logger logger = LoggerFactory.getLogger(GenerateCompleteRequestHandler.class);

    @Override
    public Class<RpcBlockBody> bodyClass() {
        return RpcBlockBody.class;
    }

    @Override
    public Object handler(BlockPacket packet, RpcBlockBody rpcBlockBody, ChannelContext channelContext) {
        Block block = rpcBlockBody.getBlock();
        Transaction transaction = block.getBlockBody().getInstructions().get(0);
        Version version = transaction.getVersion();
        logger.info("收到来自于<" + rpcBlockBody.getAppId() + "><生成了新的transaction>消息, 交易序号为[" + version.getTxNum() + "]");
        if (AppId.value.equalsIgnoreCase(rpcBlockBody.getAppId())) {
            return null;
        }
        ApplicationContextProvider.publishEvent(new NewTransactionEvent(block));
        //ApplicationContextProvider.getBean(KafkaConsumer.class).addTransaction(block);
        //延迟2秒校验一下本地是否有该区块，如果没有，则发请求去获取新Block
        //延迟的目的是可能刚好自己也马上就要生成同样的Block了，就可以省一次请求
        /*CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
                Block block = ApplicationContextProvider.getBean(DbBlockManager.class).getBlockByHash(rpcBlockBody
                        .getHash());
                //本地有了
                if (block == null) {
                    logger.info("开始去获取别人的新区块");
                    //在这里发请求，去获取group别人的新区块
                    BlockPacket nextBlockPacket = NextBlockPacketBuilder.build();
                    ApplicationContextProvider.getBean(PacketSender.class).sendGroup(nextBlockPacket);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        });*/
        return null;
    }
}
