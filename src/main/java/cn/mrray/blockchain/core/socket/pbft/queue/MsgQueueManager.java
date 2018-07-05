package cn.mrray.blockchain.core.socket.pbft.queue;

import cn.mrray.blockchain.core.ApplicationContextProvider;
import cn.mrray.blockchain.core.socket.pbft.VoteType;
import cn.mrray.blockchain.core.socket.pbft.msg.VoteMsg;
import org.springframework.stereotype.Component;

@Component
public class MsgQueueManager {
    private BaseMsgQueue baseMsgQueue;

    public void pushMsg(VoteMsg voteMsg) {
        switch (voteMsg.getVoteType()) {
            case VoteType
                    .PREPREPARE:
                baseMsgQueue = ApplicationContextProvider.getBean(PreMsgQueue.class);
                break;
            case VoteType.PREPARE:
                baseMsgQueue = ApplicationContextProvider.getBean(PrepareMsgQueue.class);
                break;
            case VoteType.COMMIT:
                baseMsgQueue = ApplicationContextProvider.getBean(CommitMsgQueue.class);
                break;
            default:
                break;
        }

        baseMsgQueue.push(voteMsg);
    }
}
