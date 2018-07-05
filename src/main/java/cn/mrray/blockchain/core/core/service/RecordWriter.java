package cn.mrray.blockchain.core.core.service;

import cn.mrray.blockchain.core.block.BlockInfo;
import cn.mrray.blockchain.core.block.TransactionInfo;
import cn.mrray.blockchain.core.core.repository.BlockInfoRepository;
import cn.mrray.blockchain.core.core.repository.TransactionInfoRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class RecordWriter {
    @Resource
    private BlockInfoRepository blockInfoRepository;
    @Resource
    private TransactionInfoRepository transactionInfoRepository;

    @Async
    public void writeTransactionInfo(List<TransactionInfo> transactionInfos) {
        transactionInfoRepository.save(transactionInfos);
    }

    @Async
    public void writeBlockInfo(List<BlockInfo> blockInfos) {
        blockInfoRepository.save(blockInfos);
    }
}
