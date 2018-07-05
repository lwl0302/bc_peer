package cn.mrray.blockchain.core.init;

import cn.mrray.blockchain.core.chaincode.service.ChaicodeInitService;
import cn.mrray.blockchain.core.chaincode.service.ChaincodeInfoService;
import cn.mrray.blockchain.core.core.utils.BlockChainUtil;
import cn.mrray.blockchain.core.grpc.GRPCTxServer;
import cn.mrray.blockchain.core.socket.client.ClientStarter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.logging.Logger;

/**
 * 启动统一初始化类
 */
@Component
public class StartInit {
    @Autowired
    private ChaicodeInitService chaicodeInitService;
    @Autowired
    private GRPCTxServer grpcTxServer;
    @Autowired
    private ChaincodeInfoService chaincodeInfoService;
    @Autowired
    private ClientStarter clientStarter;
    @Value("${name}")
    private String name;
    private static final Logger logger = Logger.getLogger(ChaicodeInitService.class.getName());

    /**
     * 启动时初始化方法
     */
    @PostConstruct
    public void init() {
        grpcTxServer.start();
        chaincodeInfoService.initChaincodeInfo();
        BlockChainUtil.getBlockChain().setId(name)
                .setAddress(System.getenv().get("PEER_ADDRESS"));
        if (BlockChainUtil.environmentIsDocker()) {
            BlockChainUtil.getBlockChain().setNetworkMode(System.getenv().get("CORE_VM_DOCKER_HOSTCONFIG_NETWORKMODE"));
            chaicodeInitService.initDockerClient();
        }

    }
}
