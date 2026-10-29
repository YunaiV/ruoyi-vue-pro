package cn.iocoder.yudao.framework.mybatis.core.util;

import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.framework.mybatis.core.enums.DbTypeEnum;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.mybatisplus.annotation.DbType;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC 工具类
 *
 * @author 芋道源码
 */
public class JdbcUtils {

    /**
     * 判断连接是否正确
     *
     * @param url      数据源连接
     * @param username 账号
     * @param password 密码
     * @return 是否正确
     */
    public static boolean isConnectionOK(String url, String username, String password) {
        try (Connection ignored = DriverManager.getConnection(url, username, password)) {
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 获得 URL 对应的 DB 类型
     *
     * 特殊：以下原生驱动连接的数据库兼容 PostgreSQL，按 PostgreSQL 处理，复用其序列主键、Quartz、代码生成等适配：
     * 1. 瀚高 HighGo（jdbc:highgo:）
     * 2. openGauss、MogDB（jdbc:opengauss:），需要使用 PG 兼容模式（DBCOMPATIBILITY = 'PG'）创建的数据库
     * 3. 华为 GaussDB（jdbc:gaussdb:），需要使用 PG 兼容模式（DBCOMPATIBILITY = 'PG'）创建的数据库
     *
     * 注意：MyBatis Plus 的分页仍按原生 URL 识别，例如说 GaussDB 使用 GaussDBDialect
     *
     * @param url URL
     * @return DB 类型
     */
    public static DbType getDbType(String url) {
        DbType dbType = com.baomidou.mybatisplus.extension.toolkit.JdbcUtils.getDbType(url);
        return ObjectUtils.equalsAny(dbType, DbType.HIGH_GO, DbType.OPENGAUSS, DbType.GAUSS_DB)
                ? DbType.POSTGRE_SQL : dbType;
    }

    /**
     * 通过当前数据库连接获得对应的 DB 类型
     *
     * @return DB 类型
     */
    public static DbType getDbType() {
        DataSource dataSource;
        try {
            DynamicRoutingDataSource dynamicRoutingDataSource = SpringUtils.getBean(DynamicRoutingDataSource.class);
            dataSource = dynamicRoutingDataSource.determineDataSource();
        } catch (NoSuchBeanDefinitionException e) {
            dataSource = SpringUtils.getBean(DataSource.class);
        }
        try (Connection conn = dataSource.getConnection()) {
            return DbTypeEnum.find(conn.getMetaData().getDatabaseProductName());
        } catch (SQLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * 判断 JDBC 连接是否为 SQLServer 数据库
     *
     * @param url JDBC 连接
     * @return 是否为 SQLServer 数据库
     */
    public static boolean isSQLServer(String url) {
        DbType dbType = getDbType(url);
        return isSQLServer(dbType);
    }

    /**
     * 判断 JDBC 连接是否为 SQLServer 数据库
     *
     * @param dbType DB 类型
     * @return 是否为 SQLServer 数据库
     */
    public static boolean isSQLServer(DbType dbType) {
        return ObjectUtils.equalsAny(dbType, DbType.SQL_SERVER, DbType.SQL_SERVER2005);
    }

}
