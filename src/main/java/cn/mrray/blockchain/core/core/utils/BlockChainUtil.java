package cn.mrray.blockchain.core.core.utils;

import cn.mrray.blockchain.core.chaincode.client.PeerChaincodeClient;
import cn.mrray.blockchain.core.chaincode.vo.BlockChaincodeVo;
import cn.mrray.blockchain.core.core.vo.BlockChainVo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author weijun
 * @date 2018/6/11 14:40
 */
public class BlockChainUtil {
    private static final BlockChainVo blockChainVo = new BlockChainVo();
    private static final List<BlockChaincodeVo> blockChaincodeVos = new ArrayList<>();

    private BlockChainUtil() {

    }

    public static BlockChainVo getBlockChain() {
        return blockChainVo;
    }

    public static List<BlockChaincodeVo> getBlockChaincodes() {
        return blockChaincodeVos;
    }

    public static boolean environmentIsDocker() {
        String environment = System.getenv().get("BLOCKCHAIN_ENVIRONMENT");
        if ("DOCKER".equals(environment)) {
            return true;
        }
        return false;
    }


}