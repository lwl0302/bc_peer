package cn.mrray.blockchain.core.common.algorithm;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

public abstract class SM2Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SM2Utils.class);

    private static final BigInteger SM2_ECC_P = new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFF", 16);
    private static final BigInteger SM2_ECC_A = new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFC", 16);
    private static final BigInteger SM2_ECC_B = new BigInteger("28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93", 16);
    private static final BigInteger SM2_ECC_N = new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123", 16);
    private static final BigInteger SM2_ECC_GX = new BigInteger("32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7", 16);
    private static final BigInteger SM2_ECC_GY = new BigInteger("BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0", 16);

    public static AsymmetricCipherKeyPair genKeyPair() {
        ECCurve curve = new ECCurve.Fp(SM2_ECC_P, SM2_ECC_A, SM2_ECC_B);
        ECPoint g = curve.createPoint(SM2_ECC_GX, SM2_ECC_GY);
        ECDomainParameters domainParams = new ECDomainParameters(curve, g, SM2_ECC_N);
        ECKeyPairGenerator keyPairGenerator = new ECKeyPairGenerator();
        ECKeyGenerationParameters aKeyGenParams = new ECKeyGenerationParameters(domainParams, new SecureRandom());
        keyPairGenerator.init(aKeyGenParams);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 生成密钥对文件
     *
     * @param keyPair
     * @throws IOException
     */
    public static void genKeyPairPemFile(AsymmetricCipherKeyPair keyPair) {
        try (
                FileWriter pubfw = new FileWriter("sm2_public.key");
                PemWriter pubpemWriter = new PemWriter(pubfw);
                FileWriter prifw = new FileWriter("sm2_private.key");
                PemWriter pripemWriter = new PemWriter(prifw)
        ) {
            // public
            ECPublicKeyParameters pubKey = (ECPublicKeyParameters) keyPair.getPublic();
            pubpemWriter.writeObject(new PemObject("PUBLIC KEY", pubKey.getQ().getEncoded(false)));
            pubpemWriter.flush();

            // private
            ECPrivateKeyParameters priKey = (ECPrivateKeyParameters) keyPair.getPrivate();
            pripemWriter.writeObject(new PemObject("PRIVATE KEY", priKey.getD().toByteArray()));
            pripemWriter.flush();
        } catch (IOException e) {
            LOGGER.error("generate key pair failed.", e);
        }
    }

    public static ECPublicKeyParameters readPubKey(String fileName) {
        try (
                FileReader fileReader = new FileReader(fileName);
                PemReader pemReader = new PemReader(fileReader)
        ) {

            PemObject pem = pemReader.readPemObject();
            byte[] content = pem.getContent();
            ECCurve curve = new ECCurve.Fp(SM2_ECC_P, SM2_ECC_A, SM2_ECC_B);
            ECPoint g = curve.createPoint(SM2_ECC_GX, SM2_ECC_GY);
            ECDomainParameters domainParams = new ECDomainParameters(curve, g, SM2_ECC_N);
            return new ECPublicKeyParameters(curve.decodePoint(content), domainParams);
        } catch (IOException e) {
            LOGGER.error("generate key pair failed.", e);
        }
        return null;
    }

    public static ECPrivateKeyParameters readPriKey(String fileName) {
        try (
                FileReader fileReader = new FileReader(fileName);
                PemReader pemReader = new PemReader(fileReader)
        ) {

            PemObject pem = pemReader.readPemObject();
            byte[] content = pem.getContent();
            ECCurve curve = new ECCurve.Fp(SM2_ECC_P, SM2_ECC_A, SM2_ECC_B);
            ECPoint g = curve.createPoint(SM2_ECC_GX, SM2_ECC_GY);
            ECDomainParameters domainParams = new ECDomainParameters(curve, g, SM2_ECC_N);
            return new ECPrivateKeyParameters(new BigInteger(content), domainParams);
        } catch (IOException e) {
            LOGGER.error("read pem file failed. ", e);
        }
        return null;
    }

    public static byte[] encrypt(ECPublicKeyParameters pubKey, String content) throws Exception {
        SM2Engine sm2Engine = new SM2Engine();
        byte[] m = Strings.toByteArray(content);
        sm2Engine.init(true, new ParametersWithRandom(pubKey, new SecureRandom()));
        return sm2Engine.processBlock(m, 0, m.length);
    }

    public static byte[] encrypt(ECPublicKeyParameters pubKey, byte[] contentBytes) throws Exception {
        SM2Engine sm2Engine = new SM2Engine();
        sm2Engine.init(true, new ParametersWithRandom(pubKey, new SecureRandom()));
        return sm2Engine.processBlock(contentBytes, 0, contentBytes.length);
    }

    public static byte[] decrypt(ECPrivateKeyParameters priKey, byte[] encrypted) throws Exception {
        SM2Engine sm2Engine = new SM2Engine();
        sm2Engine.init(false, priKey);
        return sm2Engine.processBlock(encrypted, 0, encrypted.length);
    }

    public static String sign(byte[] content, ECPrivateKeyParameters priKey) throws Exception {
        SM2Signer signer = new SM2Signer();
        signer.init(true,
                new ParametersWithID(new ParametersWithRandom(priKey,
                        new SecureRandom()),
                        Strings.toByteArray("tanghuanyou@163.com")));
        signer.update(content, 0, content.length);
        byte[] sig = signer.generateSignature();
        BigInteger[] decode = decode(sig);
        return String.format("%s,%s", decode[0].toString(16), decode[1].toString(16));
    }

    public static String sign(String content, ECPrivateKeyParameters priKey) throws Exception {
        SM2Signer signer = new SM2Signer();
        signer.init(true,
                new ParametersWithID(new ParametersWithRandom(priKey,
                        new SecureRandom()),
                        Strings.toByteArray("tanghuanyou@163.com")));
        byte[] msg = Strings.toByteArray(content);
        signer.update(msg, 0, msg.length);
        byte[] sig = signer.generateSignature();
        BigInteger[] decode = decode(sig);
        return String.format("%s,%s", decode[0].toString(16), decode[1].toString(16));
    }

    public static String sign(String content, String priKey) throws Exception {
        ECCurve curve = new ECCurve.Fp(SM2_ECC_P, SM2_ECC_A, SM2_ECC_B);
        ECPoint g = curve.createPoint(SM2_ECC_GX, SM2_ECC_GY);
        ECDomainParameters domainParams = new ECDomainParameters(curve, g, SM2_ECC_N);
        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(new BigInteger(Base64Utils.decodeFromString(priKey)), domainParams);

        SM2Signer signer = new SM2Signer();
        signer.init(true,
                new ParametersWithID(new ParametersWithRandom(privKey,
                        new SecureRandom()),
                        Strings.toByteArray("tanghuanyou@163.com")));
        byte[] msg = Strings.toByteArray(content);
        signer.update(msg, 0, msg.length);
        byte[] sig = signer.generateSignature();
        BigInteger[] decode = decode(sig);
        return String.format("%s,%s", decode[0].toString(16), decode[1].toString(16));
    }

    public static boolean verify(byte[] content, byte[] signature, ECPublicKeyParameters pubKey) throws Exception {
        SM2Signer signer = new SM2Signer();
        signer.init(false, new ParametersWithID(pubKey, Strings.toByteArray("tanghuanyou@163.com")));
        signer.update(content, 0, content.length);
        return signer.verifySignature(signature);
    }

    public static boolean verify(String content, byte[] signature, ECPublicKeyParameters pubKey) throws Exception {
        SM2Signer signer = new SM2Signer();
        signer.init(false, new ParametersWithID(pubKey, Strings.toByteArray("tanghuanyou@163.com")));
        byte[] msg = Strings.toByteArray(content);
        signer.update(msg, 0, msg.length);
        return signer.verifySignature(signature);
    }

    public static boolean verify(String content, String signature, ECPublicKeyParameters pubKey) throws Exception {
        SM2Signer signer = new SM2Signer();
        signer.init(false, new ParametersWithID(pubKey, Strings.toByteArray("tanghuanyou@163.com")));
        byte[] msg = Strings.toByteArray(content);
        signer.update(msg, 0, msg.length);
        String[] split = signature.split(",");
        BigInteger r = new BigInteger(split[0], 16);
        BigInteger s = new BigInteger(split[1], 16);
        return signer.verifySignature(encode(r, s));
    }

    public static boolean verify(String content, String signature, String pubKey) throws Exception {
        ECCurve curve = new ECCurve.Fp(SM2_ECC_P, SM2_ECC_A, SM2_ECC_B);
        ECPoint g = curve.createPoint(SM2_ECC_GX, SM2_ECC_GY);
        ECDomainParameters domainParams = new ECDomainParameters(curve, g, SM2_ECC_N);
        ECPublicKeyParameters publicKey = new ECPublicKeyParameters(curve.decodePoint(Base64Utils.decodeFromString(pubKey)), domainParams);

        SM2Signer signer = new SM2Signer();
        signer.init(false, new ParametersWithID(publicKey, Strings.toByteArray("tanghuanyou@163.com")));
        byte[] msg = Strings.toByteArray(content);
        signer.update(msg, 0, msg.length);
        String[] split = signature.split(",");
        BigInteger r = new BigInteger(split[0], 16);
        BigInteger s = new BigInteger(split[1], 16);
        return signer.verifySignature(encode(r, s));
    }

    private static BigInteger[] decode(byte[] sig) {
        ASN1Sequence s = ASN1Sequence.getInstance(sig);
        return new BigInteger[]{ASN1Integer.getInstance(s.getObjectAt(0)).getValue(),
                ASN1Integer.getInstance(s.getObjectAt(1)).getValue()};
    }

    private static byte[] encode(BigInteger r, BigInteger s)
            throws IOException {
        return new DERSequence(new ASN1Encodable[]{new ASN1Integer(r), new ASN1Integer(s)}).getEncoded();
    }
}
