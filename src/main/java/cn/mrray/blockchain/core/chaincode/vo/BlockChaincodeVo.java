package cn.mrray.blockchain.core.chaincode.vo;

import cn.mrray.blockchain.core.chaincode.client.PeerChaincodeClient;
import cn.mrray.blockchain.core.chaincode.po.ChaincodeInfoPo;

import java.io.Serializable;

public class BlockChaincodeVo implements Serializable {
    private ChaincodeInfoPo chaincodeInfoPo;
    private PeerChaincodeClient peerChaincodeClient;

    public BlockChaincodeVo() {
        super();
    }

    public BlockChaincodeVo(ChaincodeInfoPo chaincodeInfoPo, PeerChaincodeClient peerChaincodeClient) {
        this.chaincodeInfoPo = chaincodeInfoPo;
        this.peerChaincodeClient = peerChaincodeClient;
    }

    public ChaincodeInfoPo getChaincodeInfoPo() {
        return chaincodeInfoPo;
    }

    public BlockChaincodeVo setChaincodeInfoPo(ChaincodeInfoPo chaincodeInfoPo) {
        this.chaincodeInfoPo = chaincodeInfoPo;
        return this;
    }

    public PeerChaincodeClient getPeerChaincodeClient() {
        return peerChaincodeClient;
    }

    public BlockChaincodeVo setPeerChaincodeClient(PeerChaincodeClient peerChaincodeClient) {
        this.peerChaincodeClient = peerChaincodeClient;
        return this;
    }
}
