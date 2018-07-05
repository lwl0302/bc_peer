package cn.mrray.blockchain.core.chaincode.service;

import cn.mrray.blockchain.core.chaincode.client.PeerChaincodeClient;
import cn.mrray.blockchain.core.chaincode.po.ChaincodeInfoPo;
import cn.mrray.blockchain.core.chaincode.repository.ChaincodeInfoRepository;
import cn.mrray.blockchain.core.chaincode.vo.BlockChaincodeVo;
import cn.mrray.blockchain.core.core.utils.BlockChainUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChaincodeInfoService {
    @Autowired
    private ChaincodeInfoRepository chaincodeInfoRepository;


    public void initChaincodeInfo() {
        PeerChaincodeClient peerChaincodeClient;
        BlockChaincodeVo blockChaincodeVo;
        for (ChaincodeInfoPo chaincodeInfoPo: chaincodeInfoRepository.findAll()) {
            peerChaincodeClient = new PeerChaincodeClient(chaincodeInfoPo.getContainerIpAddress(), chaincodeInfoPo.getContainerPort());
            blockChaincodeVo = new BlockChaincodeVo(chaincodeInfoPo, peerChaincodeClient);
            BlockChainUtil.getBlockChaincodes().add(blockChaincodeVo);
        }
    }
}
