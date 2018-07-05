package cn.mrray.blockchain.core.socket.packet;

/**
 * packetType大于0时是请求类型，小于0时为响应类型
 */
public interface PacketType {
    /**
     * 心跳包
     */
    byte HEART_BEAT = 0;
    /**
     * 已生成新的区块
     */
    byte GENERATE_COMPLETE_REQUEST = 1;
    /**
     * 已生成新的区块回应
     */
    byte GENERATE_COMPLETE_RESPONSE = -1;
    /**
     * 请求生成block
     */
    byte GENERATE_BLOCK_REQUEST = 2;
    /**
     * 同意、拒绝生成
     */
    byte GENERATE_BLOCK_RESPONSE = -2;
    /**
     * 获取所有block信息
     */
    byte TOTAL_BLOCK_INFO_REQUEST = 3;
    /**
     * 我的所有块信息
     */
    byte TOTAL_BLOCK_INFO_RESPONSE = -3;
    /**
     * 获取一个block信息
     */
    byte FETCH_BLOCK_INFO_REQUEST = 4;
    /**
     * 获取一块信息响应
     */
    byte FETCH_BLOCK_INFO_RESPONSE = -4;
    /**
     * 获取下一个区块的信息
     */
    byte NEXT_BLOCK_INFO_REQUEST = 5;
    /**
     * 获取下一个区块的信息
     */
    byte NEXT_BLOCK_INFO_RESPONSE = -5;
    /**
     * pbft投票
     */
    byte PBFT_VOTE = 10;
    /**
     * leader心跳
     */
    byte LEADER_HEART_BEAT = 12;
    /**
     * 发起新leader投票
     */
    byte NEW_LEADER_VOTE = 13;
    /**
     * 投票
     */
    byte VOTE_LEADER = 14;
    /**
     * 成为leader
     */
    byte BECOME_NEW_LEADER = 15;
    /**
     * 新交易广播
     */
    byte NEW_TRANSACTION = 11;
    /**
     * 交易排序后回应
     */
    byte SORT_TRANSACTION = -11;
}
