package cn.mrray.blockchain.core.socket.client;

import cn.mrray.blockchain.core.block.NodeInfo;
import cn.mrray.blockchain.core.common.algorithm.SignUtils;
import cn.mrray.blockchain.core.grpc.GRPClinet;
import cn.mrray.blockchain.core.socket.packet.BlockPacket;
import cn.mrray.blockchain.core.socket.packet.NextBlockPacketBuilder;
import cn.mrray.blockchain.grpc.manager.SdkManagerReply;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tio.client.AioClient;
import org.tio.client.ClientChannelContext;
import org.tio.client.ClientGroupContext;
import org.tio.core.Aio;
import org.tio.core.ChannelContext;
import org.tio.core.Node;
import org.tio.utils.lock.SetWithLock;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import static cn.mrray.blockchain.core.socket.common.Const.GROUP_NAME;

@Component
public class ClientStarter {
    @Resource
    private ClientGroupContext clientGroupContext;
    @Value("${appId}")
    private String appId;
    @Value("${name}")
    private String name;
    @Value("${localIp}")
    private String localIp;
    @Resource
    private PacketSender packetSender;
    @Resource
    private GRPClinet grpClinet;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static Set<Node> nodes = new HashSet<>();

    /**
     * 从区块链管理端获取已登记的各服务器ip
     * 隔5分钟去获取一次
     */
    /*@Scheduled(fixedRate = 300000)
    public void fetchOtherServer() {
        logger.info("我的ip是：" + localIp);
        try {
            //如果连不上服务器，就不让启动
            MemberData memberData = restTemplate.getForEntity(managerUrl + "member?name=" + name + "&appId=" + AppId
                            .value +
                            "&ip=" +
                            localIp,
                    MemberData.class).getBody();
            //合法的客户端
            if (memberData.getCode() == 0) {
                List<Member> memberList = memberData.getMembers();
                logger.info("共有" + memberList.size() + "个成员需要连接：" + memberList.toString());

                nodes.clear();
                for (Member member : memberList) {
                    //if (!member.getIp().equalsIgnoreCase(localIp)) {
                    Node node = new Node(member.getIp(), member.getPort());
                    nodes.add(node);
                }
                //开始尝试绑定到对方开启的server
                bindServerGroup(nodes);
            } else {
                logger.error("不是合法有效的已注册的客户端");
                //System.exit(0);
            }
        } catch (Exception e) {
            logger.error("请先启动md_blockchain_manager服务，并配置appId等属性，只有合法联盟链成员才能启动该服务");
            //System.exit(0);
        }
    }*/
    @Scheduled(fixedRate = 60000)
    public void fetchOtherServer() {
        logger.info("我的ip是：" + localIp);
        //GRPClinet grpClinet = GRPClinet.getInstance();
        try {
            //如果连不上服务器，就不让启动
            //MemberData memberData = restTemplate.getForEntity(managerUrl + "member?name=" + name + "&appId=" + AppId.value + "&ip=" + localIp, MemberData.class).getBody();
            //合法的客户端
            Map<String, Object> map = new HashMap<>();
            map.put("sign", SignUtils.sign(name));
            map.put("name", name);
            String jsonString = JSONObject.toJSONString(map);
            SdkManagerReply greet = grpClinet.greet(jsonString);
            String payload = greet.getPayload();
            if (payload != null && !"".equals(payload)) {
                List<NodeInfo> nodeInfos = ((JSONArray) JSONObject.parseObject(payload, Map.class).get("nodeInfos")).toJavaList(NodeInfo.class);
                //List<NodeInfo> nodeInfos = JSONArray.parseArray(infos, NodeInfo.class);
                //List<Member> memberList = memberData.getMembers();
                logger.info("共有" + nodeInfos.size() + "个成员需要连接：" + nodeInfos.toString());
                nodes.clear();
                for (NodeInfo member : nodeInfos) {
                    Node node = new Node(member.getNodeIp(), Integer.parseInt(member.getNodeProt()));
                    nodes.add(node);
                }
                //开始尝试绑定到对方开启的server
                bindServerGroup(nodes);
            } else {
                logger.error("不是合法有效的已注册的客户端");
                //System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("请先启动md_blockchain_manager服务，并配置appId等属性，只有合法联盟链成员才能启动该服务");
            //System.exit(0);
        }
    }

    /**
     * client在此绑定多个服务器，多个服务器为一个group，将来发消息时发给一个group。
     * 此处连接的server的ip需要和服务器端保持一致，服务器删了，这边也要踢出Group
     */
    private void bindServerGroup(Set<Node> serverNodes) {
        //当前已经连接的
        SetWithLock<ChannelContext> setWithLock = Aio.getAllChannelContexts(clientGroupContext);
        Lock lock2 = setWithLock.getLock().readLock();
        lock2.lock();
        try {
            Set<ChannelContext> set = setWithLock.getObj();
            //已连接的节点集合
            Set<Node> connectedNodes = set.stream().map(ChannelContext::getServerNode).collect(Collectors.toSet());
            //连接新增的，删掉已在管理端不存在的
            for (Node node : serverNodes) {
                if (!connectedNodes.contains(node)) {
                    connect(node);
                }
            }
            //删掉已经不存在
            for (ChannelContext channelContext : set) {
                Node node = channelContext.getServerNode();
                if (!serverNodes.contains(node)) {
                    Aio.remove(channelContext, "主动关闭" + node.getIp());
                }
            }
        } finally {
            lock2.unlock();
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void fetchNextBlock() throws InterruptedException {
        Thread.sleep(10000);
        logger.info("开始群发信息获取next Block");
        //在这里发请求，去获取group别人的新区块
        BlockPacket nextBlockPacket = NextBlockPacketBuilder.build();
        if (nextBlockPacket != null) {
            packetSender.sendGroup(nextBlockPacket);
        }
    }

    public int connectedNumber() {
        SetWithLock<ChannelContext> setWithLock = Aio.getAllChannelContexts(clientGroupContext);
        Lock lock2 = setWithLock.getLock().readLock();
        lock2.lock();
        int connectedNumber;
        try {
            Set<ChannelContext> set = setWithLock.getObj();
            //已连接的节点集合
            Set<Node> connectedNodes = set.stream().map(ChannelContext::getServerNode).collect(Collectors.toSet());
            connectedNumber = connectedNodes.size();
        } finally {
            lock2.unlock();
        }
        return connectedNumber;
    }

    private void connect(Node serverNode) {
        try {
            AioClient aioClient = new AioClient(clientGroupContext);
            logger.info("开始绑定" + ":" + serverNode.toString());
            ClientChannelContext clientChannelContext = aioClient.connect(serverNode);
            if (clientChannelContext == null) {
                logger.info("绑定" + serverNode.toString() + "失败");
                return;
            }
            //绑group是将要连接的各个服务器节点做为一个group
            Aio.bindGroup(clientChannelContext, GROUP_NAME);
        } catch (Exception e) {
            logger.info("异常");
        }
    }

    public int pbftSize() {
        //Group内共有多少个节点
        int total = nodes.size();
        int pbft = (total - 1) / 3;
        if (pbft <= 0) {
            pbft = 1;
        }
        return pbft;
    }

    public int pbftAgreeCount() {
        return pbftSize() * 2 + 1;
        //return 2;
    }
}
