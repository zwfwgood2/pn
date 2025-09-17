package com.example.provincialnode.service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.provincialnode.entity.SysAccessOrganizationEntity;
import com.example.provincialnode.entity.SysInterfaceDefinitionEntity;
import com.example.provincialnode.mapper.SysAccessOrganizationMapper;
import com.example.provincialnode.mapper.SysInterfaceDefinitionMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SysInterfaceDefinitionService extends ServiceImpl<SysInterfaceDefinitionMapper, SysInterfaceDefinitionEntity> {

    public SysInterfaceDefinitionEntity findByInterfaceCode(String interfaceCode) {
        return this.getOne(new QueryWrapper<SysInterfaceDefinitionEntity>().eq("interface_code", interfaceCode));
    }

    public  SysInterfaceDefinitionEntity findByRequestPath(String interfacePath) {
        return getOne(new QueryWrapper<SysInterfaceDefinitionEntity>().eq("request_path", interfacePath));
    }
}
