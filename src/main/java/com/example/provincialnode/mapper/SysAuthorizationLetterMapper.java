package com.example.provincialnode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.provincialnode.entity.SysAuthorizationLetterEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 授权记录数据访问接口
 */
@Mapper
public interface SysAuthorizationLetterMapper extends BaseMapper<SysAuthorizationLetterEntity> {
}