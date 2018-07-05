package cn.mrray.blockchain.core.core.model;

public interface RaftType {
    byte FOLLOWER = 0;
    byte CANDIDATE = 1;
    byte LEADER = 2;
}
