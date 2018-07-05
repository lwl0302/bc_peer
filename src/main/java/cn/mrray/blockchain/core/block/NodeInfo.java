package cn.mrray.blockchain.core.block;

import javax.persistence.*;

/**
 * 节点信息
 */
@Entity
@Table(name = "t_node_info")
public class NodeInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int nodeId;//节点ID
    private String nodeIp;//节点IP
    private String nodeProt;//节点端口
    private String nodeName;//节点名称
    @OneToOne
    @JoinColumn(name = "id", foreignKey = @ForeignKey(name = "fk_pubprivkey_node"))
    private PubPrivKeyInfo pubPrivKeyInfo;//公私钥信息


    public PubPrivKeyInfo getPubPrivKeyInfo() {
        return pubPrivKeyInfo;
    }

    public void setPubPrivKeyInfo(PubPrivKeyInfo pubPrivKeyInfo) {
        this.pubPrivKeyInfo = pubPrivKeyInfo;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    public String getNodeProt() {
        return nodeProt;
    }

    public void setNodeProt(String nodeProt) {
        this.nodeProt = nodeProt;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
}
