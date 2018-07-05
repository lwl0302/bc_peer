package cn.mrray.blockchain.core.core.manager;

import cn.mrray.blockchain.core.block.Block;
import cn.mrray.blockchain.core.block.VoteBlock;
import cn.mrray.blockchain.core.core.event.AddBlockEvent;
import cn.mrray.blockchain.core.core.event.NewTransactionEvent;
import cn.mrray.blockchain.core.core.model.Transaction;
import cn.mrray.blockchain.core.kafka.ConsumerConnection;
import cn.mrray.blockchain.core.kafka.ProductConnection;
import cn.mrray.blockchain.core.kafka.QueueTimmer;
import cn.mrray.blockchain.core.socket.body.RpcTransactionBody;
import cn.mrray.blockchain.core.socket.body.VoteBlockBody;
import cn.mrray.blockchain.core.socket.client.PacketSender;
import cn.mrray.blockchain.core.socket.packet.BlockPacket;
import cn.mrray.blockchain.core.socket.packet.PacketBuilder;
import cn.mrray.blockchain.core.socket.packet.PacketType;
import cn.mrray.blockchain.core.util.PropertiesPo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AlgorithmManager {
    @Value("${sync-algorithm}")
    private int syncAlgorithm;
    @Value("${countPerBlock}")
    private int countPerBlock;
    @Resource
    private LeaderManager leaderManager;
    @Resource
    private BlockManager blockManager;
    @Resource
    private PacketSender packetSender;
    @Resource
    private PropertiesPo propertiesPo;

    private Logger logger = LoggerFactory.getLogger(getClass());

    //pbft已排序交易缓存
    //private ConcurrentHashMap<String, Transaction> pbftQueue = new ConcurrentHashMap<>();

    //pbft未排序交易缓存
    private ConcurrentHashMap<String, Transaction> pbftTempQueue = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<String> hashQueue = new ConcurrentLinkedQueue<>();

    //区块同步队列
    private ConcurrentLinkedQueue<Block> queue = new ConcurrentLinkedQueue<>();

    public static AtomicInteger term = new AtomicInteger(0);

    //加入交易同步队列
    @EventListener(NewTransactionEvent.class)
    public void addTransaction(NewTransactionEvent newTransactionEvent) {
        //logger.info("get NewTransactionEvent");
        //if (!leaderManager.isLeader()) {
        //logger.info("put NewTransactionEvent");
        //queue.addLast((Block) newTransactionEvent.getSource());
        queue.add((Block) newTransactionEvent.getSource());
        QueueTimmer.setLastAdd(System.currentTimeMillis());
        //logger.warning("size:" + queue.size());
        //}
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initAlgorithm() {
        logger.error("===================ApplicationReadyEvent===================");
        if (syncAlgorithm == 1) {
            startPbft();
        } else if (syncAlgorithm == 2) {
            startRaft();
        }
    }

    @Async
    public void startPbft() {
        Block block;
        while (true) {
            if ((block = queue.poll()) != null) {
                logger.warn("solve block : " + block.getBlockHeader().getNumber());
                blockManager.pbftSync(block);
            }
            if (!QueueTimmer.isPbftWorking() && leaderManager.isLeader()) {
                if (hashQueue.size() >= countPerBlock) {
                    logger.info("pack by count");
                    pack(term.get());
                }
                if ((QueueTimmer.getLastPack() + 5000) < System.currentTimeMillis() && hashQueue.size() > 0) {
                    logger.info("pack by time");
                    pack(term.get());
                }
            }
        }
    }


    @Async
    public void startRaft() {
        logger.info("kafkaConsumer-----------------start");
        ObjectMapper mapper = new ObjectMapper();
        ConsumerConnection consumerConnection = null;
        int timeout = Integer.parseInt(propertiesPo.getValueByKey("timeout"));
        logger.warn("block manager thread started !");
        while (true) {
            if (leaderManager.isLeader()) {
                //logger.info("use leader handler");
                if (consumerConnection == null) {
                    consumerConnection = new ConsumerConnection();
                }
                try {
                    ConsumerRecords<String, String> records = consumerConnection.CONSUMER.poll(timeout);
                    for (ConsumerRecord<String, String> record : records) {
                        String input = record.value();
                        long offset = record.offset();
                        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        Transaction tran = mapper.readValue(input, Transaction.class);
                        tran.setOffset(offset);
                        blockManager.handleTransaction(tran);
                        consumerConnection.CONSUMER.commitSync();
                        logger.info("提交完毕:" + offset);
                        if (!leaderManager.isLeader()) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    consumerConnection.CONSUMER.close();
                    consumerConnection = null;
                }
            } else {
                if (consumerConnection != null) {
                    consumerConnection.CONSUMER.close();
                    consumerConnection = null;
                }
                //logger.info("use follower handler");
                Block block;
                if ((block = queue.poll()) != null) {
                    //logger.info("============================ get block form queue ============================");
                    blockManager.handleTransaction(block);
                }
            }
        }
    }

    private void timeoutThread(int number) {
        CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (term.get() == number && QueueTimmer.isPbftWorking()) {
                QueueTimmer.setPbftWorking(false);
            }
            return null;
        });
    }

    public void addTransaction(Transaction transaction) {
        String hash = transaction.getHash();
        if (!pbftTempQueue.containsKey(hash)) {
            hashQueue.add(hash);
            pbftTempQueue.put(hash, transaction);
            //if (!QueueTimmer.isPbftWorking() && leaderManager.isLeader() && hashQueue.size() >= countPerBlock) {
            //    pack();
            //}
        }
    }

    public void sendTransaction(Transaction transaction) {
        if (syncAlgorithm == 1) {
            String hash = transaction.getHash();
            if (!pbftTempQueue.containsKey(hash)) {
                hashQueue.add(hash);
                pbftTempQueue.put(hash, transaction);
                packetSender.sendGroup(new PacketBuilder<>().setType(PacketType.NEW_TRANSACTION).setBody(new RpcTransactionBody(transaction)).build());
                //if (!QueueTimmer.isPbftWorking() && leaderManager.isLeader() && hashQueue.size() >= countPerBlock) {
                //    QueueTimmer.setLastPack(System.currentTimeMillis());
                //    QueueTimmer.setPbftWorking(true);
                //    pack();
                //    timeoutThread();
                //}
            }
        } else if (syncAlgorithm == 2) {
            try {
                ProducerRecord<String, String> record = new ProducerRecord<>(propertiesPo.getValueByKey("topic"), "tranKey", new ObjectMapper().writeValueAsString(transaction));
                ProductConnection.getInstance().producer.send(record, (metadata, e) -> {
                    if (e != null) {
                        e.printStackTrace();
                    }
                }).get();
            } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private void pack(int number) {
        QueueTimmer.setLastPack(System.currentTimeMillis());
        QueueTimmer.setLastAdd(System.currentTimeMillis());
        QueueTimmer.setPbftWorking(true);
        List<Transaction> transactions = new LinkedList<>();
        int i = 0;
        String hash;
        while ((hash = hashQueue.poll()) != null) {
            transactions.add(pbftTempQueue.get(hash));
            if (i >= countPerBlock - 1) {
                break;
            }
            i++;
        }
        VoteBlock block = blockManager.prePackBlock(transactions);
        if (block != null) {
            BlockPacket blockPacket = new PacketBuilder<>().setType(PacketType.GENERATE_BLOCK_REQUEST).setBody(new VoteBlockBody(block)).build();
            packetSender.sendGroup(blockPacket);
        }
        timeoutThread(number);
    }

    Transaction getByHash(String hash) {
        return pbftTempQueue.get(hash);
    }

    @EventListener(AddBlockEvent.class)
    public void clearQueue(AddBlockEvent addBlockEvent) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        VoteBlock block = (VoteBlock) addBlockEvent.getSource();
        for (String hash : block.getTxHash()) {
            hashQueue.remove(hash);
            pbftTempQueue.remove(hash);
        }
    }

    public int getSyncAlgorithm() {
        return syncAlgorithm;
    }
}
