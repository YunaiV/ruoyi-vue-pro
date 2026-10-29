package cn.iocoder.yudao.framework.mybatis.core.util;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.framework.mybatis.core.enums.DbTypeEnum;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.mybatisplus.annotation.DbType;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ConcurrentReferenceHashMap;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;

/**
 * JDBC 工具类
 *
 * @author 芋道源码
 */
public class JdbcUtils {

    /**
     * 数据源与 DB 类型的缓存；弱引用数据源，动态移除的数据源可被回收
     */
    private static final Map<DataSource, DbType> DB_TYPE_CACHE =
            new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK);

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
     * 获得当前执行时路由到的数据源
     *
     * 存在动态数据源时，按 {@link DynamicRoutingDataSource#determineDataSource()} 获取当前线程路由到的数据源；否则使用唯一的数据源
     *
     * @return 数据源
     */
    public static DataSource getDataSource() {
        DataSource dataSource = getRoutingDataSource();
        return dataSource instanceof DynamicRoutingDataSource
                ? ((DynamicRoutingDataSource) dataSource).determineDataSource() : dataSource;
    }

    /**
     * 取得注册的路由数据源；没有动态数据源时使用唯一的数据源。
     *
     * @return 路由数据源或唯一数据源
     */
    private static DataSource getRoutingDataSource() {
        try {
            return SpringUtils.getBean(DynamicRoutingDataSource.class);
        } catch (NoSuchBeanDefinitionException e) {
            return SpringUtils.getBean(DataSource.class);
        }
    }

    /**
     * 获得当前数据源上 Spring 事务已绑定的连接，不借出新连接，也不转移连接的关闭责任。
     * 动态数据源的事务资源绑定在路由数据源上；事务内再次切换路由键，不会改变已绑定的执行连接。
     */
    static Connection getTransactionalConnection() {
        Object resource = TransactionSynchronizationManager.getResource(getRoutingDataSource());
        return resource instanceof ConnectionHolder ? ((ConnectionHolder) resource).getConnection() : null;
    }

    /**
     * 通过当前数据库连接获得对应的 DB 类型
     *
     * 事务内复用已绑定连接；其它情况按当前路由到的数据源实例缓存，命中缓存时不再获取连接
     *
     * @return DB 类型
     */
    public static DbType getDbType() {
        Connection connection = getTransactionalConnection();
        if (connection != null) {
            try {
                return detectDbType(connection);
            } catch (SQLException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        return getDbType(getDataSource());
    }

    // TODO DONE @AI：方法注释；已说明连接归属、缓存或数据源选择。
    /**
     * 按实际数据源实例缓存数据库类型；检测失败时不写入缓存。
     *
     * @param dataSource 实际路由到的数据源
     * @return 数据库类型
     */
    static DbType getDbType(DataSource dataSource) {
        return DB_TYPE_CACHE.computeIfAbsent(dataSource, JdbcUtils::detectDbType);
    }

    /**
     * 通过自行借用的连接识别数据库类型，并在结束后关闭该连接。
     *
     * @param dataSource 待识别的数据源
     * @return 数据库类型
     */
    private static DbType detectDbType(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            return detectDbType(conn);
        } catch (SQLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * 读取执行连接的元数据识别数据库类型，不关闭调用方持有的连接。
     *
     * @param connection 当前执行连接
     * @return 数据库类型；产品名为空时保留原有 null 返回
     * @throws SQLException 读取数据库元数据失败
     */
    private static DbType detectDbType(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String productName = metaData.getDatabaseProductName();
        DbType dbType = DbTypeEnum.find(productName);
        if (dbType != null || StrUtil.isBlank(productName)) {
            return dbType;
        }
        // 不使用 MyBatis Plus 的 URL 识别，避免其在日志中打印完整 URL
        dbType = findDbTypeByNativeUrl(metaData.getURL());
        if (dbType == null) {
            throw new IllegalArgumentException("不支持的数据库产品：" + productName);
        }
        return dbType;
    }

    /**
     * 按原生驱动 URL 识别产品名未登记的数据库，与 {@link #getDbType(String)} 的归类保持一致
     *
     * @param url JDBC URL
     * @return DB 类型；无法识别时返回 null
     */
    private static DbType findDbTypeByNativeUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return null;
        }
        String lowerUrl = url.toLowerCase(Locale.ROOT);
        if (lowerUrl.contains(":highgo:")) {
            return DbType.POSTGRE_SQL;
        }
        if (lowerUrl.contains(":mariadb:")) {
            return DbType.MARIADB;
        }
        return null;
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
