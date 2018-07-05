package cn.mrray.blockchain.core.socket.distruptor;

import cn.mrray.blockchain.core.ApplicationContextProvider;
import cn.mrray.blockchain.core.socket.distruptor.base.BaseEvent;
import com.lmax.disruptor.EventHandler;

public class DisruptorServerHandler implements EventHandler<BaseEvent> {
    @Override
    public void onEvent(BaseEvent baseEvent, long sequence, boolean endOfBatch) throws Exception {
        ApplicationContextProvider.getBean(DisruptorServerConsumer.class).receive(baseEvent);
    }
}
