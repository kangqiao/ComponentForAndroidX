package com.zp.androidx.security;

import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by zhaopan on 2019-11-07.
 */

public class CipherUtils {

    private static final String TAG = "CipherUtils";
    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    private static final String SEPARATOR_AES_KEY_IV = "\n";
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    //private static final String CIPHER_AES = "AES/CBC/PKCS5Padding";
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String CIPHER_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES;
    private static final String CIPHER_BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC;
    private static final String CIPHER_PADDING = "PKCS5Padding";
    private static final String CIPHER_AES = CIPHER_ALGORITHM + "/" +
            CIPHER_BLOCK_MODE + "/" +
            CIPHER_PADDING;
    private static final String RSA_ALGORITHM = "RSA/NONE/PKCS1Padding";
    private static final int AES_ = 256;
    private static final int CIPHER_KEY_SIZE = 256;


    public static byte[] decodeBase64(String value) {
        return Base64.decode(value, Base64.NO_WRAP);
    }

    public static String encodeBase64(byte[] bytes) {
        if (null != bytes && bytes.length > 0) {
            return Base64.encodeToString(bytes, Base64.NO_WRAP).trim();
        }
        return "";
    }

    public static String encodeBase64(String data) {
        if (TextUtils.isEmpty(data)) {
            return "";
        }
        return Base64.encodeToString(data.getBytes(DEFAULT_CHARSET), Base64.NO_WRAP);
    }

