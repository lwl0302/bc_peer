package cn.mrray.blockchain.core.socket.handler.server;

import cn.mrray.blockchain.core.ApplicationContextProvider;
import cn.mrray.blockchain.core.core.manager.LeaderManager;
import cn.mrray.blockchain.core.socket.base.AbstractBlockHandler;
import cn.mrray.blockchain.core.socket.body.LeaderHeartBeatBody;
import cn.mrray.blockchain.core.socket.packet.BlockPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;

public class LeaderHeartBeatHandler extends AbstractBlockHandler<LeaderHeartBeatBody> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Class<LeaderHeartBeatBody> bodyClass() {
        return LeaderHeartBeatBody.class;
    }

    @Override
    public Object handler(BlockPacket packet, LeaderHeartBeatBody bsBody, ChannelContext channelContext) throws Exception {
        ApplicationContextProvider.getBean(LeaderManager.class).refresh(bsBody);
        return null;
    }
}
