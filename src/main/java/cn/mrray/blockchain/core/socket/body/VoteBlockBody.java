package cn.mrray.blockchain.core.socket.body;

import cn.mrray.blockchain.core.block.VoteBlock;

public class VoteBlockBody extends BaseBody {
    private VoteBlock voteBlock;

    public VoteBlockBody(VoteBlock voteBlock) {
        super();
        this.voteBlock = voteBlock;
    }

    public VoteBlock getVoteBlock() {
        return voteBlock;
    }

    public void setVoteBlock(VoteBlock voteBlock) {
        this.voteBlock = voteBlock;
    }
}
