package com.example.provincialnode.common;

import cn.hutool.crypto.asymmetric.SM2;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.HashMap;
import java.util.Map;

/**
 * SM2密钥对生成工具类
 * 用于生成SM2算法的公私钥对
 * 使用Hutool库实现
 */
@Slf4j
public class SM2KeyGenerator {
    
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    
    /**
     * 生成SM2密钥对
     * @return 包含公私钥的Map，key分别为"publicKey"和"privateKey"
     */
    public static Map<String, String> generateKeyPair() {
        try {
            // 使用KeyPairGenerator生成EC密钥对
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
            keyPairGenerator.initialize(new ECGenParameterSpec("sm2p256v1"));
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            
            // 创建SM2实例，用于获取格式化的密钥
            SM2 sm2 = new SM2(keyPair.getPrivate(), keyPair.getPublic());
            
            // 获取格式化的十六进制密钥
            String privateKey = sm2.getPrivateKeyBase64();
            String publicKey = sm2.getPublicKeyBase64();
            
            Map<String, String> keyMap = new HashMap<>();
            keyMap.put("publicKey", publicKey);
            keyMap.put("privateKey", privateKey);
            return keyMap;
        } catch (Exception e) {
            log.error("生成SM2密钥对异常", e);
            throw new RuntimeException("生成SM2密钥对失败", e);
        }
    }


    /**
     * 生成指定数量的SM2密钥对
     * @param count 密钥对数量
     * @return 包含多个密钥对的Map，key为序号，value为包含公私钥的Map
     */
    public static Map<String, Map<String, String>> generateKeyPairs(int count) {
        Map<String, Map<String, String>> keyPairsMap = new HashMap<>();
        for (int i = 1; i <= count; i++) {
            Map<String, String> keyPair = generateKeyPair();
            keyPairsMap.put("keyPair" + i, keyPair);
        }
        return keyPairsMap;
    }

    /**
     * 主方法，用于测试密钥生成
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        Map<String, String> keyPair = generateKeyPair();
        System.out.println("SM2 Public Key:");
        System.out.println(keyPair.get("publicKey"));
        System.out.println("\nSM2 Private Key:");
        System.out.println(keyPair.get("privateKey"));
        
        System.out.println("\n--- 生成两套密钥对 ---");
        Map<String, Map<String, String>> keyPairs = generateKeyPairs(2);
        for (Map.Entry<String, Map<String, String>> entry : keyPairs.entrySet()) {
            System.out.println("\n" + entry.getKey() + ":");
            System.out.println("Public Key: " + entry.getValue().get("publicKey"));
            System.out.println("Private Key: " + entry.getValue().get("privateKey"));
        }
    }
}