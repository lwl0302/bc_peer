package cn.mrray.blockchain.core.socket.handler.server;

import cn.mrray.blockchain.core.ApplicationContextProvider;
import cn.mrray.blockchain.core.core.manager.LeaderManager;
import cn.mrray.blockchain.core.socket.base.AbstractBlockHandler;
import cn.mrray.blockchain.core.socket.body.VoteBody;
import cn.mrray.blockchain.core.socket.packet.BlockPacket;
import cn.mrray.blockchain.core.socket.packet.PacketBuilder;
import cn.mrray.blockchain.core.socket.packet.PacketType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Aio;
import org.tio.core.ChannelContext;

public class NewLeaderVoteHandler extends AbstractBlockHandler<VoteBody> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Class<VoteBody> bodyClass() {
        return VoteBody.class;
    }

    @Override
    public Object handler(BlockPacket packet, VoteBody bsBody, ChannelContext channelContext) throws Exception {
        String ip = bsBody.getIp();
        logger.info("收到节点:" + ip + " 的投票请求");
        VoteBody voteCheck = ApplicationContextProvider.getBean(LeaderManager.class).voteCheck(bsBody);
        if (voteCheck != null) {
            logger.info("同意节点:" + ip + " 成为leader");
            BlockPacket blockPacket = new PacketBuilder<>().setType(PacketType.VOTE_LEADER).setBody(voteCheck).build();
            Aio.send(channelContext, blockPacket);
        }
        return null;
    }
}
