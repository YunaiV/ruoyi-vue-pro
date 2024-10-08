package com.somle.esb.job;


import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import com.somle.esb.service.EsbService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


public class DataJob implements JobHandler {
    LocalDate scheduleDate;
    LocalDate today;
    LocalDate yesterday;
    LocalDate beforeYesterday;
    LocalDateTime yesterdayFirstSecond;
    LocalDateTime yesterdayLastSecond;

    void setDate(String param) {
        scheduleDate = StrUtils.isEmpty(param) ? LocalDate.now() : LocalDate.parse(param);
        today = scheduleDate;
        yesterday = scheduleDate.minusDays(1);
        beforeYesterday = scheduleDate.minusDays(2);
        yesterdayFirstSecond = yesterday.atStartOfDay();
        yesterdayLastSecond = today.atStartOfDay().minusSeconds(1);
    }


    @Override
    public String execute(String param) throws Exception {
        return "";
    }
}