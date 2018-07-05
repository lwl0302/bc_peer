package cn.mrray.blockchain.core.core.manager;

import cn.mrray.blockchain.core.ApplicationContextProvider;
import cn.mrray.blockchain.core.core.model.RaftType;
import cn.mrray.blockchain.core.core.model.Version;
import cn.mrray.blockchain.core.socket.body.LeaderHeartBeatBody;
import cn.mrray.blockchain.core.socket.body.NewLeaderBody;
import cn.mrray.blockchain.core.socket.body.VoteBody;
import cn.mrray.blockchain.core.socket.client.ClientStarter;
import cn.mrray.blockchain.core.socket.client.PacketSender;
import cn.mrray.blockchain.core.socket.packet.BlockPacket;
import cn.mrray.blockchain.core.socket.packet.PacketBuilder;
import cn.mrray.blockchain.core.socket.packet.PacketType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

@Component
public class LeaderManager {
    @Resource
    private BlockManager blockManager;
    @Resource
    private ClientStarter clientStarter;

    @Value("${localIp}")
    private String localIp;

    private long lastHeartBeat = 0;
    private long voteLaunch;
    private Set<String> votes = new HashSet<>();

    private String leaderIp;

    private volatile byte raftType = RaftType.FOLLOWER;

    private int term = 0;
    private int lastVoteTerm = 0;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Scheduled(fixedRate = 120000)
    public void changeLeader() {
        if (isLeader()) {
            logger.info("本节点重置为follower");
            raftType = RaftType.FOLLOWER;
        }
    }

    @Scheduled(fixedRate = 5000)
    public void heartBeat() {
        if (isLeader()) {
            logger.info("发送Leader心跳,任期:" + term);
            BlockPacket packet = new PacketBuilder<>().setType(PacketType.LEADER_HEART_BEAT).setBody(new LeaderHeartBeatBody(localIp, term)).build();
            ApplicationContextProvider.getBean(PacketSender.class).sendGroup(packet);
        } else {
            if (lastHeartBeat + 11000 < System.currentTimeMillis()) {
                raftType = RaftType.CANDIDATE;
                voteLaunch = System.currentTimeMillis();
                votes.clear();
                term++;
                VoteBody voteBody = new VoteBody();
                voteBody.setIp(localIp);
                Version version = new Version();
                version.setBlockNum(blockManager.getHeight());
                version.setTxNum(blockManager.getSort());
                voteBody.setVersion(version);
                voteBody.setTerm(term);
                BlockPacket packet = new PacketBuilder<>().setType(PacketType.NEW_LEADER_VOTE).setBody(voteBody).build();
                ApplicationContextProvider.getBean(PacketSender.class).sendGroup(packet);
                logger.info("心跳超时发起投票");
            } else {
                logger.info("心跳检测正常");
            }
        }
    }

    public synchronized VoteBody voteCheck(VoteBody voteBody) {
        int voteTerm = voteBody.getTerm();
        Version version = voteBody.getVersion();
        if (voteBody.getIp().equalsIgnoreCase(localIp)) {
            return voteBody;
        }
        if (lastVoteTerm != voteTerm && voteTerm > term) {
            raftType = RaftType.FOLLOWER;
            votes.clear();
            term = voteTerm;
            lastVoteTerm = voteTerm;
            if (version.getBlockNum() == blockManager.getHeight()) {
                if (version.getTxNum() < blockManager.getSort()) {
                    return null;
                }
            } else if (version.getBlockNum() < blockManager.getHeight()) {
                return null;
            }
            voteBody.setIp(localIp);
            return voteBody;
        }
        return null;
    }

    public void collectVote(VoteBody voteBody) {
        if (voteLaunch + 2000 < System.currentTimeMillis()) {
            return;
        }
        if (raftType != RaftType.CANDIDATE) {
            return;
        }
        if (voteBody.getTerm() != term) {
            return;
        }
        votes.add(voteBody.getIp());
        if (votes.size() >= clientStarter.connectedNumber() / 2 + 1) {
            raftType = RaftType.LEADER;
            votes.clear();
            BlockPacket packet = new PacketBuilder<>().setType(PacketType.BECOME_NEW_LEADER).setBody(new NewLeaderBody(localIp, term)).build();
            ApplicationContextProvider.getBean(PacketSender.class).sendGroup(packet);
            logger.info(localIp + " 成为leader");
        }
    }

    public void changeLeader(NewLeaderBody newLeaderBody) {
        leaderIp = newLeaderBody.getIp();
        if (leaderIp.equalsIgnoreCase(localIp)) {
            raftType = RaftType.LEADER;
        } else {
            raftType = RaftType.FOLLOWER;
        }
        term = newLeaderBody.getTerm();
        lastHeartBeat = System.currentTimeMillis();
    }

    public void refresh(LeaderHeartBeatBody leaderHeartBeatBody) {
        int leaderTerm = leaderHeartBeatBody.getTerm();
        logger.info("收到:" + leaderHeartBeatBody.getIp() + "的心跳,任期:" + leaderTerm);
        if (term <= leaderTerm) {
            logger.info("刷新心跳时间");
            lastHeartBeat = System.currentTimeMillis();
            leaderIp = leaderHeartBeatBody.getIp();
            term = leaderTerm;
            if (!leaderHeartBeatBody.getIp().equalsIgnoreCase(localIp)) {
                raftType = RaftType.FOLLOWER;
            }
        }
    }

    public boolean isLeader() {
        return raftType == RaftType.LEADER;
    }

    public String getLeaderIp() {
        return leaderIp;
    }

    public void setLeaderIp(String leaderIp) {
        this.leaderIp = leaderIp;
    }
}
