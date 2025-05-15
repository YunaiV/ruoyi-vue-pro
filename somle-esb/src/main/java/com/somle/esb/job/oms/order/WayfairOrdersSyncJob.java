package com.somle.esb.job.oms.order;

import cn.iocoder.yudao.module.oms.api.dto.OmsOrderSaveReqDTO;
import com.somle.esb.converter.oms.WayfairToOmsConverter;
import com.somle.wayfair.service.WayfairService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
public class WayfairOrdersSyncJob extends BaseOrdersSyncJob {

    @Resource
    WayfairService wayfairService;

    @Resource
    WayfairToOmsConverter wayfairToOmsConverter;

    @Override
    public List<OmsOrderSaveReqDTO> listOrders(String param) {
        LocalDate baseDate = LocalDate.parse(param);
        // 2. 计算前两天的日期
        LocalDate targetDate = baseDate.minusDays(2);
        // 3. 生成起始时间（00:00:00）
        LocalDateTime startTime = targetDate.atStartOfDay();

        return wayfairService.clients.stream()
            .flatMap(client -> {
                try {
                    // 假设 getOrders() 返回 List<Order>，toOrders() 转换为 List<OmsOrderSaveReqDTO>
                    return wayfairToOmsConverter.toOrders(client.getOrders(startTime), client.getToken()).stream();
                } catch (Exception e) {
                    log.error("Failed to fetch orders for client", e);
                    return Stream.empty(); // 发生异常时返回空流
                }
            })
            .toList();
    }
}
