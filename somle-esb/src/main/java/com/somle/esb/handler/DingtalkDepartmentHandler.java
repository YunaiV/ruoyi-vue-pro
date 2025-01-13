package com.somle.esb.handler;

import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserReqDTO;
import com.somle.dingtalk.service.DingTalkService;
import com.somle.eccang.model.EccangCategory;
import com.somle.eccang.model.EccangResponse;
import com.somle.eccang.service.EccangService;
import com.somle.esb.converter.DingTalkToErpConverter;
import com.somle.esb.converter.ErpToEccangConverter;
import com.somle.esb.converter.ErpToKingdeeConverter;
import com.somle.esb.enums.TenantId;
import com.somle.esb.service.EsbMappingService;
import com.somle.kingdee.model.KingdeeAuxInfoDetail;
import com.somle.kingdee.service.KingdeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;


/**
* @Description: $
* @Author: c-tao
* @Date: 2025/1/13$
*/
@Slf4j
@Component
public class DingtalkDepartmentHandler {

    @Autowired
    DingTalkService dingTalkService;

    @Autowired
    DingTalkToErpConverter dingTalkToErpConverter;

    @Autowired
    private DeptApi deptApi;

    @Autowired
    private EsbMappingService mappingService;

    @Autowired
    ErpToEccangConverter erpToEccangConverter;

    @Autowired
    EccangService eccangService;

    @Autowired
    ErpToKingdeeConverter erpToKingdeeConverter;

    @Autowired
    KingdeeService kingdeeService;

    public void syncDepartments() {
        dingTalkService.getDepartmentStream().forEach(dingTalkDepartment -> {
            log.info("begin syncing: " + dingTalkDepartment.toString());
            DeptDTO erpDepartment = dingTalkToErpConverter.toErp(dingTalkDepartment);
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
            //同步到易仓
            if (!Objects.equals(deptId, TenantId.DEFAULT.getId())){
                EccangCategory eccang = erpToEccangConverter.toEccang(String.valueOf(deptId));
                EccangResponse.EccangPage response = eccangService.addDepartment(eccang);
                log.info(response.toString());
            }
            //同步到金蝶
            KingdeeAuxInfoDetail kingdee = erpToKingdeeConverter.toKingdee(String.valueOf(deptId));
            kingdeeService.addDepartment(kingdee);
        });
    }
}
