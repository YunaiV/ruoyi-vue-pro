package cn.iocoder.dashboard.config;

import io.reactivex.rxjava3.core.Scheduler;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@Lazy(false)
public class QuartzTestConfiguration {

    @Bean
    public Scheduler scheduler() {
        return Mockito.mock(Scheduler.class);
    }

}
