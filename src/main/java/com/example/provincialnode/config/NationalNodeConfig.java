package com.example.provincialnode.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 全国节点配置类
 * 存储全国节点的连接信息、令牌和公钥
 */
@Setter
@Getter
@Component
public class NationalNodeConfig {

    // 获取全国节点地址
    // 全国节点地址
    @Value("${provincial.node.national-node-url}")
    private String nationalNodeUrl;

    // 获取连接超时时间
    // 全国节点连接超时时间
    @Value("${provincial.node.connect-timeout}")
    private int connectTimeout;

    // 获取读取超时时间
    // 全国节点读取超时时间
    @Value("${provincial.node.read-timeout}")
    private int readTimeout;

    // 获取全国节点公钥
    // 全国节点公钥
    private volatile String publicKey;

    // 获取全国节点令牌
    // 全国节点令牌
    private volatile String token;

    // 获取令牌过期时间
    // 令牌过期时间（秒）
    @Value("${provincial.node.token.expire-time}")
    private int tokenExpireTime;

    // 获取令牌刷新阈值
    // 令牌刷新阈值（秒）
    @Value("${provincial.node.token.refresh-threshold}")
    private int tokenRefreshThreshold;
}