package cn.mrray.blockchain.core.grpc;

import cn.mrray.blockchain.grpc.manager.SdkManagerReply;
import cn.mrray.blockchain.grpc.manager.SdkManagerRequest;
import cn.mrray.blockchain.grpc.manager.SdkManagerServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class GRPClinet {
    //private static final PropertiesPo configProper = new PropertiesPo();
    private static final Logger logger = Logger.getLogger(GRPClinet.class.getName());
    //private static GRPClinet instance = null;
    private final ManagedChannel channel;
    private final SdkManagerServiceGrpc.SdkManagerServiceBlockingStub blockingStub;

    //public static synchronized GRPClinet getInstance() {
    //    if (instance == null) {
    //        instance = new GRPClinet();
    //    }
    //    return instance;
    //}

    /**
     * Construct client connecting to HelloWorld server at {@code host:port}.
     */
    public GRPClinet(@Value("${ca.ip}") String ip, @Value("${ca.port}") int port) {
        channel = NettyChannelBuilder.forAddress(ip, port).negotiationType(NegotiationType.PLAINTEXT).build();
        blockingStub = SdkManagerServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Say hello to server.
     */
    public SdkManagerReply greet(String payload) {
        logger.info("Will try to greet " + payload + " ...");
        SdkManagerRequest request = SdkManagerRequest.newBuilder().setPayload(payload).build();
        SdkManagerReply response;
        try {
            response = blockingStub.getAllInfo(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return null;
        }
        logger.info("Greeting: " + response.getMessage());
        return response;
    }
}
