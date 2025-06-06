package com.somle.esb.job.oms.order;

import cn.iocoder.yudao.module.oms.api.dto.OmsOrderSaveReqDTO;
import com.somle.bestbuy.service.BestbuyService;
import com.somle.esb.converter.oms.BestbuyToOmsConverter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
public class BestbuyOrdersSyncJob extends BaseOrdersSyncJob {

    @Resource
    BestbuyService bestbuyService;
    @Resource
    BestbuyToOmsConverter bestbuyToOmsConverter;

    @Override
    public List<OmsOrderSaveReqDTO> listOrders(String param) {
        LocalDate baseDate = LocalDate.parse(param);
        // 2. 计算前两天的日期
        LocalDate targetDate = baseDate.minusDays(2);
        // 3. 生成起始时间（00:00:00）
        LocalDateTime startTime = targetDate.atStartOfDay();
        // 4. 生成结束时间（23:59:59）
        LocalDateTime endTime = targetDate.atTime(23, 59, 59);

        return bestbuyService.bestbuyClients.stream()
            .flatMap(client -> {
                try {
                    return bestbuyToOmsConverter.toOrders(client.getAllOrders(startTime, endTime), client).stream();
                } catch (Exception e) {
                    log.error("Failed to fetch orders for client", e);
                    return Stream.empty(); // 发生异常时返回空流
                }
            })
            .toList();
    }
}
