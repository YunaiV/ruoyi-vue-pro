package cn.iocoder.dashboard.framework.datasource.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 数据库匹配类
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true) // 启动事务管理
public class DataSourceConfiguration {
}
