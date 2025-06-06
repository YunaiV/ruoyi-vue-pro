package com.somle.esb.job.oms.order;

import cn.iocoder.yudao.module.oms.api.dto.OmsOrderSaveReqDTO;
import com.somle.esb.converter.oms.OttoToOmsConverter;
import com.somle.otto.service.OttoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OttoOrdersSyncJob extends BaseOrdersSyncJob {


    @Resource
    OttoService ottoService;

    @Resource
    OttoToOmsConverter ottoToOmsConverter;

    @Override
    public List<OmsOrderSaveReqDTO> listOrders(String param) {
        LocalDate baseDate = LocalDate.parse(param);
        // 2. 计算前两天的日期
        LocalDate targetDate = baseDate.minusDays(2);
        // 3. 生成起始时间（00:00:00）
        LocalDateTime startTime = targetDate.atStartOfDay();
        // 4. 生成结束时间（23:59:59）
        LocalDateTime endTime = targetDate.atTime(23, 59, 59);


        return ottoService.ottoClients.stream()
            .flatMap(ottoClient -> {
                return ottoToOmsConverter.toOrders(ottoClient.getAllOrders(startTime.toString(), endTime.toString()), ottoClient.ottoAccount).stream();
            }).toList();
    }
}
