package com.example.provincialnode.init;

import com.example.provincialnode.common.CacheService;
import com.example.provincialnode.entity.SysAccessOrganizationEntity;
import com.example.provincialnode.entity.SysProcessNodeConfigEntity;
import com.example.provincialnode.service.SysAccessOrganizationService;
import com.example.provincialnode.service.SysProcessNodeConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 缓存初始化器
 * 在系统启动时加载接入机构和节点配置到缓存中
 */
@Slf4j
@Component
public class CacheInitializer implements CommandLineRunner {
    @Autowired
    private SysProcessNodeConfigService sysProcessNodeConfigService;
    @Autowired
    private SysAccessOrganizationService sysAccessOrganizationService;

    @Autowired
    private CacheService cacheService;

    // 缓存前缀
    private static final String CACHE_PREFIX_ACCESS_ORG = "access_org:";
    private static final String CACHE_PREFIX_NODE_CONFIG = "node_config:";

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化系统缓存...");
        //加载接入机构到缓存
        loadAccessOrganizations();
        
        //加载节点配置到缓存
        loadProcessNodeConfigs();
        
        log.info("系统缓存初始化完成");
    }

    /**
     * 加载接入机构到缓存
     */
    private void loadAccessOrganizations() {
        try {
            List<SysAccessOrganizationEntity> organizations = sysAccessOrganizationService.selectAll();
            log.info("加载接入机构数量: {}", organizations.size());
            for (SysAccessOrganizationEntity org : organizations) {
                // 按AppKey缓存
                if (org.getAppKey() != null) {
                    cacheService.set(CACHE_PREFIX_ACCESS_ORG + "app_key:" + org.getAppKey(), org);
                }
            }
            // 缓存所有启用的机构列表
            List<SysAccessOrganizationEntity> enabledOrgs = sysAccessOrganizationService.selectEnabled();
            cacheService.set(CACHE_PREFIX_ACCESS_ORG + "enabled_list", enabledOrgs);
            
        } catch (Exception e) {
            log.error("加载接入机构到缓存失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 加载节点配置到缓存
     */
    private void loadProcessNodeConfigs() {
        try {
            // 获取所有流程节点配置
            List<SysProcessNodeConfigEntity> allNodeConfigs = sysProcessNodeConfigService.selectAll();
            // 按流程编码分组
            Map<String, List<SysProcessNodeConfigEntity>> configsByProcessCode = allNodeConfigs.stream()
                    .collect(Collectors.groupingBy(SysProcessNodeConfigEntity::getProcessCode));
            
            for (Map.Entry<String, List<SysProcessNodeConfigEntity>> entry : configsByProcessCode.entrySet()) {
                String processCode = entry.getKey();
                List<SysProcessNodeConfigEntity> nodeConfigs = entry.getValue();
                // 缓存该流程的所有节点配置
                cacheService.set(CACHE_PREFIX_NODE_CONFIG + "all:" + processCode, nodeConfigs);
            }
            log.info("加载节点配置到缓存完成，流程数量: {}", configsByProcessCode.size());
        } catch (Exception e) {
            log.error("加载节点配置到缓存失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 刷新接入机构缓存
     * @param org 接入机构对象
     */
    public void refreshAccessOrganizationCache(SysAccessOrganizationEntity org) {
        if (org == null) {
            return;
        }
        
        try {
            cacheService.set(CACHE_PREFIX_ACCESS_ORG + "app_key:" + org.getAppKey(), org);
            // 刷新启用的机构列表缓存
            List<SysAccessOrganizationEntity> enabledOrgs = sysAccessOrganizationService.selectEnabled();
            cacheService.set(CACHE_PREFIX_ACCESS_ORG + "enabled_list", enabledOrgs);
            log.info("刷新接入机构缓存成功: {}", org.getOrgName());
        } catch (Exception e) {
            log.error("刷新接入机构缓存失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 刷新节点配置缓存
     * @param processCode 流程代码
     */
    public void refreshProcessNodeConfigCache(String processCode) {
        if (processCode == null || processCode.isEmpty()) {
            return;
        }
        
        try {
            // 获取该流程的所有节点配置
            List<SysProcessNodeConfigEntity> allConfigs = sysProcessNodeConfigService.getByProcessCode(processCode);
            cacheService.set(CACHE_PREFIX_NODE_CONFIG + "all:" + processCode, allConfigs);
            log.info("刷新节点配置缓存成功: {}", processCode);
        } catch (Exception e) {
            log.error("刷新节点配置缓存失败: {}", e.getMessage(), e);
        }
    }
}