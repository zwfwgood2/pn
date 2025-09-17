package com.example.provincialnode.controller;

import com.example.provincialnode.common.CacheService;
import com.example.provincialnode.common.Result;
import com.example.provincialnode.common.ResultCode;
import com.example.provincialnode.config.NationalNodeConfig;
import com.example.provincialnode.entity.SysAccessOrganizationEntity;
import com.example.provincialnode.init.CacheInitializer;
import com.example.provincialnode.service.SysAccessOrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器
 * 处理获取token、修改密码等认证相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private SysAccessOrganizationService sysAccessOrganizationService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private CacheInitializer cacheInitializer;

    @Autowired
    private NationalNodeConfig nationalNodeConfig;

    /**
     * 获取访问令牌
     * 用于市级节点获取访问令牌
     */
    @PostMapping("/token")
    public Result<?> getToken(@RequestBody Map<String, Object> requestParams) {
        log.info("获取访问令牌请求");
        try {
            // 1. 获取请求参数
            String appKey = (String) requestParams.get("appKey");
            String username = (String) requestParams.get("username");
            String password = (String) requestParams.get("password");
            
            // 2. 参数校验
            if (appKey == null || username == null || password == null) {
                log.error("获取令牌参数不完整");
                return Result.error(ResultCode.PARAM_ERROR.getCode(), "参数不完整");
            }
            
            // 3. 验证接入机构信息
            SysAccessOrganizationEntity org = sysAccessOrganizationService.selectByAppKey(appKey);
            if (org == null) {
                log.error("无效的AppKey: {}", appKey);
                throw new RuntimeException("无效的AppKey: " + appKey);
            }
            
            // 4. 验证机构状态
            if (org.getStatus() != 1) {
                log.error("机构已禁用: {}", org.getOrgName());
                throw new RuntimeException("机构已禁用");
            }
            
            // 5. 验证用户名密码
            if (!org.getUsername().equals(username) || !passwordEncoder.matches(password, org.getPassword())) {
                log.error("用户名或密码错误: {}", username);
                throw new RuntimeException("用户名或密码错误");
            }
            
            // 6. 生成令牌
            String token = generateToken(org.getId(), appKey);
            
            // 7. 将令牌存入Redis
            String tokenKey = "node:token:" + token;
            cacheService.set(tokenKey, appKey, nationalNodeConfig.getTokenExpireTime(), TimeUnit.SECONDS);
            
            // 8. 更新最后登录时间
            //sysAccessOrganizationService.updateLastLoginTime(org.getId(), new Date());

            // 10. 构建响应数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("token", token);
            resultData.put("expireTime", System.currentTimeMillis() + nationalNodeConfig.getTokenExpireTime() * 1000);
            resultData.put("userId", org.getId());
            resultData.put("username", org.getUsername());
            resultData.put("realName", org.getContactPerson());
            resultData.put("orgName", org.getOrgName());
            resultData.put("structureType", org.getStructureType());
            
            log.info("获取令牌成功: {}, appKey: {}", username, appKey);
            return Result.success(resultData);
        } catch (RuntimeException e) {
            log.error("获取令牌失败: {}", e.getMessage());
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("获取令牌异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR.getCode(), "系统内部错误");
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/changePassword")
    public Result<?> changePassword(@RequestBody Map<String, Object> requestParams) {
        log.info("修改密码请求");
        
        try {
            // 1. 获取请求参数
            String username = (String) requestParams.get("username");
            String oldPassword = (String) requestParams.get("oldPassword");
            String newPassword = (String) requestParams.get("newPassword");
            
            // 2. 参数校验
            if (username == null || oldPassword == null || newPassword == null) {
                log.error("修改密码参数不完整");
                return Result.error(ResultCode.PARAM_ERROR.getCode(), "参数不完整");
            }
            
            // 3. 验证用户信息
//            AccessOrganization user = sysAccessOrganizationService.selectByUsername(username);
//            if (user == null) {
//                throw new RuntimeException("用户名不存在: " + username);
//            }
//
//            // 4. 验证旧密码
//            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
//                log.error("旧密码错误: {}", username);
//                return Result.error(ResultCode.PARAM_ERROR.getCode(), "旧密码错误");
//            }
//
//            // 5. 密码强度校验
//            if (!isPasswordStrong(newPassword)) {
//                log.error("新密码强度不足: {}", username);
//                return Result.error(ResultCode.PARAM_ERROR.getCode(), "新密码至少包含8位，且包含字母和数字");
//            }
//
//            // 6. 更新密码
//            user.setPassword(passwordEncoder.encode(newPassword));
//            user.setUpdateTime(new Date());
//            accessOrganizationMapper.update(user);
//
//            // 7. 刷新缓存
//            cacheInitializer.refreshAccessOrganizationCache(accessOrganizationMapper.selectById(user.getId()));
            
            log.info("修改密码成功: {}", username);
            return Result.success("修改密码成功");
        } catch (RuntimeException e) {
            log.error("修改密码失败: {}", e.getMessage());
            return Result.error(ResultCode.OPERATION_FAILED.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("修改密码异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR.getCode(), "系统内部错误");
        }
    }

    /**
     * 获取公钥认证接口
     * 用于市级节点获取省级节点的公钥，或获取其他机构的公钥
     */
    @GetMapping("/publicKey")
    public Result<?> getPublicKey(@RequestParam(required = false) String appKey) {
        log.info("获取公钥请求, appKey: {}", appKey);
        
        try {
            if (appKey != null) {
//                // 获取指定AppKey的公钥
//                AccessOrganization org = accessOrganizationMapper.selectByAppKey(appKey);
//                if (org == null) {
//                    throw new RuntimeException("无效的AppKey: " + appKey);
//                }
//
//                Map<String, Object> resultData = new HashMap<>();
//                resultData.put("appKey", org.getAppKey());
//                resultData.put("orgName", org.getOrgName());
//                resultData.put("publicKey", org.getPublicKey());
//                resultData.put("structureType", org.getStructureType());
//
//                log.info("获取指定AppKey公钥成功: {}", appKey);
//                return Result.success(resultData);
            } else {
                // 获取省级节点的公钥（这里简化实现，实际应从安全的存储中获取）
                Map<String, Object> resultData = new HashMap<>();
                resultData.put("appKey", "provincial_node");
                resultData.put("orgName", "省级节点");
                //resultData.put("publicKey", getProvincialPublicKey());
                
                log.info("获取省级节点公钥成功");
                return Result.success(resultData);
            }
        } catch (RuntimeException e) {
            log.error("获取公钥失败: {}", e.getMessage());
            return Result.error(ResultCode.PARAM_ERROR.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("获取公钥异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR.getCode(), "系统内部错误");
        }
        return Result.error(ResultCode.SYSTEM_ERROR.getCode(), "系统内部错误");
    }

    /**
     * 生成访问令牌
     * 简化实现，实际项目中应使用JWT等标准方案
     */
    private String generateToken(Long userId, String appKey) {
        // 实际项目中应使用JWT或OAuth2等标准方案生成令牌
        return UUID.randomUUID().toString() + "_" + userId + "_" + appKey;
    }

}