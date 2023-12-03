package cn.iocoder.yudao.module.report.framework.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * UReport配置类
 *
 * @author 赤焰
 */
@Data
@ConfigurationProperties(prefix = "ureport.provider.database")
public class UReportProperties {
	private String name = "数据库文件系统";
	private String prefix = "db-";
	private boolean disabled = false;
}
