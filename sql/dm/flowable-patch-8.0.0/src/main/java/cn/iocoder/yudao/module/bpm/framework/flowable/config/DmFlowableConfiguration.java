package cn.iocoder.yudao.module.bpm.framework.flowable.config;

import org.flowable.common.engine.impl.AbstractEngineConfiguration;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 达梦数据库的 Flowable 配置
 *
 * @author 芋道源码
 */
@Configuration(proxyBeanMethods = false)
public class DmFlowableConfiguration {

    /**
     * 在 Spring 管理的各个 Flowable 引擎初始化前设置达梦方言与插入策略
     */
    @Bean
    public static BeanPostProcessor dmEngineConfigurationBeanPostProcessor() {
        return new BeanPostProcessor() {

            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) {
                if (!(bean instanceof AbstractEngineConfiguration)) {
                    return bean;
                }
                AbstractEngineConfiguration configuration = (AbstractEngineConfiguration) bean;
                if (configuration.getDataSource() == null) {
                    return bean;
                }
                // 在各个 Spring 引擎初始化前处理，避免子引擎重新开启批量插入；使用实际数据源识别达梦
                try (Connection connection = configuration.getDataSource().getConnection()) {
                    if ("DM DBMS".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName())) {
                        configuration.setDatabaseType("oracle");
                        // 特殊：达梦普通用户执行 Oracle INSERT ALL 时可能访问 SYS.SYSDUAL2，改为普通插入，避免扩大数据库权限
                        configuration.setBulkInsertEnabled(false);
                    }
                } catch (SQLException e) {
                    throw new IllegalStateException("读取 Flowable 数据库类型失败", e);
                }
                return bean;
            }
        };
    }

}
