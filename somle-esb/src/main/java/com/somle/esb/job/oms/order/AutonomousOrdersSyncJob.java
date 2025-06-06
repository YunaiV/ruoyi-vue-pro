package com.somle.esb.job.oms.order;

import cn.iocoder.yudao.module.oms.api.dto.OmsOrderSaveReqDTO;
import com.somle.autonomous.service.AutonomousService;
import com.somle.esb.converter.oms.AutonomousToOmsConverter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
public class AutonomousOrdersSyncJob extends BaseOrdersSyncJob {

    @Resource
    AutonomousService autonomousService;
    @Resource
    AutonomousToOmsConverter autonomousToOmsConverter;

    @Override
    public List<OmsOrderSaveReqDTO> listOrders(String param) {
        LocalDate baseDate = LocalDate.parse(param);
        // 2. 计算前两天的日期
        LocalDate targetDate = baseDate.minusDays(2);
        // 3. 生成起始时间（00:00:00）
        LocalDateTime startTime = targetDate.atStartOfDay();
        // 4. 生成结束时间（23:59:59）
        LocalDateTime endTime = targetDate.atTime(23, 59, 59);

        ZonedDateTime startTimeUTC = startTime.atZone(ZoneId.of("UTC"));
        ZonedDateTime endTimeUTC = endTime.atZone(ZoneId.of("UTC"));

        return autonomousService.autonomousClients.stream()
            .flatMap(client -> {
                try {
                    return autonomousToOmsConverter.toOrders(client.getAllOrder(startTimeUTC.format(DateTimeFormatter.ISO_INSTANT), endTimeUTC.format(DateTimeFormatter.ISO_INSTANT)), client.autonomousAccount).stream();
                } catch (Exception e) {
                    log.error("Failed to fetch orders for client", e);
                    return Stream.empty();
                }
            }).toList();
    }
}
