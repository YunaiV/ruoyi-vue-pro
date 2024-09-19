package com.somle.esb.job;


import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import com.somle.esb.service.EsbService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class DataJob implements JobHandler {
    @Autowired
    EsbService service;


    @Override
    public String execute(String param) throws Exception {
//        throw new RuntimeException("this is an exception");
        service.dataCollect();
        return "data upload success";
    }
}