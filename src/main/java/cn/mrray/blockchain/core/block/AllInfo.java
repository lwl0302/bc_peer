package cn.mrray.blockchain.core.block;

import java.util.List;

/**
 * 用户节点信息
 */
public class AllInfo {
    private List<AccountInfo> accountInfos;//用户信息
    private List<NodeInfo> nodeInfos;//节点信息

    public List<AccountInfo> getAccountInfos() {
        return accountInfos;
    }

    public void setAccountInfos(List<AccountInfo> accountInfos) {
        this.accountInfos = accountInfos;
    }

    public List<NodeInfo> getNodeInfos() {
        return nodeInfos;
    }

    public void setNodeInfos(List<NodeInfo> nodeInfos) {
        this.nodeInfos = nodeInfos;
    }
}
