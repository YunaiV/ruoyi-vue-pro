package cn.iocoder.yudao.module.report.framework.ureport.config;

import com.bstek.ureport.definition.datasource.BuildinDatasource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.report.enums.ErrorCodeConstants.REPORT_DATABASE_NOT_EXISTS;

/**
 * UReport 内置数据源
 * @author 赤焰
 */
@Slf4j
@Component
public class UReportDataSource implements BuildinDatasource {

	private static final String NAME = "UReportDataSource";

	@Resource
	private DataSource dataSource;

	/**
	 * @return 数据源名称
	 */
	@Override
	public String name() {
		return NAME;
	}

	/**
	 * @return 获取连接
	 */
	@Override
	public Connection getConnection() {
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			log.error("UReportDataSource获取连接失败！");
			throw exception(REPORT_DATABASE_NOT_EXISTS);
		}
	}

}
