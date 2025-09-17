package com.example.provincialnode.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

/**
 * 应用程序配置类
 * 配置项目中需要的一些组件
 */
@Configuration
public class AppConfig {

    /**
     * 配置BCrypt密码编码器
     * 用于用户密码加密和验证
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置RestTemplate
     * 用于发送HTTP请求，特别是调用全国节点服务
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}