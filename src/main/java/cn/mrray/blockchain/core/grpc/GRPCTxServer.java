package cn.mrray.blockchain.core.grpc;

import cn.mrray.blockchain.core.block.AllInfo;
import cn.mrray.blockchain.core.chaincode.service.ChaincodePeerHandleService;
import cn.mrray.blockchain.core.chaincode.service.SdkPeerHandleService;
import cn.mrray.blockchain.core.common.algorithm.SignUtils;
import cn.mrray.blockchain.core.core.vo.NodeVo;
import cn.mrray.blockchain.core.interceptor.InterceptorHandler;
import cn.mrray.blockchain.core.util.AllInfoUtil;
import cn.mrray.blockchain.core.util.PropertiesPo;
import cn.mrray.blockchain.grpc.manager.SdkManagerReply;
import cn.mrray.blockchain.grpc.peer.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.logging.Logger;

@Component
public class GRPCTxServer {
    private static final Logger logger = Logger.getLogger(GRPCTxServer.class.getName());
    @Resource
    private PropertiesPo configProper;
    /* The port on which the server should run */
    private Server server;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private SdkPeerHandleService sdkPeerHandleService;
    @Resource
    private ChaincodePeerHandleService chaincodePeerHandleService;
    @Resource
    private InterceptorHandler interceptorHandler;
    @Value("${ca.ip}")
    private String caIp;
    @Value("${ca.port}")
    private int caPort;
    //@Value("${privateKey}")
    //private String privateKey;
    @Value("${name}")
    private String name;
    @Resource
    private GRPClinet grpClinet;

    public void start() {
        String port = configProper.getValueByKey("port");
        try {
            server = ServerBuilder.forPort(Integer.parseInt(port))
                    .addService(new SdkPeerServiceGrpcService())
                    .addService(new ChaincodePeerServiceGrpcService())
                    .build()
                    .start();
            //GRPClinet grpClinet = GRPClinet.getInstance();
            SdkManagerReply reply = grpClinet.greet(objectMapper.writeValueAsString(new NodeVo().setName(name)
                    .setSign(SignUtils.sign(name))));
            if (reply != null && StringUtils.isNoneBlank(reply.getPayload())) {
                AllInfoUtil.initAllInfo(objectMapper.readValue(reply.getPayload(),
                        AllInfo.class));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                GRPCTxServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }


    /**
     * sdk到peer的grpc服务
     */
    private class SdkPeerServiceGrpcService extends SdkPeerServiceGrpc.SdkPeerServiceImplBase {

        /**
         * 处理用户链码invoke方法
         *
         * @param request
         * @param responseObserver
         */
        @Override
        public void invoke(SdkPeerRequest request, StreamObserver<SdkPeerReply> responseObserver) {
            if (!interceptorHandler.interceptorHandler(request, responseObserver)) {
                return;
            }
            sdkPeerHandleService.invoke(request, responseObserver);
        }

        /**
         * 链码初始化
         *
         * @param request
         * @param responseObserver
         */
        @Override
        public void initChaincode(ChaincodeSpec request, StreamObserver<SdkPeerReply> responseObserver) {
            if (!interceptorHandler.interceptorHandler(request, responseObserver)) {
                return;
            }
            sdkPeerHandleService.initChaincode(request, responseObserver);
        }


        /**
         * 链码停止
         *
         * @param request
         * @param responseObserver
         */
        @Override
        public void stopChaincode(SdkPeerRequest request, StreamObserver<SdkPeerReply> responseObserver) {
            if (!interceptorHandler.interceptorHandler(request, responseObserver)) {
                return;
            }
            sdkPeerHandleService.stopChaincode(request, responseObserver);
        }

        /**
         * 链码删除
         *
         * @param request
         * @param responseObserver
         */
        @Override
        public void removeChaincode(SdkPeerRequest request, StreamObserver<SdkPeerReply> responseObserver) {
            if (!interceptorHandler.interceptorHandler(request, responseObserver)) {
                return;
            }
            sdkPeerHandleService.removeChaincode(request, responseObserver);
        }
    }


    /**
     * 链码到peer的grpc服务
     */
    private class ChaincodePeerServiceGrpcService extends ChaincodePeerServiceGrpc.ChaincodePeerServiceImplBase {
        @Override
        public void getState(ChaincodePeerRequst request, StreamObserver<ChaincodePeerReply> responseObserver) {
            chaincodePeerHandleService.getState(request, responseObserver);
        }

        @Override
        public void selectState(ChaincodePeerRequst request, StreamObserver<ChaincodePeerReply> responseObserver) {
            chaincodePeerHandleService.selectState(request, responseObserver);
        }
    }


}