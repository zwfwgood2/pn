package com.example.provincialnode.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.provincialnode.entity.SysProcessDefinitionEntity;
import com.example.provincialnode.mapper.SysProcessDefinitionMapper;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 流程定义服务类
 * 处理流程定义相关的业务逻辑
 */
@Service
public class SysProcessDefinitionService extends ServiceImpl<SysProcessDefinitionMapper, SysProcessDefinitionEntity> {

    /**
     * 根据流程代码查询流程定义
     * @param processCode 流程代码
     * @return 流程定义
     */
    public SysProcessDefinitionEntity getByProcessCode(String processCode) {
        return getOne(new LambdaQueryWrapper<SysProcessDefinitionEntity>()
                .eq(SysProcessDefinitionEntity::getProcessCode, processCode));
    }

    /**
     * 根据接口代码查询流程定义
     * @param interfaceCode 接口代码
     * @return 流程定义
     */
    public SysProcessDefinitionEntity getByInterfaceCode(String interfaceCode) {
        return getOne(new LambdaQueryWrapper<SysProcessDefinitionEntity>()
                .eq(SysProcessDefinitionEntity::getInterfaceCode, interfaceCode));
    }

    /**
     * 查询所有启用的流程定义
     * @return 流程定义列表
     */
    public List<SysProcessDefinitionEntity> getAllEnabledProcessDefinitions() {
        return list(new LambdaQueryWrapper<SysProcessDefinitionEntity>()
                .eq(SysProcessDefinitionEntity::getStatus, 1));
    }

    /**
     * 保存流程定义
     * @param processDefinition 流程定义
     * @return 保存后的流程定义
     */
    public boolean save(SysProcessDefinitionEntity processDefinition) {
        super.save(processDefinition);
        return true;
    }

    /**
     * 启用/禁用流程定义
     * @param processCode 流程代码
     * @param status 状态
     * @return 更新后的流程定义
     */
    public SysProcessDefinitionEntity updateStatus(String processCode, Integer status) {
        SysProcessDefinitionEntity processDefinition = getByProcessCode(processCode);
        if (processDefinition == null) {
            throw new RuntimeException("流程定义不存在: " + processCode);
        }
        processDefinition.setStatus(status);
        updateById(processDefinition);
        return processDefinition;
    }

}