package cn.mrray.blockchain.core.socket.handler.server;

import cn.mrray.blockchain.core.ApplicationContextProvider;
import cn.mrray.blockchain.core.block.VoteBlock;
import cn.mrray.blockchain.core.core.manager.BlockManager;
import cn.mrray.blockchain.core.socket.base.AbstractBlockHandler;
import cn.mrray.blockchain.core.socket.body.VoteBlockBody;
import cn.mrray.blockchain.core.socket.packet.BlockPacket;
import cn.mrray.blockchain.core.socket.pbft.VoteType;
import cn.mrray.blockchain.core.socket.pbft.msg.VotePreMsg;
import cn.mrray.blockchain.core.socket.pbft.queue.MsgQueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;

/**
 * 收到请求生成区块消息，进入PrePre队列
 *
 * @author wuweifeng wrote on 2018/3/12.
 */
public class GenerateBlockRequestHandler extends AbstractBlockHandler<VoteBlockBody> {
    private Logger logger = LoggerFactory.getLogger(GenerateBlockRequestHandler.class);


    @Override
    public Class<VoteBlockBody> bodyClass() {
        return VoteBlockBody.class;
    }

    @Override
    public Object handler(BlockPacket packet, VoteBlockBody voteBlockBody, ChannelContext channelContext) {
        VoteBlock block = voteBlockBody.getVoteBlock();
        String appId = voteBlockBody.getAppId();
        logger.info("收到来自于<" + appId + "><请求生成Block>消息,height " + block.getBlockHeader().getNumber());

        BlockManager blockManager = ApplicationContextProvider.getBean(BlockManager.class);
        //对区块的基本信息进行校验，校验通过后进入pbft的Pre队列
        boolean check = blockManager.checkBlock(block);
        logger.info("校验结果:" + check);
        if (check) {
            VotePreMsg votePreMsg = new VotePreMsg();
            votePreMsg.setVoteBlock(block);
            votePreMsg.setVoteType(VoteType.PREPREPARE);
            votePreMsg.setNumber(block.getBlockHeader().getNumber());
            votePreMsg.setAppId(appId);
            votePreMsg.setHash(block.getHash());
            votePreMsg.setAgree(true);
            //将消息推入PrePrepare队列
            ApplicationContextProvider.getBean(MsgQueueManager.class).pushMsg(votePreMsg);
        }

        return null;
    }
}
