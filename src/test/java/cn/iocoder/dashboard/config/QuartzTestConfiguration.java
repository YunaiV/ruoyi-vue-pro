package cn.iocoder.dashboard.config;

import org.mockito.Mockito;
import org.quartz.Scheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzTestConfiguration {

    @Bean
    public Scheduler scheduler() {
        return Mockito.mock(Scheduler.class);
    }

}
