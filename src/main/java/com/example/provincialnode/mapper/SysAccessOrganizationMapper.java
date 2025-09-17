package com.example.provincialnode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.provincialnode.entity.SysAccessOrganizationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 接入机构数据访问接口
 */
@Mapper
public interface SysAccessOrganizationMapper extends BaseMapper<SysAccessOrganizationEntity> {
}