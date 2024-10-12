package com.somle.esb.job;


import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import com.somle.amazon.service.AmazonService;
import com.somle.eccang.model.EccangOrderVO;
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
public class EccangDataJob extends DataJob{
    @Autowired
    EsbService service;

    @Autowired
    EccangService eccangService;

    final String DATABASE = Domain.ECCANG.toString();


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

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

        eccangService.getOrderPages(
                EccangOrderVO.builder()
                    .dateCreateSysStart(yesterdayFirstSecond)
                    .dateCreateSysEnd(yesterdayLastSecond)
                    .build()
            )
            .forEach(page -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("order_sys_create")
                    .syncType("inc")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(yesterday)
                    .content(page)
                    .headers(null)
                    .build();
                service.send(data);
            });

        eccangService.getOrderPages(
                EccangOrderVO.builder()
                    .platformPaidDateStart(yesterdayFirstSecond)
                    .platformPaidDateEnd(yesterdayLastSecond)
                    .build()
            )
            .forEach(page -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("order_platform_pay")
                    .syncType("inc")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(yesterday)
                    .content(page)
                    .headers(null)
                    .build();
                service.send(data);
            });

        eccangService.getOrderPages(
                EccangOrderVO.builder()
                    .warehouseShipDateStart(yesterdayFirstSecond)
                    .warehouseShipDateEnd(yesterdayLastSecond)
                    .build()
            )
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

        eccangService.getOrderPages(
            EccangOrderVO.builder()
                .platformShipDateStart(yesterdayFirstSecond)
                .platformShipDateEnd(yesterdayLastSecond)
                .build()
        )
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



        eccangService.getOrderPages(
            EccangOrderVO.builder()
                .dateCreateSysEnd(yesterdayLastSecond)
                .status("3")
                .build()
        )
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

        service.send(
                OssData.builder()
                        .database(DATABASE)
                        .tableName("stock")
                        .syncType("full")
                        .requestTimestamp(System.currentTimeMillis())
                        .folderDate(today)
                        .content(eccangService.getInventory())
                        .headers(null)
                        .build()
        );

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