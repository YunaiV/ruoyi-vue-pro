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
public class AmazonadDataJob extends DataJob{
    @Autowired
    EsbService service;

    @Autowired
    AmazonService amazonService;

    final String DATABASE = Domain.AMAZONAD.toString();


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        amazonService.adClient.getAllAdReport(beforeYesterday)
            .forEach(page -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("ad_report")
                    .syncType("inc")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(beforeYesterday)
                    .content(page)
                    .headers(null)
                    .build();
                service.send(data);
            });
        return "data upload success";
    }
}