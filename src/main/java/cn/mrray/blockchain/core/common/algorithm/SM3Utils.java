package cn.mrray.blockchain.core.common.algorithm;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Hex;

import java.util.Arrays;

public abstract class SM3Utils {

    public static String encrypt(String src) {
        SM3Digest digest = new SM3Digest();
        byte[] buf = new byte[digest.getDigestSize()];

        byte[] contentBytes = Strings.toByteArray(src);
        digest.update(contentBytes, 0, contentBytes.length);
        digest.doFinal(buf, 0);
        return Strings.fromByteArray(Hex.encode(buf));
    }

    public static boolean match(String src, String encode) {
        SM3Digest digest = new SM3Digest();
        byte[] buf = new byte[digest.getDigestSize()];
        byte[] contentBytes = Strings.toByteArray(src);
        digest.update(contentBytes, 0, contentBytes.length);
        digest.doFinal(buf, 0);
        byte[] encodeBytes = Hex.decode(Strings.toByteArray(encode));
        return Arrays.equals(buf, encodeBytes);
    }

    public static void main(String[] args) {
        String content = "Hello World";
        String encrypt = encrypt(content);
        System.out.println(String.format("encrypt = %s", encrypt));
        boolean match = match(content, encrypt);
        System.out.println(String.format("match = %s", match));
    }

}
