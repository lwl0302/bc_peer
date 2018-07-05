package cn.mrray.blockchain.core.gossip.transport;

import cn.mrray.blockchain.core.gossip.Gossiper;
import cn.mrray.blockchain.core.gossip.active.ActiveGossiper;
import cn.mrray.blockchain.core.gossip.active.SimpleActiveGossiper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class AbstractTransportManager implements TransportManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTransportManager.class);

    protected final Gossiper gossiper;
    private final ExecutorService gossipThreadExecutor;
    private final ActiveGossiper activeGossiper;

    public AbstractTransportManager(Gossiper gossiper) {
        this.gossiper = gossiper;
        this.gossipThreadExecutor = Executors.newCachedThreadPool();
        this.activeGossiper = new SimpleActiveGossiper(gossiper);
    }

    @Override
    public void shutdown() {
        gossipThreadExecutor.shutdown();
        if (activeGossiper != null) {
            activeGossiper.shutdown();
        }
        try {
            boolean result = gossipThreadExecutor.awaitTermination(10, TimeUnit.MILLISECONDS);
            if (!result) {
                LOGGER.warn("executor shutdown timed out");
            }
        } catch (InterruptedException e) {
            LOGGER.error("gossipThreadExecutor awaitTermination failed", e);
        }
        gossipThreadExecutor.shutdownNow();
    }

    @Override
    public void startActiveGossiper() {
        activeGossiper.active();
    }
}
