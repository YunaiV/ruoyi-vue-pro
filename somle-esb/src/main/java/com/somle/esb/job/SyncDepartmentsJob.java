package com.somle.esb.job;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import com.somle.esb.enums.TenantId;
import com.somle.esb.handler.DingtalkDepartmentHandler;
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
    @Autowired
    DingtalkDepartmentHandler dingtalkDepartmentHandler;


    @Override
    public String execute(String param) throws Exception {
        try {
            TenantContextHolder.setTenantId(TenantId.DEFAULT.getId());
            dingtalkDepartmentHandler.syncDepartments();
            return "sync success";
        } finally {
            TenantContextHolder.clear();
        }
    }
}
