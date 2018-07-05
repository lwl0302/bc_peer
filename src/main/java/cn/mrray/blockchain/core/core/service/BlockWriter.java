package cn.mrray.blockchain.core.core.service;

import cn.hutool.crypto.digest.DigestUtil;
import cn.mrray.blockchain.core.block.*;
import cn.mrray.blockchain.core.block.merkle.MerkleTree;
import cn.mrray.blockchain.core.core.model.Transaction;
import cn.mrray.blockchain.core.core.model.Version;
import cn.mrray.blockchain.core.core.repository.BlockInfoRepository;
import cn.mrray.blockchain.core.core.repository.TransactionInfoRepository;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
public class BlockWriter {
    private int SIZE = 1048576 - 132;
    private final BlockInfoRepository blockInfoRepository;
    private final TransactionInfoRepository transactionInfoRepository;
    private final NioWriter nioWriter;

    private LinkedList<byte[]> bufferOne = new LinkedList<>();
    private LinkedList<byte[]> bufferTwo = new LinkedList<>();
    private LinkedList<byte[]> currentBuffer;

    private File currentFile;
    private int currentNumber = 0;
    private int currentSort = 0;
    private String hash;

    private List<TransactionInfo> transactionInfoList = new ArrayList<>();

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public BlockWriter(BlockInfoRepository blockInfoRepository, TransactionInfoRepository transactionInfoRepository, NioWriter nioWriter) {
        this.blockInfoRepository = blockInfoRepository;
        this.transactionInfoRepository = transactionInfoRepository;
        this.nioWriter = nioWriter;
    }

    synchronized Block writeBlock(List<Transaction> instructions) {
        //BlockInfo blockInfo = blockInfoRepository.findFirstByOrderByNumberDesc();
        Block block = new Block();
        BlockHeader blockHeader;
        if (currentNumber == 0 && currentBuffer == null) {
            blockHeader = new BlockHeader();
            blockHeader.setNumber(currentNumber);
            blockHeader.setSize(SIZE);
            blockHeader.setHashPreviousBlock("");
            blockHeader.setTimeStamp(System.currentTimeMillis());
            blockHeader.setNonce(RandomStringUtils.randomNumeric(32));
            writeHeader(blockHeader);
        } else {
            //currentNumber = blockInfo.getNumber();
            blockHeader = JSON.parseObject(Base64Utils.decode(currentBuffer.get(1)), BlockHeader.class);
        }
        block.setBlockHeader(blockHeader);
        block.setHash(hash);
        BlockBody blockBody = new BlockBody();
        blockBody.setInstructions(instructions);
        block.setBlockBody(blockBody);
        writeBody(block);
        return block;
    }

    private void writeHeader(BlockHeader blockHeader) {
        while (true) {
            if (bufferOne.isEmpty()) {
                currentBuffer = bufferOne;
                logger.info("use buffer one");
                break;
            } else if (bufferTwo.isEmpty()) {
                currentBuffer = bufferTwo;
                logger.info("use buffer two");
                break;
            }
            logger.warn("no buffer to use");
        }
        String format = String.format("000000%s", blockHeader.getNumber());
        String name = String.format("blockfile_%s", format.substring(format.length() - 6));
        currentFile = new File("files", name);

        //写区块头
        String jsonString = JSON.toJSONString(blockHeader);
        byte[] headJsonBytes = Base64Utils.encode(jsonString.getBytes());
        int headLength = headJsonBytes.length;
        String lengthStr = String.format("00000000%s", headLength);
        byte[] lengthBytes = lengthStr.substring(lengthStr.length() - 8).getBytes();
        currentBuffer.add(lengthBytes);
        currentBuffer.add(headJsonBytes);

        //写hash
        BlockInfo blockInfo = new BlockInfo();
        if (currentNumber == 0) {
            blockInfo.setHashPreviousBlock("");
        } else {
            blockInfo.setHashPreviousBlock(hash);
        }

        hash = DigestUtil.sha256Hex(jsonString);
        System.out.println(hash);
        byte[] hashBytes = Base64Utils.encode(hash.getBytes());
        int hashLength = hashBytes.length;
        lengthStr = String.format("00000000%s", hashLength);
        lengthBytes = lengthStr.substring(lengthStr.length() - 8).getBytes();
        currentBuffer.add(lengthBytes);
        currentBuffer.add(hashBytes);

        //int number = blockHeader.getNumber();

        blockInfo.setHashThisBolck(hash);
        blockInfo.setNumber(currentNumber);
        blockInfoRepository.save(blockInfo);
        SIZE -= headLength + hashLength + lengthBytes.length * 2;
    }

    private void writeEnd(BlockEnd blockEnd) {
        String blockEndJson = JSON.toJSONString(blockEnd);
        byte[] blockEndBytes = Base64Utils.encode(blockEndJson.getBytes());
        int blockEndLength = blockEndBytes.length;
        String lengthStr = String.format("00000000%s", blockEndLength);
        byte[] lengthBytes = lengthStr.substring(lengthStr.length() - 8).getBytes();
        currentBuffer.add(lengthBytes);
        currentBuffer.add(blockEndBytes);
        nioWriter.write(currentFile, currentBuffer);
        SIZE = 1048576 - 132;
    }

