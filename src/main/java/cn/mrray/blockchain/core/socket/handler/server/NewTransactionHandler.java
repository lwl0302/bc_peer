package cn.mrray.blockchain.core.socket.handler.server;

import cn.mrray.blockchain.core.ApplicationContextProvider;
import cn.mrray.blockchain.core.core.manager.AlgorithmManager;
import cn.mrray.blockchain.core.socket.base.AbstractBlockHandler;
import cn.mrray.blockchain.core.socket.body.RpcTransactionBody;
import cn.mrray.blockchain.core.socket.packet.BlockPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;

public class NewTransactionHandler extends AbstractBlockHandler<RpcTransactionBody> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Class<RpcTransactionBody> bodyClass() {
        return RpcTransactionBody.class;
    }

    @Override
    public Object handler(BlockPacket packet, RpcTransactionBody bsBody, ChannelContext channelContext) throws Exception {
        //logger.info("收到" + bsBody.getAppId() + "交易:" + bsBody.getTransaction().getHash());
        ApplicationContextProvider.getBean(AlgorithmManager.class).addTransaction(bsBody.getTransaction());
        return null;
    }
}
