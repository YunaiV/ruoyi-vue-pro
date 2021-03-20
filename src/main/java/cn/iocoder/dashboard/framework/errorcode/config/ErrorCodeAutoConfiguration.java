package cn.iocoder.dashboard.framework.errorcode.config;

import cn.iocoder.dashboard.framework.errorcode.core.ErrorCodeRemoteLoader;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableConfigurationProperties(ErrorCodeProperties.class)
@EnableScheduling // 开启调度任务的功能，因为 ErrorCodeRemoteLoader 通过定时刷新错误码
public class ErrorCodeAutoConfiguration {

    @Bean
    public cn.iocoder.dashboard.framework.errorcode.core.ErrorCodeAutoGenerator errorCodeAutoGenerator(ErrorCodeProperties errorCodeProperties) {
        return new cn.iocoder.dashboard.framework.errorcode.core.ErrorCodeAutoGenerator(errorCodeProperties.getGroup())
                .setErrorCodeConstantsClass(errorCodeProperties.getConstantsClass());
    }

    @Bean
    public ErrorCodeRemoteLoader errorCodeRemoteLoader(ErrorCodeProperties errorCodeProperties) {
        return new ErrorCodeRemoteLoader(errorCodeProperties.getGroup());
    }

}