package com.example.provincialnode.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.provincialnode.entity.SysProcessNodeConfigEntity;
import com.example.provincialnode.mapper.SysProcessNodeConfigMapper;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * 流程节点配置服务类
 * 处理流程节点配置相关的业务逻辑
 */
@Service
public class SysProcessNodeConfigService extends ServiceImpl<SysProcessNodeConfigMapper, SysProcessNodeConfigEntity> {
    /**
     * 根据流程代码查询节点配置列表
     * @param processCode 流程代码
     * @return 节点配置列表
     */
    public List<SysProcessNodeConfigEntity> getByProcessCode(String processCode) {
        return list(new LambdaQueryWrapper<SysProcessNodeConfigEntity>()
                .eq(SysProcessNodeConfigEntity::getProcessCode, processCode));
    }

    /**
     * 根据流程代码和节点ID查询节点配置
     * @param processCode 流程代码
     * @param nodeId 节点ID
     * @return 节点配置
     */
    public SysProcessNodeConfigEntity getByProcessCodeAndNodeId(String processCode, String nodeId) {
        return getOne(new LambdaQueryWrapper<SysProcessNodeConfigEntity>()
                .eq(SysProcessNodeConfigEntity::getProcessCode, processCode)
                .eq(SysProcessNodeConfigEntity::getNodeId, nodeId));
    }

    /**
     * 查询流程中启用的节点配置列表（按顺序）
     * @param processCode 流程代码
     * @return 节点配置列表
     */
    public List<SysProcessNodeConfigEntity> getEnabledNodesByProcessCode(String processCode) {
        return list(new LambdaQueryWrapper<SysProcessNodeConfigEntity>()
                .eq(SysProcessNodeConfigEntity::getProcessCode, processCode)
                .eq(SysProcessNodeConfigEntity::getStatus, 1)
                .orderByAsc(SysProcessNodeConfigEntity::getNodeOrder));
    }

    /**
     * 保存流程节点配置
     * @param processNodeConfig 流程节点配置
     * @return 保存后的流程节点配置
     */
    public boolean save(SysProcessNodeConfigEntity processNodeConfig) {
        return super.save(processNodeConfig);
    }

    /**
     * 批量保存流程节点配置
     * @param processNodeConfigs 流程节点配置列表
     * @return 保存后的流程节点配置列表
     */
    public List<SysProcessNodeConfigEntity> saveAll(List<SysProcessNodeConfigEntity> processNodeConfigs) {
        if (processNodeConfigs == null || processNodeConfigs.isEmpty()) {
            return new ArrayList<>();
        }
        saveBatch(processNodeConfigs);
        return processNodeConfigs;
    }

    public List<SysProcessNodeConfigEntity> selectAll() {
        return list();
    }
}