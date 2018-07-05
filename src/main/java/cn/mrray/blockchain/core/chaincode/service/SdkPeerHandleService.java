package cn.mrray.blockchain.core.chaincode.service;

import cn.mrray.blockchain.core.block.AccountInfo;
import cn.mrray.blockchain.core.chaincode.client.PeerChaincodeClient;
import cn.mrray.blockchain.core.chaincode.model.ResposeModel;
import cn.mrray.blockchain.core.chaincode.po.ChaincodeInfoPo;
import cn.mrray.blockchain.core.chaincode.vo.BlockChaincodeVo;
import cn.mrray.blockchain.core.common.algorithm.SignUtils;
import cn.mrray.blockchain.core.core.manager.AlgorithmManager;
import cn.mrray.blockchain.core.core.model.Transaction;
import cn.mrray.blockchain.core.core.utils.BlockChainUtil;
import cn.mrray.blockchain.core.util.AllInfoUtil;
import cn.mrray.blockchain.core.util.SdkPeerReplyUtil;
import cn.mrray.blockchain.grpc.common.Status;
import cn.mrray.blockchain.grpc.peer.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.docker.client.exceptions.DockerException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * sdk 访问peer的处理类
 *
 * @author weijun
 * @date 2018/6/12 19:33
 */
@Service
public class SdkPeerHandleService {

    private static final Logger logger = LoggerFactory.getLogger(SdkPeerHandleService.class);
    private ObjectMapper objectMapper = new ObjectMapper();
    @Resource
    private ChaicodeInitService chaicodeInitService;
    @Resource
    private AlgorithmManager algorithmManager;

    /**
     * 处理用户链码invoke方法
     *
     * @param request
     * @param responseObserver
     */
    public void invoke(SdkPeerRequest request, StreamObserver<SdkPeerReply> responseObserver) {
        //if (!check(request.getUserName(), request.getSign())) {
        //    authenticationReturn(responseObserver);
        //    return;
        //}

        ChaincodeInfoPo chaincodeInfoPo;
        for (BlockChaincodeVo blockChaincodeVo : BlockChainUtil.getBlockChaincodes()) {
            chaincodeInfoPo = blockChaincodeVo.getChaincodeInfoPo();
            if (chaincodeInfoPo.getChaincodeName().equals(request.getChaincodeId().getName())
                    && chaincodeInfoPo.getPath().equals(request.getChaincodeId().getPath())
                    && chaincodeInfoPo.getChaincodeVersion().equals(request.getChaincodeId().getVersion())) {

                if (chaincodeInfoPo.getIsRunning() == ChaincodeInfoPo.ISRUNNING_FALSE_STATUS) {
                    responseObserver.onNext(SdkPeerReplyUtil.newErrorReply("该链码未运行"));
                } else if (chaincodeInfoPo.getIsRunning() == ChaincodeInfoPo.ISRUNNING_TRUE_STATUS) {
                    responseObserver.onNext(handleChaincodeReturnData(request, blockChaincodeVo.getPeerChaincodeClient()));
                }
                responseObserver.onCompleted();
                return;
            }
        }

        responseObserver.onNext(SdkPeerReplyUtil.newErrorReply("该链码不存在"));
        responseObserver.onCompleted();
    }

    /**
     * 认证返回处理方法
     *
     * @param responseObserver
     */
    private void authenticationReturn(StreamObserver<SdkPeerReply> responseObserver) {
        responseObserver.onNext(SdkPeerReplyUtil.newErrorReply("用户认证不通过"));
        responseObserver.onCompleted();
    }

    /**
     * 处理链码返回数据
     *
     * @param request
     * @param peerChaincodeClient
     * @return
     */
    private SdkPeerReply handleChaincodeReturnData(SdkPeerRequest request, PeerChaincodeClient peerChaincodeClient) {
        PeerChaincodeReply peerChaincodeReply = peerChaincodeClient.invoke(PeerChaincodeRequst.newBuilder()
                .setMethod(request.getMethod())
                .setPayload(request.getPayload())
                .build());

        if (peerChaincodeReply.getCode() == Status.INTERNAL_SERVER_ERROR_VALUE) {
            return SdkPeerReplyUtil.newErrorReply(peerChaincodeReply.getMessage(), peerChaincodeReply.getPayload());
        }

        try {
            ResposeModel resposeModel = objectMapper.readValue(peerChaincodeReply.getPayload(), ResposeModel.class);
            Transaction transaction = resposeModel.getTransaction();
            if(transaction != null){
                transaction.setChannelName(request.getChannelName());
                algorithmManager.sendTransaction(transaction);
            }
            return SdkPeerReplyUtil.newSussessReply(peerChaincodeReply.getMessage(), objectMapper.writeValueAsString(resposeModel.getValue()));
        } catch (IOException e) {
            logger.error(e.getMessage());
            return SdkPeerReplyUtil.newErrorReply(e.getMessage());
        }
    }

    /**
     * 检查认证是否通过
     *
     * @param username 用户名
     * @param sign     签名
     * @return
     */
    private boolean check(String username, String sign) {
        List<AccountInfo> accountInfos = AllInfoUtil.getAllInfo().getAccountInfos();
        if (accountInfos != null && !accountInfos.isEmpty()) {
            for (AccountInfo accountInfo : accountInfos) {
                if (accountInfo.getAccountName().equals(username)) {
                    try {
                        return SignUtils.verify(username, sign, accountInfo.getPubPrivKeyInfo().getPublicKey());
                    } catch (Exception e) {
                        logger.info(e.getMessage());
                        return false;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 初始化链码
     *
     * @param request
     * @param responseObserver
     */
    public void initChaincode(ChaincodeSpec request, StreamObserver<SdkPeerReply> responseObserver) {
        if (!check(request.getUserName(), request.getSign())) {
            authenticationReturn(responseObserver);
            return;
        }

        logger.info("init");
        chaicodeInitService.init(request);
        responseObserver.onNext(SdkPeerReplyUtil.newSussessReply());
        responseObserver.onCompleted();
    }

    /**
     * 停止链码
     *
     * @param request
     * @param responseObserver
     */
    public void stopChaincode(SdkPeerRequest request, StreamObserver<SdkPeerReply> responseObserver) {
        if (!check(request.getUserName(), request.getSign())) {
            authenticationReturn(responseObserver);
            return;
        }

        try {
            chaicodeInitService.stopContainer(request);
            responseObserver.onNext(SdkPeerReplyUtil.newSussessReply());
        } catch (DockerException | InterruptedException e) {
            logger.info(e.getMessage());
            responseObserver.onNext(SdkPeerReplyUtil.newErrorReply(e.getMessage()));
        }
        responseObserver.onCompleted();
    }

    /**
     * 删除链码
     *
     * @param request
     * @param responseObserver
     */
    public void removeChaincode(SdkPeerRequest request, StreamObserver<SdkPeerReply> responseObserver) {
        if (!check(request.getUserName(), request.getSign())) {
            authenticationReturn(responseObserver);
            return;
        }

        try {
            chaicodeInitService.removeChaincode(request);
            responseObserver.onNext(SdkPeerReplyUtil.newSussessReply());
        } catch (DockerException | InterruptedException e) {
            logger.info(e.getMessage());
            responseObserver.onNext(SdkPeerReplyUtil.newErrorReply(e.getMessage()));
        }
        responseObserver.onCompleted();
    }

}
