package com.somle.esb.job.oms.order;

import cn.iocoder.yudao.module.oms.api.dto.OmsOrderSaveReqDTO;
import com.somle.esb.converter.oms.WalmartToOmsConverter;
import com.somle.walmart.model.req.WalmartOrderReqVO;
import com.somle.walmart.service.WalmartService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class WalmartOrdersSyncJob extends BaseOrdersSyncJob {

    @Resource
    private WalmartService walmartService;

    @Resource
    private WalmartToOmsConverter walmartMarketplaceToOmsConverter;

    @Override
    public List<OmsOrderSaveReqDTO> listOrders(String param) {
        return walmartService.walmartClients.stream()
            .filter(client -> client.token.getSvcName().equals("Walmart Marketplace"))
            .flatMap(client -> {
                LocalDate baseDate = LocalDate.parse(param);
                // 2. 计算前两天的日期
                LocalDate targetDate = baseDate.minusDays(2);
                // 3. 生成起始时间（00:00:00）
                LocalDateTime startTime = targetDate.atStartOfDay();
                // 4. 生成结束时间（23:59:59）
                LocalDateTime endTime = targetDate.atTime(23, 59, 59);

                var vo = WalmartOrderReqVO.builder()
                    .limit(100)
                    .createdStartDate(startTime)
                    .createdEndDate(endTime)
                    .build();
                return walmartMarketplaceToOmsConverter.toOrders(client.getOrders(vo), client.token).stream();
            })
            .toList();
    }
}
