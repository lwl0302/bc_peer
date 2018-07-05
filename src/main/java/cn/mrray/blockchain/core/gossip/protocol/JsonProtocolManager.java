package cn.mrray.blockchain.core.gossip.protocol;

import cn.mrray.blockchain.core.gossip.message.Message;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.IOException;
import java.nio.charset.Charset;

public class JsonProtocolManager implements ProtocolManager {

    @Override
    public byte[] write(Message message) throws IOException {
        return JSON.toJSONString(message, SerializerFeature.WriteClassName).getBytes(Charset.forName("UTF-8"));
    }

    @Override
    public Message read(byte[] bytes) throws IOException {
        return JSON.parseObject(bytes, Message.class);
    }
}
