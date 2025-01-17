package com.somle.esb.handler;

import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptSaveReqDTO;
import com.somle.eccang.model.EccangCategory;
import com.somle.eccang.model.EccangResponse;
import com.somle.eccang.service.EccangService;
import com.somle.esb.converter.ErpToEccangConverter;
import com.somle.esb.converter.ErpToKingdeeConverter;
import com.somle.esb.enums.TenantId;
import com.somle.kingdee.model.KingdeeAuxInfoDetail;
import com.somle.kingdee.service.KingdeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Objects;


/**
* @Description: $
* @Author: c-tao
* @Date: 2025/1/13$
*/
@Slf4j
@Component
public class DepartmentHandler {

    @Autowired
    ErpToEccangConverter erpToEccangConverter;

    @Autowired
    EccangService eccangService;

    @Autowired
    ErpToKingdeeConverter erpToKingdeeConverter;

    @Autowired
    KingdeeService kingdeeService;

    @ServiceActivator(inputChannel = "departmentOutputChannel")
    public void handle(@Payload DeptRespDTO department) {
        Long deptId = department.getId();
        //同步到易仓
        if (!Objects.equals(deptId, TenantId.DEFAULT.getId())){
            EccangCategory eccang = erpToEccangConverter.toEccang(String.valueOf(deptId));
            EccangResponse.EccangPage response = eccangService.addDepartment(eccang);
            log.info(response.toString());
        }
        //同步到金蝶
        KingdeeAuxInfoDetail kingdee = erpToKingdeeConverter.toKingdee(String.valueOf(deptId));
        kingdeeService.addDepartment(kingdee);
    }
}
