package cn.mrray.blockchain.core.gossip;

import cn.mrray.blockchain.core.gossip.message.BlockSyncMessage;
import cn.mrray.blockchain.core.gossip.message.BlockVersionMessage;
import cn.mrray.blockchain.core.gossip.message.handler.*;
import org.springframework.stereotype.Component;

@Component
public class GossipStarter {

    //@PostConstruct
    public void start() {

        MessageHandler messageHandler = MessageHandlerFactory.concurrentHandler(
                MessageHandlerFactory.defaultHandler(),
                new TypedMessageHandler(BlockVersionMessage.class, new BlockVersionMessageHandler()),
                new TypedMessageHandler(BlockSyncMessage.class, new BlockSyncMessageHandler())
        );

        Gossiper gossiper = new Gossiper(messageHandler);
        gossiper.start();
    }

}
