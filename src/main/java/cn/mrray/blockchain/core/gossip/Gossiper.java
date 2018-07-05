package cn.mrray.blockchain.core.gossip;

import cn.mrray.blockchain.core.gossip.member.LocalMember;
import cn.mrray.blockchain.core.gossip.member.Member;
import cn.mrray.blockchain.core.gossip.message.Message;
import cn.mrray.blockchain.core.gossip.message.handler.MessageHandler;
import cn.mrray.blockchain.core.gossip.protocol.JsonProtocolManager;
import cn.mrray.blockchain.core.gossip.protocol.ProtocolManager;
import cn.mrray.blockchain.core.gossip.transport.TransportManager;
import cn.mrray.blockchain.core.gossip.transport.UdpTransportManager;
import cn.mrray.blockchain.core.gossip.utils.MemberUtils;
import cn.mrray.blockchain.core.gossip.utils.SysUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Gossiper {

    private static final Logger LOGGER = LoggerFactory.getLogger(Gossiper.class);

    private final LocalMember self;
    private final ConcurrentSkipListMap<LocalMember, GossipState> members;

    private final MessageHandler messageHandler;
    private final ProtocolManager protocolManager;
    private TransportManager transportManager;
    private final ScheduledExecutorService scheduledServiced;

    // 阈值
    private final double threshold;
    private final double cleanupInterval;
    public static Map gossipConf;

    static {
        Yaml yaml = new Yaml();
        // 加载Gossip 配置
        File conf = new File("./application.yml");
        if (conf.exists()) {
            try (InputStream fis = new FileInputStream(conf)) {
                gossipConf = yaml.loadAs(fis, Map.class);
            } catch (IOException e) {
                LOGGER.error("load gossip config failed", e);
                throw new RuntimeException(e);
            }
        } else {
            try (InputStream is = Gossiper.class.getResourceAsStream("/application.yml")) {
                gossipConf = yaml.loadAs(is, Map.class);
            } catch (IOException e) {
                LOGGER.error("load gossip config failed", e);
                throw new RuntimeException(e);
            }
        }
        Map gossip = (Map) gossipConf.get("gossip");
        gossipConf.clear();
        for (Object key : gossip.keySet()) {
            gossipConf.put(key, gossip.get(key));
        }
    }


    public Gossiper(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
        this.protocolManager = new JsonProtocolManager();
        this.scheduledServiced = Executors.newScheduledThreadPool(2);
        this.threshold = GossipProps.DETECTOR_THRESHOLD;
        this.cleanupInterval = GossipProps.CLEANUP_INTERVAL;

        // 当前node
        String cluster = gossipConf.get("cluster").toString();
        Object seedObj = gossipConf.get("seed");

        if (seedObj == null) {
            RuntimeException exception = new RuntimeException("your must config one seed node.");
            LOGGER.error("new Gossiper failed: {}", exception);
            throw exception;
        }
        Map seedProps = (Map) seedObj;

        Object nodeObj = gossipConf.get("node");

        // 如果配置了使用配置项。
        if (nodeObj != null) {
            Map node = (Map) nodeObj;
            String nodeId = node.get("id").toString();
            String nodeHost = node.get("host").toString();
            int nodePort = Integer.parseInt(node.get("port").toString());
            this.self = new LocalMember(nodeId, nodeHost, nodePort, System.nanoTime(), cluster);
        } else {
            // 如果没有配置自动获取，根据seed 来确定IP。
            String nodeId = SysUtils.uuid();
            LOGGER.info("current node id : {}", nodeId);

            // 使用种子节点的地址获取本节点的地址。
            String seedHost = seedProps.get("host").toString();
            String nodeHost = SysUtils.localIp(seedHost);
            if (StringUtils.isBlank(nodeHost)) {
                RuntimeException exception = new RuntimeException("get host ip failed.");
                LOGGER.error("new Gossiper failed: {}", exception);
                throw exception;
            }

            // 和种子节点使用同样的端口。
            int nodePort = Integer.parseInt(seedProps.get("port").toString());

            LOGGER.info("Gossiper.self =======> {id: {}, host: {}, port: {}}", nodeId, nodeHost, nodePort);

            this.self = new LocalMember(nodeId, nodeHost, nodePort, System.nanoTime(), cluster);
        }


//        this.self = new LocalMember(nodeId, nodeHost, nodePort, System.nanoTime(), cluster);
        this.members = new ConcurrentSkipListMap<>();

        // 种子节点
        String seedId = seedProps.get("id").toString();
        String seedHost = seedProps.get("host").toString();
        int seedPort = Integer.parseInt(seedProps.get("port").toString());
        LocalMember seed = new LocalMember(seedId, seedHost, seedPort, System.nanoTime(), cluster);

        // 剔除自身
        if (!self.getId().equals(seedId)) {
            members.put(seed, GossipState.DOWN);
        }
    }

    // Gossip 启动函数
    public void start() {
        transportManager = new UdpTransportManager(this);

        // 启动一个UDP的EndPoint
        transportManager.startEndpoint();

        // 启动一个UDP的Server
        transportManager.startActiveGossiper();

        // 定时更新 Member 状态
        scheduledServiced.scheduleAtFixedRate(this::refreshMemberState, 0, 100, TimeUnit.MILLISECONDS);


        scheduledServiced.scheduleAtFixedRate(this::printMembers, 0, 10000, TimeUnit.MILLISECONDS);
    }

    // 处理接收到的消息
    public void handleMessage(byte[] buf) {
        try {

            // 通过协议将 消息翻译成 Message
            Message msg = protocolManager.read(buf);

            // 消息处理器
            messageHandler.handle(this, msg);

        } catch (IOException e) {
            LOGGER.error("unable to handle message", e);
            throw new RuntimeException(e);
        }
    }

    public ConcurrentSkipListMap<LocalMember, GossipState> getMembers() {
        return members;
    }

    public List<LocalMember> getLiveMembers() {
        return Collections.unmodifiableList(
                members.entrySet().stream().filter(entry -> entry.getValue() == GossipState.UP).map(Map.Entry::getKey).collect(Collectors.toList())
        );
    }

    public List<LocalMember> getDeadMembers() {
        return Collections.unmodifiableList(
                members.entrySet().stream().filter(entry -> entry.getValue() == GossipState.DOWN).map(Map.Entry::getKey).collect(Collectors.toList())
        );
    }

    public LocalMember getSelf() {
        return self;
    }

    public void sendOneWay(Message message, LocalMember target) {
        sendInternal(message, target.getUri());
    }

    private void sendInternal(Message message, URI uri) {
        byte[] json_bytes;
        try {
            json_bytes = protocolManager.write(message);
        } catch (IOException e) {
            LOGGER.error("protocolManager write message failed", e);
            throw new RuntimeException(e);
        }
        try {
            transportManager.send(uri, json_bytes);
            LOGGER.debug("protocolManager send message success");
        } catch (IOException e) {
            LOGGER.error("protocolManager send message failed", e);
            throw new RuntimeException(e);
        }
    }

    // 合并本地的Member和收到的Member
    public void mergeMembers(Member sender, List<Member> remoteMembers) {

        if (self.getId().equals(sender.getId())) {
            return;
        }

        for (Member remote : remoteMembers) {

            if (self.getId().equals(remote.getId())) {
                continue;
            }

            GossipState beforeState = members.putIfAbsent(MemberUtils.toLocalMember(remote), GossipState.UP);

            if (beforeState != null) {
                for (LocalMember local : members.keySet()) {
                    if (local.getId().equals(remote.getId())) {
                        local.recoredHeartbeat(local.getHeartbeat());
                        local.setHeartbeat(remote.getHeartbeat());
                    }
                }
            }

        }
    }

    /**
     * 刷新节点状态
     * Gossip 节点间的通信是随机的，所以不同通过记录两次心跳差来判断节点的状态，
     * 最好的方式是通过录制某个节点多次心跳值，然后计算一个阈值，（阈值的计算规则还不知道）
     * 我们可以给定一个初始的阈值，计算出来的结果和配置的进行比较，如果计算的大于配置的判定为Dead，
     * 如果计算的小于配置的判定为Live，一般来说阈值偏小会造成误判Dead增加，阈值偏大误判Live增加。
     * 较为合理的阈值是：5 ~ 12
     */
    public void refreshMemberState() {
        LOGGER.debug("refreshing members state.");

        for (Map.Entry<LocalMember, GossipState> entry : members.entrySet()) {
            LocalMember member = entry.getKey();
            Double detect = member.detect(System.nanoTime());
            GossipState state;
            if (detect != null) {
                state = detect > threshold ? GossipState.DOWN : GossipState.UP;
            } else {
                long nowInMillis = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
                long heartbeat = TimeUnit.MILLISECONDS.convert(member.getHeartbeat(), TimeUnit.NANOSECONDS);
                state = (nowInMillis - cleanupInterval > heartbeat) ? GossipState.DOWN : entry.getValue();
            }

            if (entry.getValue() != state) {
                members.put(entry.getKey(), state);

                // 其他操作
            }
        }
    }

    public void printMembers() {
        LOGGER.info("--------------------------Members-------------------------------");
        for (Map.Entry<LocalMember, GossipState> entry : members.entrySet()) {
            LOGGER.info("{} : {}", entry.getKey(), entry.getValue());
        }
    }
}
