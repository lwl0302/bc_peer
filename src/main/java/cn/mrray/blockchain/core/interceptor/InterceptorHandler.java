package cn.mrray.blockchain.core.interceptor;

import cn.mrray.blockchain.grpc.peer.ChaincodeSpec;
import cn.mrray.blockchain.grpc.peer.SdkPeerReply;
import cn.mrray.blockchain.grpc.peer.SdkPeerRequest;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

/**
 * @author weijun
 * @date 2018/6/26 16:45
 */
@Service
public class InterceptorHandler {
    public boolean interceptorHandler(SdkPeerRequest request, StreamObserver<SdkPeerReply> responseObserver) {
        return true;
    }
    public boolean interceptorHandler(ChaincodeSpec request, StreamObserver<SdkPeerReply> responseObserver) {
        return true;
    }
}
