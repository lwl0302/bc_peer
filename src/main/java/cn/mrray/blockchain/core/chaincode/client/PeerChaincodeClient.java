package cn.mrray.blockchain.core.chaincode.client;

import cn.mrray.blockchain.core.chaincode.po.ChaincodeInfoPo;
import cn.mrray.blockchain.grpc.peer.PeerChaincodeReply;
import cn.mrray.blockchain.grpc.peer.PeerChaincodeRequst;
import cn.mrray.blockchain.grpc.peer.PeerChaincodeServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;

import java.util.logging.Logger;

public class PeerChaincodeClient {

    private static final Logger logger = Logger.getLogger(PeerChaincodeClient.class.getName());
    //
    private final ManagedChannel channel;
    private final PeerChaincodeServiceGrpc.PeerChaincodeServiceBlockingStub peerChaincodeServiceBlockingStub;


    public PeerChaincodeClient(String host, int port){
        channel = NettyChannelBuilder.forAddress(host, port)
                .negotiationType(NegotiationType.PLAINTEXT)
                .build();

        peerChaincodeServiceBlockingStub = PeerChaincodeServiceGrpc.newBlockingStub(channel);
    }

    public PeerChaincodeReply invoke(PeerChaincodeRequst peerChaincodeRequst) {
        return peerChaincodeServiceBlockingStub.invoke(peerChaincodeRequst);
    }

    public void shutdown() {
        if (channel != null) {
            channel.shutdown();
        }
    }
}
