package cn.mrray.blockchain.core.core.event;

import cn.mrray.blockchain.core.block.VoteBlock;
import org.springframework.context.ApplicationEvent;

/**
 * 确定生成block的Event（添加到rocksDB，执行sqlite语句，发布给其他节点）
 */
public class AddBlockEvent extends ApplicationEvent {
    public AddBlockEvent(VoteBlock block) {
        super(block);
    }
}
