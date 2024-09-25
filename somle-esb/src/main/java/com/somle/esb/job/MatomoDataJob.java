package com.somle.esb.job;


import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import com.somle.esb.model.Domain;
import com.somle.esb.model.OssData;
import com.somle.esb.service.EsbService;
import com.somle.matomo.service.MatomoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.stream.IntStream;

@Component
public class MatomoDataJob implements JobHandler {
    @Autowired
    EsbService service;

    @Autowired
    MatomoService matomoService;


    @Override
    public String execute(String param) throws Exception {
        var scheduleDate = param.isEmpty() ? LocalDate.now() : LocalDate.parse(param);
        var dataDate = scheduleDate.minusDays(2);
        IntStream.range(1, 7).boxed().forEach(idSite -> {
            OssData data = OssData.builder()
                .database(Domain.MATOMO.getValue())
                .tableName("visit")
                .syncType("inc")
                .requestTimestamp(System.currentTimeMillis())
                .folderDate(dataDate)
                .content(matomoService.getVisits(idSite, dataDate).toList())
                .headers(null)
                .build();
            service.send(data);
        });
        return "data upload success";
    }
}