package cn.iocoder.yudao.framework.test.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.AbstractScriptDatabaseInitializer;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.sql.DataSource;

/**
 * SQL 初始化的测试 Configuration
 *
 * 为什么不使用 org.springframework.boot.autoconfigure.sql.init.DataSourceInitializationConfiguration 呢？
 * 因为我们在单元测试会使用 spring.main.lazy-initialization 为 true，开启延迟加载。此时，会导致 DataSourceInitializationConfiguration 初始化
 * 不过呢，当前类的实现代码，基本是复制 DataSourceInitializationConfiguration 的哈！
 *
 * @author 芋道源码
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(AbstractScriptDatabaseInitializer.class)
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnClass(name = "org.springframework.jdbc.datasource.init.DatabasePopulator")
@Lazy(value = false) // 禁止延迟加载
@EnableConfigurationProperties(SqlInitializationProperties.class)
public class SqlInitializationTestConfiguration {

	@Bean
	public DataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer(DataSource dataSource,
																				   SqlInitializationProperties initializationProperties) {
		DatabaseInitializationSettings settings = createFrom(initializationProperties);
		return new DataSourceScriptDatabaseInitializer(dataSource, settings);
	}

	static DatabaseInitializationSettings createFrom(SqlInitializationProperties properties) {
		DatabaseInitializationSettings settings = new DatabaseInitializationSettings();
		settings.setSchemaLocations(properties.getSchemaLocations());
		settings.setDataLocations(properties.getDataLocations());
		settings.setContinueOnError(properties.isContinueOnError());
		settings.setSeparator(properties.getSeparator());
		settings.setEncoding(properties.getEncoding());
		settings.setMode(properties.getMode());
		return settings;
	}

}
