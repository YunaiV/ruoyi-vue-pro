package cn.iocoder.yudao.framework.mybatis.core.util;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.mybatis.core.enums.DbTypeEnum;
import com.baomidou.mybatisplus.annotation.DbType;
import org.springframework.util.ConcurrentReferenceHashMap;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

// TODO DONE @AI：方法注释；已补齐方言重载与内部辅助方法的参数、返回值及边界。
// TODO DONE @AI：多余的方法，可以考虑去掉；当然，保留也可以。。。已核对生产 Java/XML 调用，现有方法均有用途，保留。
/**
 * 日期统计、行数限制、分批删除 SQL 片段的方言工具类
 *
 * 1. 每次生成 SQL 时，按当前执行时路由到的数据源选择方言（按数据源实例缓存），不使用启动时固定的 databaseId 或主库
 * 2. 只有明确识别的 PostgreSQL、Oracle、SQL Server 2012+ 输出新片段；其它已识别的数据库（MySQL、MariaDB、OceanBase、H2、
 *    达梦、神通、人大金仓、openGauss、GaussDB、瀚高、MogDB 等）保持原有 SQL 片段；完全未识别的数据库抛出异常
 * 3. 参数只能是代码中固定的列名，经过白名单校验；格式均为常量，业务值必须继续使用 #{} 绑定
 *
 * 在 XML 或注解中使用：${@cn.iocoder.yudao.framework.mybatis.core.util.SqlDialectUtils@formatDate('create_time')}
 *
 * @author 芋道源码
 */
@SuppressWarnings("EnhancedSwitchMigration")
public class SqlDialectUtils {

    /**
     * SQL Server 2012 的主版本号
     */
    private static final int SQL_SERVER_2012_MAJOR_VERSION = 11;

    /**
     * 保持原有 SQL 片段的数据库类型
     */
    private static final Set<DbType> ORIGINAL_DB_TYPES = EnumSet.of(DbType.MYSQL, DbType.OCEAN_BASE,
            DbType.H2, DbType.DM, DbType.OSCAR, DbType.KINGBASE_ES, DbType.OPENGAUSS, DbType.GAUSS_DB,
            DbType.SQL_SERVER2005);

    /**
     * 兼容 PostgreSQL 的原生驱动 URL：瀚高、openGauss、GaussDB、MogDB、人大金仓
     */
    private static final Pattern POSTGRESQL_VARIANT_URL_PATTERN =
            Pattern.compile(":(highgo|opengauss|gaussdb|mogdb|kingbase\\d*):");

    /**
     * MyBatis 参数名：参数或其属性路径，例如 limit、reqVO.limit
     */
    private static final Pattern PARAM_NAME_PATTERN =
            Pattern.compile("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*");

