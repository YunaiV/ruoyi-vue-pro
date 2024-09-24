package com.somle.esb.job;


import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import com.somle.amazon.service.AmazonService;
import com.somle.esb.model.Domain;
import com.somle.esb.model.OssData;
import com.somle.esb.service.EsbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AmazonspDataJob implements JobHandler {
    @Autowired
    EsbService service;

    @Autowired
    AmazonService amazonService;


    @Override
    public String execute(String param) throws Exception {
        var dataDate = param.isEmpty() ? LocalDateTimeUtils.getBeforeYesterday().toLocalDate() : LocalDate.parse(param);
        amazonService.spClient.getAllAsinReport(dataDate).parallel()
            .forEach(page -> {
                OssData data = OssData.builder()
                    .database(Domain.AMAZONSP.getValue())
                    .tableName("asin_report")
                    .syncType("inc")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(dataDate)
                    .content(page)
                    .headers(null)
                    .build();
                service.send(data);
            });
        return "data upload success";
    }
}