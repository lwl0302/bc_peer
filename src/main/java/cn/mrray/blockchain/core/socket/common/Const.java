package cn.mrray.blockchain.core.socket.common;

public interface Const {
    /**
     * 服务器分组名
     */
    String GROUP_NAME = "block_group";
    /**
     * 监听端口
     */
    int PORT = 6789;

    /**
     * 心跳超时时间
     */
    int TIMEOUT = 50000;

    String CHARSET = "utf-8";
}
