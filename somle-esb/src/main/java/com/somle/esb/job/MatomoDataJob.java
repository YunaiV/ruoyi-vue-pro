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
public class MatomoDataJob extends DataJob {
    @Autowired
    EsbService service;

    @Autowired
    MatomoService matomoService;

    final String DATABASE = Domain.MATOMO.toString();


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        IntStream.range(1, 7).boxed().forEach(idSite -> {
            OssData data = OssData.builder()
                .database(DATABASE)
                .tableName("visit")
                .syncType("inc")
                .requestTimestamp(System.currentTimeMillis())
                .folderDate(beforeYesterday)
                .content(matomoService.getVisits(idSite, beforeYesterday).toList())
                .headers(null)
                .build();
            service.send(data);
        });
        return "data upload success";
    }
}