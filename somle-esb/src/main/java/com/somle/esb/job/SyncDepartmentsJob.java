package com.somle.esb.job;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import com.somle.esb.service.EsbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @className: syncDepartmentsJob
 * @author: Wqh
 * @date: 2024/10/29 17:24
 * @Version: 1.0
 * @description:
 */
@Slf4j
@Component
public class SyncDepartmentsJob extends DataJob{
    //默认租户id
    public static final Long TENANT_ID_DEFAULT = 50001L;
    @Autowired
    EsbService service;


    @Override
    public String execute(String param) throws Exception {
        try {
            TenantContextHolder.setTenantId(TENANT_ID_DEFAULT);
            service.syncDepartments();
            return "sync success";
        } finally {
            TenantContextHolder.clear();
        }
    }
}
