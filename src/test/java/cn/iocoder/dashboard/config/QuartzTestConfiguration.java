package cn.iocoder.dashboard.config;

import org.mockito.Mockito;
import org.quartz.impl.StdScheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@Lazy(false)
public class QuartzTestConfiguration {

    @Bean
    public StdScheduler scheduler() {
        return Mockito.mock(StdScheduler.class);
    }

}
