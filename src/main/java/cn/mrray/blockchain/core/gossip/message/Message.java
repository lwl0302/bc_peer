package cn.mrray.blockchain.core.gossip.message;

import cn.mrray.blockchain.core.gossip.message.udp.UdpActiveGossipMessage;
import cn.mrray.blockchain.core.gossip.message.udp.UdpActiveGossipOkMessage;
import com.alibaba.fastjson.annotation.JSONType;

@JSONType(seeAlso = {
        ActiveGossipMessage.class,
        ActiveGossipOkMessage.class,
        ShutdownMessage.class,
        UdpActiveGossipOkMessage.class,
        UdpActiveGossipMessage.class,
})
public abstract class Message {

}
