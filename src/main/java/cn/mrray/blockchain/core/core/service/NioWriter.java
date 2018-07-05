package cn.mrray.blockchain.core.core.service;

import cn.mrray.blockchain.core.block.BlockEnd;
import cn.mrray.blockchain.core.block.BlockHeader;
import cn.mrray.blockchain.core.block.merkle.MerkleTree;
import cn.mrray.blockchain.core.core.model.Transaction;
import com.alibaba.fastjson.JSON;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Component
public class NioWriter {
    @Async
    public void write(int height, LinkedList<Object> objects) {
        String format = String.format("000000%s", height);
        String name = String.format("blockfile_%s", format.substring(format.length() - 6));
        File file = new File("files", name);
        List<String> hashList = new ArrayList<>();
        try {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            FileChannel channel = fos.getChannel();
            for (Object object : objects) {
                if (object instanceof BlockHeader) {
                    System.out.println(object);
                }
                if (object instanceof Transaction) {
                    hashList.add(((Transaction) object).getHash());
                }
                encodeAndWrite(object, channel);
            }
            BlockEnd blockEnd = new BlockEnd();
            blockEnd.setSize(objects.size() - 2);
            blockEnd.setMerkleTreeRoot(new MerkleTree(hashList).build().getRoot());
            encodeAndWrite(blockEnd, channel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void encodeAndWrite(Object object, FileChannel channel) throws IOException {
        String jsonString;
        if (object instanceof String) {
            jsonString = (String) object;
        } else {
            jsonString = JSON.toJSONString(object);
        }
        byte[] jsonBytes = Base64Utils.encode(jsonString.getBytes());
        int jsonLength = jsonBytes.length;
        String lengthStr = String.format("00000000%s", jsonLength);
        byte[] lengthBytes = lengthStr.substring(lengthStr.length() - 8).getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(lengthBytes.length);
        byteBuffer.put(lengthBytes);
        byteBuffer.flip();
        channel.write(byteBuffer);
        byteBuffer = ByteBuffer.allocate(jsonBytes.length);
        byteBuffer.put(jsonBytes);
        byteBuffer.flip();
        channel.write(byteBuffer);
    }

    @Async
    public void write(File file, LinkedList<byte[]> buffer) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            FileChannel channel = fos.getChannel();
            for (byte[] bytes : buffer) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
                byteBuffer.put(bytes);
                byteBuffer.flip();
                channel.write(byteBuffer);
            }
            buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
