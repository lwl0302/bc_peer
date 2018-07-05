package cn.mrray.blockchain.core.socket.handler.client;

import cn.mrray.blockchain.core.ApplicationContextProvider;
import cn.mrray.blockchain.core.core.manager.LeaderManager;
import cn.mrray.blockchain.core.socket.base.AbstractBlockHandler;
import cn.mrray.blockchain.core.socket.body.VoteBody;
import cn.mrray.blockchain.core.socket.packet.BlockPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;

public class VoteLeaderHandler extends AbstractBlockHandler<VoteBody> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Class<VoteBody> bodyClass() {
        return VoteBody.class;
    }

    @Override
    public Object handler(BlockPacket packet, VoteBody bsBody, ChannelContext channelContext) throws Exception {
        logger.info("收到:" + bsBody.getIp() + " 的投票");
        ApplicationContextProvider.getBean(LeaderManager.class).collectVote(bsBody);
        return null;
    }
}
