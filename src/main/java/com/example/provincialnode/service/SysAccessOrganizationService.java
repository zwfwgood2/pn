package com.example.provincialnode.service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.provincialnode.entity.SysAccessOrganizationEntity;
import com.example.provincialnode.mapper.SysAccessOrganizationMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysAccessOrganizationService extends ServiceImpl<SysAccessOrganizationMapper, SysAccessOrganizationEntity> {
    public SysAccessOrganizationEntity selectByAppKey(String appKey) {
        return this.getOne(new QueryWrapper<SysAccessOrganizationEntity>().eq("app_key", appKey));
    }

    public List<SysAccessOrganizationEntity> selectAll() {
        return this.list();
    }

    public List<SysAccessOrganizationEntity> selectEnabled() {
        return this.list(new QueryWrapper<SysAccessOrganizationEntity>().eq("status", 1));
    }
}
