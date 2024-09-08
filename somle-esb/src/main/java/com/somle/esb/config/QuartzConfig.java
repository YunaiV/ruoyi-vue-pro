package com.somle.esb.config;

import com.somle.esb.job.DataJob;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail dataJobDetail() {
        return JobBuilder.newJob(DataJob.class)
            .withIdentity("dataJob")
            .storeDurably()  // Keeps the job even if no trigger is associated
            .build();
    }

    @Bean
    public Trigger dataTrigger(JobDetail dataJobDetail) {
        return TriggerBuilder.newTrigger()
            .forJob(dataJobDetail)
            .withIdentity("dataTrigger")
            .withSchedule(CronScheduleBuilder.cronSchedule("0 10 1 * * ?"))
//            .withSchedule(CronScheduleBuilder.cronSchedule("0/1 * * * * ?"))
            .build();
    }

    @Bean
    public Scheduler scheduler(Trigger dataTrigger, JobDetail dataJobDetail) throws SchedulerException {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();

        // Register the job and trigger
        scheduler.scheduleJob(dataJobDetail, dataTrigger);

        // Start the scheduler
        scheduler.start();

        return scheduler;
    }
}
