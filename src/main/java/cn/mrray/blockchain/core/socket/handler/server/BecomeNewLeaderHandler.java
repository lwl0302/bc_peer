package cn.mrray.blockchain.core.socket.handler.server;

import cn.mrray.blockchain.core.ApplicationContextProvider;
import cn.mrray.blockchain.core.core.manager.LeaderManager;
import cn.mrray.blockchain.core.socket.base.AbstractBlockHandler;
import cn.mrray.blockchain.core.socket.body.NewLeaderBody;
import cn.mrray.blockchain.core.socket.packet.BlockPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;

public class BecomeNewLeaderHandler extends AbstractBlockHandler<NewLeaderBody> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Class<NewLeaderBody> bodyClass() {
        return NewLeaderBody.class;
    }

    @Override
    public Object handler(BlockPacket packet, NewLeaderBody bsBody, ChannelContext channelContext) throws Exception {
        ApplicationContextProvider.getBean(LeaderManager.class).changeLeader(bsBody);
        return null;
    }
}
