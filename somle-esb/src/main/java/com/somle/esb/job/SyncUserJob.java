package com.somle.esb.job;


import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import com.somle.esb.service.EsbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SyncUserJob extends DataJob {
    @Autowired
    EsbService service;


    @Override
    @TenantIgnore
    public String execute(String param) throws Exception {
        service.syncUsers();
        return "sync users success";
    }
}