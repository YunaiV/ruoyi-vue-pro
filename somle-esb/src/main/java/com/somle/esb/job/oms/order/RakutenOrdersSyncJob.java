package com.somle.esb.job.oms.order;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.module.oms.api.dto.OmsOrderSaveReqDTO;
import com.somle.esb.converter.oms.RakutenToOmsConverter;
import com.somle.rakuten.service.RakutenService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Component
public class RakutenOrdersSyncJob extends BaseOrdersSyncJob {

    @Resource
    RakutenService rakutenService;
    @Resource
    RakutenToOmsConverter rakutenToOmsConverter;

    @Override
    public List<OmsOrderSaveReqDTO> listOrders(String param) {

        LocalDate baseDate = LocalDate.parse(param);
        // 2. 计算前两天的日期
        LocalDate targetDate = baseDate.minusDays(2);

        String startTime = targetDate.toString() + " 00:00:00";
        String endTime = targetDate.toString() + " 23:59:59";

        // 使用日本时区 (Asia/Tokyo) 来解析 startDatetime 和 endDatetime
        ZonedDateTime startDatetime = LocalDateTimeUtil.parse(startTime, "yyyy-MM-dd HH:mm:ss")
            .atZone(ZoneId.of("Asia/Tokyo"));
        ZonedDateTime endDatetime = LocalDateTimeUtil.parse(endTime, "yyyy-MM-dd HH:mm:ss")
            .atZone(ZoneId.of("Asia/Tokyo"));


        return rakutenService.rakutenClients.stream()
            .flatMap(rakutenClient -> {
                return rakutenToOmsConverter.toOrders(rakutenClient.getAllOrders(startDatetime, endDatetime), rakutenClient.entity).stream();
            })
            .toList();
    }
}
