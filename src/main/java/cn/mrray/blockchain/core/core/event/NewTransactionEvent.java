package cn.mrray.blockchain.core.core.event;

import cn.mrray.blockchain.core.block.Block;
import org.springframework.context.ApplicationEvent;

public class NewTransactionEvent extends ApplicationEvent {
    public NewTransactionEvent(Block block) {
        super(block);
    }
}
