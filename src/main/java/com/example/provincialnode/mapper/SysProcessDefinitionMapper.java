package com.example.provincialnode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.provincialnode.entity.SysProcessDefinitionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程定义数据访问接口
 */
@Mapper
public interface SysProcessDefinitionMapper extends BaseMapper<SysProcessDefinitionEntity> {
}