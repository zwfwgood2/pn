package com.example.provincialnode.common;

import cn.hutool.core.codec.*;
import cn.hutool.crypto.asymmetric.*;
import cn.hutool.crypto.symmetric.SM4;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.HashMap;
import java.util.Map;
@Slf4j
public class SignUtil {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 验证签名
     * @param signature         签名内容
     * @param verifyPublicKey   验签公钥
     * @param decryptPrivateKey 解密私钥
     * @param encryptedData     加密数据
     * @return 原文JSON字符串
     */
    public static String verifySignature(Map<String,Object> signature,String encryptedData, String verifyPublicKey, String decryptPrivateKey) throws Exception {
        //实现需求：使用decryptPrivateKey 解密key 得到SM4的秘钥，使用秘钥解密requestData的到原文， 将原文使用SM2WithSM3算法和verifyPublicKey对数据进行验签，签名值为signatureData，
        //返回原文值JSON字符串， signature这个map中的数据分别为signatureData 签名数据、key SM4加密秘钥、requestData 加密数据 放入map返回
        // 1. 从参数中获取签名、加密的SM4密钥和加密的数据
        String signatureData = (String) signature.get("signatureData");
        String encryptedKey = (String) signature.get("key");
        // 2. 使用decryptPrivateKey解密key得到SM4的秘钥
        SM2 sm2Decrypt = new SM2(decryptPrivateKey, null);
        byte[] decryptedKeyBytes = sm2Decrypt.decrypt(Base64.decode(encryptedKey));
        String sm4Key = new String(decryptedKeyBytes, "UTF-8");

        // 3. 使用SM4秘钥解密requestData得到原文
        SM4 sm4 = new SM4(
                cn.hutool.crypto.Mode.ECB,
                cn.hutool.crypto.Padding.PKCS5Padding,
                sm4Key.getBytes()
        );
        byte[] decryptedDataBytes = sm4.decrypt(Base64.decode(encryptedData));
        String originalData = new String(decryptedDataBytes, "UTF-8");

        // 4. 使用SM2WithSM3算法和verifyPublicKey对原文进行验签
        SM2 sm2Sign = new SM2(null, verifyPublicKey);
        boolean verify = sm2Sign.verify(originalData.getBytes("UTF-8"),Base64.decode(signatureData));
        // 5. 验证签名是否匹配
        if (!verify) {
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
     * @param signPrivateKey  签名私钥字符串
     * @return signatureData 签名数据、key SM4加密秘钥、requestData 加密数据
     */
    public static Map<String,Object> signData(String content,String signPrivateKey,String encryptPublicKey) throws Exception {
        //实现需求：使用SM2WithSM3算法和signPrivateKey 私钥对数据进行签名，使用SM4 对数据进行加密，随机生成16位随机码作为SM4秘钥，将秘钥使用encryptPublicKey 进行加密；
        //返回signatureData 签名数据、key SM4加密秘钥、requestData 加密数据 放入map返回
        Map<String, Object> result = new HashMap<>();

        // 1. 生成16位随机码作为SM4秘钥
        String sm4Key = cn.hutool.core.util.RandomUtil.randomString(16);

        // 2. 使用SM4对数据进行加密
        SM4 sm4 = new SM4(
                cn.hutool.crypto.Mode.ECB,
                cn.hutool.crypto.Padding.PKCS5Padding,
                sm4Key.getBytes()
        );
        String encryptedData = Base64.encode(sm4.encrypt(content));

        // 3. 使用SM2WithSM3算法和verifyPrivateKey私钥对数据进行签名
        SM2 sm2Sign = new SM2(signPrivateKey, null);
        byte[] signBytes = sm2Sign.sign(content.getBytes("UTF-8"));
        String signatureData = Base64.encode(signBytes);

        // 4. 使用encryptPublicKey对SM4秘钥进行加密
        SM2 sm2Encrypt = new SM2(null, encryptPublicKey);
        byte[] encryptedKeyBytes = sm2Encrypt.encrypt(sm4Key.getBytes());
        String encryptedKey = Base64.encode(encryptedKeyBytes);

        // 5. 将结果放入map返回
        result.put("signatureData", signatureData);
        result.put("key", encryptedKey);
        result.put("encryptedData", encryptedData);
        return result;
    }

    public static void main(String[] args) throws Exception {
        String data="zhangsan";
        String privateKey="MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgKc2O0VKMKis8BXs7qoyG9rI5ZEhQDJ+EI6dkccdvkMOgCgYIKoEcz1UBgi2hRANCAAQJa0ePyHLISNNjcQIT2XyLZPoBCEQ6y4Piqp/ZQjh7FUfmtsgEnKuDZ113Jj5/4UYGt0C4th5GKYVj1aoW2BQW";
        String publicKey="MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEMFoUMX2vVVOhrG8tEMA4W82MZ5o0fbNnnG6SSNmT/saVGWX/bJUqKkBqsCjo0h4SXqP4iXk8R1FGO3VXd6+q4w==";
        Map<String, Object> stringStringMap = signData(data, privateKey, publicKey);
        log.info("签名结果：{}", stringStringMap);
        String vpublicKey="MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAECWtHj8hyyEjTY3ECE9l8i2T6AQhEOsuD4qqf2UI4exVH5rbIBJyrg2dddyY+f+FGBrdAuLYeRimFY9WqFtgUFg==";
        String vPrivateKey="MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgmaJlZwRi8z/EQYwz18RC/aQ4x+SCdB1e+LvQQgl+wK+gCgYIKoEcz1UBgi2hRANCAAQwWhQxfa9VU6Gsby0QwDhbzYxnmjR9s2ecbpJI2ZP+xpUZZf9slSoqQGqwKOjSHhJeo/iJeTxHUUY7dVd3r6rj";
        String s = verifySignature(stringStringMap, stringStringMap.get("encryptedData").toString(), vpublicKey, vPrivateKey);
        log.info("验签结果：{}", s);
        SM2 sm2 = new SM2(privateKey, vpublicKey);
        byte[] signData = sm2.sign(data.getBytes());
        log.info("验签结果：{}", sm2.verify(data.getBytes(), signData));
    }
}
