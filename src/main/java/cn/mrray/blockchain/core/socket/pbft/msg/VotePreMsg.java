package cn.mrray.blockchain.core.socket.pbft.msg;


import cn.mrray.blockchain.core.block.VoteBlock;

public class VotePreMsg extends VoteMsg {
    private VoteBlock voteBlock;

    public VoteBlock getVoteBlock() {
        return voteBlock;
    }

    public void setVoteBlock(VoteBlock voteBlock) {
        this.voteBlock = voteBlock;
    }
}
