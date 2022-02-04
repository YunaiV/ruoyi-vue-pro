package cn.iocoder.yudao.module.system.framework.errorcode.config;

import cn.iocoder.yudao.module.system.framework.errorcode.core.generator.ErrorCodeAutoGenerator;
import cn.iocoder.yudao.module.system.framework.errorcode.core.loader.ErrorCodeLoader;
import cn.iocoder.yudao.module.system.framework.errorcode.core.service.ErrorCodeFrameworkService;
import cn.iocoder.yudao.module.system.framework.errorcode.core.loader.ErrorCodeLoaderImpl;
import cn.iocoder.yudao.module.system.framework.errorcode.core.generator.ErrorCodeAutoGeneratorImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// TODO 芋艿：貌似放的位置有问题
/**
 * 错误码配置类
 */
@Configuration
@EnableConfigurationProperties(ErrorCodeProperties.class)
@EnableScheduling // 开启调度任务的功能，因为 ErrorCodeRemoteLoader 通过定时刷新错误码
public class ErrorCodeConfiguration {

    @Bean
    public ErrorCodeAutoGenerator errorCodeAutoGenerator(@Value("${spring.application.name}") String applicationName,
                                                         ErrorCodeProperties errorCodeProperties,
                                                         ErrorCodeFrameworkService errorCodeFrameworkService) {
        return new ErrorCodeAutoGeneratorImpl(applicationName, errorCodeProperties.getConstantsClassList(),
                errorCodeFrameworkService);
    }

    @Bean
    public ErrorCodeLoader errorCodeLoader(@Value("${spring.application.name}") String applicationName,
                                           ErrorCodeFrameworkService errorCodeFrameworkService) {
        return new ErrorCodeLoaderImpl(applicationName, errorCodeFrameworkService);
    }

}
