package cn.mrray.blockchain.core.core.manager;

import cn.hutool.crypto.digest.DigestUtil;
import cn.mrray.blockchain.core.ApplicationContextProvider;
import cn.mrray.blockchain.core.block.*;
import cn.mrray.blockchain.core.block.db.DataBaseService;
import cn.mrray.blockchain.core.core.model.*;
import cn.mrray.blockchain.core.core.repository.BlockInfoRepository;
import cn.mrray.blockchain.core.core.repository.TransactionInfoRepository;
import cn.mrray.blockchain.core.core.service.DataAndRecordQueue;
import cn.mrray.blockchain.core.kafka.QueueTimmer;
import cn.mrray.blockchain.core.socket.body.RpcBlockBody;
import cn.mrray.blockchain.core.socket.client.PacketSender;
import cn.mrray.blockchain.core.socket.packet.BlockPacket;
import cn.mrray.blockchain.core.socket.packet.PacketBuilder;
import cn.mrray.blockchain.core.socket.packet.PacketType;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BlockManager {
    @Resource
    private BlockInfoRepository blockInfoRepository;
    @Resource
    private TransactionInfoRepository transactionInfoRepository;
    @Resource
    private DataBaseService dbStore;
    @Resource
    private DataAndRecordQueue dataAndRecordQueue;
    @Resource
    private PacketSender packetSender;
    @Value("${countPerBlock}")
    private int countPerBlock;

    private AlgorithmManager algorithmManager = ApplicationContextProvider.getBean(AlgorithmManager.class);

    private AtomicInteger height = new AtomicInteger(-1);
    private volatile int sort = -1;
    //private long offset = 0;
    private String hashPreviousBlock = "first block";
    private Block block = new Block();
    private BlockHeader blockHeader = block.getBlockHeader();

    private Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * raft leader 处理交易
     *
     * @param transaction 交易
     */
    void handleTransaction(Transaction transaction) {
        initHeightSortOffset();
        //是否新区块
        if (sort == -1) {
            BlockInfo blockInfo = new BlockInfo();
            blockInfo.setHashPreviousBlock(hashPreviousBlock);
            blockInfo.setNumber(height.get());

            blockHeader.setSize(countPerBlock);
            blockHeader.setNumber(height.get());
            blockHeader.setHashPreviousBlock(hashPreviousBlock);
            blockHeader.setTimeStamp(System.currentTimeMillis());
            blockHeader.setNonce(RandomStringUtils.randomNumeric(32));

            String headerString = JSON.toJSONString(blockHeader);

            String hash = DigestUtil.sha256Hex(headerString);
            hashPreviousBlock = hash;
            blockInfo.setHashThisBolck(hash);

            dataAndRecordQueue.putData(height.get(), headerString);
            dataAndRecordQueue.putData(height.get(), hash);
            dataAndRecordQueue.putBlockInfo(blockInfo);
        }

        if (!readsCheck(transaction)) {
            return;
        }
        int sortTemp = sort;
        sort = sortTemp + 1;
        //sort++;
        Version version = new Version();
        version.setBlockNum(height.get());
        version.setTxNum(sort);
        transaction.setVersion(version);
        try {
            writeToDb(version, transaction.getWrites());
        } catch (Exception e) {
            e.printStackTrace();
            sortTemp = sort;
            sort = sortTemp - 1;
            //sort--;
            logger.error("writes 写入异常");
            return;
        }
        dataAndRecordQueue.putData(height.get(), transaction);
        //offset = transaction.getOffset();
        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.setNumber(height.get());
        transactionInfo.setSort(sort);
        transactionInfo.setTransactionHash(transaction.getHash());
        transactionInfo.setTxId(transaction.getTxId());
        dataAndRecordQueue.putTransactionInfo(transactionInfo);
        logger.info("height:" + height.get() + " sort:" + sort);
        if (sort + 1 == countPerBlock) {
            height.incrementAndGet();
            //height++;
            sort = -1;
        }

        BlockBody blockBody = new BlockBody();
        LinkedList<Transaction> Transactions = new LinkedList<>();
        Transactions.add(transaction);
        blockBody.setInstructions(Transactions);
        block.setBlockBody(blockBody);
        //ApplicationContextProvider.publishEvent(new AddBlockEvent(block));
        BlockPacket blockPacket = new PacketBuilder<>().setType(PacketType.GENERATE_COMPLETE_REQUEST).setBody(new RpcBlockBody(block)).build();
        //广播给其他人做验证
        packetSender.sendGroup(blockPacket);
    }

    private boolean readsCheck(Transaction transaction) {
        if (transaction == null) {
            return false;
        }
        List<RwSetRead> reads = transaction.getReads();
        //reads验证
        if (reads != null) {
            for (RwSetRead read : reads) {
                String key = read.getKey();
                String value = dbStore.get(key);
                if (value == null) {
                    //manager 抛弃交易
                    logger.error("reads 验证失败");
                    return false;
                }
                VersionedValue versionedValue = JSON.parseObject(value, VersionedValue.class);
                Version remoteVersion = read.getVersion();
                Version localVersion = versionedValue.getVersion();
                if (remoteVersion.getBlockNum() != localVersion.getBlockNum() || remoteVersion.getTxNum() != localVersion.getTxNum()) {
                    //manager 抛弃交易
                    logger.error("reads 验证失败");
                    return false;
                }
            }
        }
        return true;
    }

    void pbftSync(Block block) {
        initHeightSortOffset();
        if (block.getBlockHeader().getNumber() != height.get()) {
            logger.error("return  handleTransaction  getNumber " + block.getBlockHeader().getNumber() + " height" + height.get());
            //System.exit(0);
            return;
        }
        String hash = block.getHash();
        block.setBlockHeader(block.getBlockHeader());
        block.setHash(hash);

        BlockInfo blockInfo = new BlockInfo();
        blockInfo.setHashPreviousBlock(hashPreviousBlock);
        blockInfo.setNumber(height.get());

        hashPreviousBlock = hash;
        blockInfo.setHashThisBolck(hash);
        String headerString = JSON.toJSONString(block.getBlockHeader());

        dataAndRecordQueue.putData(height.get(), headerString);
        dataAndRecordQueue.putData(height.get(), hash);
        dataAndRecordQueue.putBlockInfo(blockInfo);
        for (Transaction transaction : block.getBlockBody().getInstructions()) {
            int sort = transaction.getVersion().getTxNum();
            try {
                writeToDb(transaction.getVersion(), transaction.getWrites());
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("writes 写入异常");
                break;
            }
            dataAndRecordQueue.putData(height.get(), transaction);

            TransactionInfo transactionInfo = new TransactionInfo();
            transactionInfo.setNumber(height.get());
            transactionInfo.setSort(sort);
            transactionInfo.setTransactionHash(transaction.getHash());
            transactionInfo.setTxId(transaction.getTxId());
            dataAndRecordQueue.putTransactionInfo(transactionInfo);

            logger.info("height:" + height.get() + " sort:" + sort);
        }
        height.incrementAndGet();
    }

    void handleTransaction(Block block) {
        try {
            //logger.info("height: " + block.getBlockHeader().getNumber() + " txNum: " + block.getBlockBody().getInstructions().get(0).getVersion().getTxNum());
            initHeightSortOffset();
            if (block.getBlockHeader().getNumber() != height.get()) {
                logger.error("return  handleTransaction  getNumber " + block.getBlockHeader().getNumber() + " height" + height.get());
                //System.exit(0);
                return;
            }
            List<Transaction> transactions = block.getBlockBody().getInstructions();
            for (Transaction transaction : transactions) {
                Version version = transaction.getVersion();
                if (version.getTxNum() == sort + 1) {
                    //是否新区块
                    if (sort == -1) {
                        BlockInfo blockInfo = new BlockInfo();
                        blockInfo.setHashPreviousBlock(hashPreviousBlock);
                        blockInfo.setNumber(height.get());

                        String headerString = JSON.toJSONString(block.getBlockHeader());

                        String hash = DigestUtil.sha256Hex(headerString);
                        hashPreviousBlock = hash;
                        blockInfo.setHashThisBolck(hash);

                        dataAndRecordQueue.putData(height.get(), headerString);
                        dataAndRecordQueue.putData(height.get(), hash);
                        dataAndRecordQueue.putBlockInfo(blockInfo);
                    }


                    try {
                        writeToDb(version, transaction.getWrites());
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.error("writes 写入异常");
                        return;
                    }
                    dataAndRecordQueue.putData(height.get(), transaction);
                    int sortTemp = sort;
                    sort = sortTemp + 1;
                    //sort++;
                    //offset = transaction.getOffset();

                    TransactionInfo transactionInfo = new TransactionInfo();
                    transactionInfo.setNumber(height.get());
                    transactionInfo.setSort(sort);
                    transactionInfo.setTransactionHash(transaction.getHash());
                    transactionInfo.setTxId(transaction.getTxId());
                    dataAndRecordQueue.putTransactionInfo(transactionInfo);

                    logger.info("height:" + height.get() + " sort:" + sort);
                    if (sort + 1 == countPerBlock) {
                        height.incrementAndGet();
                        //height++;
                        sort = -1;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        //if (transactions.size() == countPerBlock) {
        //    BlockPacket blockPacket = NextBlockPacketBuilder.build();
        //    if (blockPacket != null) {
        //        ApplicationContextProvider.getBean(PacketSender.class).sendGroup(blockPacket);
        //    }
        //}
    }

    private void writeToDb(Version version, List<RwSetWrite> writes) {
        if (writes != null) {
            for (RwSetWrite write : writes) {
                String key = write.getKey();
                boolean delete = write.isDelete();
                if (delete) {
                    dbStore.remove(key);
                } else {
                    VersionedValue versionedValue = new VersionedValue();
                    versionedValue.setVersion(version);
                    versionedValue.setValue(write.getValue());
                    dbStore.put(key, JSON.toJSONString(versionedValue));
                }
            }
        }
    }

    VoteBlock prePackBlock(List<Transaction> transactions) {
        initHeightSortOffset();
        VoteBlock block = new VoteBlock();
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setSize(countPerBlock);
        blockHeader.setNumber(height.get());
        blockHeader.setHashPreviousBlock(hashPreviousBlock);
        blockHeader.setTimeStamp(System.currentTimeMillis());
        blockHeader.setNonce(RandomStringUtils.randomNumeric(32));
        block.setBlockHeader(blockHeader);

        String headerString = JSON.toJSONString(blockHeader);
        String hash = DigestUtil.sha256Hex(headerString);
        block.setHash(hash);

        Set<String> keySet = new HashSet<>();
        List<String> checked = new ArrayList<>();

        for (Transaction transaction : transactions) {
            if (readsCheck(transaction)) {
                boolean writesCheck = true;
                for (RwSetWrite rwSetWrite : transaction.getWrites()) {
                    String key = rwSetWrite.getKey();
                    if (keySet.contains(key)) {
                        writesCheck = false;
                        break;
                    } else {
                        keySet.add(key);
                    }
                }
                if (writesCheck) {
                    checked.add(transaction.getHash());
                }
            }
        }
        if (checked.size() > 0) {
            block.setTxHash(checked);
            return block;
        } else {
            return null;
        }
    }

    public boolean checkBlock(VoteBlock voteBlock) {
        initHeightSortOffset();
        if (voteBlock.getBlockHeader().getNumber() != height.get()) {
            return false;
        }
        List<String> txHash = voteBlock.getTxHash();
        for (String hash : txHash) {
            Transaction transaction = algorithmManager.getByHash(hash);
            boolean readsCheck = readsCheck(transaction);
            if (!readsCheck) {
                return false;
            }
        }
        return true;
    }

    public void executeBlock(VoteBlock voteBlock) {
        Block block = new Block();
        String hash = voteBlock.getHash();
        block.setBlockHeader(voteBlock.getBlockHeader());
        block.setHash(hash);

        BlockInfo blockInfo = new BlockInfo();
        blockInfo.setHashPreviousBlock(hashPreviousBlock);
        blockInfo.setNumber(height.get());

        hashPreviousBlock = hash;
        blockInfo.setHashThisBolck(hash);
        String headerString = JSON.toJSONString(block.getBlockHeader());

        dataAndRecordQueue.putData(height.get(), headerString);
        dataAndRecordQueue.putData(height.get(), hash);
        dataAndRecordQueue.putBlockInfo(blockInfo);
        int sort = 0;
        for (String txHash : voteBlock.getTxHash()) {
            Transaction transaction = algorithmManager.getByHash(txHash);
            Version version = new Version();
            version.setBlockNum(height.get());
            version.setTxNum(sort);
            transaction.setVersion(version);
            try {
                writeToDb(version, transaction.getWrites());
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("writes 写入异常");
                break;
            }
            dataAndRecordQueue.putData(height.get(), transaction);

            TransactionInfo transactionInfo = new TransactionInfo();
            transactionInfo.setNumber(height.get());
            transactionInfo.setSort(sort);
            transactionInfo.setTransactionHash(transaction.getHash());
            transactionInfo.setTxId(transaction.getTxId());
            dataAndRecordQueue.putTransactionInfo(transactionInfo);

            logger.info("height:" + height.get() + " sort:" + sort);
            sort++;
        }
        height.incrementAndGet();
        if (QueueTimmer.isPbftWorking()) {
            AlgorithmManager.term.incrementAndGet();
            QueueTimmer.setPbftWorking(false);
        }
    }

    private void initHeightSortOffset() {
        if (height.get() == -1) {
            BlockInfo latestBlock = blockInfoRepository.findFirstByOrderByNumberDesc();
            if (latestBlock != null) {
                height.set(latestBlock.getNumber());
                hashPreviousBlock = latestBlock.getHashThisBolck();
                TransactionInfo latestTransaction = transactionInfoRepository.findFirstByNumberOrderBySortDesc(height.get());
                if (latestTransaction != null) {
                    sort = latestTransaction.getSort();
                }
            } else {
                height.set(0);
            }
        }
    }

    int getHeight() {
        initHeightSortOffset();
        return height.get();
    }

    int getSort() {
        return sort;
    }

    String getHashPreviousBlock() {
        return hashPreviousBlock;
    }

    public int getCountPerBlock() {
        return countPerBlock;
    }
    //
    //public long getOffset() {
    //    return offset;
    //}
}
