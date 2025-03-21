package com.somle.esb.handler;

import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import com.somle.eccang.model.EccangCategory;
import com.somle.eccang.model.EccangResponse;
import com.somle.eccang.service.EccangService;
import com.somle.esb.converter.ErpToEccangConverter;
import com.somle.esb.converter.ErpToKingdeeConverter;
import com.somle.esb.enums.TenantId;
import com.somle.kingdee.model.KingdeeAuxInfoDetail;
import com.somle.kingdee.service.KingdeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static cn.iocoder.yudao.module.system.enums.esb.EsbChannels.DEPARTMENT_CHANNEL;


/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/1/13$
 */
@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class DepartmentHandler {

    private final ErpToEccangConverter erpToEccangConverter;
    private final EccangService eccangService;
    private final ErpToKingdeeConverter erpToKingdeeConverter;
    private final KingdeeService kingdeeService;

    @ServiceActivator(inputChannel = DEPARTMENT_CHANNEL)
    public void handle(@Payload DeptRespDTO department) {
        Long deptId = department.getId();
        //同步到易仓
        if (!Objects.equals(deptId, TenantId.DEFAULT.getId())) {
            EccangCategory eccang = erpToEccangConverter.toEccang(String.valueOf(deptId));
            EccangResponse.EccangPage response = eccangService.addDepartment(eccang);
            log.info(response.toString());
        }
        //同步到金蝶
        KingdeeAuxInfoDetail kingdee = erpToKingdeeConverter.toKingdee(String.valueOf(deptId));
        kingdeeService.addDepartment(kingdee);
    }
}
