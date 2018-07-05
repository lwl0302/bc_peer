package cn.mrray.blockchain.core.core.service;

import cn.mrray.blockchain.core.block.BlockEnd;
import cn.mrray.blockchain.core.block.BlockHeader;
import com.alibaba.fastjson.JSON;
import org.springframework.util.Base64Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BlockReader {

    public static BlockHeader readHeader(int number) {
        List<byte[]> strings = readBlock(number, "header");
        return JSON.parseObject(Base64Utils.decode(strings.get(0)), BlockHeader.class);
    }

    public static BlockEnd readEnd(int number) {
        List<byte[]> strings = readBlock(number, "end");
        return JSON.parseObject(Base64Utils.decode(strings.get(0)), BlockEnd.class);
    }

    public static List<byte[]> readAll(int number) {
        return readBlock(number, "");
    }

    public static byte[] readFirstPart(int number) {
        return readBlock(number, "firstPart").get(0);
    }

    public static String readHash(int number) {
        return new String(Base64Utils.decode(readBlock(number, "hash").get(0)));
    }

    private static List<byte[]> readBlock(int number, String part) {
        List<byte[]> result = new ArrayList<>();
        String format = String.format("000000%s", number);
        String name = String.format("blockfile_%s", format.substring(format.length() - 6));
        File blockFile = new File("files", name);
        FileInputStream fis;
        int length;
        byte[] lengthBytes;
        byte[] contextBytes;
        try {
            fis = new FileInputStream(blockFile);
            lengthBytes = new byte[8];
            while (fis.read(lengthBytes) != -1) {
                length = Integer.valueOf(new String(lengthBytes));
                contextBytes = new byte[length];
                fis.read(contextBytes);
                //String currentPart = new String(contextBytes, "utf-8");
                if (part.equalsIgnoreCase("header")) {
                    result.add(contextBytes);
                    return result;
                } else if (part.equalsIgnoreCase("hash")) {
                    result.add(contextBytes);
                    if (result.size() == 2) {
                        result.remove(0);
                        return result;
                    }
                } else if (part.equalsIgnoreCase("firstPart")) {
                    result.add(contextBytes);
                    if (result.size() == 3) {
                        result.remove(0);
                        result.remove(0);
                        return result;
                    }
                } else if (part.equalsIgnoreCase("end")) {
                    result.clear();
                    result.add(contextBytes);
                } else {
                    result.add(contextBytes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
