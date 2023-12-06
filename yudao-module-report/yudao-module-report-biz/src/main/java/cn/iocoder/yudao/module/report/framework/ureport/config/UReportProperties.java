package cn.iocoder.yudao.module.report.framework.ureport.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * UReport2 配置类
 *
 * @author 赤焰
 */
@Data
@ConfigurationProperties(prefix = "ureport.provider.database")
public class UReportProperties {

    // TODO @赤焰：每个字段的注释写下哈；
	private String name = "数据库文件系统";

    private String prefix = "db-";

	private boolean disabled = false;

}
