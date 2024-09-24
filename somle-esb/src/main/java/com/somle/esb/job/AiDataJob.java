package com.somle.esb.job;


import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import com.somle.ai.service.AiService;
import com.somle.esb.model.Domain;
import com.somle.esb.model.OssData;
import com.somle.esb.service.EsbService;
import com.somle.matomo.service.MatomoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.stream.IntStream;

@Component
public class AiDataJob implements JobHandler {
    @Autowired
    EsbService service;

    @Autowired
    AiService aiService;


    @Override
    public String execute(String param) throws Exception {
        var scheduleDate = param.isEmpty() ? LocalDate.now() : LocalDate.parse(param);
        var yesterday = scheduleDate.minusDays(1);
        var DATABASE = Domain.ECCANG.getValue();
        
        service.send(
            OssData.builder()
                .database(DATABASE)
                .tableName("country")
                .syncType("full")
                .requestTimestamp(System.currentTimeMillis())
                .folderDate(LocalDate.now())
                .content(aiService.getCountries().toList())
                .headers(null)
                .build()
        );

        service.send(
            OssData.builder()
                .database(DATABASE)
                .tableName("currency")
                .syncType("full")
                .requestTimestamp(System.currentTimeMillis())
                .folderDate(LocalDate.now())
                .content(aiService.getCurrencies().toList())
                .headers(null)
                .build()
        );

        service.send(
            OssData.builder()
                .database(DATABASE)
                .tableName("person")
                .syncType("inc")
                .requestTimestamp(System.currentTimeMillis())
                .folderDate(yesterday)
                .content(aiService.getNames(yesterday).toList())
                .headers(null)
                .build()
        );

        service.send(
            OssData.builder()
                .database(DATABASE)
                .tableName("address")
                .syncType("inc")
                .requestTimestamp(System.currentTimeMillis())
                .folderDate(yesterday)
                .content(aiService.getAddresses(yesterday).toList())
                .headers(null)
                .build()
        );
        return "data upload success";
    }
}