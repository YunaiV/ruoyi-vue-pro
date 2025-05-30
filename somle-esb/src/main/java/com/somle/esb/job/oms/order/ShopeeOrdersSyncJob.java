package com.somle.esb.job.oms.order;

import cn.iocoder.yudao.module.oms.api.dto.OmsOrderSaveReqDTO;
import com.somle.esb.converter.oms.ShopeeToOmsConverter;
import com.somle.shopee.service.ShopeeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
public class ShopeeOrdersSyncJob extends BaseOrdersSyncJob {
    @Resource
    private ShopeeService shopeeService;

    @Resource
    private ShopeeToOmsConverter shopeeToOmsConverter;

    @Override
    public List<OmsOrderSaveReqDTO> listOrders(String param) {

        LocalDate baseDate = LocalDate.parse(param);
        // 2. 计算前两天的日期
        LocalDate targetDate = baseDate.minusDays(2);
        // 3. 生成起始时间（00:00:00）
        LocalDateTime startTime = targetDate.atStartOfDay();
        // 4. 生成结束时间（23:59:59）
        LocalDateTime endTime = targetDate.atTime(23, 59, 59);

        // 获取秒级时间戳
        long startTimestamp = startTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().getEpochSecond();
        long endTimestamp = endTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().getEpochSecond();

        return shopeeService.clients.stream()
            .map(client -> {
                return shopeeToOmsConverter.toOrders(client.getAllOrders(startTimestamp, endTimestamp), client.getAccount());
            })
            .flatMap(List::stream)
            .toList();
    }
}
