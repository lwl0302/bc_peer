package cn.mrray.blockchain.core.socket.distruptor.base;

public interface MessageConsumer {
    void receive(BaseEvent baseEvent) throws Exception;
}
