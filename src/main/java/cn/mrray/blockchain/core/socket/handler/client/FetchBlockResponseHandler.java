package cn.mrray.blockchain.core.socket.handler.client;

import cn.mrray.blockchain.core.ApplicationContextProvider;
import cn.mrray.blockchain.core.block.Block;
import cn.mrray.blockchain.core.core.event.NewTransactionEvent;
import cn.mrray.blockchain.core.core.manager.AlgorithmManager;
import cn.mrray.blockchain.core.core.manager.BlockManager;
import cn.mrray.blockchain.core.socket.base.AbstractBlockHandler;
import cn.mrray.blockchain.core.socket.body.RpcBlockBody;
import cn.mrray.blockchain.core.socket.client.PacketSender;
import cn.mrray.blockchain.core.socket.packet.BlockPacket;
import cn.mrray.blockchain.core.socket.packet.NextBlockPacketBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;

/**
 * 对方根据我们传的hash，给我们返回的block
 */
public class FetchBlockResponseHandler extends AbstractBlockHandler<RpcBlockBody> {
    private Logger logger = LoggerFactory.getLogger(FetchBlockResponseHandler.class);
    private int last = -1;
    private int count = 0;

    @Override
    public Class<RpcBlockBody> bodyClass() {
        return RpcBlockBody.class;
    }

    @Override
    public Object handler(BlockPacket packet, RpcBlockBody rpcBlockBody, ChannelContext channelContext) {

        logger.info("收到来自于<" + rpcBlockBody.getAppId() + ">的回复");
        Block block = rpcBlockBody.getBlock();
        //如果为null，说明对方也没有该Block
        if (block == null) {
            logger.info("对方也没有该Block");
        } else {
            int number = block.getBlockHeader().getNumber();
            if (last >= number) {
                count++;
                if (count >= 5) {
                    last = -1;
                    count = 0;
                }
                return null;
            }
            if (block.getBlockBody().getInstructions().size() == ApplicationContextProvider.getBean(BlockManager.class).getCountPerBlock()) {
                last = number;
            }
            if (ApplicationContextProvider.getBean(AlgorithmManager.class).getSyncAlgorithm() == 1) {
                last = number;
            }
            //此处校验传过来的block的合法性，如果合法，则更新到本地，作为next区块
            //CheckerManager checkerManager = ApplicationContextProvider.getBean(CheckerManager.class);
            //RpcCheckBlockBody rpcCheckBlockBody = checkerManager.check(block);
            //校验通过，则存入本地DB，保存新区块
            //if (rpcCheckBlockBody.getCode() == 0) {
            //FIXME 跳过校验
            ApplicationContextProvider.publishEvent(new NewTransactionEvent(block));
            //ApplicationContextProvider.getBean(KafkaConsumer.class).addTransaction(block);
            //继续请求下一块
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            BlockPacket blockPacket = NextBlockPacketBuilder.build();
            if (blockPacket != null) {
                ApplicationContextProvider.getBean(PacketSender.class).sendGroup(blockPacket);
            }
        }
        return null;
    }
}
