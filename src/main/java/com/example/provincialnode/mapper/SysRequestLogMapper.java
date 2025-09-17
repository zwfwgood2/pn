package com.example.provincialnode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.provincialnode.entity.SysRequestLogEntity;
import org.apache.ibatis.annotations.Mapper;
/**
 * 请求日志数据访问接口
 */
@Mapper
public interface SysRequestLogMapper extends BaseMapper<SysRequestLogEntity> {
}