package cn.mrray.blockchain.core.chaincode.service;

import cn.mrray.blockchain.core.block.db.DataBaseService;
import cn.mrray.blockchain.core.util.ChaincodePeerReplyUtil;
import cn.mrray.blockchain.grpc.peer.ChaincodePeerReply;
import cn.mrray.blockchain.grpc.peer.ChaincodePeerRequst;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author weijun
 * @date 2018/6/14 14:45
 */
@Service
public class ChaincodePeerHandleService {
    private static final Logger logger = LoggerFactory.getLogger(ChaincodePeerHandleService.class.getName());

    @Resource
    private DataBaseService dataBaseService;

    public void getState(ChaincodePeerRequst request, StreamObserver<ChaincodePeerReply> responseObserver) {

        try {
            responseObserver.onNext(ChaincodePeerReplyUtil.newSussessReply(null, dataBaseService.get(request.getKey())));
        } catch (Exception e) {
            logger.error(e.getMessage());
            responseObserver.onNext(ChaincodePeerReplyUtil.newErrorReply(e.getMessage()));
        }
        responseObserver.onCompleted();
    }
    public void selectState(ChaincodePeerRequst request, StreamObserver<ChaincodePeerReply> responseObserver) {

        try {
            responseObserver.onNext(ChaincodePeerReplyUtil.newSussessReply(null, dataBaseService.select(request.getKey())));
            logger.info(request.getKey());
            logger.info(dataBaseService.select(request.getKey()));
        } catch (Exception e) {
            logger.error(e.getMessage());
            responseObserver.onNext(ChaincodePeerReplyUtil.newErrorReply(e.getMessage()));
        }
        responseObserver.onCompleted();
    }
}
