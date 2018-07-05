package cn.mrray.blockchain.core.gossip.message.handler;

import cn.mrray.blockchain.core.gossip.Gossiper;
import cn.mrray.blockchain.core.gossip.message.Message;

public class TypedMessageHandler implements MessageHandler {

    final private Class<?> messageClass;
    final private MessageHandler messageHandler;

    public TypedMessageHandler(Class<?> messageClass, MessageHandler messageHandler) {
        if (messageClass == null || messageHandler == null) {
            throw new NullPointerException();
        }
        this.messageClass = messageClass;
        this.messageHandler = messageHandler;
    }

    /**
     * @param gossiper context.
     * @param base     message reference.
     * @return true if types match, false otherwise.
     */
    @Override
    public boolean handle(Gossiper gossiper, Message base) {
        if (messageClass.isAssignableFrom(base.getClass())) {
            messageHandler.handle(gossiper, base);
            return true;
        } else {
            return false;
        }
    }
}
