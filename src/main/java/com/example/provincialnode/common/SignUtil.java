package com.example.provincialnode.common;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.HashMap;
import java.util.Map;

public class SignUtil {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 验证签名
     * @param signature         签名内容
     * @param verifyPublicKey   验签公钥
     * @param decryptPrivateKey 解密私钥
     * @return 原文JSON字符串
     */
    public static String verifySignature(Map<String,Object> signature, String verifyPublicKey, String decryptPrivateKey) throws Exception {
        //实现需求：使用decryptPrivateKey 解密key 得到SM4的秘钥，使用秘钥解密requestData的到原文， 将原文使用SM2WithSM3算法和verifyPublicKey对数据进行签名，签名值为signatureData，
        //返回原文值JSON字符串， signature这个map中的数据分别为signatureData 签名数据、key SM4加密秘钥、requestData 加密数据 放入map返回
        // 1. 从参数中获取签名、加密的SM4密钥和加密的数据
        String signatureData = (String) signature.get("signatureData");
        String encryptedKey = (String) signature.get("key");
        String encryptedData = (String) signature.get("requestData");

        // 2. 使用decryptPrivateKey解密key得到SM4的秘钥
        cn.hutool.crypto.asymmetric.SM2 sm2Decrypt = new cn.hutool.crypto.asymmetric.SM2(decryptPrivateKey, null);
        byte[] decryptedKeyBytes = sm2Decrypt.decrypt(cn.hutool.core.codec.Base64.decode(encryptedKey));
        String sm4Key = new String(decryptedKeyBytes, "UTF-8");

        // 3. 使用SM4秘钥解密requestData得到原文
        cn.hutool.crypto.symmetric.SM4 sm4 = new cn.hutool.crypto.symmetric.SM4(
                cn.hutool.crypto.Mode.ECB,
                cn.hutool.crypto.Padding.PKCS5Padding,
                sm4Key.getBytes()
        );
        byte[] decryptedDataBytes = sm4.decrypt(cn.hutool.core.codec.Base64.decode(encryptedData));
        String originalData = new String(decryptedDataBytes, "UTF-8");

        // 4. 使用SM2WithSM3算法和verifyPublicKey对原文进行签名
        cn.hutool.crypto.asymmetric.SM2 sm2Sign = new cn.hutool.crypto.asymmetric.SM2(null, verifyPublicKey);
        byte[] computedSignature = sm2Sign.sign(originalData.getBytes("UTF-8"));
        String computedSignatureStr = cn.hutool.core.codec.Base64.encode(computedSignature);

        // 5. 验证签名是否匹配
        if (!computedSignatureStr.equals(signatureData)) {
            // 签名不匹配，返回null表示验证失败
            return null;
        }

        // 6. 返回原文值JSON字符串
        return originalData;
    }


    /**
     * 对数据进行签名
     * @param content          待签名内容
     * @param encryptPublicKey  加密公钥字符串
     * @param verifyPrivateKey  签名私钥字符串
     * @return signatureData 签名数据、key SM4加密秘钥、requestData 加密数据
     */
    public static Map<String,String> signData(String content, String encryptPublicKey, String verifyPrivateKey) throws Exception {
        //实现需求：使用SM2WithSM3算法和verifyPrivateKey 私钥对数据进行签名，使用SM4 对数据进行加密，随机生成16位随机码作为SM4秘钥，将秘钥使用encryptPublicKey 进行加密；
        //返回signatureData 签名数据、key SM4加密秘钥、requestData 加密数据 放入map返回
        Map<String, String> result = new HashMap<>();

        // 1. 生成16位随机码作为SM4秘钥
        String sm4Key = cn.hutool.core.util.RandomUtil.randomString(16);

        // 2. 使用SM4对数据进行加密
        cn.hutool.crypto.symmetric.SM4 sm4 = new cn.hutool.crypto.symmetric.SM4(
                cn.hutool.crypto.Mode.ECB,
                cn.hutool.crypto.Padding.PKCS5Padding,
                sm4Key.getBytes()
        );
        String encryptedData = cn.hutool.core.codec.Base64.encode(sm4.encrypt(content));

        // 3. 使用SM2WithSM3算法和verifyPrivateKey私钥对数据进行签名
        cn.hutool.crypto.asymmetric.SM2 sm2Sign = new cn.hutool.crypto.asymmetric.SM2(verifyPrivateKey, null);
        byte[] signBytes = sm2Sign.sign(content.getBytes("UTF-8"));
        String signatureData = cn.hutool.core.codec.Base64.encode(signBytes);

        // 4. 使用encryptPublicKey对SM4秘钥进行加密
        cn.hutool.crypto.asymmetric.SM2 sm2Encrypt = new cn.hutool.crypto.asymmetric.SM2(null, encryptPublicKey);
        byte[] encryptedKeyBytes = sm2Encrypt.encrypt(sm4Key.getBytes());
        String encryptedKey = cn.hutool.core.codec.Base64.encode(encryptedKeyBytes);

        // 5. 将结果放入map返回
        result.put("signatureData", signatureData);
        result.put("key", encryptedKey);
        result.put("requestData", encryptedData);
        return result;
    }
}