    private void writeBody(Block block) {
        List<Transaction> instructions = block.getBlockBody().getInstructions();
        //BlockHeader blockHeader = block.getBlockHeader();
        for (Transaction transaction : instructions) {

            //保存交易索引
            TransactionInfo transactionInfo = new TransactionInfo();
            transactionInfo.setTransactionHash(transaction.getHash());
            transactionInfo.setTxId(transaction.getTxId());
            //int thisNumber = blockHeader.getNumber();

            //添加版本号
            //TransactionInfo latestTransaction = transactionInfoRepository.findFirstByOrderByIdDesc();
            Version version = new Version();
            //if (latestTransaction == null) {
            //    version.setBlockNum(0);
            //    version.setTxNum(0);
            //} else {
            version.setBlockNum(currentNumber);
            version.setTxNum(currentSort);
            //}
            transaction.setVersion(version);

            //写入数据
            String instructionJson = JSON.toJSONString(transaction);
            byte[] instructionBytes = Base64Utils.encode(instructionJson.getBytes());
            int instructionLength = instructionBytes.length;
            String lengthStr;
            byte[] lengthBytes;
            if (instructionLength + 8 <= SIZE) {
                lengthStr = String.format("00000000%s", instructionLength);
                lengthBytes = lengthStr.substring(lengthStr.length() - 8).getBytes();
                SIZE -= instructionLength + 8;
                currentBuffer.add(lengthBytes);
                currentBuffer.add(instructionBytes);
            } else {

                //写入部分交易
                /*if (left - 8 > 0) {//保证写入长度标记后至少还能写入1byte数据
                    lengthStr = String.format("00000000%s", left - 8);
                    lengthBytes = lengthStr.substring(lengthStr.length() - 8).getBytes();
                    fos.write(lengthBytes);
                    fos.write(instructionBytes, 0, left - 8);
                    fos.flush();
                }*/


                //写入文件尾
                BlockEnd blockEnd = new BlockEnd();
                byte[] headerString = currentBuffer.get(1);
                blockEnd.setSize(currentBuffer.size() / 2 - 2);
                List<String> hashList = new ArrayList<>();
                for (int i = 5; i < currentBuffer.size(); i += 2) {
                    try {
                        Transaction instruction = JSON.parseObject(Base64Utils.decode(currentBuffer.get(i)), Transaction.class);
                        hashList.add(instruction.getHash());
                    } catch (JSONException | IllegalArgumentException e) {
                        //丢弃不完整交易
                        //目前不会出现
                    }
                }
                String root = new MerkleTree(hashList).build().getRoot();
                blockEnd.setMerkleTreeRoot(root);
                writeEnd(blockEnd);
                block.setBlockEnd(blockEnd);

                currentNumber++;
                currentSort = 0;
                //写入新区块头
                BlockHeader blockHeader = new BlockHeader();
                blockHeader.setHashPreviousBlock(DigestUtil.sha256Hex(Base64Utils.decode(headerString)));
                blockHeader.setNumber(currentNumber);
                blockHeader.setTimeStamp(System.currentTimeMillis());
                blockHeader.setNonce(RandomStringUtils.randomNumeric(32));
                blockHeader.setSize(SIZE);
                writeHeader(blockHeader);

                //写入交易剩下部分
                /*if (left - 8 > 0) {
                    lengthStr = String.format("00000000%s", instructionLength - left + 8);
                    lengthBytes = lengthStr.substring(lengthStr.length() - 8).getBytes();
                    fos.write(lengthBytes);
                    fos.write(instructionBytes, left - 8, instructionLength - left + 8);
                } else {*/

                //修改版本号
                version.setBlockNum(currentNumber);
                version.setTxNum(currentSort);
                instructionJson = JSON.toJSONString(transaction);
                instructionBytes = Base64Utils.encode(instructionJson.getBytes());
                instructionLength = instructionBytes.length;

                //写入交易
                lengthStr = String.format("00000000%s", instructionLength);
                lengthBytes = lengthStr.substring(lengthStr.length() - 8).getBytes();
                currentBuffer.add(lengthBytes);
                currentBuffer.add(instructionBytes);
                /* }*/
            }
            currentSort++;
            transactionInfo.setNumber(currentNumber);
            transactionInfo.setSort(transaction.getVersion().getTxNum());
            transactionInfoList.add(transactionInfo);
            transactionInfoRepository.save(transactionInfo);
            //if (transactionInfoList.size() == 100) {
            //    transactionInfoRepository.save(transactionInfoList);
            //    transactionInfoList.clear();
            //}
        }
    }

    List<byte[]> readBuffer() {
        List<byte[]> bytes = new ArrayList<>();
        for (int i = 1; i < currentBuffer.size(); i += 2) {
            bytes.add(currentBuffer.get(i));
        }
        return bytes;
    }

    /*private static String MD5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(s.getBytes("utf-8"));
            return toHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String toHex(byte[] bytes) {
        final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (byte aByte : bytes) {
            ret.append(HEX_DIGITS[(aByte >> 4) & 0x0f]);
            ret.append(HEX_DIGITS[aByte & 0x0f]);
        }
        return ret.toString();
    }*/
}
