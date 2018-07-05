package cn.mrray.blockchain.core.chaincode.po;

import javax.persistence.*;
import java.io.Serializable;
@Entity
@Table(name = "t_chaincode_info")
public class ChaincodeInfoPo implements Serializable {
    public static final int ISRUNNING_TRUE_STATUS = 1;
    public static final int ISRUNNING_FALSE_STATUS = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String nodeId;
    private String imageId;
    private String imageName;
    private String imageVersion;
    private String chaincodeName;
    private String chaincodeVersion;
    private String containerId;
    private String containerIpAddress;
    private int containerPort;
    private String containerHostname;
    private String containerName;
    private String path;
    private int isRunning;
    private String userId;
    private String peerIpAddress;

    public long getId() {
        return id;
    }

    public ChaincodeInfoPo setId(long id) {
        this.id = id;
        return this;
    }

    public String getNodeId() {
        return nodeId;
    }

    public ChaincodeInfoPo setNodeId(String nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public String getImageId() {
        return imageId;
    }

    public ChaincodeInfoPo setImageId(String imageId) {
        this.imageId = imageId;
        return this;
    }

    public String getImageName() {
        return imageName;
    }

    public ChaincodeInfoPo setImageName(String imageName) {
        this.imageName = imageName;
        return this;
    }

    public String getImageVersion() {
        return imageVersion;
    }

    public ChaincodeInfoPo setImageVersion(String imageVersion) {
        this.imageVersion = imageVersion;
        return this;
    }

    public String getChaincodeName() {
        return chaincodeName;
    }

    public ChaincodeInfoPo setChaincodeName(String chaincodeName) {
        this.chaincodeName = chaincodeName;
        return this;
    }

    public String getChaincodeVersion() {
        return chaincodeVersion;
    }

    public ChaincodeInfoPo setChaincodeVersion(String chaincodeVersion) {
        this.chaincodeVersion = chaincodeVersion;
        return this;
    }

    public String getContainerId() {
        return containerId;
    }

    public ChaincodeInfoPo setContainerId(String containerId) {
        this.containerId = containerId;
        return this;
    }

    public String getContainerIpAddress() {
        return containerIpAddress;
    }

    public ChaincodeInfoPo setContainerIpAddress(String containerIpAddress) {
        this.containerIpAddress = containerIpAddress;
        return this;
    }

    public int getContainerPort() {
        return containerPort;
    }

    public ChaincodeInfoPo setContainerPort(int containerPort) {
        this.containerPort = containerPort;
        return this;
    }

    public String getContainerHostname() {
        return containerHostname;
    }

    public ChaincodeInfoPo setContainerHostname(String containerHostname) {
        this.containerHostname = containerHostname;
        return this;
    }

    public String getContainerName() {
        return containerName;
    }

    public ChaincodeInfoPo setContainerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    public String getPath() {
        return path;
    }

    public ChaincodeInfoPo setPath(String path) {
        this.path = path;
        return this;
    }

    public int getIsRunning() {
        return isRunning;
    }

    public ChaincodeInfoPo setIsRunning(int isRunning) {
        this.isRunning = isRunning;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public ChaincodeInfoPo setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getPeerIpAddress() {
        return peerIpAddress;
    }

    public ChaincodeInfoPo setPeerIpAddress(String peerIpAddress) {
        this.peerIpAddress = peerIpAddress;
        return this;
    }
}