    /**
     * Computes RFC 2104-compliant HMAC signature.
     * * @param data
     * The signed data.
     *
     * @param key The signing key.
     * @return The Base64-encoded RFC 2104-compliant HMAC signature.
     * @throws java.security.SignatureException when signature generation fails
     */
    public static String signHmacSha256(String data, String key)
            throws java.security.SignatureException {
        String result;
        try {
            // Get an hmac_sha256 key from the raw key bytes.
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(DEFAULT_CHARSET), HMAC_SHA256_ALGORITHM);
            // Get an hmac_sha256 Mac instance and initialize with the signing key.
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            // Compute the hmac on input data bytes.
            byte[] rawHmac = mac.doFinal(data.getBytes(DEFAULT_CHARSET));
            // Base64-encode the hmac by using the utility in the SDK
            result = Base64.encodeToString(rawHmac, Base64.NO_WRAP);
            Log.d(TAG, "signHmacSha256:" + result);
        } catch (Exception e) {
            throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
        }
        return result;
    }

    static byte[] randomBytes(int size) {
        byte[] buffer = new byte[size];
        secureRandom.nextBytes(buffer);
        return buffer;
    }

    public static String concatKeyIvPair(byte[][] keyIVPair) {
        if (null != keyIVPair && keyIVPair.length == 2) {
            return encodeBase64(keyIVPair[0]) + SEPARATOR_AES_KEY_IV + encodeBase64(keyIVPair[1]);
        }
        return "";
    }

    /**
     * 随机生成32位AES私钥p1；
     */
    public static byte[][] genAESKeyIvPair() {
        try {
            //1.构造密钥生成器，指定为AES算法,不区分大小写
            KeyGenerator keygen = KeyGenerator.getInstance(CIPHER_ALGORITHM);
            //2.根据ecnodeRules规则初始化密钥生成器
            //生成一个128位的随机源,根据传入的字节数组
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            keygen.init(CIPHER_KEY_SIZE, random);
            //3.产生原始对称密钥
            SecretKey original_key = keygen.generateKey();
            //4.获得原始对称密钥的字节数组
            byte[] raw = original_key.getEncoded();
            //5.根据字节数组生成AES密钥
            SecretKey key = new SecretKeySpec(raw, "AES");
            //6.根据指定算法AES自成密码器
            Cipher cipher = Cipher.getInstance(CIPHER_AES);
            //7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密(Decrypt_mode)操作，第二个参数为使用的KEY
            cipher.init(Cipher.WRAP_MODE, key);
            //8.获取本次加密时使用的初始向量
            //AlgorithmParameters params = cipher.getParameters();
            //byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            byte[] iv = cipher.getIV();
            return new byte[][]{raw, iv};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encryptAES(String key, String iv, String input) {
        return encodeBase64(encryptAES(key.getBytes(DEFAULT_CHARSET), iv.getBytes(DEFAULT_CHARSET), input.getBytes(DEFAULT_CHARSET)));
    }

    public static byte[] encryptAES(byte[] key, byte[] iv, byte[] input) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_AES);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            // CBC模式需要生成一个16 bytes的initialization vector:
            IvParameterSpec ivps = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivps);
            return cipher.doFinal(input);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decryptAES(String key, String iv, String input) {
        byte[] decryptContent = decryptAES(key.getBytes(DEFAULT_CHARSET), iv.getBytes(DEFAULT_CHARSET), decodeBase64(input));
        return decryptContent == null ? "" : new String(decryptContent, DEFAULT_CHARSET);
    }

    public static byte[] decryptAES(byte[] key, byte[] iv, byte[] input) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_AES);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            // CBC模式需要生成一个16 bytes的initialization vector:
            IvParameterSpec ivps = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivps);
            return cipher.doFinal(input);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encryptRSA(PublicKey key, byte[] message) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(message);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encryptRSA(PrivateKey key, byte[] message) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(message);
    }

    public static byte[] decryptRSA(PublicKey key, byte[] input) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(input);
    }

    public static byte[] decryptRSA(PrivateKey key, byte[] input) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(input);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] signRSA(PrivateKey key, byte[] message) throws GeneralSecurityException {
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(key);
        signature.update(message);
        return signature.sign();
    }

    public boolean verifyRSA(PublicKey key, byte[] message, byte[] sign) throws GeneralSecurityException {
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initVerify(key);
        signature.update(message);
        return signature.verify(sign);
    }

    /*public static PrivateKey readRSAPrivateKey(Reader reader)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        byte[] encoded = new PemReader(reader).readPemObject().getContent();
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);

        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }*/

    public static PublicKey readRSAPublicKey(InputStream in)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {

        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        X509Certificate cer = (X509Certificate) factory.generateCertificate(in);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(cer.getPublicKey().getEncoded());

        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    /*public static PrivateKey readRSAPrivateKey(String content) {
        try (Reader in = new StringReader(content)) {
            return readRSAPrivateKey(in);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }*/

    public static PublicKey readRSAPublicKey(String content) {
        try (InputStream in = new ByteArrayInputStream(content.getBytes(DEFAULT_CHARSET))) {
            return readRSAPublicKey(in);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static RSAPublicKey loadPublicKey(String publicKeyStr) {
        try {
            publicKeyStr = publicKeyStr.replaceAll("-----BEGIN PUBLIC KEY-----", "").replaceAll("-----END PUBLIC KEY-----", "").replaceAll("\\s", "");
            byte[] buffer = decodeBase64(publicKeyStr);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static RSAPrivateKey loadPrivateKey(String privateKeyStr) {
        try {
            privateKeyStr = privateKeyStr.replaceAll("-----BEGIN PRIVATE KEY-----", "").replaceAll("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");
            byte[] buffer = decodeBase64(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String toEncodedString(PublicKey key) {
        int len = 76;
        String s = encodeBase64(key.getEncoded());

        StringBuilder sb = new StringBuilder(((s.length() / len) + 3) * (len + 1));
        sb.append("-----BEGIN PUBLIC KEY-----\n");

        int end = len;
        while (end < s.length()) {
            sb.append(s.substring(end - len, end)).append('\n');
            end += len;
        }

        sb.append(s.substring(end - len, s.length())).append('\n');
        sb.append("-----END PUBLIC KEY-----");

        return sb.toString();
    }

}
