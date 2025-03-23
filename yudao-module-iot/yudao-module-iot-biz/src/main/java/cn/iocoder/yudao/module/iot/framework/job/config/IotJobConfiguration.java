package cn.iocoder.yudao.module.iot.framework.job.config;

import cn.iocoder.yudao.module.iot.framework.job.core.IotSchedulerManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * IoT 模块的 Job 自动配置类
 *
 * @author 芋道源码
 */
@Configuration
public class IotJobConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public IotSchedulerManager iotSchedulerManager(DataSource dataSource,
                                                   ApplicationContext applicationContext) {
        return new IotSchedulerManager(dataSource, applicationContext);
    }

}
