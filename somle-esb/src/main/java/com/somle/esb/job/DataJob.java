package com.somle.esb.job;


import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import java.time.LocalDate;
import java.time.LocalDateTime;



public class DataJob implements JobHandler {
    LocalDate scheduleDate;
    LocalDate today;
    LocalDate yesterday;
    LocalDate beforeYesterday;
    LocalDateTime yesterdayFirstSecond;
    LocalDateTime yesterdayLastSecond;
    LocalDateTime beforeYesterdayFirstSecond;
    LocalDateTime beforeYesterdayLastSecond;

    void setDate(String param) {
        scheduleDate = StrUtils.isEmpty(param) ? LocalDate.now() : LocalDate.parse(param);
        today = scheduleDate;
        yesterday = scheduleDate.minusDays(1);
        beforeYesterday = scheduleDate.minusDays(2);
        beforeYesterdayFirstSecond = beforeYesterday.atStartOfDay();
        beforeYesterdayLastSecond = yesterday.atStartOfDay().minusSeconds(1);
        yesterdayFirstSecond = yesterday.atStartOfDay();
        yesterdayLastSecond = today.atStartOfDay().minusSeconds(1);
    }


    @Override
    public String execute(String param) throws Exception {
        return "";
    }
}