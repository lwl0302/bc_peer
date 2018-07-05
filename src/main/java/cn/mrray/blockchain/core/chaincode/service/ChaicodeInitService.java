package cn.mrray.blockchain.core.chaincode.service;

import cn.mrray.blockchain.core.chaincode.client.PeerChaincodeClient;
import cn.mrray.blockchain.core.chaincode.po.ChaincodeInfoPo;
import cn.mrray.blockchain.core.chaincode.repository.ChaincodeInfoRepository;
import cn.mrray.blockchain.core.chaincode.vo.BlockChaincodeVo;
import cn.mrray.blockchain.core.core.utils.BlockChainUtil;
import cn.mrray.blockchain.grpc.peer.ChaincodeID;
import cn.mrray.blockchain.grpc.peer.ChaincodeSpec;
import cn.mrray.blockchain.grpc.peer.SdkPeerRequest;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;


@Service
public class ChaicodeInitService {
    private static final Logger logger = LoggerFactory.getLogger(ChaicodeInitService.class);
    private DockerClient dockerClient;
    @Autowired
    private ChaincodeInfoRepository chaincodeInfoRepository;
    private String bashPath = "/opt/chaincode/";

    public void initDockerClient() {
        dockerClient = DefaultDockerClient.builder()
                    .uri(URI.create("unix:///var/run/docker.sock"))
                    .build();
        initNetwork();
    }

