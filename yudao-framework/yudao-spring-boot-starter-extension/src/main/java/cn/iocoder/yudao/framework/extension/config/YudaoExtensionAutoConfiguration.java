package cn.iocoder.yudao.framework.extension.config;

import cn.iocoder.yudao.framework.extension.core.ExtensionBootstrap;
import cn.iocoder.yudao.framework.extension.core.context.ExtensionContext;
import cn.iocoder.yudao.framework.extension.core.context.ExtensionContextHolder;
import cn.iocoder.yudao.framework.extension.core.context.ExtensionExecutor;
import cn.iocoder.yudao.framework.extension.core.factory.ExtensionFactory;
import cn.iocoder.yudao.framework.extension.core.factory.ExtensionRegisterFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description 扩展点组件自动装配
 * @author Qingchen
 * @version 1.0.0
 * @date 2021-08-28 21:50
 * @class cn.iocoder.yudao.framework.extension.config.YudaoExtensionAutoConfiguration.java
 */
@Configuration
public class YudaoExtensionAutoConfiguration {

    /**
     * 组件初始化
     * @return
     */
    @Bean(initMethod = "init")
    @ConditionalOnMissingBean(ExtensionBootstrap.class)
    public ExtensionBootstrap bootstrap() {
        return new ExtensionBootstrap();
    }

    /**
     * 扩展点工厂
     * @return
     */
    @Bean
    @ConditionalOnMissingBean({ExtensionRegisterFactory.class, ExtensionFactory.class})
    public ExtensionRegisterFactory registerFactory() {
        return new ExtensionRegisterFactory();
    }

    /**
     * 扩展组件上下文对象
     * @return
     */
    @Bean
    @ConditionalOnMissingBean({ExtensionContextHolder.class, ExtensionContext.class})
    public ExtensionContextHolder context() {
        return new ExtensionContextHolder();
    }

    /**
     * 扩展组件执行器
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(ExtensionExecutor.class)
    public ExtensionExecutor executor() {
        return new ExtensionExecutor();
    }
}
