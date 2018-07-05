package cn.mrray.blockchain.core.socket.base;

import cn.mrray.blockchain.core.socket.body.BaseBody;
import cn.mrray.blockchain.core.socket.common.Const;
import cn.mrray.blockchain.core.socket.packet.BlockPacket;
import org.tio.core.ChannelContext;
import org.tio.utils.json.Json;

/**
 * 基础handler
 */
public abstract class AbstractBlockHandler<T extends BaseBody> implements HandlerInterface {

    public AbstractBlockHandler() {
    }

    public abstract Class<T> bodyClass();

    @Override
    public Object handler(BlockPacket packet, ChannelContext channelContext) throws Exception {
        String jsonStr;
        T bsBody = null;
        if (packet.getBody() != null) {
            jsonStr = new String(packet.getBody(), Const.CHARSET);
            bsBody = Json.toBean(jsonStr, bodyClass());
        }

        return handler(packet, bsBody, channelContext);
    }

    /**
     * 实际的handler处理
     *
     * @param packet         packet
     * @param bsBody         解析后的对象
     * @param channelContext channelContext
     * @return 用不上
     * @throws Exception Exception
     */
    public abstract Object handler(BlockPacket packet, T bsBody, ChannelContext channelContext) throws Exception;

}
