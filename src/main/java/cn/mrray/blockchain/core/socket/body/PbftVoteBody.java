package cn.mrray.blockchain.core.socket.body;

import cn.mrray.blockchain.core.socket.pbft.msg.VoteMsg;

public class PbftVoteBody extends BaseBody {
    private VoteMsg voteMsg;

    public PbftVoteBody() {
        super();
    }

    public PbftVoteBody(VoteMsg voteMsg) {
        super();
        this.voteMsg = voteMsg;
    }

    public VoteMsg getVoteMsg() {
        return voteMsg;
    }

    public void setVoteMsg(VoteMsg voteMsg) {
        this.voteMsg = voteMsg;
    }
}
