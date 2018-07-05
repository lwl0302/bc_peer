package cn.mrray.blockchain.core.gossip.transport;

import java.io.IOException;
import java.net.URI;

public interface TransportManager {

    void startActiveGossiper();

    void startEndpoint();

    void shutdown();

    void send(URI endpoint, byte[] buf) throws IOException;

    byte[] read() throws IOException;
}
