package cn.mrray.blockchain.core.gossip.protocol;

import cn.mrray.blockchain.core.gossip.message.Message;

import java.io.IOException;

public interface ProtocolManager {

    /**
     * serialize a message
     *
     * @param message
     * @return serialized message.
     * @throws IOException
     */
    byte[] write(Message message) throws IOException;

    /**
     * Reads the next message from a byte source.
     *
     * @param bytes
     * @return a gossip message.
     * @throws IOException
     */
    Message read(byte[] bytes) throws IOException;

}
