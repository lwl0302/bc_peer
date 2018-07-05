package cn.mrray.blockchain.core.socket.pbft.queue;

import cn.mrray.blockchain.core.socket.client.ClientStarter;
import cn.mrray.blockchain.core.socket.pbft.msg.VoteMsg;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 各节点互传的投票消息存储队列基类
 */
@Component
public abstract class BaseMsgQueue {
    @Resource
    private ClientStarter clientStarter;

    public int pbftSize() {
        return clientStarter.pbftSize();
    }

    public int pbftAgreeSize() {
        return clientStarter.pbftAgreeCount();
    }

    /**
     * 来了新消息
     *
     * @param voteMsg voteMsg
     */
    protected abstract void push(VoteMsg voteMsg);
}
