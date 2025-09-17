package com.example.provincialnode.service;

import com.example.provincialnode.config.NationalNodeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

/**
 * 全国节点服务类
 * 提供与全国节点交互的方法，包括获取公钥、刷新token、更新用户密码等功能
 */
@Slf4j
@Service
public class NationalNodeService{
    @Autowired
    private NationalNodeConfig nationalNodeConfig;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 获取全国节点公钥
     * 从全国节点服务获取最新的公钥并更新配置类
     */
    public void fetchPublicKey() {
        try {
            log.info("开始获取全国节点公钥");
            // 构建请求URL
            String url = nationalNodeConfig.getNationalNodeUrl() + "/public-key";
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 创建请求实体
            HttpEntity<?> requestEntity = new HttpEntity<>(headers);
            
            // 发送请求
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, Map.class);
            
            // 处理响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String publicKey = (String) response.getBody().get("publicKey");
                if (publicKey != null && !publicKey.isEmpty()) {
                    nationalNodeConfig.setPublicKey(publicKey);
                    log.info("全国节点公钥获取成功并更新配置");
                } else {
                    log.warn("获取到的全国节点公钥为空");
                }
            } else {
                log.error("获取全国节点公钥失败，状态码: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("获取全国节点公钥发生异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 刷新全国节点token
     * 从全国节点服务获取最新的token并更新配置类
     */
    public void refreshToken() {
        try {
            log.info("开始刷新全国节点token");
            // 构建请求URL
            String url = nationalNodeConfig.getNationalNodeUrl() + "/token/refresh";
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 创建请求实体
            HttpEntity<?> requestEntity = new HttpEntity<>(headers);
            
            // 发送请求
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, Map.class);
            
            // 处理响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String token = (String) response.getBody().get("token");
                if (token != null && !token.isEmpty()) {
                    nationalNodeConfig.setToken(token);
                    log.info("全国节点token刷新成功并更新配置");
                } else {
                    log.warn("获取到的全国节点token为空");
                }
            } else {
                log.error("刷新全国节点token失败，状态码: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("刷新全国节点token发生异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 更新用户密码
     * @param username 用户名
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否更新成功
     */
    public boolean updateUserPassword(String username, String oldPassword, String newPassword) {
        try {
            log.info("开始更新用户密码，用户名: {}", username);
            
            // 构建请求URL
            String url = nationalNodeConfig.getNationalNodeUrl() + "/user/password/update";
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(nationalNodeConfig.getToken());
            
            // 构建请求参数
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("username", username);
            requestBody.put("oldPassword", oldPassword);
            requestBody.put("newPassword", newPassword);
            
            // 创建请求实体
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // 发送请求
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, Map.class);
            
            // 处理响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                boolean success = (boolean) response.getBody().get("success");
                if (success) {
                    log.info("用户密码更新成功，用户名: {}", username);
                    // 更新密码后刷新token
                    refreshToken();
                    return true;
                } else {
                    log.warn("用户密码更新失败，用户名: {}", username);
                    return false;
                }
            } else {
                log.error("用户密码更新失败，状态码: {}, 用户名: {}", response.getStatusCode(), username);
                return false;
            }
        } catch (Exception e) {
            log.error("更新用户密码发生异常，用户名: {}, 异常信息: {}", username, e.getMessage(), e);
            return false;
        }
    }
}