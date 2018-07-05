package cn.mrray.blockchain.core.core.service;

import cn.mrray.blockchain.core.block.*;
import cn.mrray.blockchain.core.core.model.Transaction;
import cn.mrray.blockchain.core.core.repository.BlockInfoRepository;
import cn.mrray.blockchain.core.core.repository.TransactionInfoRepository;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import java.util.ArrayList;
import java.util.List;

@Service
public class QueryService {
    private final TransactionInfoRepository transactionInfoRepository;
    private final BlockInfoRepository blockInfoRepository;
    //private final BlockWriter blockWriter;
    private final DataAndRecordQueue dataAndRecordQueue;

    @Autowired
    public QueryService(BlockInfoRepository blockInfoRepository, TransactionInfoRepository transactionInfoRepository, DataAndRecordQueue dataAndRecordQueue) {
        this.blockInfoRepository = blockInfoRepository;
        this.transactionInfoRepository = transactionInfoRepository;
        //this.blockWriter = blockWriter;
        this.dataAndRecordQueue = dataAndRecordQueue;
    }

    public Block findBlockByNumber(int number) {
        BlockInfo blockInfo = blockInfoRepository.findFirstByOrderByNumberDesc();
        List<byte[]> parts;
        Block block = new Block();
        BlockBody blockBody = new BlockBody();
        block.setBlockBody(blockBody);
        List<Transaction> instructions = new ArrayList<>();
        blockBody.setInstructions(instructions);
        if (blockInfo == null || blockInfo.getNumber() < number) {
            return null;
        } else if (blockInfo.getNumber() == number) {
            List<Object> objects = dataAndRecordQueue.readBuffer();
            int size = objects.size();
            for (int i = 0; i < size; i++) {
                Object object = objects.get(i);
                if (i == 0) {
                    block.setBlockHeader(JSON.parseObject((String) object, BlockHeader.class));
                } else if (i == 1) {
                    block.setHash((String) object);
                } else {
                    instructions.add((Transaction) object);
                }
            }
            return block;
        } else {
            parts = BlockReader.readAll(number);
        }
        //BlockInfo blockInfo = blockInfoRepository.findByHashThisBolck(hash);
        //int number = blockInfo.getNumber();
        for (int i = 0; i < parts.size(); i++) {
            byte[] part = parts.get(i);
            if (i == 0) {
                BlockHeader blockHeader = JSON.parseObject(Base64Utils.decode(part), BlockHeader.class);
                block.setBlockHeader(blockHeader);
                //System.out.println("    preHash : " + blockHeader.getHashPreviousBlock());
            } else if (i == 1) {
                String hash = new String(Base64Utils.decode(part));
                block.setHash(hash);
            } else if (i == parts.size() - 1) {
                BlockEnd blockEnd = JSON.parseObject(Base64Utils.decode(part), BlockEnd.class);
                if (blockEnd.getSize() == 0 || blockEnd.getMerkleTreeRoot() == null) {
                    Transaction instruction = JSON.parseObject(Base64Utils.decode(part), Transaction.class);
                    instructions.add(instruction);
                } else {
                    block.setBlockEnd(blockEnd);
                }
                //System.out.println("   thisHash : " + blockEnd.getHash());
            } else {
                try {
                    Transaction instruction = JSON.parseObject(Base64Utils.decode(part), Transaction.class);
                    instructions.add(instruction);
                } catch (JSONException | IllegalArgumentException e) {
                    e.printStackTrace();
                    //if (i != 2) {
                    //    byte[] firstPart = BlockReader.readFirstPart(number + 1);
                    //    byte[] bytes = new byte[part.length + firstPart.length];
                    //    System.arraycopy(part, 0, bytes, 0, part.length);
                    //    System.arraycopy(firstPart, 0, bytes, part.length, firstPart.length);
                    //    Transaction instruction = JSON.parseObject(Base64Utils.decode(bytes), Transaction.class);
                    //    instructions.add(instruction);
                    //}
                }
            }
        }
        return block;
    }

    public Block findBlockByHash(String hash) {
        BlockInfo blockInfo = blockInfoRepository.findByHashThisBolck(hash);
        int number = blockInfo.getNumber();
        return findBlockByNumber(number);
    }

    public Block findBlockByTxId(long txId) {
        TransactionInfo transactionInfo = transactionInfoRepository.findByTxId(txId);
        int number = transactionInfo.getNumber();
        return findBlockByNumber(number);
    }

    public Transaction findTransactionByTxId(long txId) {
        BlockInfo blockInfo = blockInfoRepository.findFirstByOrderByNumberDesc();
        TransactionInfo transactionInfo = transactionInfoRepository.findByTxId(txId);
        int number = transactionInfo.getNumber();
        String transactionHash = transactionInfo.getTransactionHash();
        List<byte[]> parts;
        if (blockInfo == null) {
            return null;
        } else if (blockInfo.getNumber() == number) {
            List<Object> objects = dataAndRecordQueue.readBuffer();
            int size = objects.size();
            for (int i = 2; i < size; i++) {
                Object object = objects.get(i);
                Transaction transaction = (Transaction) object;
                if (transaction.getTxId() == txId && transaction.getHash().equals(transactionHash)) {
                    return transaction;
                }
            }
            return null;
        } else {
            parts = BlockReader.readAll(number);
        }
        for (int i = 2; i < parts.size(); i++) {
            byte[] part = parts.get(i);
            try {
                Transaction transaction = JSON.parseObject(Base64Utils.decode(part), Transaction.class);
                if (transaction.getTxId() == txId && transaction.getHash().equals(transactionHash)) {
                    return transaction;
                }
            } catch (JSONException | IllegalArgumentException e) {
                if (i == 2) {
                    continue;
                }
                byte[] firstPart = BlockReader.readFirstPart(number + 1);
                byte[] bytes = new byte[part.length + firstPart.length];
                System.arraycopy(part, 0, bytes, 0, part.length);
                System.arraycopy(firstPart, 0, bytes, part.length, firstPart.length);
                Transaction transaction = JSON.parseObject(Base64Utils.decode(bytes), Transaction.class);
                if (transaction.getTxId() == txId && transaction.getHash().equals(transactionHash)) {
                    return transaction;
                }
            }
        }
        return null;
    }
}