    /**
     * 数据源与方言的缓存；弱引用数据源，动态移除的数据源可被回收
     */
    private static final Map<DataSource, Dialect> DIALECT_CACHE =
            new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK);

    /**
     * 方言
     */
    enum Dialect {

        /**
         * 原有 SQL 片段
         */
        ORIGINAL,
        POSTGRESQL,
        ORACLE,
        SQLSERVER

    }

    // ========== 日期字符串 ==========

    /**
     * 按天格式化为 yyyy-MM-dd 字符串；原有片段为 DATE_FORMAT(column, '%Y-%m-%d')
     *
     * @param column 列名
     * @return SQL 片段
     */
    public static String formatDate(String column) {
        return formatDate(getDialect(), column);
    }

    /**
     * 按指定方言生成 yyyy-MM-dd 字符串，保留各分支原有格式。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @return SQL 片段
     */
    static String formatDate(Dialect dialect, String column) {
        checkColumn(column);
        switch (dialect) {
            case POSTGRESQL:
            case ORACLE:
                return "TO_CHAR(" + column + ", 'YYYY-MM-DD')";
            case SQLSERVER:
                return "CONVERT(varchar(10), " + column + ", 23)";
            default:
                return "DATE_FORMAT(" + column + ", '%Y-%m-%d')";
        }
    }

    /**
     * 按月格式化为 yyyyMM 字符串；原有片段为 DATE_FORMAT(column, '%Y%m')
     *
     * @param column 列名
     * @return SQL 片段
     */
    // TODO DONE @AI：用不到的，可以暂时先去掉噢； CRM 业绩统计使用 yyyyMM，商城交易统计使用 yyyy-MM，均需保留。
    public static String formatYearMonth(String column) {
        return formatYearMonth(getDialect(), column);
    }

    /**
     * 按指定方言生成 yyyyMM 字符串，供 CRM 按月统计使用。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @return SQL 片段
     */
    static String formatYearMonth(Dialect dialect, String column) {
        checkColumn(column);
        switch (dialect) {
            case POSTGRESQL:
            case ORACLE:
                return "TO_CHAR(" + column + ", 'YYYYMM')";
            case SQLSERVER:
                return "CONVERT(varchar(6), " + column + ", 112)";
            default:
                return "DATE_FORMAT(" + column + ", '%Y%m')";
        }
    }

    /**
     * 按月格式化为 yyyy-MM 字符串；原有片段为 CONCAT(EXTRACT(YEAR ...), '-', LPAD(...))
     *
     * @param column 列名
     * @return SQL 片段
     */
    // TODO DONE @AI：用不到的，可以暂时先去掉噢； CRM 业绩统计使用 yyyyMM，商城交易统计使用 yyyy-MM，均需保留。
    public static String formatYearDashMonth(String column) {
        return formatYearDashMonth(getDialect(), column);
    }

    /**
     * 按指定方言生成 yyyy-MM 字符串，供交易统计月份标签使用。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @return SQL 片段
     */
    static String formatYearDashMonth(Dialect dialect, String column) {
        checkColumn(column);
        switch (dialect) {
            case POSTGRESQL:
            case ORACLE:
                return "TO_CHAR(" + column + ", 'YYYY-MM')";
            case SQLSERVER:
                return "CONVERT(varchar(7), " + column + ", 120)";
            default:
                return "CONCAT(EXTRACT(YEAR FROM " + column + "), '-', LPAD(CONCAT(EXTRACT(MONTH FROM " + column + ")), 2, '0'))";
        }
    }

    /**
     * 按天分组；原有片段为 DATE(column)，新方言输出 yyyy-MM-dd 字符串
     *
     * 适用于同时兼容日期值与 yyyy-MM-dd 字符串的消费者
     *
     * @param column 列名
     * @return SQL 片段
     */
    public static String date(String column) {
        return date(getDialect(), column);
    }

    /**
     * 生成按天分组表达式；原路径保留 DATE，新分支返回日期文本。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @return SQL 片段
     */
    static String date(Dialect dialect, String column) {
        checkColumn(column);
        if (dialect == Dialect.ORIGINAL) {
            return "DATE(" + column + ")";
        }
        return formatDate(dialect, column);
    }

    // TODO DONE @AI：用不到的，可以先清理掉噢； AI1 首页与交易统计的 String 日期消费者仍在使用，保留。
    /**
     * 按天分组；原有片段为 CAST(column AS DATE)，新方言输出 yyyy-MM-dd 字符串
     *
     * 适用于 String 类型的消费者
     *
     * @param column 列名
     * @return SQL 片段
     */
    public static String castDateText(String column) {
        return castDateText(getDialect(), column);
    }

    /**
     * 生成 String 日期消费者所需的转换表达式，保留原路径 CAST AS DATE。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @return SQL 片段
     */
    static String castDateText(Dialect dialect, String column) {
        checkColumn(column);
        if (dialect == Dialect.ORIGINAL) {
            return "CAST(" + column + " AS DATE)";
        }
        return formatDate(dialect, column);
    }

    // ========== 日期值 ==========

    /**
     * 按天截断为日期值；原有片段为 CAST(column AS DATE)
     *
     * 适用于 LocalDate 类型的消费者
     *
     * @param column 列名
     * @return SQL 片段
     */
    // TODO DONE @AI：多余的是不是去掉？ 会员统计的 LocalDate 消费者仍在使用，保留 DATE 类型转换。
    public static String castDate(String column) {
        return castDate(getDialect(), column);
    }

    // TODO DONE @AI：方法注释；已补齐方言重载与内部辅助方法的参数、返回值及边界。
    /**
     * 生成 LocalDate 消费者所需的日期值；Oracle 用 TRUNC 去掉时分秒。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @return SQL 片段
     */
    static String castDate(Dialect dialect, String column) {
        checkColumn(column);
        if (dialect == Dialect.ORACLE) {
            // Oracle 的 DATE 包含时分秒，CAST AS DATE 不会截断
            return "TRUNC(" + column + ")";
        }
        return "CAST(" + column + " AS DATE)";
    }

    // ========== 日期部件 ==========

    /**
     * 月份（1-12）；原有片段为 MONTH(column)
     *
     * @param column 列名
     * @return SQL 片段
     */
    public static String month(String column) {
        return month(getDialect(), column);
    }

    /**
     * 按指定方言提取月份。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @return 月份 SQL 表达式
     */
    static String month(Dialect dialect, String column) {
        return datePart(dialect, column, "MONTH");
    }

    /**
     * 年份；原有片段为 YEAR(column)
     *
     * @param column 列名
     * @return SQL 片段
     */
    // TODO DONE @AI：方法注释；已补齐方言重载与内部辅助方法的参数、返回值及边界。
    public static String year(String column) {
        return year(getDialect(), column);
    }

    /**
     * 按指定方言提取年份，供 CRM 业绩目标筛选使用。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @return 年份 SQL 表达式
     */
    static String year(Dialect dialect, String column) {
        return datePart(dialect, column, "YEAR");
    }

    /**
     * 日（1-31）；原有片段为 DAYOFMONTH(column)
     *
     * @param column 列名
     * @return SQL 片段
     */
    public static String dayOfMonth(String column) {
        return dayOfMonth(getDialect(), column);
    }

    // TODO DONE @AI：方法注释；已补齐方言重载与内部辅助方法的参数、返回值及边界。
    /**
     * 按指定方言提取月内日，原路径保留 DAYOFMONTH。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @return 日 SQL 表达式
     */
    static String dayOfMonth(Dialect dialect, String column) {
        checkColumn(column);
        if (dialect == Dialect.ORIGINAL) {
            return "DAYOFMONTH(" + column + ")";
        }
        return datePart(dialect, column, "DAY");
    }

    /**
     * 日期部件：PostgreSQL 的 EXTRACT 返回 numeric，需要转为整数；原有片段与 SQL Server 均为 field(column)
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @param field 代码中固定的 YEAR、MONTH 或 DAY
     * @return 日期部件 SQL 表达式
     */
    private static String datePart(Dialect dialect, String column, String field) {
        checkColumn(column);
        switch (dialect) {
            case POSTGRESQL:
                return "CAST(EXTRACT(" + field + " FROM " + column + ") AS INTEGER)";
            case ORACLE:
                return "EXTRACT(" + field + " FROM " + column + ")";
            default:
                return field + "(" + column + ")";
        }
    }

    // ========== 聚合 ==========

    /**
     * 平均成交周期（天）：两个时间之间的完整天数（向零截断）求平均，保留 1 位小数（截断），无数据时为 0
     *
     * 原有片段为 COALESCE(TRUNCATE(AVG(TIMESTAMPDIFF(DAY, begin, end)), 1), 0)。
     * 注意：仅 PostgreSQL 输出新片段；Oracle、SQL Server 的完整天数表达式依赖实际列类型（DATE / TIMESTAMP / datetime2 / date 等），
     * 尚未依据实际 DDL 在真实数据库验收，因此保持原有片段
     *
     * @param beginColumn 开始时间列名
     * @param endColumn   结束时间列名
     * @return SQL 片段
     */
    public static String customerDealCycle(String beginColumn, String endColumn) {
        return customerDealCycle(getDialect(), beginColumn, endColumn);
    }

    /**
     * 按指定方言生成平均完整天数；未验收的数据库继续使用原表达式。
     *
     * @param dialect 已识别的数据库方言
     * @param beginColumn 代码中固定的开始时间列名
     * @param endColumn 代码中固定的结束时间列名
     * @return 平均成交周期 SQL 表达式
     */
    static String customerDealCycle(Dialect dialect, String beginColumn, String endColumn) {
        checkColumn(beginColumn);
        checkColumn(endColumn);
        if (dialect == Dialect.POSTGRESQL) {
            return "COALESCE(TRUNC(AVG(CAST(EXTRACT(DAY FROM (CAST(" + endColumn + " AS TIMESTAMP) - CAST("
                    + beginColumn + " AS TIMESTAMP))) AS NUMERIC)), 1), 0)";
        }
        return "COALESCE(TRUNCATE(AVG(TIMESTAMPDIFF(DAY, " + beginColumn + ", " + endColumn + ")), 1), 0)";
    }

    // ========== 其它 ==========

    /**
     * 顶层 SELECT 无 FROM 时的补充：Oracle 为 FROM DUAL，其它为空
     *
     * @return SQL 片段
     */
    public static String fromDual() {
        return fromDual(getDialect());
    }

    /**
     * 为无顶层 FROM 的标量汇总补充 Oracle DUAL，其他分支保持空文本。
     *
     * @param dialect 已识别的数据库方言
     * @return Oracle 为 FROM DUAL，其他为空字符串
     */
    static String fromDual(Dialect dialect) {
        return dialect == Dialect.ORACLE ? "FROM DUAL" : "";
    }

    // ========== 行数限制 ==========

    /**
     * 只返回前 rows 行，追加在语句末尾（ORDER BY 之后）；原有片段为 LIMIT rows
     *
     * 仅用于 XML 或注解中手写的 SQL；使用 Wrapper 查询时，使用 BaseMapperX 的 selectListLimitN(queryWrapper, limit) 分页查询
     *
     * 1. Oracle：FETCH FIRST rows ROWS ONLY，要求 Oracle 12c+
     * 2. SQL Server 2012+：OFFSET 0 ROWS FETCH NEXT rows ROWS ONLY；OFFSET 必须跟在 ORDER BY 之后，
     *    语句没有排序时补充 ORDER BY (SELECT NULL)，即不保证返回哪几行，与原有 LIMIT 无排序时一致
     *
     * @param rows    行数，不能为负数；SQL Server 的 FETCH 不支持 0 行
     * @param ordered 语句是否已有 ORDER BY
     * @return SQL 片段
     */
    public static String limitRows(int rows, boolean ordered) {
        return limitRows(getDialect(), rows, ordered);
    }

    /**
     * 生成固定行数限制；负数拒绝，SQL Server 的 FETCH 不接受 0 行。
     *
     * @param dialect 已识别的数据库方言
     * @param rows 固定行数
     * @param ordered 原查询是否已有排序
     * @return 追加在查询末尾的 SQL 片段
     */
    static String limitRows(Dialect dialect, int rows, boolean ordered) {
        if (rows < 0) {
            throw new IllegalArgumentException("行数不能为负数：" + rows);
        }
        if (rows == 0 && dialect == Dialect.SQLSERVER) {
            throw new IllegalArgumentException("SQL Server 的 FETCH 行数必须大于 0");
        }
        return limit(dialect, String.valueOf(rows), ordered);
    }

    /**
     * 只返回前 #{param} 行，行数使用 #{} 绑定；规则同 {@link #limitRows(int, boolean)}
     *
     * 在 XML 或注解中使用：${@cn.iocoder.yudao.framework.mybatis.core.util.SqlDialectUtils@limitParam('limit', true)}
     *
     * @param param   行数的参数名，例如 limit
     * @param ordered 语句是否已有 ORDER BY
     * @return SQL 片段
     */
    public static String limitParam(String param, boolean ordered) {
        return limitParam(getDialect(), param, ordered);
    }

    /**
     * 生成绑定参数的行数限制，参数名通过白名单校验。
     *
     * @param dialect 已识别的数据库方言
     * @param param 行数参数名或属性路径
     * @param ordered 原查询是否已有排序
     * @return 包含 MyBatis 参数占位的 SQL 片段
     */
    static String limitParam(Dialect dialect, String param, boolean ordered) {
        checkParam(param);
        return limit(dialect, "#{" + param + "}", ordered);
    }

    /**
     * 拼装行数限制；rows 仅来自已校验的整数或 MyBatis 参数占位。
     *
     * @param dialect 已识别的数据库方言
     * @param rows 固定整数文本或已校验的参数占位
     * @param ordered 原查询是否已有排序
     * @return 行数限制 SQL 片段
     */
    private static String limit(Dialect dialect, String rows, boolean ordered) {
        switch (dialect) {
            case ORACLE:
                return "FETCH FIRST " + rows + " ROWS ONLY";
            case SQLSERVER:
                return (ordered ? "" : "ORDER BY (SELECT NULL) ") + "OFFSET 0 ROWS FETCH NEXT " + rows + " ROWS ONLY";
            default:
                return "LIMIT " + rows;
        }
    }

    // ========== 分批删除 ==========

    /**
     * 分批删除 column &lt; #{valueParam} 的记录，每次最多删除 #{limitParam} 行；
     * 原有语句为 DELETE FROM table WHERE column &lt; #{valueParam} LIMIT #{limitParam}
     *
     * 1. PostgreSQL：DELETE 不支持 LIMIT，按主键 id 的子查询选出一批
     * 2. Oracle：ROWNUM &lt;= #{limitParam}
     * 3. SQL Server 2012+：按主键 id 的子查询 SELECT TOP (#{limitParam})。
     *    不使用 DELETE TOP (n)，因为 JSqlParser 无法解析，租户、数据权限插件解析 DELETE 时会失败
     *
     * 要求表的主键列为 id；表名、列名只能是代码中固定的常量，值均使用 #{} 绑定
     *
     * 在注解中使用：${@cn.iocoder.yudao.framework.mybatis.core.util.SqlDialectUtils@deleteBatchByLessThan('infra_job_log', 'create_time', 'createTime', 'limit')}
     *
     * @param table      表名
     * @param column     比较的列名
     * @param valueParam 比较值的参数名
     * @param limitParam 每批行数的参数名
     * @return SQL 语句
     */
    public static String deleteBatchByLessThan(String table, String column, String valueParam, String limitParam) {
        return deleteBatchByLessThan(getDialect(), table, column, valueParam, limitParam);
    }

    /**
     * 按方言生成主键为 id 的分批物理删除；当前用于忽略租户的全局清理任务。
     *
     * @param dialect 已识别的数据库方言
     * @param table 代码中固定的表名
     * @param column 代码中固定的时间列名
     * @param valueParam 时间阈值参数名
     * @param limitParam 每批行数参数名
     * @return 参数绑定的 DELETE 语句
     */
    static String deleteBatchByLessThan(Dialect dialect, String table, String column,
                                        String valueParam, String limitParam) {
        checkColumn(table);
        checkColumn(column);
        checkParam(valueParam);
        checkParam(limitParam);
        String condition = column + " < #{" + valueParam + "}";
        String limit = "#{" + limitParam + "}";
        switch (dialect) {
            case POSTGRESQL:
                return "DELETE FROM " + table + " WHERE id IN (SELECT id FROM " + table
                        + " WHERE " + condition + " LIMIT " + limit + ")";
            case ORACLE:
                return "DELETE FROM " + table + " WHERE " + condition + " AND ROWNUM <= " + limit;
            case SQLSERVER:
                return "DELETE FROM " + table + " WHERE id IN (SELECT TOP (" + limit + ") id FROM " + table
                        + " WHERE " + condition + ")";
            default:
                return "DELETE FROM " + table + " WHERE " + condition + " LIMIT " + limit;
        }
    }

    // ========== 方言识别 ==========

    /**
     * 取得当前执行方言；事务内复用已绑定连接，非事务按路由后的数据源缓存。
     *
     * @return 当前执行方言
     */
    static Dialect getDialect() {
        Connection connection = JdbcUtils.getTransactionalConnection();
        if (connection != null) {
            try {
                // 使用真正执行 SQL 的事务连接；不能将其方言缓存到事务内新切换的路由键上
                return resolveDialect(connection.getMetaData());
            } catch (SQLException e) {
                throw new IllegalStateException("获取数据库方言失败", e);
            }
        }
        return getDialect(JdbcUtils.getDataSource());
    }

    /**
     * 按数据源实例缓存方言；识别失败时不写入缓存。
     *
     * @param dataSource 实际路由到的数据源
     * @return 数据库方言
     */
    static Dialect getDialect(DataSource dataSource) {
        return DIALECT_CACHE.computeIfAbsent(dataSource, SqlDialectUtils::detectDialect);
    }

    /**
     * 借用一个连接识别数据源方言，并关闭该次自行获取的连接。
     *
     * @param dataSource 待识别的数据源
     * @return 数据库方言
     */
    private static Dialect detectDialect(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            return resolveDialect(conn.getMetaData());
        } catch (SQLException e) {
            throw new IllegalStateException("获取数据库方言失败", e);
        }
    }

    /**
     * 根据数据库元数据识别方言
     *
     * 注意：使用 PostgreSQL 官方驱动（jdbc:postgresql:）连接 openGauss、瀚高等兼容 PostgreSQL 的数据库时，
     * 产品名与 URL 均与 PostgreSQL 相同，无法区分，会按 PostgreSQL 处理
     *
     * @param metaData 数据库元数据
     * @return 方言
     * @throws SQLException 读取元数据失败
     */
    static Dialect resolveDialect(DatabaseMetaData metaData) throws SQLException {
        String productName = metaData.getDatabaseProductName();
        String url = StrUtil.nullToEmpty(metaData.getURL()).toLowerCase(Locale.ROOT);
        DbType dbType = DbTypeEnum.find(productName);
        if (dbType == DbType.ORACLE) {
            return Dialect.ORACLE;
        }
        if (dbType == DbType.SQL_SERVER) {
            return metaData.getDatabaseMajorVersion() >= SQL_SERVER_2012_MAJOR_VERSION
                    ? Dialect.SQLSERVER : Dialect.ORIGINAL;
        }
        if (dbType == DbType.POSTGRE_SQL) {
            if (isPostgreSQLVariantUrl(url)) {
                return Dialect.ORIGINAL;
            }
            if (url.contains(":postgresql:")) {
                return Dialect.POSTGRESQL;
            }
            throw unsupported(productName);
        }
        if (ORIGINAL_DB_TYPES.contains(dbType)) {
            return Dialect.ORIGINAL;
        }
        // 产品名未登记时，仅按明确的原生驱动 URL 识别瀚高、MogDB 等，以及 MariaDB
        if (dbType == null && (isPostgreSQLVariantUrl(url) || url.contains(":mariadb:"))) {
            return Dialect.ORIGINAL;
        }
        throw unsupported(productName);
    }

    /**
     * 判断 URL 是否为需要保留原 SQL 路径的 PostgreSQL 兼容产品驱动。
     *
     * @param url 转为小写的 JDBC URL
     * @return 是否命中已登记的兼容产品 URL
     */
    private static boolean isPostgreSQLVariantUrl(String url) {
        return POSTGRESQL_VARIANT_URL_PATTERN.matcher(url).find();
    }

    /**
     * 创建未识别产品的异常，不将完整 JDBC URL 写入消息。
     *
     * @param productName 数据库产品名
     * @return 不支持该产品的异常
     */
    private static IllegalStateException unsupported(String productName) {
        return new IllegalStateException("SQL 方言不支持的数据库产品：" + productName);
    }

    /**
     * 校验代码中固定的表名或列名，拒绝表达式与语句片段。
     *
     * @param column 待校验的表名或列名
     */
    private static void checkColumn(String column) {
        if (!MyBatisUtils.isSafeColumnName(column)) {
            throw new IllegalArgumentException("Invalid column name: " + column);
        }
    }

    /**
     * 校验 MyBatis 参数名或属性路径，拒绝占位符逃逸与 SQL 片段。
     *
     * @param param 待校验的参数名
     */
    private static void checkParam(String param) {
        if (param == null || !PARAM_NAME_PATTERN.matcher(param).matches()) {
            throw new IllegalArgumentException("Invalid param name: " + param);
        }
    }

}
