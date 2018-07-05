package cn.mrray.blockchain.core.common.algorithm;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA 工具包
 */
public abstract class RSAUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RSAUtils.class);

    private static final String ALG = "RSA";

    private static final int KEY_SIZE = 2048;

    /**
     * 生成RSA 加密算法的密钥对
     *
     * @return KeyPair 密钥对
     * @throws NoSuchAlgorithmException 如果算法名填写错误
     */
    public static KeyPair genKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(ALG);
        generator.initialize(KEY_SIZE);
        return generator.genKeyPair();
    }

    /**
     * 生成密钥对文件
     *
     * @param keyPair
     * @throws IOException
     */
    public static void genKeyPairPemFile(KeyPair keyPair) {
        try (
                FileWriter pubfw = new FileWriter("rsa_public.key");
                PemWriter pubpemWriter = new PemWriter(pubfw);
                FileWriter prifw = new FileWriter("rsa_private.key");
                PemWriter pripemWriter = new PemWriter(prifw)
        ) {
            // public
            pubpemWriter.writeObject(new PemObject("PUBLIC KEY", keyPair.getPublic().getEncoded()));
            pubpemWriter.flush();

            // private
            pripemWriter.writeObject(new PemObject("PRIVATE KEY", keyPair.getPrivate().getEncoded()));
            pripemWriter.flush();
        } catch (IOException e) {
            LOGGER.error("generate key pair failed.", e);
        }
    }

    public static RSAPublicKey readPubKey(String fileName) {
        try (
                FileReader fileReader = new FileReader(fileName);
                PemReader pemReader = new PemReader(fileReader)
        ) {
            PemObject pem = pemReader.readPemObject();
            byte[] content = pem.getContent();
            KeyFactory kf = KeyFactory.getInstance(ALG);
            return (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(content));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
            LOGGER.error("read pem file failed. ", e);
        }
        return null;
    }

    public static RSAPrivateKey readPriKey(String fileName) {
        try (
                FileReader fileReader = new FileReader(fileName);
                PemReader pemReader = new PemReader(fileReader)
        ) {
            PemObject pem = pemReader.readPemObject();
            byte[] content = pem.getContent();
            KeyFactory kf = KeyFactory.getInstance(ALG);
            return (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(content));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
            LOGGER.error("read pem file failed. ", e);
        }
        return null;
    }

    public static byte[] encrypt(PrivateKey privateKey, String content) throws Exception {
        Cipher cipher = Cipher.getInstance(ALG);
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return cipher.doFinal(content.getBytes());
    }

    public static byte[] decrypt(PublicKey publicKey, byte[] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance(ALG);
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        return cipher.doFinal(encrypted);
    }

    public static String sign(String content, PrivateKey privateKey) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(content.getBytes("UTF-8"));
        byte[] signature = privateSignature.sign();
        return Base64.getEncoder().encodeToString(signature);
    }

    public static String sign(String content, String privateKey) throws Exception {
        KeyFactory kf = KeyFactory.getInstance(ALG);
        RSAPrivateKey privKey = (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(Base64Utils.decodeFromString(privateKey)));

        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privKey);
        privateSignature.update(content.getBytes("UTF-8"));
        byte[] signature = privateSignature.sign();
        return Base64.getEncoder().encodeToString(signature);
    }

    public static boolean verify(String content, String signature, PublicKey publicKey) throws Exception {
        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(publicKey);
        publicSignature.update(content.getBytes("UTF-8"));
        byte[] signatureBytes = Base64.getDecoder().decode(signature);
        return publicSignature.verify(signatureBytes);
    }

    public static boolean verify(String content, String signature, String publicKey) throws Exception {
        KeyFactory kf = KeyFactory.getInstance(ALG);
        RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(Base64Utils.decodeFromString(publicKey)));

        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(pubKey);
        publicSignature.update(content.getBytes("UTF-8"));
        byte[] signatureBytes = Base64.getDecoder().decode(signature);
        return publicSignature.verify(signatureBytes);
    }


}
