package cn.mrray.blockchain.core.gossip.message;

import com.alibaba.fastjson.annotation.JSONType;

@JSONType(typeName = "ShutdownMessage")
public class ShutdownMessage extends Message {
    public static final String PER_NODE_KEY = "gossipcore.shutdowmessage";
    private String nodeId;
    private long shutdownAtNanos;

    public ShutdownMessage() {

    }

    public ShutdownMessage(String nodeId, long shutdownAtNanos) {
        this.nodeId = nodeId;
        this.shutdownAtNanos = shutdownAtNanos;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public long getShutdownAtNanos() {
        return shutdownAtNanos;
    }

    public void setShutdownAtNanos(long shutdownAtNanos) {
        this.shutdownAtNanos = shutdownAtNanos;
    }

    @Override
    public String toString() {
        return "ShutdownMessage [shutdownAtNanos=" + shutdownAtNanos + ", nodeId=" + nodeId + "]";
    }
}
