package com.somle.esb.handler;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptSaveReqDTO;
import com.somle.dingtalk.model.DingTalkDepartment;
import com.somle.esb.converter.DingTalkToErpConverter;
import com.somle.esb.enums.TenantId;
import com.somle.esb.service.EsbMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


/**
* @Description: $
* @Author: c-tao
* @Date: 2025/1/13$
*/
@Slf4j
@Component
public class DingtalkDepartmentHandler {

    @Autowired
    DingTalkToErpConverter dingTalkToErpConverter;

    @Autowired
    private DeptApi deptApi;

    @Autowired
    private EsbMappingService mappingService;

    @ServiceActivator(inputChannel = "dingtalkDepartmentOutputChannel")
    public void handle(@Payload DingTalkDepartment dingTalkDepartment) {
        log.info("begin syncing: " + dingTalkDepartment.toString());
        // 同步到系统
        try {
            TenantContextHolder.setTenantId(TenantId.DEFAULT.getId());
            DeptSaveReqDTO erpDepartment = dingTalkToErpConverter.toErp(dingTalkDepartment);
            log.info("dept to add " + erpDepartment);
            Long deptId = erpDepartment.getId();
            if (deptId != null) {
                deptApi.updateDept(erpDepartment);
            } else {
                deptId = deptApi.createDept(erpDepartment);
                var mapping = mappingService.toMapping(dingTalkDepartment);
                mapping
                    .setInternalId(deptId);
                mappingService.save(mapping);
            }
        } finally {
            TenantContextHolder.clear();
        }
    }
}
