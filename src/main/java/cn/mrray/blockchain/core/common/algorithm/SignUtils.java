package cn.mrray.blockchain.core.common.algorithm;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public abstract class SignUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignUtils.class);

    public static Map signConf;

    static {
        Yaml yaml = new Yaml();
        // 加载Gossip 配置
        File conf = new File("./application.yml");
        if (conf.exists()) {
            try (InputStream fis = new FileInputStream(conf)) {
                signConf = yaml.loadAs(fis, Map.class);
            } catch (IOException e) {
                LOGGER.error("load encrypt config failed", e);
                throw new RuntimeException(e);
            }
        } else {
            try (InputStream is = SignUtils.class.getResourceAsStream("/application.yml")) {
                signConf = yaml.loadAs(is, Map.class);
            } catch (IOException e) {
                LOGGER.error("load encrypt config failed", e);
                throw new RuntimeException(e);
            }
        }
        Map gossip = (Map) signConf.get("sign");
        signConf.clear();
        for (Object key : gossip.keySet()) {
            signConf.put(key, gossip.get(key));
        }
    }

    public static String sign(String content) {
        String algorithm = signConf.get("algorithm").toString();
        return signWithAlg(content, algorithm);
    }

    public static String signWithAlg(String content, String algorithm) {
        try {
            if ("RSA".equals(algorithm)) {

                LOGGER.info("RSA sign content.");
                String priKey = signConf.get("rsaPriKey").toString();
                return RSAUtils.sign(content, priKey);

            } else if ("SM2".equals(algorithm)) {

                LOGGER.info("SM2 sign content.");
                String priKey = signConf.get("sm2PriKey").toString();
                return SM2Utils.sign(content, priKey);

            } else {

                LOGGER.info("ECDSA sign content.");
                String priKey = signConf.get("ecdsaPriKey").toString();
                return ECDSAAlgorithm.sign(priKey, Strings.toByteArray(content));

            }
        } catch (Exception e) {
            LOGGER.error("sign failed", e);
        }
        return null;
    }

    public static String sign(String priKey, String content) {
        String algorithm = signConf.get("algorithm").toString();
        return signWithAlg(priKey, content, algorithm);
    }

    public static String signWithAlg(String priKey, String content, String algorithm) {
        try {
            if ("RSA".equals(algorithm)) {

                LOGGER.info("RSA sign content.");
                return RSAUtils.sign(content, priKey);

            } else if ("SM2".equals(algorithm)) {

                LOGGER.info("SM2 sign content.");
                return SM2Utils.sign(content, priKey);

            } else {

                LOGGER.info("ECDSA sign content.");
                return ECDSAAlgorithm.sign(priKey, Strings.toByteArray(content));

            }
        } catch (Exception e) {
            LOGGER.error("sign failed", e);
        }
        return null;
    }

    public static boolean verify(String content, String signature, String pubKey) {
        String algorithm = signConf.get("algorithm").toString();
        return verifyWithAlg(content, signature, pubKey, algorithm);
    }

    public static boolean verifyWithAlg(String content, String signature, String pubKey, String algorithm) {
        try {
            if ("RSA".equals(algorithm)) {

                LOGGER.info("RSA verify content.");
                return RSAUtils.verify(content, signature, pubKey);

            } else if ("SM2".equals(algorithm)) {

                LOGGER.info("SM2 verify content.");
                return SM2Utils.verify(content, signature, pubKey);

            } else {

                LOGGER.info("ECDSA verify content.");
                return ECDSAAlgorithm.verify(content, signature, pubKey);

            }
        } catch (Exception e) {
            LOGGER.error("verify failed", e);
        }
        return false;
    }

    /**
     * 生成密钥对
     */
    public static Map<String, String> genKeyPair() {
        String algorithm = signConf.get("algorithm").toString();
        return genKeyPairWithAlg(algorithm);
    }


    public static Map<String, String> genKeyPairWithAlg(String algorithm) {
        try {
            Map<String, String> keyPairCache = new HashMap<>();
            if ("RSA".equals(algorithm)) {

                LOGGER.info("RSA generate key pair.");
                KeyPair keyPair = RSAUtils.genKeyPair();
                PublicKey publicKey = keyPair.getPublic();
                PrivateKey privateKey = keyPair.getPrivate();
                keyPairCache.put("pubKey", Base64Utils.encodeToString(publicKey.getEncoded()));
                keyPairCache.put("priKey", Base64Utils.encodeToString(privateKey.getEncoded()));

            } else if ("SM2".equals(algorithm)) {

                LOGGER.info("SM2 generate key pair.");
                AsymmetricCipherKeyPair keyPair = SM2Utils.genKeyPair();
                ECPublicKeyParameters publicKey = (ECPublicKeyParameters) keyPair.getPublic();
                ECPrivateKeyParameters privateKey = (ECPrivateKeyParameters) keyPair.getPrivate();
                keyPairCache.put("pubKey", Base64Utils.encodeToString(publicKey.getQ().getEncoded(false)));
                keyPairCache.put("priKey", Base64Utils.encodeToString(privateKey.getD().toByteArray()));

            } else {

                LOGGER.info("ECDSA generate key pair.");
                String privateKey =  ECDSAAlgorithm.generatePrivateKey();
                String pubKey = ECDSAAlgorithm.generatePublicKey(privateKey);
                keyPairCache.put("priKey", privateKey);
                keyPairCache.put("pubKey", pubKey);
            }

            return keyPairCache;
        } catch (Exception e) {
            LOGGER.error("generate key pair failed.", e);
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
//        String content = "Hello Mrray";
//        // SM2
//        String pubKey = "BBkb/4FIAG7qcthXy5dNufSQOzyjZV2NWXrUZj9QRNyx4veIivH82MZTqAWc0vN5hVOJ9xp3CeLB7h6RTIVe8Rk=";

        // RSA
//        String pubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhWok0kD8yOmXo8XSgDsWHnPLqr7ddfpMxvqUpMWlC3xAr9UAOyCxJ7S4IAPNUaGmnQQ502mAt/daA4bV8k5R2LQf831WKWRCUGb3UOzSsVHr/Pd+ArP8qcp2FHVHyUSMZI7KSM74t68Ykz/0oME5dzLdEbjZB9+pEm0U731L8tTvJ2POIKTL7EHTevTnOYPMfXd21l69AozXTyzqL6izNDkK7cMYd+T+VKQIs3dXObz5w0VovhM/UQtACbE5GAA9nddp5B5JKm2rC7wLhLKEEzfkyjHBL1lXO9PqY1gbbV9UKpHPItHaw3tqx3oCLQLxezaqstvv6YYjM1dJaE73uwIDAQAB";

//        String sign = sign(content);
//        System.out.println(String.format("sign = %s", sign));
//
//        boolean verify = verify(content, sign, pubKey);
//        System.out.println(String.format("verify = %s", verify));
    }

}

