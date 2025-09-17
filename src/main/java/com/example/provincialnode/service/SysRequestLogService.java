package com.example.provincialnode.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.provincialnode.entity.SysRequestLogEntity;
import com.example.provincialnode.mapper.SysRequestLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 授权书服务类
 * 处理授权书相关的业务逻辑
 */
@Slf4j
@Service
public class SysRequestLogService extends ServiceImpl<SysRequestLogMapper, SysRequestLogEntity> {
}