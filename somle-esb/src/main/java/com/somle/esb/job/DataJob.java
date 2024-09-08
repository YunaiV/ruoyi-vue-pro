package com.somle.esb.job;


import com.somle.esb.service.EsbService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataJob implements Job {
    @Autowired
    EsbService service;

    private static final Logger logger = LoggerFactory.getLogger(DataJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
//        System.out.println("Simple Job executed at {}");
        service.dataCollect();
    }
}