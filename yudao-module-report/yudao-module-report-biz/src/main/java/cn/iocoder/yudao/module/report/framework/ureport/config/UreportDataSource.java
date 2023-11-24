package cn.iocoder.yudao.module.report.framework.ureport.config;

import com.bstek.ureport.definition.datasource.BuildinDatasource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * UReport 内置数据源
 */
@Slf4j
@Component
public class UreportDataSource implements BuildinDatasource {

	private static final String NAME = "UreportDataSource";

	@Resource
	private DataSource dataSource;

	/**
	 * @return 数据源名称
	 */
	@Override
	public String name() {
		return NAME;
	}

    // TODO @赤焰：这个方法，如果拿不到连接，是不是抛出异常比较好？
	/**
	 * @return 获取连接
	 */
	@Override
	public Connection getConnection() {
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			log.error("Ureport 数据源 获取连接失败！");
			e.printStackTrace();
		}
		return null;
	}

}
