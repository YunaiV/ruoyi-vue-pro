package com.somle.esb.job;


import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import com.somle.amazon.service.AmazonService;
import com.somle.eccang.service.EccangService;
import com.somle.esb.model.Domain;
import com.somle.esb.model.OssData;
import com.somle.esb.service.EsbService;
import com.somle.framework.common.util.general.CoreUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class EccangDataJob implements JobHandler {
    @Autowired
    EsbService service;

    @Autowired
    EccangService eccangService;


    @Override
    public String execute(String param) throws Exception {
        var scheduleDate = param.isEmpty() ? LocalDate.now() : LocalDate.parse(param);
        LocalDate today = scheduleDate;
        LocalDate yesterday = today.minusDays(1);
        LocalDate beforeYesterday = today.minusDays(2);
        LocalDateTime yesterdayFirstSecond = yesterday.atStartOfDay();
        LocalDateTime yesterdayLastSecond = today.atStartOfDay().minusSeconds(1);
        var DATABASE = Domain.ECCANG.getValue();
        service.send(
            OssData.builder()
                .database(DATABASE)
                .tableName("warehouse")
                .syncType("full")
                .requestTimestamp(System.currentTimeMillis())
                .folderDate(today)
                .content(eccangService.getWarehouseList())
                .headers(null)
                .build()
        );

        eccangService.getOrderPlatformShipPage(yesterdayFirstSecond, yesterdayLastSecond)
            .forEach(page -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("order_platform_ship")
                    .syncType("inc")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(yesterday)
                    .content(page)
                    .headers(null)
                    .build();
                service.send(data);
            });

        eccangService.getOrderWarehouseShipPage(yesterdayFirstSecond, yesterdayLastSecond)
            .forEach(page -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("order_warehouse_ship")
                    .syncType("inc")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(yesterday)
                    .content(page)
                    .headers(null)
                    .build();
                service.send(data);
            });

        eccangService.getOrderUnShipPage()
            .forEach(page -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("order_unship")
                    .syncType("inc")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(yesterday)
                    .content(page)
                    .headers(null)
                    .build();
                service.send(data);
            });

        eccangService.getInventoryBatchLog(yesterdayFirstSecond, yesterdayLastSecond)
            .forEach(page -> {
                CoreUtils.sleep(2000);
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("stock_log")
                    .syncType("inc")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(yesterday)
                    .content(page)
                    .headers(null)
                    .build();
                service.send(data);
            });
        return "data upload success";
    }
}