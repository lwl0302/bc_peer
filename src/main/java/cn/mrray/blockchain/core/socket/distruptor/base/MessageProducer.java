package cn.mrray.blockchain.core.socket.distruptor.base;

public interface MessageProducer {
    void publish(BaseEvent baseEvent);
}
