package com.somle.esb.job.oms.order;

import cn.iocoder.yudao.module.oms.api.dto.OmsOrderSaveReqDTO;
import com.somle.amazon.controller.vo.AmazonSpMarketplaceParticipationVO;
import com.somle.amazon.controller.vo.AmazonSpMarketplaceVO;
import com.somle.amazon.controller.vo.AmazonSpOrderReqVO;
import com.somle.amazon.service.AmazonSpService;
import com.somle.esb.converter.oms.AmazonToOmsConverter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Component
public class AmazonOrdersSyncJob extends BaseOrdersSyncJob {

    @Resource
    AmazonSpService amazonSpService;
    @Resource
    AmazonToOmsConverter amazonToOmsConverter;

    @Override
    public List<OmsOrderSaveReqDTO> listOrders(String param) {
        LocalDate baseDate = LocalDate.parse(param);
        // 2. 计算前两天的日期
        LocalDate targetDate = baseDate.minusDays(2);
        // 3. 生成起始时间（00:00:00）
        LocalDateTime startTime = targetDate.atStartOfDay();
        // 4. 生成结束时间（23:59:59）
        LocalDateTime endTime = targetDate.atTime(23, 59, 59);

        return amazonSpService.clients.stream()
            .flatMap(client -> {

                List<String> marketplaceIds = Optional.ofNullable(client.getMarketplaceParticipations())
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .map(AmazonSpMarketplaceParticipationVO::getMarketplace)
                    .map(AmazonSpMarketplaceVO::getId)
                    .filter(Objects::nonNull)
                    .toList();

                if (marketplaceIds.isEmpty()) {
                    return Stream.empty();
                }

                AmazonSpOrderReqVO vo = AmazonSpOrderReqVO.builder()
                    .createdAfter(startTime)
                    .createdBefore(endTime)
                    .marketplaceIds(marketplaceIds)
                    .build();

                // 3. 获取订单并转换（添加异常处理）
                try {
                    return amazonToOmsConverter.toOrders(client.getAllOrders(vo), client.getAuth()).stream();
                } catch (Exception e) {
                    log.error("Failed to process orders for client: {}", client, e);
                    return Stream.empty();
                }
            }).toList();
    }
}