    private void initNetwork() {
        try {
            Network network = dockerClient.inspectNetwork(BlockChainUtil.getBlockChain().getNetworkMode());
            if (network == null || StringUtils.isBlank(network.id())) {
                createNetwork();
            }
        } catch (DockerException | InterruptedException e) {
            logger.warn(e.getMessage());
            try {
                createNetwork();
            } catch (DockerException | InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void createNetwork() throws DockerException, InterruptedException {
        dockerClient.createNetwork(NetworkConfig.builder()
                .checkDuplicate(true)
                .attachable(true)
                .driver("bridge")
                .name(BlockChainUtil.getBlockChain().getNetworkMode())
                .build());
    }


    private void addChaincode(ChaincodeInfoPo chaincodeInfoPo) {
        ChaincodeInfoPo chaincodeInfoPoTemp = chaincodeInfoRepository.save(chaincodeInfoPo);
        BlockChainUtil.getBlockChaincodes().add(new BlockChaincodeVo(chaincodeInfoPoTemp, new PeerChaincodeClient(chaincodeInfoPoTemp.getContainerIpAddress(), chaincodeInfoPoTemp.getContainerPort())));
    }

    @Transactional
    public synchronized void init(ChaincodeSpec request) {
        try {
            logger.info(request.getChaincodeId().getName() + "--" + request.getChaincodeId().getVersion());
            File file = new File(StringUtils.join(bashPath, request.getChaincodeId().getName(), "/",
                    request.getChaincodeId().getVersion(), "/", request.getChaincodeId().getPath(), "/", request.getFileName()));
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            IOUtils.write(request.getChaincodeInput(), new FileOutputStream(file), "UTF-8");
            ChaincodeID chaincodeID = request.getChaincodeId();
            addChaincode(createContainer(createImage(new ChaincodeInfoPo()
                    .setNodeId(BlockChainUtil.getBlockChain().getId())
                    .setChaincodeName(chaincodeID.getName())
                    .setChaincodeVersion(chaincodeID.getVersion())
                    .setPath(chaincodeID.getPath()))));
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.info(e.getMessage());
        } catch (DockerException e) {
            e.printStackTrace();
            logger.info(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            logger.info(e.getMessage());
        }
    }


    private synchronized ChaincodeInfoPo createImage(ChaincodeInfoPo chaincodeInfoPo) throws InterruptedException, DockerException, IOException {

        String basepath = "/opt/docker";
        File file = new File(basepath);
        if (!file.exists()) {
            file.mkdirs();
        }
        String[] children = file.list();
        File fileTemp;
        for (int i = 0; i < children.length; i++) {
            fileTemp = new File(children[i]);
            if (fileTemp.exists()) {
                fileTemp.delete();
            }
        }
        FileUtils.copyDirectory(new File(StringUtils.join(bashPath, "/", chaincodeInfoPo.getChaincodeName(), "/", chaincodeInfoPo.getChaincodeVersion())),
                new File(StringUtils.join(basepath, "/chaincode")));
        FileUtils.copyURLToFile(ChaicodeInitService.this.getClass().getResource("/docker/chaincode/Dockerfile"), new File("/opt/docker/Dockerfile"));
        String dockerImageName = StringUtils.join( chaincodeInfoPo.getNodeId(), "-", chaincodeInfoPo.getChaincodeName(), "-", chaincodeInfoPo.getChaincodeVersion(), "-", UUID.randomUUID().toString().replaceAll("-", ""));
        logger.info("chaincode-ImageName--" + dockerImageName);
        return chaincodeInfoPo
                .setImageId(dockerClient.build(Paths.get(basepath), DockerClient.BuildParam.name(dockerImageName)))
                .setImageName(dockerImageName)
                .setImageVersion("latest");
    }

    private String getIpAddress() {
        try {
            String address = InetAddress.getLocalHost().getHostAddress();
            logger.info("-----------------------------------------------------");
            logger.info(address);
            return address;
        } catch (UnknownHostException e) {
            return "";
        }
    }

    private synchronized ChaincodeInfoPo createContainer(ChaincodeInfoPo chaincodeInfoPo) throws DockerException, InterruptedException {
        chaincodeInfoPo.setPeerIpAddress(getIpAddress())
        .setContainerPort(50054);
        final String id = dockerClient.createContainer(ContainerConfig
                .builder()
                .hostConfig(HostConfig
                        .builder()
                        .links(chaincodeInfoPo.getNodeId())
                        .networkMode(BlockChainUtil.getBlockChain().getNetworkMode())
                        .build())
                .image(StringUtils.join(chaincodeInfoPo.getImageName(), ":", chaincodeInfoPo.getImageVersion()))
                .exposedPorts(String.valueOf(chaincodeInfoPo.getContainerPort()))
                .env(StringUtils.join("PEER_ADDRESS=", chaincodeInfoPo.getPeerIpAddress(), ":50051"))
                .build())
                .id();
        logger.info("-----------------------------------------------------");
        dockerClient.startContainer(id);
        final ContainerInfo info = dockerClient.inspectContainer(id);
        dockerClient.renameContainer(id, chaincodeInfoPo.getImageName());

        return chaincodeInfoPo.setContainerId(id)
                .setContainerName(chaincodeInfoPo.getImageName())
                .setContainerHostname(info.config().hostname())
                .setIsRunning(ChaincodeInfoPo.ISRUNNING_TRUE_STATUS)
                .setContainerIpAddress(info.networkSettings().networks().get(BlockChainUtil.getBlockChain().getNetworkMode()).ipAddress());
    }

    public synchronized void stopContainer(SdkPeerRequest request) throws DockerException, InterruptedException {
        ChaincodeInfoPo chaincodeInfoPo;

        for (BlockChaincodeVo blockChaincodeVo: BlockChainUtil.getBlockChaincodes()) {
            chaincodeInfoPo = blockChaincodeVo.getChaincodeInfoPo();
            if (chaincodeInfoPo.getChaincodeName().equals(request.getChaincodeId().getName())
                    && chaincodeInfoPo.getChaincodeVersion().equals(request.getChaincodeId().getVersion())) {
                dockerClient.stopContainer(chaincodeInfoPo.getContainerId(), 0);
                //TODO 还没有修改保存到数据库
                return;
            }
        }

    }

    @Transactional
    public synchronized void removeChaincode(SdkPeerRequest request) throws DockerException, InterruptedException {
        ChaincodeInfoPo chaincodeInfoPo;
        List<BlockChaincodeVo> blockChaincodes = BlockChainUtil.getBlockChaincodes();
        BlockChaincodeVo blockChaincodeVo;
        for (int i = 0, len = blockChaincodes.size(); i < len; i++) {
            blockChaincodeVo = blockChaincodes.get(i);
            chaincodeInfoPo = blockChaincodeVo.getChaincodeInfoPo();
            if (chaincodeInfoPo.getChaincodeName().equals(request.getChaincodeId().getName())
                    && chaincodeInfoPo.getPath().equals(request.getChaincodeId().getPath())
                    && chaincodeInfoPo.getChaincodeVersion().equals(request.getChaincodeId().getVersion())) {

                dockerClient.stopContainer(chaincodeInfoPo.getContainerId(), 0);
                dockerClient.removeContainer(chaincodeInfoPo.getContainerId());
                dockerClient.removeImage(chaincodeInfoPo.getImageId());
                chaincodeInfoRepository.delete(chaincodeInfoPo);
                blockChaincodeVo.getPeerChaincodeClient().shutdown();
                blockChaincodes.remove(i);
                return;
            }
        }
    }

    private String getPath() {
        return ChaicodeInitService.this.getClass().getResource("/docker").getPath();
    }

}
