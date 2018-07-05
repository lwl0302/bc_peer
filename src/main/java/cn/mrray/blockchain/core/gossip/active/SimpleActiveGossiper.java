package cn.mrray.blockchain.core.gossip.active;

import cn.mrray.blockchain.core.ApplicationContextProvider;
import cn.mrray.blockchain.core.core.manager.DbBlockManager;
import cn.mrray.blockchain.core.gossip.GossipProps;
import cn.mrray.blockchain.core.gossip.Gossiper;
import cn.mrray.blockchain.core.gossip.member.LocalMember;
import cn.mrray.blockchain.core.gossip.message.udp.UdpBlockVersionMessage;
import cn.mrray.blockchain.core.gossip.utils.MemberUtils;
import cn.mrray.blockchain.core.socket.body.RpcSimpleBlockBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

public class SimpleActiveGossiper extends ActiveGossiper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleActiveGossiper.class);

    private ScheduledExecutorService scheduler;
    private final BlockingQueue<Runnable> workQueue;
    private ThreadPoolExecutor threadService;
    private final int gossipInterval;

    public SimpleActiveGossiper(Gossiper gossiper) {
        super(gossiper);

        scheduler = Executors.newScheduledThreadPool(3);
        workQueue = new ArrayBlockingQueue<>(1024);
        threadService = new ThreadPoolExecutor(1, 30, 1, TimeUnit.SECONDS, workQueue,
                new ThreadPoolExecutor.DiscardOldestPolicy());

        gossipInterval = GossipProps.GOSSIP_INTERVAL;
    }

    @Override
    public void active() {
        // 第一次调用结束时间 + 间隔时间 = 下次调用时间
        // 定时往live 节点发送成员信息
        scheduler.scheduleWithFixedDelay(() -> threadService.execute(this::sendToALiveMember), 0, gossipInterval, TimeUnit.MILLISECONDS);

        // 定时往dead节点发送成员信息
        scheduler.scheduleWithFixedDelay(() -> threadService.execute(this::sendToDeadMember), 0, gossipInterval, TimeUnit.MILLISECONDS);

        // 定时往live节点同步数据
        scheduler.scheduleWithFixedDelay(() -> threadService.execute(this::sendBlockSyncMessage), 0, gossipInterval, TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.debug("issue during shutdown", e);
        }
        sendShutdownMessage();

        threadService.shutdown();
        try {
            threadService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.debug("issue during shutdown", e);
        }
    }

    protected void sendToALiveMember() {
        LOGGER.debug("random send UdpActiveGossipMessage to alive member");
        LocalMember member = selectPartner(gossiper.getLiveMembers());
        if (member != null) {
            sendMembershipList(gossiper.getSelf(), member);
        }
    }


    protected void sendToDeadMember() {
        // 随机选择一个失活成员发送
        LOGGER.debug("random send UdpActiveGossipMessage to dead member");
        LocalMember member = selectPartner(gossiper.getDeadMembers());
        if (member != null) {
            sendMembershipList(gossiper.getSelf(), member);
        }
    }

    // 发送shutdown 消息
    private void sendShutdownMessage() {
        List<LocalMember> liveMembers = gossiper.getLiveMembers();
        int sendTo = liveMembers.size() < 3 ? 1 : liveMembers.size() / 2;
        for (int i = 0; i < sendTo; i++) {
            LocalMember member = selectPartner(liveMembers);
            if (member != null) {
                threadService.execute(() -> sendShutdownMessage(gossiper.getSelf(), member));
            }
        }
    }

    // 发送区块同步消息
    public void sendBlockSyncMessage() {
        // 随机选择一个
        LocalMember member = selectPartner(gossiper.getLiveMembers());
        if (member != null) {
            DbBlockManager blockManager = ApplicationContextProvider.getBean(DbBlockManager.class);
            RpcSimpleBlockBody blockInfo = blockManager.getLastBlockInfo();
            // get local block height
            int localHeight = blockInfo.getNumber();
            // get local last block txNum
            int localTxNum = blockInfo.getSort();
            UdpBlockVersionMessage versionMessage = new UdpBlockVersionMessage();
            versionMessage.setSender(MemberUtils.toMember(gossiper.getSelf()));
            versionMessage.setUid(MemberUtils.uid());
            versionMessage.setBlockHeight(localHeight);
            versionMessage.setTxNum(localTxNum);
            gossiper.sendOneWay(versionMessage, member);
        }
    }
}
