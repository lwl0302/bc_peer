package cn.mrray.blockchain.core.core.service;

import cn.mrray.blockchain.core.block.BlockInfo;
import cn.mrray.blockchain.core.block.TransactionInfo;
import cn.mrray.blockchain.core.core.repository.BlockInfoRepository;
import cn.mrray.blockchain.core.core.repository.TransactionInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class DataAndRecordQueue {
    @Resource
    private NioWriter nioWriter;
    @Resource
    private RecordWriter recordWriter;
    @Resource
    private BlockInfoRepository blockInfoRepository;
    @Resource
    private TransactionInfoRepository transactionInfoRepository;

    private Map<Integer, LinkedList<Object>> blocks = new HashMap<>();
    private List<BlockInfo> blockInfos = new LinkedList<>();
    private volatile List<TransactionInfo> transactionInfos = new LinkedList<>();

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    private void init() {
        blocks.put(0, new LinkedList<>());
    }

    @Scheduled(fixedRate = 10000)
    public void autoFlush() {
        if (transactionInfos.size() > 0) {
            recordWriter.writeTransactionInfo(transactionInfos);
            transactionInfos = new LinkedList<>();
        }
    }

    public void putBlockInfo(BlockInfo blockInfo) {
        blockInfos.add(blockInfo);
        recordWriter.writeBlockInfo(blockInfos);
        blockInfos = new LinkedList<>();
        //blockInfoRepository.save(blockInfo);
    }

    public void putTransactionInfo(TransactionInfo transactionInfo) {
        transactionInfos.add(transactionInfo);
        if (transactionInfos.size() == 50) {
            recordWriter.writeTransactionInfo(transactionInfos);
            transactionInfos = new LinkedList<>();
        }
        //transactionInfoRepository.save(transactionInfo);
    }

    public void putData(Integer height, Object object) {
        LinkedList<Object> objects;
        if (blocks.containsKey(height)) {
            objects = blocks.get(height);
        } else {
            nioWriter.write(height - 1, blocks.get(height - 1));
            blocks.remove(height - 1);

            logger.info("write block : " + (height - 1));

            objects = new LinkedList<>();
            blocks.put(height, objects);
        }
        objects.add(object);
    }

    public List<Object> readBuffer() {
        return blocks.get(blocks.keySet().iterator().next());
    }
}