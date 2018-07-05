package cn.mrray.blockchain.core.gossip.message.handler;

import cn.mrray.blockchain.core.gossip.Gossiper;
import cn.mrray.blockchain.core.gossip.message.Message;

public interface MessageHandler {

    boolean handle(Gossiper gossiper, Message base);
}
