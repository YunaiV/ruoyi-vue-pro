package com.somle.esb.job;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import com.somle.esb.service.EsbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @className: SyncDepartmentsToKingdeeJob
 * @author: Wqh
 * @date: 2024/11/4 11:21
 * @Version: 1.0
 * @description:
 */
@Slf4j
@Component
public class SyncDepartmentsToKingdeeJob extends DataJob{
    @Autowired
    EsbService service;


    @Override
    //由于目前租户只有一个，目前忽略租户，后续新增租户后进行修改
    @TenantIgnore
    public String execute(String param) throws Exception {
        service.syncKingDeeDepartments();
        return "sync success";
    }
}
