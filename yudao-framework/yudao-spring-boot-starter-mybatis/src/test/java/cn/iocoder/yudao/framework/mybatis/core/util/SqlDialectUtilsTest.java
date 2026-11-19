package cn.iocoder.yudao.framework.mybatis.core.util;

import cn.hutool.core.exceptions.UtilException;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.mybatis.core.type.OracleClobTypeHandler;
import cn.iocoder.yudao.framework.mybatis.core.util.SqlDialectUtils.Dialect;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

/**
 * {@link SqlDialectUtils} 的单元测试
 *
 * @author 芋道源码
 */
public class SqlDialectUtilsTest {

    private static final String P = "${@cn.iocoder.yudao.framework.mybatis.core.util.SqlDialectUtils@";

    /**
     * 输出日期运算片段的数据库类型
     */
    private static final List<DbType> DATE_DB_TYPES = List.of(DbType.MYSQL, DbType.MARIADB, DbType.OCEAN_BASE,
            DbType.H2, DbType.DM, DbType.POSTGRE_SQL, DbType.KINGBASE_ES, DbType.OPENGAUSS, DbType.GAUSS_DB,
            DbType.ORACLE, DbType.SQL_SERVER, DbType.OSCAR);

    private ConfigurableListableBeanFactory originBeanFactory;

    @BeforeEach
    public void setUp() {
        try {
            originBeanFactory = SpringUtil.getConfigurableBeanFactory();
        } catch (UtilException e) {
            originBeanFactory = null;
        }
    }

    @AfterEach
    public void tearDown() {
        DynamicDataSourceContextHolder.clear();
        new SpringUtil().postProcessBeanFactory(originBeanFactory);
    }

    // ========== SQL 片段 ==========

    @Test
    public void testFormatDate() {
        assertEquals("DATE_FORMAT(create_time, '%Y-%m-%d')", SqlDialectUtils.formatDate(Dialect.ORIGINAL, "create_time"));
        assertEquals("TO_CHAR(t.create_time, 'YYYY-MM-DD')", SqlDialectUtils.formatDate(Dialect.POSTGRESQL, "t.create_time"));
        assertEquals("TO_CHAR(create_time, 'YYYY-MM-DD')", SqlDialectUtils.formatDate(Dialect.ORACLE, "create_time"));
        assertEquals("CONVERT(varchar(10), create_time, 23)", SqlDialectUtils.formatDate(Dialect.SQLSERVER, "create_time"));
        // 达梦：原有片段；神通：没有 DATE_FORMAT
        assertEquals("DATE_FORMAT(create_time, '%Y-%m-%d')", SqlDialectUtils.formatDate(Dialect.DM, "create_time"));
        assertEquals("TO_CHAR(create_time, 'YYYY-MM-DD')", SqlDialectUtils.formatDate(Dialect.OSCAR, "create_time"));
    }

    @Test
    public void testFormatYearMonth() {
        assertEquals("DATE_FORMAT(order_date, '%Y%m')", SqlDialectUtils.formatYearMonth(Dialect.ORIGINAL, "order_date"));
        assertEquals("TO_CHAR(order_date, 'YYYYMM')", SqlDialectUtils.formatYearMonth(Dialect.POSTGRESQL, "order_date"));
        assertEquals("TO_CHAR(order_date, 'YYYYMM')", SqlDialectUtils.formatYearMonth(Dialect.ORACLE, "order_date"));
        assertEquals("CONVERT(varchar(6), order_date, 112)", SqlDialectUtils.formatYearMonth(Dialect.SQLSERVER, "order_date"));
        assertEquals("DATE_FORMAT(order_date, '%Y%m')", SqlDialectUtils.formatYearMonth(Dialect.DM, "order_date"));
        assertEquals("TO_CHAR(order_date, 'YYYYMM')", SqlDialectUtils.formatYearMonth(Dialect.OSCAR, "order_date"));
    }

    @Test
    public void testFormatYearDashMonth() {
        // 原有片段：与 formatDate、formatYearMonth 同族的 DATE_FORMAT；%m 为补零的两位月份（%c 不补零），不再使用达梦无法解析的单参数 CONCAT
        assertEquals("DATE_FORMAT(pay_time, '%Y-%m')", SqlDialectUtils.formatYearDashMonth(Dialect.ORIGINAL, "pay_time"));
        assertEquals("DATE_FORMAT(pay_time, '%Y-%m')", SqlDialectUtils.formatYearDashMonth(Dialect.H2, "pay_time"));
        assertEquals("TO_CHAR(pay_time, 'YYYY-MM')", SqlDialectUtils.formatYearDashMonth(Dialect.POSTGRESQL, "pay_time"));
        assertEquals("TO_CHAR(pay_time, 'YYYY-MM')", SqlDialectUtils.formatYearDashMonth(Dialect.ORACLE, "pay_time"));
        assertEquals("CONVERT(varchar(7), pay_time, 120)", SqlDialectUtils.formatYearDashMonth(Dialect.SQLSERVER, "pay_time"));
        assertEquals("DATE_FORMAT(pay_time, '%Y-%m')", SqlDialectUtils.formatYearDashMonth(Dialect.DM, "pay_time"));
        assertEquals("TO_CHAR(pay_time, 'YYYY-MM')", SqlDialectUtils.formatYearDashMonth(Dialect.OSCAR, "pay_time"));
        for (Dialect dialect : Dialect.values()) {
            assertFalse(SqlDialectUtils.formatYearDashMonth(dialect, "pay_time").contains("CONCAT("), dialect.name());
        }
    }

    @Test
    public void testDate() {
        // 原有片段：标准 CAST AS DATE，不再使用达梦不支持的 DATE 函数
        assertEquals("CAST(send_time AS DATE)", SqlDialectUtils.date(Dialect.ORIGINAL, "send_time"));
        assertEquals("TO_CHAR(send_time, 'YYYY-MM-DD')", SqlDialectUtils.date(Dialect.POSTGRESQL, "send_time"));
        assertEquals("TO_CHAR(send_time, 'YYYY-MM-DD')", SqlDialectUtils.date(Dialect.ORACLE, "send_time"));
        assertEquals("CONVERT(varchar(10), send_time, 23)", SqlDialectUtils.date(Dialect.SQLSERVER, "send_time"));
        // 与 castDateText 的契约一致：原路径返回日期值，新方言返回 yyyy-MM-dd 文本
        for (Dialect dialect : Dialect.values()) {
            assertEquals(SqlDialectUtils.castDateText(dialect, "t.send_time"), SqlDialectUtils.date(dialect, "t.send_time"));
        }
    }

    @Test
    public void testCastDateText() {
        assertEquals("CAST(create_time AS DATE)", SqlDialectUtils.castDateText(Dialect.ORIGINAL, "create_time"));
        assertEquals("TO_CHAR(create_time, 'YYYY-MM-DD')", SqlDialectUtils.castDateText(Dialect.POSTGRESQL, "create_time"));
        assertEquals("TO_CHAR(create_time, 'YYYY-MM-DD')", SqlDialectUtils.castDateText(Dialect.ORACLE, "create_time"));
        assertEquals("CONVERT(varchar(10), create_time, 23)", SqlDialectUtils.castDateText(Dialect.SQLSERVER, "create_time"));
        assertEquals("CAST(create_time AS DATE)", SqlDialectUtils.castDateText(Dialect.DM, "create_time"));
        // 神通：CAST AS DATE 保留时分秒，不能按天分组
        assertEquals("TO_CHAR(create_time, 'YYYY-MM-DD')", SqlDialectUtils.castDateText(Dialect.OSCAR, "create_time"));
    }

    @Test
    public void testCastDate() {
        assertEquals("CAST(create_time AS DATE)", SqlDialectUtils.castDate(Dialect.ORIGINAL, "create_time"));
        assertEquals("CAST(create_time AS DATE)", SqlDialectUtils.castDate(Dialect.POSTGRESQL, "create_time"));
        assertEquals("TRUNC(create_time)", SqlDialectUtils.castDate(Dialect.ORACLE, "create_time"));
        assertEquals("CAST(create_time AS DATE)", SqlDialectUtils.castDate(Dialect.SQLSERVER, "create_time"));
        assertEquals("CAST(create_time AS DATE)", SqlDialectUtils.castDate(Dialect.DM, "create_time"));
        assertEquals("TRUNC(create_time)", SqlDialectUtils.castDate(Dialect.OSCAR, "create_time"));
    }

    @Test
    public void testDayStart() {
        assertEquals("CAST(t.create_time AS DATE)", SqlDialectUtils.dayStart(Dialect.ORIGINAL, "t.create_time"));
        assertEquals("CAST(CAST(t.create_time AS DATE) AS TIMESTAMP)", SqlDialectUtils.dayStart(Dialect.POSTGRESQL, "t.create_time"));
        assertEquals("CAST(TRUNC(t.create_time) AS TIMESTAMP)", SqlDialectUtils.dayStart(Dialect.ORACLE, "t.create_time"));
        assertEquals("CAST(CAST(t.create_time AS DATE) AS datetime2)", SqlDialectUtils.dayStart(Dialect.SQLSERVER, "t.create_time"));
        assertEquals("CAST(t.create_time AS DATE)", SqlDialectUtils.dayStart(Dialect.H2, "t.create_time"));
        assertEquals("CAST(t.create_time AS DATE)", SqlDialectUtils.dayStart(Dialect.DM, "t.create_time"));
        assertEquals("CAST(TRUNC(t.create_time) AS TIMESTAMP)", SqlDialectUtils.dayStart(Dialect.OSCAR, "t.create_time"));
    }

    @Test
    public void testMonth() {
        assertEquals("MONTH(order_date)", SqlDialectUtils.month(Dialect.ORIGINAL, "order_date"));
        assertEquals("CAST(EXTRACT(MONTH FROM order_date) AS INTEGER)", SqlDialectUtils.month(Dialect.POSTGRESQL, "order_date"));
        assertEquals("EXTRACT(MONTH FROM order_date)", SqlDialectUtils.month(Dialect.ORACLE, "order_date"));
        assertEquals("MONTH(order_date)", SqlDialectUtils.month(Dialect.SQLSERVER, "order_date"));
        assertEquals("MONTH(order_date)", SqlDialectUtils.month(Dialect.DM, "order_date"));
        assertEquals("EXTRACT(MONTH FROM order_date)", SqlDialectUtils.month(Dialect.OSCAR, "order_date"));
    }

    @Test
    public void testYear() {
        assertEquals("YEAR(return_time)", SqlDialectUtils.year(Dialect.ORIGINAL, "return_time"));
        assertEquals("CAST(EXTRACT(YEAR FROM return_time) AS INTEGER)", SqlDialectUtils.year(Dialect.POSTGRESQL, "return_time"));
        assertEquals("EXTRACT(YEAR FROM return_time)", SqlDialectUtils.year(Dialect.ORACLE, "return_time"));
        assertEquals("YEAR(return_time)", SqlDialectUtils.year(Dialect.SQLSERVER, "return_time"));
        assertEquals("YEAR(return_time)", SqlDialectUtils.year(Dialect.DM, "return_time"));
        assertEquals("EXTRACT(YEAR FROM return_time)", SqlDialectUtils.year(Dialect.OSCAR, "return_time"));
    }

    @Test
    public void testDayOfMonth() {
        assertEquals("DAYOFMONTH(birthday)", SqlDialectUtils.dayOfMonth(Dialect.ORIGINAL, "birthday"));
        assertEquals("CAST(EXTRACT(DAY FROM birthday) AS INTEGER)", SqlDialectUtils.dayOfMonth(Dialect.POSTGRESQL, "birthday"));
        assertEquals("EXTRACT(DAY FROM birthday)", SqlDialectUtils.dayOfMonth(Dialect.ORACLE, "birthday"));
        assertEquals("DAY(birthday)", SqlDialectUtils.dayOfMonth(Dialect.SQLSERVER, "birthday"));
        assertEquals("DAYOFMONTH(birthday)", SqlDialectUtils.dayOfMonth(Dialect.DM, "birthday"));
        // 神通：没有 DAY 函数
        assertEquals("EXTRACT(DAY FROM birthday)", SqlDialectUtils.dayOfMonth(Dialect.OSCAR, "birthday"));
    }

    @Test
    public void testHour() {
        assertEquals("HOUR(t.create_time)", SqlDialectUtils.hour(Dialect.ORIGINAL, "t.create_time"));
        assertEquals("CAST(EXTRACT(HOUR FROM t.create_time) AS INTEGER)", SqlDialectUtils.hour(Dialect.POSTGRESQL, "t.create_time"));
        // Oracle：DATE 不支持 EXTRACT(HOUR)，先转为 TIMESTAMP(9)，避免默认精度 6 将小数秒进位到下一小时
        assertEquals("EXTRACT(HOUR FROM CAST(t.create_time AS TIMESTAMP(9)))", SqlDialectUtils.hour(Dialect.ORACLE, "t.create_time"));
        assertEquals("DATEPART(HOUR, t.create_time)", SqlDialectUtils.hour(Dialect.SQLSERVER, "t.create_time"));
        assertEquals("HOUR(t.create_time)", SqlDialectUtils.hour(Dialect.H2, "t.create_time"));
        assertEquals("HOUR(t.create_time)", SqlDialectUtils.hour(Dialect.DM, "t.create_time"));
        assertEquals("HOUR(t.create_time)", SqlDialectUtils.hour(Dialect.OSCAR, "t.create_time"));
    }

    @Test
    public void testCustomerDealCycle() {
        String original = "COALESCE(TRUNCATE(AVG(TIMESTAMPDIFF(DAY, customer.create_time, contract.order_date)), 1), 0)";
        assertEquals(original, SqlDialectUtils.customerDealCycle(Dialect.ORIGINAL, "customer.create_time", "contract.order_date"));
        // PostgreSQL：两个 TIMESTAMP 相减为 interval，EXTRACT(DAY) 即完整天数（向零截断、保留负号），TRUNC 截断保留 1 位
        assertEquals("COALESCE(TRUNC(AVG(CAST(EXTRACT(DAY FROM (CAST(contract.order_date AS TIMESTAMP) - "
                        + "CAST(customer.create_time AS TIMESTAMP))) AS NUMERIC)), 1), 0)",
                SqlDialectUtils.customerDealCycle(Dialect.POSTGRESQL, "customer.create_time", "contract.order_date"));
        // Oracle：升格为 TIMESTAMP(9)，DATE 与 TIMESTAMP(9) 均不丢失精度，INTERVAL 的 DAY 部分即完整天数
        assertEquals("COALESCE(TRUNC(AVG(EXTRACT(DAY FROM (CAST(contract.order_date AS TIMESTAMP(9)) - "
                        + "CAST(customer.create_time AS TIMESTAMP(9))))), 1), 0)",
                SqlDialectUtils.customerDealCycle(Dialect.ORACLE, "customer.create_time", "contract.order_date"));
        // SQL Server：DATEDIFF(DAY) 按时刻向零修正，DECIMAL 平均，ROUND(..., 1, 1) 截断
        String beginTime = "CAST(CAST(customer.create_time AS datetime2(7)) AS time(7))";
        String endTime = "CAST(CAST(contract.order_date AS datetime2(7)) AS time(7))";
        String days = "DATEDIFF(DAY, customer.create_time, contract.order_date)";
        assertEquals("COALESCE(ROUND(AVG(CAST((CASE WHEN contract.order_date >= customer.create_time THEN "
                        + days + " - CASE WHEN " + endTime + " < " + beginTime + " THEN 1 ELSE 0 END ELSE "
                        + days + " + CASE WHEN " + endTime + " > " + beginTime + " THEN 1 ELSE 0 END END)"
                        + " AS DECIMAL(19, 4))), 1, 1), 0)",
                SqlDialectUtils.customerDealCycle(Dialect.SQLSERVER, "customer.create_time", "contract.order_date"));
        assertEquals(original, SqlDialectUtils.customerDealCycle(Dialect.DM, "customer.create_time", "contract.order_date"));
        // 神通：没有 TIMESTAMPDIFF，两个零点之间的 DATEDIFF('DAY') 按 time(6) 的时刻向零修正，NUMBER 平均，TRUNC 截断
        String oscarBeginTime = "CAST(customer.create_time AS TIME(6))";
        String oscarEndTime = "CAST(contract.order_date AS TIME(6))";
        String oscarDays = "DATEDIFF('DAY', TRUNC(customer.create_time), TRUNC(contract.order_date))";
        assertEquals("COALESCE(TRUNC(AVG(CAST((CASE WHEN contract.order_date >= customer.create_time THEN "
                        + oscarDays + " - CASE WHEN " + oscarEndTime + " < " + oscarBeginTime + " THEN 1 ELSE 0 END ELSE "
                        + oscarDays + " + CASE WHEN " + oscarEndTime + " > " + oscarBeginTime + " THEN 1 ELSE 0 END END)"
                        + " AS NUMBER)), 1), 0)",
                SqlDialectUtils.customerDealCycle(Dialect.OSCAR, "customer.create_time", "contract.order_date"));
    }

    @Test
    public void testFromDual() {
        assertEquals("", SqlDialectUtils.fromDual(Dialect.ORIGINAL));
        assertEquals("", SqlDialectUtils.fromDual(Dialect.POSTGRESQL));
        assertEquals("FROM DUAL", SqlDialectUtils.fromDual(Dialect.ORACLE));
        assertEquals("", SqlDialectUtils.fromDual(Dialect.SQLSERVER));
        assertEquals("", SqlDialectUtils.fromDual(Dialect.OSCAR));
    }

    @Test
    public void testAuditUserId() {
        // 原有片段：MySQL、H2 共用，DECIMAL(20, 0) 可容纳 Long 最大值
        assertEquals("CAST(CASE WHEN t.creator NOT REGEXP '[^0-9]' THEN NULLIF(t.creator, '') END AS DECIMAL(20, 0))",
                SqlDialectUtils.auditUserId(Dialect.ORIGINAL, "t.creator"));
        // PostgreSQL：转为 BIGINT，与 BIGINT 主键同类型比较
        assertEquals("CAST(CASE WHEN t.creator ~ '^[0-9]+$' THEN t.creator END AS BIGINT)",
                SqlDialectUtils.auditUserId(Dialect.POSTGRESQL, "t.creator"));
        assertEquals("CAST(CASE WHEN NOT REGEXP_LIKE(t.creator, '[^0-9]') THEN t.creator END AS NUMBER(20))",
                SqlDialectUtils.auditUserId(Dialect.ORACLE, "t.creator"));
        // SQL Server：TRY_CAST 的空串为 0，先转为 NULL
        assertEquals("TRY_CAST(NULLIF(t.updater, '') AS BIGINT)",
                SqlDialectUtils.auditUserId(Dialect.SQLSERVER, "t.updater"));
        // 达梦、神通：CAST 写在 CASE 之外
        for (Dialect dialect : List.of(Dialect.DM, Dialect.OSCAR)) {
            assertEquals("CAST(CASE WHEN NOT REGEXP_LIKE(t.creator, '[^0-9]') THEN NULLIF(t.creator, '') END AS BIGINT)",
                    SqlDialectUtils.auditUserId(dialect, "t.creator"));
        }
    }

    @Test
    public void testAuditUserId_invalidColumn() {
        for (Dialect dialect : Dialect.values()) {
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.auditUserId(dialect, "t.creator) OR (1=1"));
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.auditUserId(dialect, "'1'"));
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.auditUserId(dialect, ""));
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.auditUserId(dialect, null));
        }
    }

    /**
     * 与 CMS 标签、表单提交记录按名称排序相同的查询列，租户插件会解析查询列中的子查询，所有方言均需可被 JSqlParser 解析
     */
    @Test
    public void testAuditUserId_parsedByJSqlParser() throws Exception {
        for (Dialect dialect : Dialect.values()) {
            String sql = "SELECT t.*, (SELECT u.nickname FROM system_users u WHERE u.id = "
                    + SqlDialectUtils.auditUserId(dialect, "t.creator") + " AND u.deleted = 0) AS sorting_creator_name"
                    + " FROM cms_tag t WHERE t.deleted = 0 ORDER BY sorting_creator_name ASC";
            assertNotNull(CCJSqlParserUtil.parse(sql), dialect + " " + sql);
        }
    }

    @Test
    public void testAuditUserId_followsDynamicRouting() {
        FakeDataSource postgresql = new FakeDataSource("PostgreSQL", "jdbc:postgresql://127.0.0.1:5432/a", 17);
        FakeDataSource mysql = new FakeDataSource("MySQL", "jdbc:mysql://127.0.0.1:3306/a", 8);
        FakeDataSource sqlServer = new FakeDataSource("Microsoft SQL Server", "jdbc:sqlserver://127.0.0.1:1433", 16);
        mockDynamicRoutingDataSource(Map.of("master", postgresql, "mysql", mysql, "sqlserver", sqlServer));

        // 同一线程依次切换数据源，每次调用按当前数据源生成
        assertEquals(SqlDialectUtils.auditUserId(Dialect.POSTGRESQL, "t.creator"), SqlDialectUtils.auditUserId("t.creator"));
        DynamicDataSourceContextHolder.push("mysql");
        assertEquals(SqlDialectUtils.auditUserId(Dialect.ORIGINAL, "t.creator"), SqlDialectUtils.auditUserId("t.creator"));
        DynamicDataSourceContextHolder.push("sqlserver");
        assertEquals(SqlDialectUtils.auditUserId(Dialect.SQLSERVER, "t.creator"), SqlDialectUtils.auditUserId("t.creator"));
        DynamicDataSourceContextHolder.poll();
        DynamicDataSourceContextHolder.poll();
        assertEquals(SqlDialectUtils.auditUserId(Dialect.POSTGRESQL, "t.creator"), SqlDialectUtils.auditUserId("t.creator"));
    }

    @Test
    public void testInvalidColumnName() {
        List<BiFunction<Dialect, String, String>> fragments = List.of(SqlDialectUtils::formatDate,
                SqlDialectUtils::formatYearMonth, SqlDialectUtils::formatYearDashMonth, SqlDialectUtils::date,
                SqlDialectUtils::castDateText, SqlDialectUtils::castDate, SqlDialectUtils::dayStart,
                SqlDialectUtils::month, SqlDialectUtils::year, SqlDialectUtils::dayOfMonth, SqlDialectUtils::hour,
                SqlDialectUtils::stripHtmlTags);
        for (BiFunction<Dialect, String, String> fragment : fragments) {
            for (Dialect dialect : Dialect.values()) {
                assertThrows(IllegalArgumentException.class, () -> fragment.apply(dialect, "create_time) OR (1=1"));
                assertThrows(IllegalArgumentException.class, () -> fragment.apply(dialect, "create_time;drop table t"));
                assertThrows(IllegalArgumentException.class, () -> fragment.apply(dialect, "'2026-01-01'"));
                assertThrows(IllegalArgumentException.class, () -> fragment.apply(dialect, ""));
                assertThrows(IllegalArgumentException.class, () -> fragment.apply(dialect, null));
            }
        }
        for (Dialect dialect : Dialect.values()) {
            assertThrows(IllegalArgumentException.class,
                    () -> SqlDialectUtils.customerDealCycle(dialect, "customer.create_time", "NOW()"));
            assertThrows(IllegalArgumentException.class,
                    () -> SqlDialectUtils.customerDealCycle(dialect, "1) - (1", "contract.order_date"));
        }
    }

    /**
     * MyBatis Plus 租户等插件会使用 JSqlParser 解析最终 SQL，所有方言的片段均需可被解析
     */
    @Test
    public void testFragments_parsedByJSqlParser() throws Exception {
        for (Dialect dialect : Dialect.values()) {
            List<String> expressions = List.of(
                    SqlDialectUtils.formatDate(dialect, "t.create_time"),
                    SqlDialectUtils.formatYearMonth(dialect, "t.create_time"),
                    SqlDialectUtils.formatYearDashMonth(dialect, "t.create_time"),
                    SqlDialectUtils.date(dialect, "t.create_time"),
                    SqlDialectUtils.castDateText(dialect, "t.create_time"),
                    SqlDialectUtils.castDate(dialect, "t.create_time"),
                    SqlDialectUtils.dayStart(dialect, "t.create_time"),
                    SqlDialectUtils.month(dialect, "t.create_time"),
                    SqlDialectUtils.year(dialect, "t.create_time"),
                    SqlDialectUtils.dayOfMonth(dialect, "t.create_time"));
            for (String expression : expressions) {
                String sql = "SELECT " + expression + " AS statDate, COUNT(1) AS total FROM crm_customer t"
                        + " WHERE t.create_time >= ? GROUP BY " + expression + " ORDER BY statDate";
                assertNotNull(CCJSqlParserUtil.parse(sql), dialect + " " + expression);
            }
            String dealCycleSql = "SELECT customer.owner_user_id, "
                    + SqlDialectUtils.customerDealCycle(dialect, "customer.create_time", "contract.order_date")
                    + " AS customer_deal_cycle FROM crm_customer customer"
                    + " LEFT JOIN crm_contract contract ON contract.customer_id = customer.id"
                    + " GROUP BY customer.owner_user_id";
            assertNotNull(CCJSqlParserUtil.parse(dealCycleSql), dialect + " customerDealCycle");
            String summarySql = "SELECT (SELECT COUNT(1) FROM ai1_agent) AS agentCount, "
                    + "(SELECT COUNT(1) FROM ai1_model) AS modelCount " + SqlDialectUtils.fromDual(dialect);
            assertNotNull(CCJSqlParserUtil.parse(summarySql), dialect + " fromDual");
        }
    }

    // ========== 逗号分隔列表 ==========

    @Test
    public void testFindInSetColumn() {
        // 原有片段保持不变，不追加 <> 0
        assertEquals("FIND_IN_SET(cc.id, cfur.contact_ids)",
                SqlDialectUtils.findInSetColumn(Dialect.ORIGINAL, "cc.id", "cfur.contact_ids"));
        assertEquals("POSITION(',' || CAST(cc.id AS VARCHAR) || ',' IN ',' || cfur.contact_ids || ',') > 0",
                SqlDialectUtils.findInSetColumn(Dialect.POSTGRESQL, "cc.id", "cfur.contact_ids"));
        assertEquals("(cc.id IS NOT NULL AND INSTR(',' || cfur.contact_ids || ',', ',' || cc.id || ',') > 0)",
                SqlDialectUtils.findInSetColumn(Dialect.ORACLE, "cc.id", "cfur.contact_ids"));
        assertEquals("(cc.id IS NOT NULL AND CHARINDEX(',' + CAST(cc.id AS varchar(20)) + ',', ',' + cfur.contact_ids + ',') > 0)",
                SqlDialectUtils.findInSetColumn(Dialect.SQLSERVER, "cc.id", "cfur.contact_ids"));
        // 达梦：数值不能直接作为布尔条件，与 DbTypeEnum#DM 的模板一致比较 <> 0
        assertEquals("FIND_IN_SET(cc.id, cfur.contact_ids) <> 0",
                SqlDialectUtils.findInSetColumn(Dialect.DM, "cc.id", "cfur.contact_ids"));
        // 神通：没有 FIND_IN_SET；ID 转为 TEXT（不指定长度的 VARCHAR 为 VARCHAR(1)），显式排除 NULL ID
        assertEquals("(cc.id IS NOT NULL AND POSITION(',' || CAST(cc.id AS TEXT) || ',' IN ',' || cfur.contact_ids || ',') > 0)",
                SqlDialectUtils.findInSetColumn(Dialect.OSCAR, "cc.id", "cfur.contact_ids"));
        // 不带表别名、以 _id 结尾的 ID 列
        assertEquals("FIND_IN_SET(contact_id, contact_ids)",
                SqlDialectUtils.findInSetColumn(Dialect.ORIGINAL, "contact_id", "contact_ids"));
        assertEquals("(t.CONTACT_ID IS NOT NULL AND CHARINDEX(',' + CAST(t.CONTACT_ID AS varchar(20)) + ',', ',' + t.contact_ids + ',') > 0)",
                SqlDialectUtils.findInSetColumn(Dialect.SQLSERVER, "t.CONTACT_ID", "t.contact_ids"));
    }

    @Test
    public void testFindInSetColumn_invalidColumn() {
        for (Dialect dialect : Dialect.values()) {
            // 值列：只能是 ID 列名，不能是表达式、字面量或业务字符串列
            assertThrows(IllegalArgumentException.class,
                    () -> SqlDialectUtils.findInSetColumn(dialect, "cc.id) OR (1=1", "cfur.contact_ids"));
            assertThrows(IllegalArgumentException.class,
                    () -> SqlDialectUtils.findInSetColumn(dialect, "'1'", "cfur.contact_ids"));
            assertThrows(IllegalArgumentException.class,
                    () -> SqlDialectUtils.findInSetColumn(dialect, "1", "cfur.contact_ids"));
            assertThrows(IllegalArgumentException.class,
                    () -> SqlDialectUtils.findInSetColumn(dialect, "cc.name", "cfur.contact_ids"));
            assertThrows(IllegalArgumentException.class,
                    () -> SqlDialectUtils.findInSetColumn(dialect, "cc.idx", "cfur.contact_ids"));
            assertThrows(IllegalArgumentException.class,
                    () -> SqlDialectUtils.findInSetColumn(dialect, "id.name", "cfur.contact_ids"));
            assertThrows(IllegalArgumentException.class,
                    () -> SqlDialectUtils.findInSetColumn(dialect, "", "cfur.contact_ids"));
            assertThrows(IllegalArgumentException.class,
                    () -> SqlDialectUtils.findInSetColumn(dialect, null, "cfur.contact_ids"));
            // 列表列
            assertThrows(IllegalArgumentException.class,
                    () -> SqlDialectUtils.findInSetColumn(dialect, "cc.id", "cfur.contact_ids) OR (1=1"));
            assertThrows(IllegalArgumentException.class,
                    () -> SqlDialectUtils.findInSetColumn(dialect, "cc.id", "'1,2,3'"));
            assertThrows(IllegalArgumentException.class,
                    () -> SqlDialectUtils.findInSetColumn(dialect, "cc.id", "cfur.contact_ids;drop table t"));
            assertThrows(IllegalArgumentException.class,
                    () -> SqlDialectUtils.findInSetColumn(dialect, "cc.id", ""));
            assertThrows(IllegalArgumentException.class,
                    () -> SqlDialectUtils.findInSetColumn(dialect, "cc.id", null));
        }
    }

    /**
     * 与 CRM 跟进次数排行相同的 LEFT JOIN 条件，所有方言均需可被 JSqlParser 解析
     */
    @Test
    public void testFindInSetColumn_parsedByJSqlParser() throws Exception {
        for (Dialect dialect : Dialect.values()) {
            String sql = "SELECT COUNT(DISTINCT cc.id) AS count, cc.owner_user_id FROM crm_follow_up_record cfur"
                    + " LEFT JOIN crm_contact cc ON " + SqlDialectUtils.findInSetColumn(dialect, "cc.id", "cfur.contact_ids")
                    + " WHERE cfur.deleted = 0 AND cc.deleted = 0 AND cc.owner_user_id IN (?, ?)"
                    + " AND cfur.create_time BETWEEN ? AND ? GROUP BY cc.owner_user_id";
            assertNotNull(CCJSqlParserUtil.parse(sql), dialect + " " + sql);
        }
    }

    @Test
    public void testBoundSql_findInSetColumn() throws Exception {
        FakeDataSource postgresql = new FakeDataSource("PostgreSQL", "jdbc:postgresql://127.0.0.1:5432/a", 17);
        FakeDataSource oracle = new FakeDataSource("Oracle", "jdbc:oracle:thin:@127.0.0.1:1521:xe", 21);
        FakeDataSource sqlServer = new FakeDataSource("Microsoft SQL Server", "jdbc:sqlserver://127.0.0.1:1433", 16);
        FakeDataSource mysql = new FakeDataSource("MySQL", "jdbc:mysql://127.0.0.1:3306/a", 8);
        mockDynamicRoutingDataSource(Map.of("master", postgresql, "oracle", oracle, "sqlserver", sqlServer, "mysql", mysql));
        // 与 CrmStatisticsRankMapper.xml 的 selectFollowCustomerCountRank 相同
        SqlSource sqlSource = new XMLLanguageDriver().createSqlSource(new Configuration(),
                "<script>SELECT COUNT(DISTINCT cc.id) AS count, cc.owner_user_id FROM crm_follow_up_record cfur"
                        + " LEFT JOIN crm_contact cc ON " + P + "findInSetColumn('cc.id', 'cfur.contact_ids')}"
                        + " WHERE cfur.deleted = 0 AND cc.deleted = 0 AND cc.owner_user_id in"
                        + " <foreach collection=\"userIds\" item=\"userId\" open=\"(\" close=\")\" separator=\",\">#{userId}</foreach>"
                        + " AND cfur.create_time between #{times[0],javaType=java.time.LocalDateTime}"
                        + " and #{times[1],javaType=java.time.LocalDateTime}"
                        + " GROUP BY cc.owner_user_id</script>", Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("userIds", List.of(1L, 2L));
        params.put("times", new LocalDateTime[]{LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 31, 23, 59, 59)});
        // 依次切换：主库 PostgreSQL、Oracle、SQL Server、MySQL
        Map<String, Dialect> routes = new LinkedHashMap<>();
        routes.put("master", Dialect.POSTGRESQL);
        routes.put("oracle", Dialect.ORACLE);
        routes.put("sqlserver", Dialect.SQLSERVER);
        routes.put("mysql", Dialect.ORIGINAL);

        for (Map.Entry<String, Dialect> route : routes.entrySet()) {
            DynamicDataSourceContextHolder.push(route.getKey());
            BoundSql boundSql = sqlSource.getBoundSql(params);
            String sql = normalize(boundSql.getSql());
            String fragment = SqlDialectUtils.findInSetColumn(route.getValue(), "cc.id", "cfur.contact_ids");
            assertTrue(sql.startsWith("SELECT COUNT(DISTINCT cc.id) AS count, cc.owner_user_id FROM crm_follow_up_record cfur"
                    + " LEFT JOIN crm_contact cc ON " + fragment + " WHERE cfur.deleted = 0 AND cc.deleted = 0"), sql);
            assertTrue(sql.endsWith("AND cfur.create_time between ? and ? GROUP BY cc.owner_user_id"), sql);
            // 列对列的片段不引入参数：只有 2 个用户编号与 2 个时间
            List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
            assertEquals(4, parameterMappings.size(), sql);
            assertEquals("times[0]", parameterMappings.get(2).getProperty());
            assertEquals("times[1]", parameterMappings.get(3).getProperty());
            assertNotNull(CCJSqlParserUtil.parse(sql), route.getValue() + " " + sql);
        }
    }

    // ========== JSON 数组 ==========

    @Test
    public void testJsonArrayContains() {
        // 原有片段：保持原有 JSON_CONTAINS
        assertEquals("JSON_CONTAINS(booking_user_ids, {0})",
                SqlDialectUtils.jsonArrayContains(Dialect.ORIGINAL, "booking_user_ids", false));
        assertEquals("JSON_CONTAINS(t.label_ids, JSON_ARRAY({0}))",
                SqlDialectUtils.jsonArrayContains(Dialect.ORIGINAL, "t.label_ids", true));
        // PostgreSQL：两种原有片段统一为 jsonb 包含，参数按 BIGINT 匹配数字元素
        assertEquals(postgreSQLJsonArrayContains("booking_user_ids"),
                SqlDialectUtils.jsonArrayContains(Dialect.POSTGRESQL, "booking_user_ids", false));
        assertEquals(postgreSQLJsonArrayContains("t.label_ids"),
                SqlDialectUtils.jsonArrayContains(Dialect.POSTGRESQL, "t.label_ids", true));
        // PostgreSQL：{0} 只出现一次；openGauss、GaussDB 没有 jsonb_build_array，
        // openGauss 的 A、B 兼容模式下 || 会忽略 NULL 或作为逻辑或，所以不使用；null 参数由 COALESCE 拼成 [null]
        String postgreSQL = SqlDialectUtils.jsonArrayContains(Dialect.POSTGRESQL, "t.label_ids", true);
        assertEquals(1, postgreSQL.split("\\{0}", -1).length - 1, postgreSQL);
        assertTrue(postgreSQL.contains("CAST({0} AS BIGINT)"), postgreSQL);
        assertTrue(postgreSQL.contains("COALESCE(CAST(CAST({0} AS BIGINT) AS TEXT), 'null')"), postgreSQL);
        assertFalse(postgreSQL.contains("jsonb_build_array"), postgreSQL);
        assertFalse(postgreSQL.contains("||"), postgreSQL);
        // Oracle、SQL Server、H2：两种原有片段相同，{0} 只出现一次，不使用 JSON 函数
        for (Dialect dialect : List.of(Dialect.ORACLE, Dialect.SQLSERVER, Dialect.H2)) {
            String condition = SqlDialectUtils.jsonArrayContains(dialect, "t.label_ids", true);
            assertEquals(condition, SqlDialectUtils.jsonArrayContains(dialect, "t.label_ids", false), dialect.name());
            assertEquals(1, condition.split("\\{0}", -1).length - 1, condition);
            assertFalse(condition.toUpperCase().contains("JSON"), condition);
        }
        String oracle = SqlDialectUtils.jsonArrayContains(Dialect.ORACLE, "t.label_ids", true);
        // CLOB 超过 4000 字节时 TRANSLATE 报 ORA-22835，空白只能用 REPLACE 替换
        assertTrue(oracle.contains("REGEXP_LIKE(REPLACE(REPLACE(REPLACE(t.label_ids, CHR(9), ' '), CHR(10), ' '), CHR(13), ' '), "),
                oracle);
        assertFalse(oracle.toUpperCase().contains("TRANSLATE"), oracle);
        assertTrue(oracle.contains("'" + SqlDialectUtils.ORACLE_LONG_ARRAY_PATTERN + "'"), oracle);
        assertFalse(oracle.contains(SqlDialectUtils.H2_LONG_ARRAY_PATTERN), oracle);
        assertTrue(oracle.contains("NOT REGEXP_LIKE(t.label_ids, '" + SqlDialectUtils.LONG_OVERFLOW_PATTERN + "')"), oracle);
        assertTrue(oracle.contains("COALESCE(TO_CHAR(CAST({0} AS NUMBER(19, 0)), 'TM9', "
                + "'NLS_NUMERIC_CHARACTERS = ''.,'''), 'null')"), oracle);
        // SQL Server：与原生验收的 SQL 一致；XML 用 .query('.') 物化后整体 exist 校验，再按逗号边界 CHARINDEX 匹配
        String sqlServer = SqlDialectUtils.jsonArrayContains(Dialect.SQLSERVER, "t.label_ids", true);
        assertEquals("EXISTS (SELECT 1"
                + " FROM (SELECT LTRIM(RTRIM(REPLACE(REPLACE(REPLACE(CAST(t.label_ids AS nvarchar(max)),CHAR(9),N' '),CHAR(10),N' '),CHAR(13),N' '))) AS doc) yd_jd"
                + " CROSS APPLY (SELECT CASE WHEN LEFT(yd_jd.doc,1)=N'[' AND RIGHT(yd_jd.doc,1)=N']'"
                + " THEN SUBSTRING(yd_jd.doc,2,CASE WHEN LEN(yd_jd.doc)>=2"
                + " THEN LEN(yd_jd.doc)-2 ELSE 0 END) ELSE NULL END AS body) yd_jb"
                + " CROSS APPLY (SELECT CAST(CASE WHEN yd_jb.body IS NOT NULL"
                + " AND PATINDEX(N'%[^0-9,nul -]%',COALESCE(yd_jb.body,N'') COLLATE Latin1_General_100_BIN2)=0"
                + " AND LTRIM(RTRIM(yd_jb.body))<>N''"
                + " THEN N'<r><i>'+REPLACE(yd_jb.body,N',',N'</i><i>')+N'</i></r>' ELSE N'<r/>' END AS XML) AS tokens) yd_jx"
                + " CROSS APPLY (SELECT yd_jx.tokens.query('.') AS tokens) yd_jm"
                + " WHERE yd_jm.tokens.exist('/r[i and not(i[string-length(string(xs:token(string(.)))) = 0"
                + " or (string(xs:token(string(.))) != \"null\""
                + " and string(xs:token(string(.))) != string((string(xs:token(string(.))) cast as xs:long?)))])]') = 1"
                + " AND CHARINDEX(N','+COALESCE(CONVERT(nvarchar(20),CAST({0} AS BIGINT)),N'null')+N',',N','+REPLACE(yd_jb.body,N' ',N'')+N',')>0)",
                sqlServer);
        // SQL Server 2012~2016 不支持 OPENJSON，任何分支都不能出现；不逐个遍历节点，XQuery 中不嵌入业务列
        assertFalse(sqlServer.toUpperCase().contains("OPENJSON"), sqlServer);
        assertFalse(sqlServer.contains(".nodes("), sqlServer);
        assertFalse(sqlServer.contains("sql:column"), sqlServer);
        // H2：与 Oracle 共用拼装，使用 H2 专用的正则（Java 正则的占有量词），不使用 Oracle 的正则；INSTR 为 H2 的兼容函数
        String h2 = SqlDialectUtils.jsonArrayContains(Dialect.H2, "t.label_ids", true);
        assertEquals("^ *+\\[ *+(?:(?:0|-?[1-9][0-9]{0,18}|null) *+(?:, *+(?:0|-?[1-9][0-9]{0,18}|null) *+)*+)?\\] *+$",
                SqlDialectUtils.H2_LONG_ARRAY_PATTERN);
        assertEquals("(REGEXP_LIKE(REPLACE(REPLACE(REPLACE(t.label_ids, CHR(9), ' '), CHR(10), ' '), CHR(13), ' '), '"
                        + SqlDialectUtils.H2_LONG_ARRAY_PATTERN + "')"
                        + " AND NOT REGEXP_LIKE(t.label_ids, '" + SqlDialectUtils.LONG_OVERFLOW_PATTERN + "')"
                        + " AND INSTR(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(t.label_ids, CHR(9), ' '), CHR(10), ' '), CHR(13), ' '),"
                        + " ' ', ''), '[', ','), ']', ','),"
                        + " ',' || COALESCE(CAST(CAST({0} AS BIGINT) AS VARCHAR), 'null') || ',') > 0)",
                h2);
        assertFalse(h2.contains(SqlDialectUtils.ORACLE_LONG_ARRAY_PATTERN), h2);
    }

    /**
     * Oracle 片段的匹配语义：按 REGEXP_LIKE、REPLACE、INSTR 的组合在 Java 中复现，校验严格的平面 Long / null 数组契约
     */
    @Test
    public void testJsonArrayContains_oracleLongArraySemantics() {
        // 普通匹配：相邻 ID、负数、大 ID、Long 边界、空白
        assertTrue(oracleJsonContains("[1,11]", 1L));
        assertTrue(oracleJsonContains("[1,11]", 11L));
        assertFalse(oracleJsonContains("[11,21]", 1L));
        assertTrue(oracleJsonContains("[2115311103682412546,2115311103682412547]", 2115311103682412546L));
        assertFalse(oracleJsonContains("[2115311103682412546]", 211531110368241254L));
        assertTrue(oracleJsonContains("[-1, 1]", -1L));
        assertFalse(oracleJsonContains("[-1]", 1L));
        assertTrue(oracleJsonContains("[" + Long.MIN_VALUE + "," + Long.MAX_VALUE + "]", Long.MIN_VALUE));
        assertTrue(oracleJsonContains("[" + Long.MIN_VALUE + "," + Long.MAX_VALUE + "]", Long.MAX_VALUE));
        assertTrue(oracleJsonContains(" [ 1 ,\t2 ,\r\n 3 ] ", 2L));
        assertTrue(oracleJsonContains("[0]", 0L));
        // null 元素：参数为 null 时匹配 null 元素
        assertTrue(oracleJsonContains("[null]", null));
        assertTrue(oracleJsonContains("[1, null]", null));
        assertTrue(oracleJsonContains("[1, null]", 1L));
        assertFalse(oracleJsonContains("[1]", null));
        assertFalse(oracleJsonContains("[NULL]", null));
        // 非数组、空数组、列为 NULL
        assertFalse(oracleJsonContains(null, 1L));
        assertFalse(oracleJsonContains(null, null));
        assertFalse(oracleJsonContains("null", null));
        assertFalse(oracleJsonContains("[]", 1L));
        assertFalse(oracleJsonContains("[ ]", null));
        assertFalse(oracleJsonContains("1", 1L));
        // 字符串、嵌套、对象、小数、指数、前导零、空元素、缺分隔符：整体不匹配
        assertFalse(oracleJsonContains("[\"1\"]", 1L));
        assertFalse(oracleJsonContains("[\"x,1,y\"]", 1L));
        assertFalse(oracleJsonContains("[1,\"x\"]", 1L));
        assertFalse(oracleJsonContains("[[1]]", 1L));
        assertFalse(oracleJsonContains("[1,[2]]", 1L));
        assertFalse(oracleJsonContains("[{\"a\":1}]", 1L));
        assertFalse(oracleJsonContains("[1.0]", 1L));
        assertFalse(oracleJsonContains("[1e3]", 1000L));
        assertFalse(oracleJsonContains("[01]", 1L));
        assertFalse(oracleJsonContains("[-0]", 0L));
        assertFalse(oracleJsonContains("[1,,2]", 1L));
        assertFalse(oracleJsonContains("[,1]", 1L));
        assertFalse(oracleJsonContains("[1,]", 1L));
        assertFalse(oracleJsonContains("[1 2]", 12L));
        assertFalse(oracleJsonContains("[1 2]", 1L));
        assertFalse(oracleJsonContains("[1]]", 1L));
        // 超出 Long 范围：整个数组不匹配
        assertFalse(oracleJsonContains("[9223372036854775808,1]", 1L));
        assertFalse(oracleJsonContains("[-9223372036854775809,1]", 1L));
        assertFalse(oracleJsonContains("[9999999999999999999,1]", 1L));
        assertFalse(oracleJsonContains("[99999999999999999999,1]", 1L));
        assertTrue(oracleJsonContains("[-9223372036854775808,1]", 1L));
        assertTrue(oracleJsonContains("[9223372036854775807,1]", 1L));
        assertTrue(oracleJsonContains("[1000000000000000000,1]", 1L));
        // 长 CLOB 数组由 Oracle 实库验证；Java Pattern 的递归栈不代表 Oracle 的正则执行能力。
        // H2 使用 Java 正则，其长数组边界由 testJsonArrayContains_h2LongArraySemantics 验证。
    }

    /**
     * H2 片段的匹配语义：H2 的 REGEXP_LIKE 使用 Java 正则，H2 专用的正则在长数组上不能栈溢出
     *
     * 注意：仅校验正则与匹配规则，不代替 H2 实库验收
     */
    @Test
    public void testJsonArrayContains_h2LongArraySemantics() {
        // 普通匹配、null 元素、空白
        assertTrue(h2JsonContains("[1,11]", 1L));
        assertFalse(h2JsonContains("[11,21]", 1L));
        assertTrue(h2JsonContains("[2115311103682412546,2115311103682412547]", 2115311103682412547L));
        assertTrue(h2JsonContains("[" + Long.MIN_VALUE + "," + Long.MAX_VALUE + "]", Long.MIN_VALUE));
        assertTrue(h2JsonContains(" [ 1 ,\t2 ,\r\n 3 ] ", 2L));
        assertTrue(h2JsonContains("[1, null]", null));
        assertFalse(h2JsonContains("[1]", null));
        // 非数组、字符串、嵌套、小数、前导零、空元素、缺分隔符、超出 Long 范围：整体不匹配
        assertFalse(h2JsonContains(null, 1L));
        assertFalse(h2JsonContains("null", null));
        assertFalse(h2JsonContains("[]", 1L));
        assertFalse(h2JsonContains("[\"1\"]", 1L));
        assertFalse(h2JsonContains("[\"x,1,y\"]", 1L));
        assertFalse(h2JsonContains("[[1]]", 1L));
        assertFalse(h2JsonContains("[1.0]", 1L));
        assertFalse(h2JsonContains("[01]", 1L));
        assertFalse(h2JsonContains("[1,,2]", 1L));
        assertFalse(h2JsonContains("[1 2]", 1L));
        assertFalse(h2JsonContains("[9223372036854775808,1]", 1L));
        assertFalse(h2JsonContains("[-9223372036854775809,1]", 1L));
        // 1400 个元素的长数组：占有量词不回溯、不递归，不能栈溢出；末尾非法元素整体不匹配
        StringBuilder longArray = new StringBuilder("[0");
        for (int i = 1; i < 1400; i++) {
            longArray.append(i % 2 == 0 ? ",\n" : " , ").append(2115311103682412546L + i);
        }
        String longJson = longArray.append(']').toString();
        assertTrue(h2JsonContains(longJson, 2115311103682412546L + 1399));
        assertFalse(h2JsonContains(longJson, 2115311103682412546L + 1400));
        assertFalse(h2JsonContains(longJson.replace("]", ",\"x\"]"), 0L));
    }

    /**
     * H2 片段在 H2 内存库中实际执行：INSTR 的参数顺序、null 元素、大 Long、非法数组整体不匹配
     *
     * 列值为内联的 CLOB，编号按 JDBC 参数绑定，不建表；分别在默认模式与单元测试使用的 MySQL 模式下执行
     */
    @Test
    public void testJsonArrayContains_h2Executed() throws SQLException {
        String condition = SqlDialectUtils.jsonArrayContains(Dialect.H2, "j.label_ids", true).replace("{0}", "?");
        for (String url : List.of("jdbc:h2:mem:", "jdbc:h2:mem:;MODE=MYSQL;DATABASE_TO_UPPER=false;NON_KEYWORDS=value,day")) {
            try (Connection connection = DriverManager.getConnection(url)) {
                // 匹配：INSTR 在列表中查找 ,元素,，相邻 ID 不误中；空白、Long 边界、大 ID 的前缀不误中
                assertTrue(h2Execute(connection, condition, "[1,11]", 11L), url);
                assertTrue(h2Execute(connection, condition, "[1,11]", 1L), url);
                assertFalse(h2Execute(connection, condition, "[11,21]", 1L), url);
                assertTrue(h2Execute(connection, condition, " [ 1 ,\t2 ,\r\n 3 ] ", 2L), url);
                assertTrue(h2Execute(connection, condition, "[" + Long.MIN_VALUE + "," + Long.MAX_VALUE + "]", Long.MAX_VALUE), url);
                assertTrue(h2Execute(connection, condition, "[" + Long.MIN_VALUE + "," + Long.MAX_VALUE + "]", Long.MIN_VALUE), url);
                assertTrue(h2Execute(connection, condition, "[2115311103682412546,2115311103682412547]", 2115311103682412547L), url);
                assertFalse(h2Execute(connection, condition, "[2115311103682412546]", 211531110368241254L), url);
                // null 元素：参数为 null 时只匹配 null 元素
                assertTrue(h2Execute(connection, condition, "[1, null]", null), url);
                assertFalse(h2Execute(connection, condition, "[1]", null), url);
                // 非法数组整体不匹配：嵌套、字符串、前导零、超出 Long 范围；列为 NULL 时不匹配
                assertFalse(h2Execute(connection, condition, "[[1]]", 1L), url);
                assertFalse(h2Execute(connection, condition, "[1,[2]]", 1L), url);
                assertFalse(h2Execute(connection, condition, "[\"1\"]", 1L), url);
                assertFalse(h2Execute(connection, condition, "[01]", 1L), url);
                assertFalse(h2Execute(connection, condition, "[9223372036854775808,1]", 1L), url);
                assertFalse(h2Execute(connection, condition, "[-9223372036854775809,1]", 1L), url);
                assertFalse(h2Execute(connection, condition, null, 1L), url);
                // 1400 个元素的长数组：H2 的 Java 正则不能栈溢出
                StringBuilder longArray = new StringBuilder("[0");
                for (int i = 1; i < 1400; i++) {
                    longArray.append(i % 2 == 0 ? ",\n" : " , ").append(2115311103682412546L + i);
                }
                String longJson = longArray.append(']').toString();
                assertTrue(h2Execute(connection, condition, longJson, 2115311103682412546L + 1399), url);
                assertFalse(h2Execute(connection, condition, longJson, 2115311103682412546L + 1400), url);
            }
        }
    }

    @Test
    public void testGreaterDigitsPattern() {
        assertEquals("(1[9-9]|[2-9][0-9]{1})", SqlDialectUtils.greaterDigitsPattern("18"));
        Pattern pattern = Pattern.compile(SqlDialectUtils.greaterDigitsPattern("9223372036854775808"));
        assertTrue(pattern.matcher("9223372036854775809").matches());
        assertTrue(pattern.matcher("9223372036854775810").matches());
        assertTrue(pattern.matcher("9300000000000000000").matches());
        assertTrue(pattern.matcher("9999999999999999999").matches());
        assertFalse(pattern.matcher("9223372036854775808").matches());
        assertFalse(pattern.matcher("9223372036854775807").matches());
        assertFalse(pattern.matcher("8999999999999999999").matches());
        assertFalse(pattern.matcher("1000000000000000000").matches());
        // Oracle 正则最长 512 字节；也不能包含会被 Wrapper 当作占位符的 {0}
        assertTrue(SqlDialectUtils.LONG_OVERFLOW_PATTERN.length() < 512);
        assertTrue(SqlDialectUtils.ORACLE_LONG_ARRAY_PATTERN.length() < 512);
        assertFalse(SqlDialectUtils.LONG_OVERFLOW_PATTERN.contains("{0}"));
        assertFalse(SqlDialectUtils.H2_LONG_ARRAY_PATTERN.contains("{0}"));
    }

    @Test
    public void testResolveJsonDialect() throws SQLException {
        // 瀚高原生驱动：JSON 片段与其它片段均使用 PostgreSQL 写法
        assertEquals(Dialect.POSTGRESQL, resolveJson("HighGo", "jdbc:highgo://127.0.0.1:5866/highgo", 6));
        assertEquals(Dialect.POSTGRESQL, resolveJson("PostgreSQL", "jdbc:highgo://127.0.0.1:5866/highgo", 6));
        assertEquals(Dialect.POSTGRESQL, resolve("PostgreSQL", "jdbc:highgo://127.0.0.1:5866/highgo", 6));
        // 人大金仓（产品名 KingbaseES）：JSON 片段与其它片段均使用 PostgreSQL 写法
        assertEquals(Dialect.POSTGRESQL, resolveJson("KingbaseES", "jdbc:kingbase8://127.0.0.1:54321/test", 8));
        assertEquals(Dialect.POSTGRESQL, resolve("KingbaseES", "jdbc:kingbase8://127.0.0.1:54321/test", 8));
        // H2：仅 JSON 片段使用严格的平面 Long 数组写法，其它片段仍保持原有
        assertEquals(Dialect.H2, resolveJson("H2", "jdbc:h2:mem:testdb;MODE=MYSQL", 2));
        assertEquals(Dialect.ORIGINAL, resolve("H2", "jdbc:h2:mem:testdb;MODE=MYSQL", 2));
        // openGauss、GaussDB：JSON 片段使用 PostgreSQL 写法，见 testResolveJsonDialect_openGauss
        assertEquals(Dialect.POSTGRESQL, resolveJson("PostgreSQL", "jdbc:opengauss://127.0.0.1:5432/test", 9));
        assertEquals(Dialect.POSTGRESQL, resolveJson("openGauss", "jdbc:opengauss://127.0.0.1:5432/test", 5));
        // 其它 PostgreSQL 变种（含产品名为 PostgreSQL 的金仓 URL）、达梦、神通、MySQL：保持原有 JSON_CONTAINS
        assertEquals(Dialect.ORIGINAL, resolveJson("PostgreSQL", "jdbc:kingbase8://127.0.0.1:54321/test", 12));
        assertEquals(Dialect.ORIGINAL, resolveJson("DM DBMS", "jdbc:dm://127.0.0.1:5236", 8));
        assertEquals(Dialect.ORIGINAL, resolveJson("OSCAR", "jdbc:oscar://127.0.0.1:2003/osrdb", 7));
        assertEquals(Dialect.ORIGINAL, resolveJson("MySQL", "jdbc:mysql://127.0.0.1:3306/highgo", 8));
        assertEquals(Dialect.ORIGINAL, resolveJson("MariaDB", "jdbc:mariadb://127.0.0.1:3306/a", 11));
        assertEquals(Dialect.ORIGINAL, resolveJson("MariaDB", "jdbc:p6spy:mariadb://127.0.0.1:3306/a", 11));
        assertEquals(Dialect.ORIGINAL, resolveJson("MariaDB", "jdbc:mysql://127.0.0.1:3306/a", 11));
        assertEquals(Dialect.ORIGINAL, resolveJson("Microsoft SQL Server 2005", "jdbc:jtds:sqlserver://127.0.0.1:1433", 9));
        // 其它与 resolveDialect 一致
        assertEquals(Dialect.POSTGRESQL, resolveJson("PostgreSQL", "jdbc:postgresql://127.0.0.1:5432/a", 17));
        assertEquals(Dialect.ORACLE, resolveJson("Oracle", "jdbc:oracle:thin:@127.0.0.1:1521:xe", 21));
        assertEquals(Dialect.SQLSERVER, resolveJson("Microsoft SQL Server", "jdbc:sqlserver://127.0.0.1:1433", 11));
        assertThrows(IllegalStateException.class, () -> resolveJson("SQLite", "jdbc:sqlite:/tmp/test.db", 3));
    }

    /**
     * 兼容 PostgreSQL 的数据库：普通片段均按 PostgreSQL 处理；JSON 片段只有已验收 jsonb 写法的瀚高原生驱动、人大金仓，
     * 以及 openGauss、GaussDB（含 MogDB，见 {@link #testResolveJsonDialect_openGauss()}）使用 jsonb 写法，其它保持原有片段
     */
    @Test
    public void testResolveJsonDialect_postgreSQLVariant() throws SQLException {
        // 已验收 jsonb 写法：瀚高原生驱动（产品名未登记或为 PostgreSQL）、人大金仓（产品名 KingbaseES，任意 URL）
        assertEquals(Dialect.POSTGRESQL, resolveJson("HighGo", "jdbc:highgo://127.0.0.1:5866/highgo", 6));
        assertEquals(Dialect.POSTGRESQL, resolveJson("KingbaseES", "jdbc:postgresql://127.0.0.1:54321/test", 8));
        // 产品名为 PostgreSQL 的 openGauss、GaussDB、MogDB 原生驱动：使用 jsonb 写法；人大金仓原生驱动见下方产品名未登记的用例
        assertEquals(Dialect.POSTGRESQL, resolveJson("PostgreSQL", "jdbc:gaussdb://127.0.0.1:8000/test", 9));
        assertEquals(Dialect.POSTGRESQL, resolveJson("PostgreSQL", "jdbc:mogdb://127.0.0.1:5432/test", 9));
        // 瀚高仅识别 jdbc:highgo: 前缀，经 p6spy 等包装的 URL 保持原有 JSON_CONTAINS
        assertEquals(Dialect.ORIGINAL, resolveJson("PostgreSQL", "jdbc:p6spy:highgo://127.0.0.1:5866/highgo", 6));
        // 产品名为 openGauss、GaussDB：无论原生驱动还是 PostgreSQL 驱动，使用 jsonb 写法
        assertEquals(Dialect.POSTGRESQL, resolveJson("openGauss", "jdbc:postgresql://127.0.0.1:5432/test", 5));
        assertEquals(Dialect.POSTGRESQL, resolveJson("GaussDB", "jdbc:gaussdb://127.0.0.1:8000/test", 5));
        assertEquals(Dialect.POSTGRESQL, resolveJson("GaussDB", "jdbc:postgresql://127.0.0.1:8000/test", 5));
        // 产品名未登记：瀚高、openGauss、GaussDB、MogDB 原生驱动使用 jsonb 写法；
        // 人大金仓原生驱动、经 p6spy 等包装的 URL 保持原有 JSON_CONTAINS
        assertEquals(Dialect.POSTGRESQL, resolveJson("Highgo Database", "jdbc:highgo://127.0.0.1:5866/highgo", 6));
        assertEquals(Dialect.POSTGRESQL, resolveJson("MogDB", "jdbc:mogdb://127.0.0.1:5432/test", 5));
        assertEquals(Dialect.POSTGRESQL, resolveJson("MogDB", "jdbc:opengauss://127.0.0.1:5432/test", 5));
        assertEquals(Dialect.ORIGINAL, resolveJson("Kingbase", "jdbc:kingbase8://127.0.0.1:54321/test", 8));
        assertEquals(Dialect.POSTGRESQL, resolveJson("Vastbase", "jdbc:gaussdb://127.0.0.1:8000/test", 5));
        assertEquals(Dialect.ORIGINAL, resolveJson("MogDB", "jdbc:p6spy:mogdb://127.0.0.1:5432/test", 5));
        // PostgreSQL 官方驱动：只按 JDBC 协议识别，参数中的 :opengauss:、:highgo: 不再使其误判为变种
        assertEquals(Dialect.POSTGRESQL, resolveJson("PostgreSQL",
                "jdbc:postgresql://127.0.0.1:5432/a?ApplicationName=:opengauss:", 17));
        assertEquals(Dialect.POSTGRESQL, resolveJson("PostgreSQL",
                "jdbc:p6spy:postgresql://127.0.0.1:5432/a?ApplicationName=:highgo:", 17));
        // 上述 JSON 片段按各自规则识别，普通片段均已按 PostgreSQL 处理
        assertEquals(Dialect.POSTGRESQL, resolve("openGauss", "jdbc:postgresql://127.0.0.1:5432/test", 5));
        assertEquals(Dialect.POSTGRESQL, resolve("MogDB", "jdbc:mogdb://127.0.0.1:5432/test", 5));
        assertEquals(Dialect.POSTGRESQL, resolve("Kingbase", "jdbc:kingbase8://127.0.0.1:54321/test", 8));
        // 未识别的产品与 URL 仍抛出异常
        assertThrows(IllegalStateException.class, () -> resolveJson("Vastbase", "jdbc:vastbase://127.0.0.1:5432/test", 9));
        assertThrows(IllegalStateException.class, () -> resolveJson("PostgreSQL", "jdbc:vastbase://127.0.0.1:5432/test", 9));
    }

    /**
     * openGauss、GaussDB（含 MogDB）：JSON 片段与普通片段均按 PostgreSQL 处理
     *
     * 项目要求使用 PG 兼容模式，原有 JSON_CONTAINS 不存在或匹配不精确，所以 JSON 片段按 PostgreSQL 处理；
     * 普通片段按兼容 PostgreSQL 的数据库处理，见 {@link #testResolveJsonDialect_postgreSQLVariant()}
     */
    @Test
    public void testResolveJsonDialect_openGauss() throws SQLException {
        // 原生产品名：openGauss（含 openGauss 原生驱动连接的 MogDB）、GaussDB
        assertEquals(Dialect.POSTGRESQL, resolveJson("openGauss", "jdbc:opengauss://127.0.0.1:15434/postgres", 9));
        assertEquals(Dialect.POSTGRESQL, resolve("openGauss", "jdbc:opengauss://127.0.0.1:15434/postgres", 9));
        assertEquals(Dialect.POSTGRESQL, resolveJson("GaussDB", "jdbc:gaussdb://127.0.0.1:8000/postgres", 9));
        assertEquals(Dialect.POSTGRESQL, resolve("GaussDB", "jdbc:gaussdb://127.0.0.1:8000/postgres", 9));
        // 产品名为 PostgreSQL 或未登记时，按原生驱动 URL 识别，不区分大小写
        for (String url : List.of("jdbc:opengauss://127.0.0.1:5432/test", "jdbc:gaussdb://127.0.0.1:8000/test",
                "jdbc:mogdb://127.0.0.1:5432/test", "JDBC:OPENGAUSS://127.0.0.1:5432/test")) {
            assertEquals(Dialect.POSTGRESQL, resolveJson("PostgreSQL", url, 9), url);
            assertEquals(Dialect.POSTGRESQL, resolveJson("MogDB", url, 5), url);
            assertEquals(Dialect.POSTGRESQL, resolve("PostgreSQL", url, 9), url);
        }
        // URL 只按开头的协议识别：库名中出现 opengauss 的 MySQL 仍保持原有片段
        assertEquals(Dialect.ORIGINAL, resolveJson("MySQL", "jdbc:mysql://127.0.0.1:3306/opengauss", 8));
        assertEquals(Dialect.ORIGINAL, resolveJson("DM DBMS", "jdbc:dm://127.0.0.1:5236?schema=gaussdb", 8));
    }

    @Test
    public void testJsonArrayContains_invalidColumn() {
        for (Dialect dialect : Dialect.values()) {
            for (boolean wrapArray : new boolean[]{true, false}) {
                assertThrows(IllegalArgumentException.class,
                        () -> SqlDialectUtils.jsonArrayContains(dialect, "label_ids, '[1]') OR (1=1", wrapArray));
                assertThrows(IllegalArgumentException.class,
                        () -> SqlDialectUtils.jsonArrayContains(dialect, "'[1, 2]'", wrapArray));
                assertThrows(IllegalArgumentException.class,
                        () -> SqlDialectUtils.jsonArrayContains(dialect, "label_ids;drop table t", wrapArray));
                assertThrows(IllegalArgumentException.class,
                        () -> SqlDialectUtils.jsonArrayContains(dialect, "", wrapArray));
                assertThrows(IllegalArgumentException.class,
                        () -> SqlDialectUtils.jsonArrayContains(dialect, null, wrapArray));
            }
        }
    }

    /**
     * 与 PMS 工作项任一标签筛选相同的 OR 条件，所有方言均需可被 JSqlParser 解析
     */
    @Test
    public void testJsonArrayContains_parsedByJSqlParser() throws Exception {
        for (Dialect dialect : Dialect.values()) {
            for (boolean wrapArray : new boolean[]{true, false}) {
                String condition = SqlDialectUtils.jsonArrayContains(dialect, "label_ids", wrapArray).replace("{0}", "?");
                String sql = "SELECT id FROM pms_work_item WHERE project_id = ? AND (" + condition + " OR " + condition + ")";
                assertNotNull(CCJSqlParserUtil.parse(sql), dialect + " " + sql);
            }
        }
    }

    /**
     * 与 Mapper 相同：通过 MyBatis Plus Wrapper 的 apply 绑定参数，再由 MyBatis 生成 BoundSql
     */
    @Test
    public void testBoundSql_jsonArrayContains() throws Exception {
        FakeDataSource postgresql = new FakeDataSource("PostgreSQL", "jdbc:postgresql://127.0.0.1:5432/a", 17);
        FakeDataSource oracle = new FakeDataSource("Oracle", "jdbc:oracle:thin:@127.0.0.1:1521:xe", 21);
        FakeDataSource sqlServer = new FakeDataSource("Microsoft SQL Server", "jdbc:sqlserver://127.0.0.1:1433", 16);
        FakeDataSource mysql = new FakeDataSource("MySQL", "jdbc:mysql://127.0.0.1:3306/a", 8);
        FakeDataSource h2 = new FakeDataSource("H2", "jdbc:h2:mem:testdb;MODE=MYSQL", 2);
        FakeDataSource kingbase = new FakeDataSource("KingbaseES", "jdbc:kingbase8://127.0.0.1:54321/test", 8);
        mockDynamicRoutingDataSource(Map.of("master", postgresql, "oracle", oracle, "sqlserver", sqlServer,
                "mysql", mysql, "h2", h2, "kingbase", kingbase));
        SqlSource sqlSource = new XMLLanguageDriver().createSqlSource(new Configuration(),
                "<script>SELECT id FROM pms_work_item ${ew.customSqlSegment}</script>", Map.class);
        // 依次切换：主库 PostgreSQL、Oracle、SQL Server、MySQL、H2、人大金仓
        Map<String, Dialect> routes = new LinkedHashMap<>();
        routes.put("master", Dialect.POSTGRESQL);
        routes.put("oracle", Dialect.ORACLE);
        routes.put("sqlserver", Dialect.SQLSERVER);
        routes.put("mysql", Dialect.ORIGINAL);
        routes.put("h2", Dialect.H2);
        routes.put("kingbase", Dialect.POSTGRESQL);

        for (Map.Entry<String, Dialect> route : routes.entrySet()) {
            DynamicDataSourceContextHolder.push(route.getKey());
            // PMS：多个标签按 OR 匹配任一标签，参数为 Long
            String labelCondition = SqlDialectUtils.jsonArrayContains("label_ids", true);
            QueryWrapper<Object> labelWrapper = new QueryWrapper<>();
            labelWrapper.eq("project_id", 100L).and(w -> w.or().apply(labelCondition, 1L).or().apply(labelCondition, 11L));
            BoundSql boundSql = sqlSource.getBoundSql(Map.of("ew", labelWrapper));
            String sql = normalize(boundSql.getSql());
            String expected = SqlDialectUtils.jsonArrayContains(route.getValue(), "label_ids", true).replace("{0}", "?");
            assertTrue(sql.startsWith("SELECT id FROM pms_work_item WHERE"), sql);
            assertTrue(sql.contains(expected + " OR " + expected), sql);
            assertFalse(sql.replace(expected, "").contains("11"), sql); // 值只通过参数绑定，不拼接到 SQL 中
            assertEquals(List.of(100L, 1L, 11L), getParameterValues(boundSql, labelWrapper));
            assertNotNull(CCJSqlParserUtil.parse(sql), route.getValue() + " " + sql);

            // OA：参数仍为 ID 的数字文本
            String bookingCondition = SqlDialectUtils.jsonArrayContains("booking_user_ids", false);
            QueryWrapper<Object> bookingWrapper = new QueryWrapper<>();
            bookingWrapper.eq("booking_scope", 1).apply(bookingCondition, "1024");
            boundSql = sqlSource.getBoundSql(Map.of("ew", bookingWrapper));
            sql = normalize(boundSql.getSql());
            expected = SqlDialectUtils.jsonArrayContains(route.getValue(), "booking_user_ids", false).replace("{0}", "?");
            assertTrue(sql.contains(expected), sql);
            assertFalse(sql.replace(expected, "").contains("1024"), sql);
            assertEquals(List.of(1, "1024"), getParameterValues(boundSql, bookingWrapper));
            assertNotNull(CCJSqlParserUtil.parse(sql), route.getValue() + " " + sql);
        }
    }

    /**
     * openGauss、GaussDB 路由到 PostgreSQL 的 JSON 片段：超出 2^53 的相邻编号仍各自作为参数绑定，按 BIGINT 比较。
     * 这里只验证片段与参数结构，数据库中的实际匹配结果需在实库验收
     */
    @Test
    public void testBoundSql_jsonArrayContains_openGauss() throws Exception {
        FakeDataSource openGauss = new FakeDataSource("openGauss", "jdbc:opengauss://127.0.0.1:15434/postgres", 9);
        FakeDataSource gaussDB = new FakeDataSource("GaussDB", "jdbc:gaussdb://127.0.0.1:8000/postgres", 9);
        mockDynamicRoutingDataSource(Map.of("master", openGauss, "gaussdb", gaussDB));
        SqlSource sqlSource = new XMLLanguageDriver().createSqlSource(new Configuration(),
                "<script>SELECT id FROM pms_work_item ${ew.customSqlSegment}</script>", Map.class);
        long id = 9007199254740992L; // 2^53，与相邻编号转为 double 后相同

        for (String route : List.of("master", "gaussdb")) {
            DynamicDataSourceContextHolder.push(route);
            // PMS：参数为 Long
            String labelCondition = SqlDialectUtils.jsonArrayContains("label_ids", true);
            assertEquals(postgreSQLJsonArrayContains("label_ids"), labelCondition, route);
            QueryWrapper<Object> labelWrapper = new QueryWrapper<>();
            labelWrapper.and(w -> w.or().apply(labelCondition, id).or().apply(labelCondition, id + 1));
            BoundSql boundSql = sqlSource.getBoundSql(Map.of("ew", labelWrapper));
            String sql = normalize(boundSql.getSql());
            String expected = labelCondition.replace("{0}", "?");
            assertTrue(sql.contains(expected + " OR " + expected), sql);
            assertFalse(sql.toUpperCase().contains("JSON_CONTAINS"), sql);
            assertFalse(sql.contains(String.valueOf(id)), sql); // 值只通过参数绑定，不拼接到 SQL 中
            assertEquals(List.of(id, id + 1), getParameterValues(boundSql, labelWrapper));
            assertNotNull(CCJSqlParserUtil.parse(sql), route + " " + sql);

            // OA：参数仍为 ID 的数字文本，由 CAST(? AS BIGINT) 转为整数
            String bookingCondition = SqlDialectUtils.jsonArrayContains("booking_user_ids", false);
            assertEquals(postgreSQLJsonArrayContains("booking_user_ids"), bookingCondition, route);
            QueryWrapper<Object> bookingWrapper = new QueryWrapper<>();
            bookingWrapper.apply(bookingCondition, String.valueOf(id + 1));
            boundSql = sqlSource.getBoundSql(Map.of("ew", bookingWrapper));
            sql = normalize(boundSql.getSql());
            assertTrue(sql.contains(bookingCondition.replace("{0}", "?")), sql);
            assertEquals(List.of("9007199254740993"), getParameterValues(boundSql, bookingWrapper));
            assertNotNull(CCJSqlParserUtil.parse(sql), route + " " + sql);

            // null 参数仍只绑定一次，由片段中的 COALESCE 拼成 [null]，不拼接到 SQL 中
            QueryWrapper<Object> nullWrapper = new QueryWrapper<>();
            nullWrapper.apply(bookingCondition, (Object) null);
            boundSql = sqlSource.getBoundSql(Map.of("ew", nullWrapper));
            assertEquals(Collections.singletonList(null), getParameterValues(boundSql, nullWrapper));
            DynamicDataSourceContextHolder.poll();
        }
        // 普通片段按兼容 PostgreSQL 的数据库处理
        DynamicDataSourceContextHolder.push("gaussdb");
        assertEquals(Dialect.POSTGRESQL, SqlDialectUtils.getDialect());
        assertEquals(Dialect.POSTGRESQL, SqlDialectUtils.getJsonDialect());
    }

    // ========== 行数限制 ==========

    @Test
    public void testLimitRows() {
        assertEquals("LIMIT 1", SqlDialectUtils.limitRows(Dialect.ORIGINAL, 1, false));
        assertEquals("LIMIT 10", SqlDialectUtils.limitRows(Dialect.POSTGRESQL, 10, true));
        assertEquals("FETCH FIRST 1 ROWS ONLY", SqlDialectUtils.limitRows(Dialect.ORACLE, 1, false));
        assertEquals("FETCH FIRST 10 ROWS ONLY", SqlDialectUtils.limitRows(Dialect.ORACLE, 10, true));
        // SQL Server：OFFSET 必须跟在 ORDER BY 之后，无排序时补充常量排序
        assertEquals("OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY", SqlDialectUtils.limitRows(Dialect.SQLSERVER, 10, true));
        assertEquals("ORDER BY (SELECT NULL) OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY",
                SqlDialectUtils.limitRows(Dialect.SQLSERVER, 1, false));
        // 达梦、神通：原有 LIMIT（神通 SELECT 的 LIMIT 1、LIMIT 0、LIMIT ? 均可执行）
        assertEquals("LIMIT 10", SqlDialectUtils.limitRows(Dialect.DM, 10, true));
        assertEquals("LIMIT 10", SqlDialectUtils.limitRows(Dialect.OSCAR, 10, false));
    }

    @Test
    public void testLimitRows_zeroAndNegative() {
        assertEquals("LIMIT 0", SqlDialectUtils.limitRows(Dialect.ORIGINAL, 0, true));
        assertEquals("LIMIT 0", SqlDialectUtils.limitRows(Dialect.POSTGRESQL, 0, true));
        assertEquals("FETCH FIRST 0 ROWS ONLY", SqlDialectUtils.limitRows(Dialect.ORACLE, 0, true));
        assertEquals("LIMIT 0", SqlDialectUtils.limitRows(Dialect.OSCAR, 0, true));
        // SQL Server 的 FETCH 不支持 0 行
        assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.limitRows(Dialect.SQLSERVER, 0, true));
        for (Dialect dialect : Dialect.values()) {
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.limitRows(dialect, -1, true));
        }
    }

    @Test
    public void testLimitParam() {
        assertEquals("LIMIT #{limit}", SqlDialectUtils.limitParam(Dialect.ORIGINAL, "limit", true));
        assertEquals("LIMIT #{reqVO.limit}", SqlDialectUtils.limitParam(Dialect.POSTGRESQL, "reqVO.limit", true));
        assertEquals("FETCH FIRST #{limit} ROWS ONLY", SqlDialectUtils.limitParam(Dialect.ORACLE, "limit", false));
        assertEquals("OFFSET 0 ROWS FETCH NEXT #{limit} ROWS ONLY", SqlDialectUtils.limitParam(Dialect.SQLSERVER, "limit", true));
        assertEquals("ORDER BY (SELECT NULL) OFFSET 0 ROWS FETCH NEXT #{limit} ROWS ONLY",
                SqlDialectUtils.limitParam(Dialect.SQLSERVER, "limit", false));
        assertEquals("LIMIT #{limit}", SqlDialectUtils.limitParam(Dialect.DM, "limit", false));
        assertEquals("LIMIT #{limit}", SqlDialectUtils.limitParam(Dialect.OSCAR, "limit", false));
        // 参数名只能是标识符或属性路径
        for (Dialect dialect : Dialect.values()) {
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.limitParam(dialect, "limit} OR 1=1 #{a", true));
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.limitParam(dialect, "1", true));
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.limitParam(dialect, "", true));
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.limitParam(dialect, null, true));
        }
    }

    // ========== 分批删除 ==========

    @Test
    public void testDeleteBatchByLessThan() {
        // 原有语句保持不变
        assertEquals("DELETE FROM infra_job_log WHERE create_time < #{createTime} LIMIT #{limit}",
                SqlDialectUtils.deleteBatchByLessThan(Dialect.ORIGINAL, "infra_job_log", "create_time", "createTime", "limit"));
        assertEquals("DELETE FROM infra_job_log WHERE id IN (SELECT id FROM infra_job_log"
                        + " WHERE create_time < #{createTime} LIMIT #{limit})",
                SqlDialectUtils.deleteBatchByLessThan(Dialect.POSTGRESQL, "infra_job_log", "create_time", "createTime", "limit"));
        assertEquals("DELETE FROM system_oauth2_access_token WHERE expires_time < #{expiresTime} AND ROWNUM <= #{limit}",
                SqlDialectUtils.deleteBatchByLessThan(Dialect.ORACLE, "system_oauth2_access_token", "expires_time", "expiresTime", "limit"));
        assertEquals("DELETE FROM system_oauth2_access_token WHERE id IN (SELECT TOP (#{limit}) id FROM system_oauth2_access_token"
                        + " WHERE expires_time < #{expiresTime})",
                SqlDialectUtils.deleteBatchByLessThan(Dialect.SQLSERVER, "system_oauth2_access_token", "expires_time", "expiresTime", "limit"));
        // 达梦：原有语句
        assertEquals("DELETE FROM infra_job_log WHERE create_time < #{createTime} LIMIT #{limit}",
                SqlDialectUtils.deleteBatchByLessThan(Dialect.DM, "infra_job_log", "create_time", "createTime", "limit"));
        // 神通：DELETE ... LIMIT 报语法错误，与 PostgreSQL 一致按主键 id 的子查询选出一批
        assertEquals("DELETE FROM infra_job_log WHERE id IN (SELECT id FROM infra_job_log"
                        + " WHERE create_time < #{createTime} LIMIT #{limit})",
                SqlDialectUtils.deleteBatchByLessThan(Dialect.OSCAR, "infra_job_log", "create_time", "createTime", "limit"));
    }

    @Test
    public void testDeleteBatchByLessThan_invalidName() {
        for (Dialect dialect : Dialect.values()) {
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.deleteBatchByLessThan(dialect,
                    "infra_job_log; DROP TABLE t", "create_time", "createTime", "limit"));
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.deleteBatchByLessThan(dialect,
                    "infra_job_log", "create_time) OR (1=1", "createTime", "limit"));
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.deleteBatchByLessThan(dialect,
                    "infra_job_log", "create_time", "createTime} OR 1=1 OR #{a", "limit"));
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.deleteBatchByLessThan(dialect,
                    "infra_job_log", "create_time", "createTime", null));
        }
    }

    /**
     * MyBatis Plus 租户、数据权限插件会使用 JSqlParser 解析最终 SQL，所有方言的语句均需可被解析
     *
     * 注意：JSqlParser 5.2 不支持 DELETE TOP (?)，因此 SQL Server 使用主键子查询
     */
    @Test
    public void testLimitAndDelete_parsedByJSqlParser() throws Exception {
        for (Dialect dialect : Dialect.values()) {
            String deleteSql = SqlDialectUtils.deleteBatchByLessThan(dialect, "infra_job_log", "create_time", "createTime", "limit");
            assertNotNull(CCJSqlParserUtil.parse(bind(deleteSql)), dialect + " " + deleteSql);

            String orderedSql = "SELECT user_id AS userId, COUNT(*) AS messageCount FROM im_private_message"
                    + " WHERE send_time >= ? GROUP BY user_id ORDER BY messageCount DESC "
                    + SqlDialectUtils.limitParam(dialect, "limit", true);
            assertNotNull(CCJSqlParserUtil.parse(bind(orderedSql)), dialect + " " + orderedSql);
            String unorderedSql = "SELECT * FROM hrm_salary_option WHERE code = ? "
                    + SqlDialectUtils.limitRows(dialect, 1, false);
            assertNotNull(CCJSqlParserUtil.parse(unorderedSql), dialect + " " + unorderedSql);
        }
    }

    // ========== HTML 标签 ==========

    @Test
    public void testStripHtmlTags() {
        // 原有片段（含 H2、达梦）与 Oracle 保持原有写法，PostgreSQL 系传 'g' 全局替换
        assertEquals("REGEXP_REPLACE(extension.html, '<[^>]*>', '')",
                SqlDialectUtils.stripHtmlTags(Dialect.ORIGINAL, "extension.html"));
        assertEquals("REGEXP_REPLACE(extension.html, '<[^>]*>', '')",
                SqlDialectUtils.stripHtmlTags(Dialect.H2, "extension.html"));
        assertEquals("REGEXP_REPLACE(extension.html, '<[^>]*>', '')",
                SqlDialectUtils.stripHtmlTags(Dialect.DM, "extension.html"));
        // 神通：三参数默认全局替换，不接受第 4 个参数 'g'
        assertEquals("REGEXP_REPLACE(extension.html, '<[^>]*>', '')",
                SqlDialectUtils.stripHtmlTags(Dialect.OSCAR, "extension.html"));
        assertEquals("REGEXP_REPLACE(extension.html, '<[^>]*>', '')",
                SqlDialectUtils.stripHtmlTags(Dialect.ORACLE, "extension.html"));
        assertEquals("REGEXP_REPLACE(extension.html, '<[^>]*>', '', 'g')",
                SqlDialectUtils.stripHtmlTags(Dialect.POSTGRESQL, "extension.html"));
        // SQL Server：按 > 拆分为 JSON 数组，CHARINDEX 与 LEFT 使用同一个二进制排序规则，按段序拼接后恢复数据库默认排序规则
        String value = "yd_h.value COLLATE Latin1_General_100_BIN2";
        assertEquals("CAST((SELECT STRING_AGG(CAST(CASE WHEN yd_h.idx = yd_h.cnt - 1 THEN yd_h.value"
                        + " WHEN CHARINDEX(N'<', " + value + ") > 0 THEN LEFT(" + value + ", CHARINDEX(N'<', " + value + ") - 1)"
                        + " ELSE yd_h.value + N'>' END AS nvarchar(max)), N'') WITHIN GROUP (ORDER BY yd_h.idx)"
                        + " FROM (SELECT CAST([key] AS int) AS idx, value, COUNT(*) OVER () AS cnt"
                        + " FROM OPENJSON(N'[\"' + REPLACE(STRING_ESCAPE(extension.html, 'json') COLLATE Latin1_General_100_BIN2,"
                        + " N'>', N'\",\"') + N'\"]')) yd_h) AS nvarchar(max)) COLLATE DATABASE_DEFAULT",
                SqlDialectUtils.stripHtmlTags(Dialect.SQLSERVER, "extension.html"));
        // 片段不含 {、%，可直接用于 Wrapper 的 apply 与 selectFunc（会经过 String.format）模板
        for (Dialect dialect : Dialect.values()) {
            String sql = SqlDialectUtils.stripHtmlTags(dialect, "extension.html");
            assertFalse(sql.contains("{") || sql.contains("%"), dialect + " " + sql);
        }
    }

    /**
     * CMS 搜索正文的用法：WHERE 中的 LIKE 条件、SELECT 中的得分 CASE WHEN，以及分页计数，所有方言均需可被 JSqlParser 解析
     */
    @Test
    public void testStripHtmlTags_parsedByJSqlParser() throws Exception {
        for (Dialect dialect : Dialect.values()) {
            String text = "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE("
                    + SqlDialectUtils.stripHtmlTags(dialect, "extension.html") + ", "
                    + "'&nbsp;', ' '), '&lt;', '<'), '&gt;', '>'), '&quot;', '\"'), '&amp;', '&')";
            String like = "(" + text + " LIKE CONCAT(CONCAT('%', ?), '%') ESCAPE '!'"
                    + " OR (COALESCE(extension.html, '') = '' AND extension.markdown LIKE CONCAT(CONCAT('%', ?), '%') ESCAPE '!'))";
            String from = " FROM cms_article t INNER JOIN cms_article_extension extension ON extension.id = t.id"
                    + " WHERE t.site_id = ? AND " + like;
            String selectSql = "SELECT t.id, CASE WHEN " + like + " THEN 1 ELSE 0 END AS search_score"
                    + from + " ORDER BY search_score DESC";
            assertNotNull(CCJSqlParserUtil.parse(selectSql), dialect + " " + selectSql);
            String countSql = "SELECT COUNT(*) AS total" + from;
            assertNotNull(CCJSqlParserUtil.parse(countSql), dialect + " " + countSql);
            String groupSql = "SELECT t.status, COUNT(*) AS total" + from + " GROUP BY t.status";
            assertNotNull(CCJSqlParserUtil.parse(groupSql), dialect + " " + groupSql);
        }
    }

    /**
     * 按当前路由到的数据源生成：SQL Server 2012+ 使用 OPENJSON 片段，兼容 PostgreSQL 的数据库传 'g'；
     * 关键词只通过参数绑定，不拼接到 SQL 中
     */
    @Test
    public void testBoundSql_stripHtmlTags() throws Exception {
        mockDynamicRoutingDataSource(Map.of(
                "master", new FakeDataSource("MySQL", "jdbc:mysql://127.0.0.1:3306/a", 8),
                "oracle", new FakeDataSource("Oracle", "jdbc:oracle:thin:@127.0.0.1:1521:xe", 21),
                "sqlserver", new FakeDataSource("Microsoft SQL Server", "jdbc:sqlserver://127.0.0.1:1433", 14),
                "postgresql", new FakeDataSource("PostgreSQL", "jdbc:postgresql://127.0.0.1:5432/a", 16),
                "kingbase", new FakeDataSource("KingbaseES", "jdbc:kingbase8://127.0.0.1:54321/test", 8),
                "opengauss", new FakeDataSource("openGauss", "jdbc:opengauss://127.0.0.1:5432/test", 5),
                "highgo", new FakeDataSource("HighGo", "jdbc:highgo://127.0.0.1:5866/highgo", 6),
                "h2", new FakeDataSource("H2", "jdbc:h2:mem:testdb;MODE=MYSQL", 2),
                "dm", new FakeDataSource("DM DBMS", "jdbc:dm://127.0.0.1:5236", 8)));
        Map<String, Dialect> routes = new LinkedHashMap<>();
        routes.put("master", Dialect.ORIGINAL);
        routes.put("oracle", Dialect.ORACLE);
        routes.put("sqlserver", Dialect.SQLSERVER);
        routes.put("postgresql", Dialect.POSTGRESQL);
        routes.put("kingbase", Dialect.POSTGRESQL);
        routes.put("opengauss", Dialect.POSTGRESQL);
        routes.put("highgo", Dialect.POSTGRESQL);
        routes.put("h2", Dialect.ORIGINAL);
        routes.put("dm", Dialect.ORIGINAL);
        SqlSource sqlSource = new XMLLanguageDriver().createSqlSource(new Configuration(),
                "<script>SELECT t.id FROM cms_article t INNER JOIN cms_article_extension extension ON extension.id = t.id"
                        + " ${ew.customSqlSegment}</script>", Map.class);

        for (Map.Entry<String, Dialect> route : routes.entrySet()) {
            DynamicDataSourceContextHolder.push(route.getKey());
            try {
                String text = SqlDialectUtils.stripHtmlTags("extension.html");
                assertEquals(SqlDialectUtils.stripHtmlTags(route.getValue(), "extension.html"), text, route.getKey());
                String like = text + " LIKE CONCAT(CONCAT('%', {0}), '%') ESCAPE '!'";
                QueryWrapper<Object> wrapper = new QueryWrapper<>();
                wrapper.eq("t.site_id", 1L).apply(like, "50!%");
                BoundSql boundSql = sqlSource.getBoundSql(Map.of("ew", wrapper));
                String sql = normalize(boundSql.getSql());
                assertTrue(sql.contains(normalize(bindIndexParams(like))), route.getKey() + " " + sql);
                assertFalse(sql.contains("50!%"), sql);
                assertEquals(List.of(1L, "50!%"), getParameterValues(boundSql, wrapper), route.getKey());
                assertNotNull(CCJSqlParserUtil.parse(sql), route.getKey() + " " + sql);
            } finally {
                DynamicDataSourceContextHolder.poll();
            }
        }
    }

    // ========== LIKE 转义 ==========

    @Test
    public void testEscapeLike_wildcardsWithEscapeCharFirst() {
        // 先转义转义符本身，原文中的 ! 不会与后续生成的 !% 混淆
        assertEquals("100!% !_off !!!%", SqlDialectUtils.escapeLike("100% _off !%"));
        assertEquals("普通文本", SqlDialectUtils.escapeLike("普通文本"));
    }

    @Test
    public void testEscapeLike_bracketOnlyForSqlServer() {
        try (MockedStatic<JdbcUtils> jdbcUtils = mockStatic(JdbcUtils.class)) {
            jdbcUtils.when(() -> JdbcUtils.isSQLServer(any(DbType.class))).thenCallRealMethod();
            // 不含 [ 时不识别数据库类型，单独的 ] 在各数据库均为字面量
            assertEquals("100!% !_off !!!%", SqlDialectUtils.escapeLike("100% _off !%"));
            assertEquals("a]b", SqlDialectUtils.escapeLike("a]b"));
            jdbcUtils.verify(JdbcUtils::getDbType, never());

            // SQL Server：先转义 !、%、_，再将每个 [ 转义一次为 ![，包括重复、不成对、范围、取反的字符类
            Map<String, String> sqlServerCases = Map.of(
                    "![a]%_", "!!![a]!%!_",
                    "[[a", "![![a",
                    "[", "![",
                    "[a-z]", "![a-z]",
                    "[^a]", "![^a]",
                    "a]", "a]");
            for (DbType dbType : List.of(DbType.SQL_SERVER, DbType.SQL_SERVER2005)) {
                jdbcUtils.when(JdbcUtils::getDbType).thenReturn(dbType);
                sqlServerCases.forEach((value, expected) ->
                        assertEquals(expected, SqlDialectUtils.escapeLike(value), dbType + " " + value));
            }

            // 其它数据库的 [ 本身就是字面量，保持不转义（Oracle 的转义符后不能跟 [）
            for (DbType dbType : List.of(DbType.MYSQL, DbType.POSTGRE_SQL, DbType.ORACLE, DbType.DM)) {
                jdbcUtils.when(JdbcUtils::getDbType).thenReturn(dbType);
                assertEquals("!![a]!%!_", SqlDialectUtils.escapeLike("![a]%_"), dbType.name());
                assertEquals("[^a]", SqlDialectUtils.escapeLike("[^a]"), dbType.name());
            }
        }
    }

    @Test
    public void testEscapeLike_resolveDbTypeForEachCall() {
        try (MockedStatic<JdbcUtils> jdbcUtils = mockStatic(JdbcUtils.class)) {
            jdbcUtils.when(() -> JdbcUtils.isSQLServer(any(DbType.class))).thenCallRealMethod();
            // 每次调用按当前路由到的数据源识别，切换数据源后不沿用 SQL Server 的转义
            jdbcUtils.when(JdbcUtils::getDbType).thenReturn(DbType.SQL_SERVER, DbType.ORACLE, DbType.SQL_SERVER);
            assertEquals("![a]", SqlDialectUtils.escapeLike("[a]"));
            assertEquals("[a]", SqlDialectUtils.escapeLike("[a]"));
            assertEquals("![a]", SqlDialectUtils.escapeLike("[a]"));
            jdbcUtils.verify(JdbcUtils::getDbType, times(3));
        }
    }

    // ========== 布尔 ==========

    @Test
    public void testIsTrue() {
        // 标准 DDL 中 Boolean 字段为 bool 的数据库
        assertEquals("t.new_visitor = TRUE", SqlDialectUtils.isTrue(DbType.POSTGRE_SQL, "t.new_visitor"));
        assertEquals("t.new_visitor = TRUE", SqlDialectUtils.isTrue(DbType.KINGBASE_ES, "t.new_visitor"));
        assertEquals("t.new_visitor = TRUE", SqlDialectUtils.isTrue(DbType.OPENGAUSS, "t.new_visitor"));
        assertEquals("t.new_visitor = TRUE", SqlDialectUtils.isTrue(DbType.GAUSS_DB, "t.new_visitor"));
        assertEquals("t.new_visitor = TRUE", SqlDialectUtils.isTrue(DbType.OSCAR, "t.new_visitor"));
        assertEquals("t.new_visitor = TRUE", SqlDialectUtils.isTrue(DbType.H2, "t.new_visitor"));
        // 标准 DDL 中 Boolean 字段为 bit、NUMBER(1, 0) 的数据库
        assertEquals("extended = 1", SqlDialectUtils.isTrue(DbType.MYSQL, "extended"));
        assertEquals("extended = 1", SqlDialectUtils.isTrue(DbType.MARIADB, "extended"));
        assertEquals("extended = 1", SqlDialectUtils.isTrue(DbType.OCEAN_BASE, "extended"));
        assertEquals("extended = 1", SqlDialectUtils.isTrue(DbType.ORACLE, "extended"));
        assertEquals("extended = 1", SqlDialectUtils.isTrue(DbType.SQL_SERVER, "extended"));
        assertEquals("extended = 1", SqlDialectUtils.isTrue(DbType.SQL_SERVER2005, "extended"));
        assertEquals("extended = 1", SqlDialectUtils.isTrue(DbType.DM, "extended"));
    }

    @Test
    public void testIsTrue_unsupportedDbType() {
        // 未识别的数据库不按 MySQL 处理
        assertThrows(IllegalStateException.class, () -> SqlDialectUtils.isTrue(null, "extended"));
        for (DbType dbType : List.of(DbType.SQLITE, DbType.DB2, DbType.VASTBASE, DbType.OTHER)) {
            assertThrows(IllegalStateException.class, () -> SqlDialectUtils.isTrue(dbType, "extended"), dbType.name());
        }
    }

    @Test
    public void testIsTrue_invalidColumn() {
        for (DbType dbType : List.of(DbType.POSTGRE_SQL, DbType.MYSQL, DbType.ORACLE, DbType.SQL_SERVER)) {
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.isTrue(dbType, "extended) OR (1=1"));
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.isTrue(dbType, "extended = 0 OR extended"));
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.isTrue(dbType, "extended;drop table t"));
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.isTrue(dbType, "'1'"));
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.isTrue(dbType, ""));
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.isTrue(dbType, null));
        }
    }

    @Test
    public void testIsTrue_resolvedByProduct() {
        assertEquals("extended = TRUE", isTrue("PostgreSQL", "jdbc:postgresql://127.0.0.1:5432/a"));
        assertEquals("extended = TRUE", isTrue("KingbaseES", "jdbc:kingbase8://127.0.0.1:54321/test"));
        assertEquals("extended = TRUE", isTrue("openGauss", "jdbc:opengauss://127.0.0.1:5432/test"));
        assertEquals("extended = TRUE", isTrue("GaussDB", "jdbc:gaussdb://127.0.0.1:8000/test"));
        assertEquals("extended = TRUE", isTrue("OSCAR", "jdbc:oscar://127.0.0.1:2003/osrdb"));
        assertEquals("extended = TRUE", isTrue("H2", "jdbc:h2:mem:testdb;MODE=MYSQL"));
        // 瀚高原生驱动的产品名未登记，按 URL 归为 PostgreSQL；MogDB 通过 PostgreSQL 驱动连接时产品名为 PostgreSQL
        assertEquals("extended = TRUE", isTrue("HighGo", "jdbc:highgo://127.0.0.1:5866/highgo"));
        assertEquals("extended = TRUE", isTrue("PostgreSQL", "jdbc:mogdb://127.0.0.1:5432/test"));
        assertEquals("extended = 1", isTrue("MySQL", "jdbc:mysql://127.0.0.1:3306/a"));
        assertEquals("extended = 1", isTrue("MariaDB", "jdbc:mariadb://127.0.0.1:3306/a"));
        assertEquals("extended = 1", isTrue("OceanBase", "jdbc:oceanbase://127.0.0.1:2881/a"));
        assertEquals("extended = 1", isTrue("Oracle", "jdbc:oracle:thin:@127.0.0.1:1521:xe"));
        assertEquals("extended = 1", isTrue("Microsoft SQL Server", "jdbc:sqlserver://127.0.0.1:1433"));
        assertEquals("extended = 1", isTrue("DM DBMS", "jdbc:dm://127.0.0.1:5236"));
        // 未识别的产品、产品名为空
        assertThrows(IllegalArgumentException.class, () -> isTrue("SQLite", "jdbc:sqlite:/tmp/test.db"));
        assertThrows(IllegalStateException.class, () -> isTrue(null, "jdbc:unknown://127.0.0.1/test"));
    }

    @Test
    public void testIsTrue_followsDynamicRouting() {
        FakeDataSource postgresql = new FakeDataSource("PostgreSQL", "jdbc:postgresql://127.0.0.1:5432/a", 17);
        FakeDataSource mysql = new FakeDataSource("MySQL", "jdbc:mysql://127.0.0.1:3306/a", 8);
        FakeDataSource h2 = new FakeDataSource("H2", "jdbc:h2:mem:testdb;MODE=MYSQL", 2);
        mockDynamicRoutingDataSource(Map.of("master", postgresql, "mysql", mysql, "h2", h2));

        for (int i = 0; i < 3; i++) {
            // 未切换时为主库
            assertEquals("t.new_visitor = TRUE", SqlDialectUtils.isTrue("t.new_visitor"));
            DynamicDataSourceContextHolder.push("mysql");
            assertEquals("t.new_visitor = 1", SqlDialectUtils.isTrue("t.new_visitor"));
            DynamicDataSourceContextHolder.push("h2");
            assertEquals("t.new_visitor = TRUE", SqlDialectUtils.isTrue("t.new_visitor"));
            DynamicDataSourceContextHolder.poll();
            assertEquals("t.new_visitor = 1", SqlDialectUtils.isTrue("t.new_visitor"));
            DynamicDataSourceContextHolder.poll();
        }
        assertEquals(1, postgresql.getConnectionCount());
        assertEquals(1, mysql.getConnectionCount());
        assertEquals(1, h2.getConnectionCount());
    }

    @Test
    public void testIsTrue_transactionUsesBoundConnection() throws SQLException {
        FakeDataSource postgresql = new FakeDataSource("PostgreSQL", "jdbc:postgresql://probe/boolean", 17);
        FakeDataSource mysql = new FakeDataSource("MySQL", "jdbc:mysql://probe/boolean", 8);
        mockDynamicRoutingDataSource(Map.of("master", postgresql, "mysql", mysql));
        DynamicRoutingDataSource routing = SpringUtil.getBean(DynamicRoutingDataSource.class);
        Connection held = postgresql.getDataSource().getConnection();
        TransactionSynchronizationManager.bindResource(routing, new ConnectionHolder(held));
        DynamicDataSourceContextHolder.push("mysql");
        try {
            // Spring 事务的执行连接仍为 PG，切换线程路由键不会改变它
            assertEquals("extended = TRUE", SqlDialectUtils.isTrue("extended"));
            assertEquals(1, postgresql.getConnectionCount());
            assertEquals(0, mysql.getConnectionCount());
            assertEquals(0, postgresql.closeCount);
        } finally {
            TransactionSynchronizationManager.unbindResource(routing);
            held.close();
        }
        // 事务结束后该路由真正使用 MySQL
        assertEquals("extended = 1", SqlDialectUtils.isTrue("extended"));
        assertEquals(1, mysql.getConnectionCount());
    }

    /**
     * MyBatis Plus 租户等插件会使用 JSqlParser 解析最终 SQL，所有数据库类型的片段均需可被解析
     */
    @Test
    public void testIsTrue_parsedByJSqlParser() throws Exception {
        for (DbType dbType : List.of(DbType.POSTGRE_SQL, DbType.KINGBASE_ES, DbType.OPENGAUSS, DbType.GAUSS_DB,
                DbType.OSCAR, DbType.H2, DbType.MYSQL, DbType.MARIADB, DbType.OCEAN_BASE, DbType.ORACLE,
                DbType.SQL_SERVER, DbType.SQL_SERVER2005, DbType.DM)) {
            String condition = SqlDialectUtils.isTrue(dbType, "t.new_visitor");
            String name = "CASE WHEN " + condition + " THEN '1' ELSE '0' END";
            String sql = "SELECT t.site_id AS siteId, " + name + " AS name,"
                    + " SUM(CASE WHEN " + condition + " THEN 1 ELSE 0 END) AS nvCount FROM cms_visit_log t"
                    + " WHERE t.create_time >= ? GROUP BY t.site_id, " + name + ", t.session_id";
            assertNotNull(CCJSqlParserUtil.parse(sql), dbType + " " + condition);
        }
    }

    @Test
    public void testBoundSql_isTrue() {
        FakeDataSource postgresql = new FakeDataSource("PostgreSQL", "jdbc:postgresql://127.0.0.1:5432/a", 17);
        FakeDataSource oracle = new FakeDataSource("Oracle", "jdbc:oracle:thin:@127.0.0.1:1521:xe", 21);
        mockDynamicRoutingDataSource(Map.of("master", postgresql, "oracle", oracle));
        SqlSource sqlSource = new XMLLanguageDriver().createSqlSource(new Configuration(),
                "<script>SELECT type_id AS typeId, SUM(CASE WHEN " + P + "isTrue('extended')} THEN 1 ELSE 0 END) AS extendedCount"
                        + " FROM cms_mailbox WHERE deleted = 0 AND site_id = #{siteId} GROUP BY type_id</script>", Map.class);
        Map<String, Object> params = Map.of("siteId", 1L);

        // 主库：PostgreSQL
        BoundSql boundSql = sqlSource.getBoundSql(params);
        assertEquals("SELECT type_id AS typeId, SUM(CASE WHEN extended = TRUE THEN 1 ELSE 0 END) AS extendedCount"
                + " FROM cms_mailbox WHERE deleted = 0 AND site_id = ? GROUP BY type_id", normalize(boundSql.getSql()));
        assertParameters(boundSql, "siteId");

        // 同一个 SqlSource 在下一次执行时，跟随路由切换到 Oracle
        DynamicDataSourceContextHolder.push("oracle");
        boundSql = sqlSource.getBoundSql(params);
        assertEquals("SELECT type_id AS typeId, SUM(CASE WHEN extended = 1 THEN 1 ELSE 0 END) AS extendedCount"
                + " FROM cms_mailbox WHERE deleted = 0 AND site_id = ? GROUP BY type_id", normalize(boundSql.getSql()));
        assertParameters(boundSql, "siteId");
    }

    // ========== 日期运算 ==========

    // DONE @AI：是不是融合到一个 SqlDialectUtilsTest 单测里；（已合并：原 SqlDialectUtilsDateTest 的 9 个用例迁入本分段，
    // 同名的 JSqlParser 用例改名为 testDateFragments_parsedByJSqlParser，编号参数替换使用 bindIndexParams，原独立测试类已删除）
    // 各数据库的执行结果需在真实数据库上验证，这里只校验片段的分支、参数占位与可解析性

    @Test
    public void testDateDecayScore() {
        // MySQL 系、H2：原有片段
        String mysql = "{0} * POWER({1}, GREATEST(ABS(TIMESTAMPDIFF(SECOND, t.publish_time, {2})) / 86400E0 - {3}, 0) / {4})";
        for (DbType dbType : List.of(DbType.MYSQL, DbType.MARIADB, DbType.OCEAN_BASE, DbType.H2)) {
            assertEquals(mysql, SqlDialectUtils.dateDecayScore(dbType, "t.publish_time"), dbType.name());
        }
        // 达梦：显式转为 DOUBLE，避免整数除法
        assertEquals("{0} * POWER(CAST({1} AS DOUBLE), GREATEST(CAST(ABS(TIMESTAMPDIFF(SECOND, t.publish_time, {2}))"
                + " AS DOUBLE) / 86400 - {3}, 0) / {4})", SqlDialectUtils.dateDecayScore(DbType.DM, "t.publish_time"));
        // PostgreSQL 系：显式保留 NULL；权重、衰减系数、天数均按 NUMERIC 计算，避免 FLOAT8 下溢报错，也不按阈值截断指数
        String postgresql = "CASE WHEN t.publish_time IS NOT NULL THEN CAST({0} AS NUMERIC) * POWER(CAST({1} AS NUMERIC),"
                + " GREATEST(CAST(ABS(TRUNC(EXTRACT(EPOCH FROM (CAST({2} AS TIMESTAMP) - CAST(t.publish_time AS TIMESTAMP)))))"
                + " AS NUMERIC) / 86400 - {3}, 0) / {4}) END";
        for (DbType dbType : List.of(DbType.POSTGRE_SQL, DbType.KINGBASE_ES, DbType.OPENGAUSS, DbType.GAUSS_DB)) {
            String sql = SqlDialectUtils.dateDecayScore(dbType, "t.publish_time");
            assertEquals(postgresql, sql, dbType.name());
            assertFalse(sql.contains("DOUBLE PRECISION"), dbType.name());
            assertFalse(sql.contains("LEAST"), dbType.name());
        }
        // Oracle：TIMESTAMP(9) 相减，按 INTERVAL 各部分求秒
        String interval = "(CAST({2} AS TIMESTAMP(9)) - CAST(t.publish_time AS TIMESTAMP(9)))";
        assertEquals("{0} * POWER(CAST({1} AS BINARY_DOUBLE), GREATEST(CAST(ABS(TRUNC("
                + "EXTRACT(DAY FROM " + interval + ") * 86400 + EXTRACT(HOUR FROM " + interval + ") * 3600"
                + " + EXTRACT(MINUTE FROM " + interval + ") * 60 + EXTRACT(SECOND FROM " + interval + ")"
                + ")) AS BINARY_DOUBLE) / 86400 - {3}, 0) / {4})", SqlDialectUtils.dateDecayScore(DbType.ORACLE, "t.publish_time"));
    }

    /**
     * 神通：没有 TIMESTAMPDIFF，INTERVAL 不支持 EXTRACT，TIMESTAMP 最大精度为 6；按日期差、日内整秒差求整秒，再按六位微秒文本向零修正。
     * 显式保留 NULL。指数按 FLOAT8 计算（宽 NUMERIC 的除法只保留约 9 位小数，1 秒的得分与 MySQL 相差约 5E-11）；
     * 权重、衰减系数、乘积按 NUMERIC(1000, 500) 计算（FLOAT8 的 POWER 下溢报错，NUMBER 丢失 1E-300）；
     * 衰减部分按 MySQL DOUBLE 舍入为 0（不大于 2^-1075）时为 0；同 MySQL 先将衰减部分舍入为 Double，再乘以权重
     */
    @Test
    public void testDateDecayScore_oscar() {
        String begin = "CAST(t.publish_time AS TIMESTAMP(6))";
        String end = "CAST({2} AS TIMESTAMP(6))";
        String forward = "(CAST(DATEDIFF('DAY', TRUNC(" + begin + "), TRUNC(" + end + ")) AS BIGINT) * 86400"
                + " + (EXTRACT(HOUR FROM " + end + ") - EXTRACT(HOUR FROM " + begin + ")) * 3600"
                + " + (EXTRACT(MINUTE FROM " + end + ") - EXTRACT(MINUTE FROM " + begin + ")) * 60"
                + " + TRUNC(EXTRACT(SECOND FROM " + end + ")) - TRUNC(EXTRACT(SECOND FROM " + begin + "))"
                + " - CASE WHEN TO_CHAR(" + end + ", 'US') < TO_CHAR(" + begin + ", 'US') THEN 1 ELSE 0 END)";
        String backward = "(CAST(DATEDIFF('DAY', TRUNC(" + end + "), TRUNC(" + begin + ")) AS BIGINT) * 86400"
                + " + (EXTRACT(HOUR FROM " + begin + ") - EXTRACT(HOUR FROM " + end + ")) * 3600"
                + " + (EXTRACT(MINUTE FROM " + begin + ") - EXTRACT(MINUTE FROM " + end + ")) * 60"
                + " + TRUNC(EXTRACT(SECOND FROM " + begin + ")) - TRUNC(EXTRACT(SECOND FROM " + end + "))"
                + " - CASE WHEN TO_CHAR(" + begin + ", 'US') < TO_CHAR(" + end + ", 'US') THEN 1 ELSE 0 END)";
        String exponent = "GREATEST(CAST(CASE WHEN " + end + " >= " + begin + " THEN " + forward + " ELSE " + backward
                + " END AS FLOAT8) / 86400 - CAST({3} AS FLOAT8), 0) / CAST({4} AS FLOAT8)";
        String sql = SqlDialectUtils.dateDecayScore(DbType.OSCAR, "t.publish_time");

        // 衰减系数的对数：次正规数不能转为 FLOAT8，按宽 NUMERIC 乘以 1E300 归一化；正规数仍按 LN(FLOAT8)
        // 神通对未执行的 ELSE 分支也会先转换参数：ELSE 分支只转换“次正规数取 1，否则取原值”的 CASE 结果
        String subnormal = "CAST({1} AS NUMERIC(1000, 500)) < CAST(2.2250738585072014E-308 AS NUMERIC(1000, 500))";
        String lnDecay = "CASE WHEN " + subnormal
                + " THEN LN(CAST({1} AS NUMERIC(1000, 500)) * CAST(1E300 AS NUMERIC(1000, 500))) - LN(CAST(1E300 AS FLOAT8))"
                + " ELSE LN(CAST(CASE WHEN " + subnormal + " THEN 1 ELSE CAST({1} AS NUMERIC(1000, 500)) END AS FLOAT8)) END";
        String lnScore = "(" + exponent + ") * (" + lnDecay + ")";
        // 对数积 L 出现 4 次，按 L 核对结构：先将衰减部分舍入为 Double（次正规区按 2^-1074 的整数倍），再乘以权重
        assertEquals("CASE WHEN t.publish_time IS NULL THEN NULL"
                + " WHEN L <= -745.1332191019412 THEN 0"
                + " WHEN L < -708 THEN CAST({0} AS NUMERIC(1000, 500)) * (CAST(CAST((EXP(CAST(744.4400719213812 AS FLOAT8)"
                + " - GREATEST(CAST(708 AS FLOAT8), 0 - GREATEST(L, CAST(-745.1332191019412 AS FLOAT8))))"
                + " + CAST(4503599627370496 AS FLOAT8)) - CAST(4503599627370496 AS FLOAT8) AS BIGINT) AS NUMERIC(1000, 500))"
                + " * POWER(CAST(0.5 AS NUMERIC(1000, 500)), 1074))"
                + " ELSE CAST({0} AS NUMERIC(1000, 500)) * CAST(EXP(GREATEST(L, CAST(-708 AS FLOAT8))) AS NUMERIC(1000, 500)) END",
                sql.replace(lnScore, "L"));
        // 不使用神通不支持的 TIMESTAMPDIFF、INTERVAL 的 EXTRACT、TIMESTAMP(9)
        assertFalse(sql.contains("TIMESTAMPDIFF"));
        assertFalse(sql.contains("EPOCH"));
        assertFalse(sql.contains(" - CAST(t.publish_time"));
        assertFalse(sql.contains("TIMESTAMP(9)"));
        // 归零边界为 MySQL DOUBLE 的下溢边界 ln(2^-1075)，不是任意阈值：0.5 的 1075 次方为 0，1074.9999 次方仍为正数
        assertEquals(-1075 * Math.log(2), Double.parseDouble("-745.1332191019412"), 1E-12);
        assertEquals(0D, Math.pow(0.5, 1075));
        assertTrue(Math.pow(0.5, 1074.9999) > 0);
        assertFalse(sql.contains("-600"));
        // 次正规数的判断边界为 Double.MIN_NORMAL；Double.MIN_VALUE（4.9E-324）走归一化分支，乘以 1E300 后为正规数
        assertEquals(Double.toString(Double.MIN_NORMAL), "2.2250738585072014E-308");
        assertTrue(Double.MIN_VALUE < Double.MIN_NORMAL);
        assertTrue(Double.MIN_VALUE * 1E300 >= Double.MIN_NORMAL);
        // 不使用缺省标度为 0 的 NUMERIC、丢失极小值的 NUMBER
        assertFalse(sql.contains("AS NUMERIC)"));
        assertFalse(sql.contains("NUMBER"));
        // 神通的 POWER 在指数为 NUMERIC、BIGINT 时转为 FLOAT8，结果低于正规数范围即报错：不对衰减系数直接 POWER，
        // 只对 0.5 按整数常量 1074 次方得到宽 NUMERIC 的最小单位，不用 4.9E-324 的近似值
        assertFalse(sql.contains("POWER(CAST({1}"));
        assertFalse(sql.contains("4.9E-324"));
        // 衰减系数在任何分支都只转为宽 NUMERIC，不直接转为 FLOAT8
        assertFalse(sql.contains("CAST({1} AS FLOAT8"));
        assertEquals(sql.split("\\{1}", -1).length, sql.split("CAST\\(\\{1} AS NUMERIC\\(1000, 500\\)\\)", -1).length);

        // 常量：744.4400719213812 为 ln(2^1074)；EXP(-708) 为正规数且小于 2^-1021，小于该值时 Double 的间隔同为 2^-1074
        assertEquals(1074 * Math.log(2), 744.4400719213812, 1E-12);
        assertTrue(Math.exp(-708) >= Double.MIN_NORMAL && Math.exp(-708) < 2 * Double.MIN_NORMAL);
        // 未执行的分支提前求值时 EXP 的参数也在安全范围内：次正规区的比值为 0.5 ~ 1.49 * 2^52，正规区不小于 EXP(-708)
        assertTrue(Math.exp(744.4400719213812 - 745.1332191019412) < 0.5);
        assertTrue(Math.exp(744.4400719213812 - 708) < 2 * 4503599627370496D);
        // (比值 + 2^52) - 2^52 平分时取偶数，同 IEEE 的就近舍入；CAST AS BIGINT 只转换已舍入的整数
        assertEquals(0D, (0.5 + 4503599627370496D) - 4503599627370496D);
        assertEquals(2D, (1.5 + 4503599627370496D) - 4503599627370496D);
        assertEquals(2D, (2.5 + 4503599627370496D) - 4503599627370496D);
        assertEquals(1D, (0.7071067811865476 + 4503599627370496D) - 4503599627370496D);

        // 实际文章评分（衰减系数 0.5、半径 2 天、偏移 0）：MySQL 的 POWER 先舍入为 Double，2149 天的 0.707 个最小单位为 1 个，
        // 再乘以 Float 权重；2150 天为 0
        assertEquals(Double.MIN_VALUE, Math.pow(0.5, 1074.5));
        for (float boost : new float[]{10F, 1E20F, 1E30F}) {
            for (int days : new int[]{2148, 2149, 2150}) {
                double lnValue = days / 2D * Math.log(0.5);
                assertEquals(boost * Math.pow(0.5, days / 2D), oscarDateDecayScore(boost, lnValue), days + " " + boost);
            }
        }
        // 下溢边界两侧、极小的衰减系数与权重
        assertEquals(Math.pow(0.5, 1074.9999), oscarDateDecayScore(1, 1074.9999 * Math.log(0.5)));
        assertEquals(0D, oscarDateDecayScore(1, 1075 * Math.log(0.5)));
        double lnMinValue = Math.log(Double.MIN_VALUE * 1E300) - Math.log(1E300);
        assertEquals(1D, oscarDateDecayScore(1, 0 * lnMinValue));
        assertEquals(Double.MIN_VALUE, oscarDateDecayScore(1, lnMinValue));
        assertEquals(Double.MIN_VALUE, oscarDateDecayScore(Double.MIN_VALUE, 0));
        // 正规区：相对误差约为 |对数积| * 1E-16
        for (double exponentValue : new double[]{1 / 86400D, 86399 / 86400D, 900, 1000, 1021.9}) {
            double expected = Math.pow(0.5, exponentValue);
            assertEquals(expected, oscarDateDecayScore(1, exponentValue * Math.log(0.5)), expected * 1E-13);
        }
    }

    /**
     * 神通日期衰减得分：Double.MIN_VALUE 等极小的权重、衰减系数只作为 Wrapper 的编号参数绑定，不拼接到 SQL 中；
     * 参数保持 Double 类型，由 SQL 中的 CAST 决定按宽 NUMERIC 或 FLOAT8 计算
     */
    @Test
    public void testDateDecayScore_oscarBoundByWrapper() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 2, 10, 0, 0);
        QueryWrapper<Object> wrapper = new QueryWrapper<>();
        wrapper.apply(SqlDialectUtils.dateDecayScore(DbType.OSCAR, "t.publish_time") + " > 0",
                Double.MIN_VALUE, Double.MIN_VALUE, now, 0, 1);
        String sql = wrapper.getSqlSegment();

        assertFalse(sql.matches("(?s).*\\{[0-4]}.*"), sql);
        assertFalse(sql.contains("4.9E-324") || sql.contains("2026"), sql);
        // 五个编号参数各绑定一次，{2} 等在片段中重复出现时复用同一个参数
        assertEquals(5, wrapper.getParamNameValuePairs().size());
        assertTrue(wrapper.getParamNameValuePairs().values().containsAll(List.of(Double.MIN_VALUE, now, 0, 1)));
        assertEquals(2, wrapper.getParamNameValuePairs().values().stream().filter(Double.valueOf(Double.MIN_VALUE)::equals).count());
    }

    @Test
    public void testDateDecayScore_sqlServer() {
        String begin = "CAST(t.publish_time AS datetime2(7))";
        String end = "CAST({2} AS datetime2(7))";
        String forward = "(CAST(DATEDIFF(DAY, " + begin + ", " + end + ") AS BIGINT) * 86400"
                + " + DATEDIFF(SECOND, CAST(" + begin + " AS time(7)), CAST(" + end + " AS time(7)))"
                + " - CASE WHEN DATEPART(NANOSECOND, " + end + ") < DATEPART(NANOSECOND, " + begin + ") THEN 1 ELSE 0 END)";
        String backward = "(CAST(DATEDIFF(DAY, " + end + ", " + begin + ") AS BIGINT) * 86400"
                + " + DATEDIFF(SECOND, CAST(" + end + " AS time(7)), CAST(" + begin + " AS time(7)))"
                + " - CASE WHEN DATEPART(NANOSECOND, " + begin + ") < DATEPART(NANOSECOND, " + end + ") THEN 1 ELSE 0 END)";
        String days = "CAST(CASE WHEN " + end + " >= " + begin + " THEN " + forward + " ELSE " + backward
                + " END AS FLOAT) / 86400 - {3}";
        String sql = SqlDialectUtils.dateDecayScore(DbType.SQL_SERVER, "t.publish_time");

        assertEquals("{0} * POWER(CAST({1} AS FLOAT), (CASE WHEN " + days + " < 0 THEN 0 ELSE " + days + " END) / {4})", sql);
        // 不使用 2016+ 的 DATEDIFF_BIG，也不对整个时间差使用会溢出的 DATEDIFF(SECOND)
        assertFalse(sql.contains("DATEDIFF_BIG"));
        assertFalse(sql.contains("DATEDIFF(SECOND, " + begin));
        assertFalse(sql.contains("GREATEST"));
    }

    @Test
    public void testDateDecayScore_placeholders() {
        for (DbType dbType : DATE_DB_TYPES) {
            String sql = SqlDialectUtils.dateDecayScore(dbType, "t.publish_time");
            // 五个编号参数全部出现，没有多余的编号；片段经过 MPJ selectFunc 的 String.format 时不能含 %
            assertEquals(List.of("{0}", "{1}", "{2}", "{3}", "{4}"), distinctPlaceholders(sql), dbType.name());
            assertFalse(sql.contains("%"), dbType.name());
        }
    }

    @Test
    public void testDateDecayScore_unsupportedDbType() {
        // 未验收的数据库不按 MySQL 处理
        assertThrows(IllegalStateException.class, () -> SqlDialectUtils.dateDecayScore(null, "t.publish_time"));
        for (DbType dbType : List.of(DbType.SQL_SERVER2005, DbType.SQLITE, DbType.DB2, DbType.OTHER)) {
            assertThrows(IllegalStateException.class, () -> SqlDialectUtils.dateDecayScore(dbType, "t.publish_time"),
                    dbType.name());
        }
    }

    @Test
    public void testSetMillisVersion() {
        // MySQL 系、H2、达梦：原有片段
        String mysql = "live_update_time = GREATEST(CAST({0} AS DATETIME(3)),"
                + " COALESCE(TIMESTAMPADD(MICROSECOND, 1000, live_update_time), CAST({0} AS DATETIME(3))))";
        for (DbType dbType : List.of(DbType.MYSQL, DbType.MARIADB, DbType.OCEAN_BASE, DbType.H2, DbType.DM)) {
            assertEquals(mysql, SqlDialectUtils.setMillisVersion(dbType, "live_update_time"), dbType.name());
        }
        // PostgreSQL 系：参数显式转换类型
        String postgresql = "live_update_time = GREATEST(CAST({0} AS TIMESTAMP(3)),"
                + " COALESCE(live_update_time + INTERVAL '0.001' SECOND, CAST({0} AS TIMESTAMP(3))))";
        for (DbType dbType : List.of(DbType.POSTGRE_SQL, DbType.KINGBASE_ES, DbType.OPENGAUSS, DbType.GAUSS_DB)) {
            assertEquals(postgresql, SqlDialectUtils.setMillisVersion(dbType, "live_update_time"), dbType.name());
        }
        // Oracle：兼容 DATE、TIMESTAMP 列
        assertEquals("live_update_time = GREATEST(CAST({0} AS TIMESTAMP(9)),"
                        + " COALESCE(CAST(live_update_time AS TIMESTAMP(9)) + INTERVAL '0.001' SECOND, CAST({0} AS TIMESTAMP(9))))",
                SqlDialectUtils.setMillisVersion(DbType.ORACLE, "live_update_time"));
        // SQL Server：没有 GREATEST，原值为 NULL 时取参数
        assertEquals("live_update_time = CASE WHEN live_update_time IS NULL"
                        + " OR DATEADD(MILLISECOND, 1, live_update_time) < CAST({0} AS datetime2(3))"
                        + " THEN CAST({0} AS datetime2(3)) ELSE DATEADD(MILLISECOND, 1, live_update_time) END",
                SqlDialectUtils.setMillisVersion(DbType.SQL_SERVER, "live_update_time"));
        // 神通：不依赖 GREATEST 的 NULL 语义，原值为 NULL 时取参数
        assertEquals("live_update_time = CASE WHEN live_update_time IS NULL"
                        + " OR live_update_time + INTERVAL '0.001' SECOND < CAST({0} AS TIMESTAMP(3))"
                        + " THEN CAST({0} AS TIMESTAMP(3)) ELSE live_update_time + INTERVAL '0.001' SECOND END",
                SqlDialectUtils.setMillisVersion(DbType.OSCAR, "live_update_time"));
    }

    @Test
    public void testSetMillisVersion_unsupportedDbType() {
        assertThrows(IllegalStateException.class, () -> SqlDialectUtils.setMillisVersion(null, "live_update_time"));
        for (DbType dbType : List.of(DbType.SQL_SERVER2005, DbType.SQLITE, DbType.DB2, DbType.OTHER)) {
            assertThrows(IllegalStateException.class, () -> SqlDialectUtils.setMillisVersion(dbType, "live_update_time"),
                    dbType.name());
        }
    }

    /**
     * UpdateWrapper 的 setSql 将片段中的同一编号参数全部替换为同一个绑定值，不拼接参数值
     */
    @Test
    public void testSetMillisVersion_boundByWrapper() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 22, 0, 0, 0, 123_000_000);
        for (DbType dbType : DATE_DB_TYPES) {
            UpdateWrapper<Object> wrapper = new UpdateWrapper<>()
                    .setSql(SqlDialectUtils.setMillisVersion(dbType, "live_update_time"), now);
            String sqlSet = wrapper.getSqlSet();

            assertFalse(sqlSet.contains("{0}"), dbType.name());
            assertFalse(sqlSet.contains("2026"), dbType.name());
            assertEquals(1, wrapper.getParamNameValuePairs().size(), dbType.name());
            assertEquals(now, wrapper.getParamNameValuePairs().values().iterator().next(), dbType.name());
        }
    }

    @Test
    public void testDateFragments_invalidColumn() {
        for (DbType dbType : DATE_DB_TYPES) {
            for (String column : new String[]{"t.publish_time) OR (1=1", "publish_time;drop table t", "'1'", "", null}) {
                assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.dateDecayScore(dbType, column),
                        dbType + " " + column);
                assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.setMillisVersion(dbType, column),
                        dbType + " " + column);
            }
        }
    }

    /**
     * MyBatis Plus 租户等插件会使用 JSqlParser 解析最终 SQL，所有数据库类型的日期运算片段均需可被解析
     */
    @Test
    public void testDateFragments_parsedByJSqlParser() throws Exception {
        for (DbType dbType : DATE_DB_TYPES) {
            String score = SqlDialectUtils.dateDecayScore(dbType, "t.publish_time");
            String select = "SELECT t.id, " + score + " + CASE WHEN t.slug LIKE ? THEN 3 ELSE 0 END AS search_score"
                    + " FROM cms_article t WHERE t.deleted = 0 ORDER BY search_score DESC, t.publish_time DESC, t.id DESC";
            assertNotNull(CCJSqlParserUtil.parse(bindIndexParams(select)), dbType + " " + score);

            String set = SqlDialectUtils.setMillisVersion(dbType, "live_update_time");
            String update = "UPDATE cms_interview SET status = ?, end_time = NULL, " + set
                    + " WHERE id = ? AND status = ? AND deleted = 0";
            assertNotNull(CCJSqlParserUtil.parse(bindIndexParams(update)), dbType + " " + set);
        }
    }

    // ========== 方言识别 ==========

    @Test
    public void testResolveDialect_accepted() throws SQLException {
        assertEquals(Dialect.POSTGRESQL, resolve("PostgreSQL", "jdbc:postgresql://127.0.0.1:5432/ruoyi-vue-pro", 17));
        assertEquals(Dialect.POSTGRESQL, resolve("PostgreSQL", "jdbc:p6spy:postgresql://127.0.0.1:5432/ruoyi-vue-pro", 17));
        assertEquals(Dialect.ORACLE, resolve("Oracle", "jdbc:oracle:thin:@127.0.0.1:1521:xe", 21));
        assertEquals(Dialect.SQLSERVER, resolve("Microsoft SQL Server", "jdbc:sqlserver://127.0.0.1:1433", 16));
        assertEquals(Dialect.SQLSERVER, resolve("Microsoft SQL Server", "jdbc:sqlserver://127.0.0.1:1433", 11));
    }

    @Test
    public void testResolveDialect_original() throws SQLException {
        // MySQL 族
        assertEquals(Dialect.ORIGINAL, resolve("MySQL", "jdbc:mysql://127.0.0.1:3306/ruoyi-vue-pro", 8));
        assertEquals(Dialect.ORIGINAL, resolve("MariaDB", "jdbc:mariadb://127.0.0.1:3306/ruoyi-vue-pro", 11));
        // MariaDB 产品名已登记：p6spy 包装、MySQL 驱动 URL 同样为原有片段，不因登记而变为不支持
        assertEquals(Dialect.ORIGINAL, resolve("MariaDB", "jdbc:p6spy:mariadb://127.0.0.1:3306/ruoyi-vue-pro", 11));
        assertEquals(Dialect.ORIGINAL, resolve("MariaDB", "jdbc:mysql://127.0.0.1:3306/ruoyi-vue-pro", 11));
        assertEquals(Dialect.ORIGINAL, resolve("OceanBase", "jdbc:oceanbase://127.0.0.1:2881/ruoyi-vue-pro", 4));
        // 已有旧路径：H2
        assertEquals(Dialect.ORIGINAL, resolve("H2", "jdbc:h2:mem:testdb;MODE=MYSQL", 2));
        // 达梦、神通：单独识别，未单独处理的片段仍为原有片段
        assertEquals(Dialect.DM, resolve("DM DBMS", "jdbc:dm://127.0.0.1:5236", 8));
        assertEquals(Dialect.OSCAR, resolve("OSCAR", "jdbc:oscar://127.0.0.1:2003/osrdb", 7));
        // 产品名已登记为其它数据库时，URL 命中 PostgreSQL 变种的原生驱动也不按 PostgreSQL 处理
        assertEquals(Dialect.ORIGINAL, resolve("MySQL", "jdbc:p6spy:highgo://127.0.0.1:5866/highgo", 8));
        assertEquals(Dialect.DM, resolve("DM DBMS", "jdbc:p6spy:kingbase8://127.0.0.1:54321/test", 8));
    }

    /**
     * SQL Server 2012 以下：原有的 DATE_FORMAT、LIMIT、FIND_IN_SET 等 MySQL 片段无法执行，普通片段不再按 MySQL 处理；
     * JSON 片段不随之变化，仍为原有片段
     */
    @Test
    public void testResolveDialect_legacySqlServer() throws SQLException {
        String secretUrl = "jdbc:sqlserver://127.0.0.1:1433;user=sa;password=secret";
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> resolve("Microsoft SQL Server", secretUrl, 10));
        assertTrue(ex.getMessage().contains("SQL Server 2012"), ex.getMessage());
        assertTrue(ex.getMessage().contains("10"), ex.getMessage());
        assertFalse(ex.getMessage().contains("secret"));
        assertFalse(ex.getMessage().contains("jdbc:"));
        assertThrows(IllegalStateException.class,
                () -> resolve("Microsoft SQL Server 2005", "jdbc:jtds:sqlserver://127.0.0.1:1433", 9));
        // 与 JSON 片段的识别相互独立
        assertEquals(Dialect.ORIGINAL, resolveJson("Microsoft SQL Server", secretUrl, 10));
        assertEquals(Dialect.ORIGINAL, resolveJson("Microsoft SQL Server 2005", "jdbc:jtds:sqlserver://127.0.0.1:1433", 9));
        assertEquals(Dialect.ORIGINAL, resolveJsonValue("Microsoft SQL Server", secretUrl, 10));
        assertEquals(Dialect.ORIGINAL, resolveJsonValue("Microsoft SQL Server 2005", "jdbc:jtds:sqlserver://127.0.0.1:1433", 9));
    }

    /**
     * JSON 字段值：在数据库产品方言的基础上只单独识别达梦，不随普通片段对神通、SQL Server 2012 以下的识别变化
     */
    @Test
    public void testResolveJsonValueDialect() throws SQLException {
        assertEquals(Dialect.DM, resolveJsonValue("DM DBMS", "jdbc:dm://127.0.0.1:5236", 8));
        assertEquals(Dialect.DM, resolveJsonValue("DM DBMS", "jdbc:p6spy:kingbase8://127.0.0.1:54321/test", 8));
        assertEquals(Dialect.ORIGINAL, resolveJsonValue("OSCAR", "jdbc:oscar://127.0.0.1:2003/osrdb", 7));
        assertEquals(Dialect.ORIGINAL, resolveJsonValue("MySQL", "jdbc:mysql://127.0.0.1:3306/a", 8));
        assertEquals(Dialect.ORIGINAL, resolveJsonValue("MariaDB", "jdbc:mariadb://127.0.0.1:3306/a", 11));
        assertEquals(Dialect.ORIGINAL, resolveJsonValue("MariaDB", "jdbc:mysql://127.0.0.1:3306/a", 11));
        assertEquals(Dialect.ORIGINAL, resolveJsonValue("H2", "jdbc:h2:mem:testdb;MODE=MYSQL", 2));
        assertEquals(Dialect.POSTGRESQL, resolveJsonValue("openGauss", "jdbc:opengauss://127.0.0.1:5432/test", 5));
        assertEquals(Dialect.POSTGRESQL, resolveJsonValue("PostgreSQL", "jdbc:postgresql://127.0.0.1:5432/a", 17));
        assertEquals(Dialect.ORACLE, resolveJsonValue("Oracle", "jdbc:oracle:thin:@127.0.0.1:1521:xe", 21));
        assertEquals(Dialect.SQLSERVER, resolveJsonValue("Microsoft SQL Server", "jdbc:sqlserver://127.0.0.1:1433", 11));
        assertThrows(IllegalStateException.class, () -> resolveJsonValue("SQLite", "jdbc:sqlite:/tmp/test.db", 3));
    }

    /**
     * 兼容 PostgreSQL 的数据库：与 {@link JdbcUtils#getDbType(String)} 一致，普通片段按 PostgreSQL 处理
     */
    @Test
    public void testResolveDialect_postgreSQLVariant() throws SQLException {
        // 产品名已登记：人大金仓、openGauss、GaussDB，原生驱动或 PostgreSQL 驱动均可
        assertEquals(Dialect.POSTGRESQL, resolve("KingbaseES", "jdbc:kingbase8://127.0.0.1:54321/test", 8));
        assertEquals(Dialect.POSTGRESQL, resolve("KingbaseES", "jdbc:kingbase://127.0.0.1:54321/test", 7));
        assertEquals(Dialect.POSTGRESQL, resolve("KingbaseES", "jdbc:postgresql://127.0.0.1:54321/test", 8));
        assertEquals(Dialect.POSTGRESQL, resolve("openGauss", "jdbc:opengauss://127.0.0.1:5432/test", 5));
        assertEquals(Dialect.POSTGRESQL, resolve("openGauss", "jdbc:postgresql://127.0.0.1:5432/test", 5));
        assertEquals(Dialect.POSTGRESQL, resolve("GaussDB", "jdbc:gaussdb://127.0.0.1:8000/test", 5));
        assertEquals(Dialect.POSTGRESQL, resolve("GaussDB", "jdbc:postgresql://127.0.0.1:8000/test", 5));
        // 产品名为 PostgreSQL：按原生驱动 URL 识别，含 p6spy 等包装
        assertEquals(Dialect.POSTGRESQL, resolve("PostgreSQL", "jdbc:highgo://127.0.0.1:5866/highgo", 6));
        assertEquals(Dialect.POSTGRESQL, resolve("PostgreSQL", "jdbc:p6spy:highgo://127.0.0.1:5866/highgo", 6));
        assertEquals(Dialect.POSTGRESQL, resolve("PostgreSQL", "jdbc:opengauss://127.0.0.1:5432/test", 9));
        assertEquals(Dialect.POSTGRESQL, resolve("PostgreSQL", "jdbc:mogdb://127.0.0.1:5432/test", 9));
        assertEquals(Dialect.POSTGRESQL, resolve("PostgreSQL", "jdbc:gaussdb://127.0.0.1:8000/test", 9));
        assertEquals(Dialect.POSTGRESQL, resolve("PostgreSQL", "jdbc:kingbase8://127.0.0.1:54321/test", 12));
        // 产品名未登记：按原生驱动 URL 识别，例如瀚高原生驱动的产品名 HighGo、MogDB
        assertEquals(Dialect.POSTGRESQL, resolve("HighGo", "jdbc:highgo://127.0.0.1:5866/highgo", 6));
        assertEquals(Dialect.POSTGRESQL, resolve("Highgo Database", "JDBC:HIGHGO://127.0.0.1:5866/highgo", 6));
        assertEquals(Dialect.POSTGRESQL, resolve("MogDB", "jdbc:mogdb://127.0.0.1:5432/test", 5));
        assertEquals(Dialect.POSTGRESQL, resolve("MogDB", "jdbc:opengauss://127.0.0.1:5432/test", 5));
        assertEquals(Dialect.POSTGRESQL, resolve("Kingbase", "jdbc:kingbase86://127.0.0.1:54321/test", 8));
        assertEquals(Dialect.POSTGRESQL, resolve("Vastbase", "jdbc:gaussdb://127.0.0.1:8000/test", 5));
        // 只识别 JDBC 协议：p6spy 包装、大写均可
        assertEquals(Dialect.POSTGRESQL, resolve("MogDB", "jdbc:p6spy:mogdb://127.0.0.1:5432/test", 5));
        assertEquals(Dialect.POSTGRESQL, resolve("PostgreSQL", "JDBC:P6SPY:OPENGAUSS://127.0.0.1:5432/test", 9));
        // PostgreSQL 官方驱动：参数、fragment 中的 :opengauss: 等不影响，按真实协议识别为 PostgreSQL
        assertEquals(Dialect.POSTGRESQL, resolve("PostgreSQL",
                "jdbc:postgresql://127.0.0.1:5432/a?ApplicationName=:opengauss:", 17));
        assertEquals(Dialect.POSTGRESQL, resolve("PostgreSQL",
                "jdbc:p6spy:postgresql://127.0.0.1:5432/a?ApplicationName=:highgo:#:mogdb:", 17));
    }

    @Test
    public void testResolveDialect_unsupported() {
        String secretUrl = "jdbc:sqlite:/tmp/test.db?user=root&password=secret";
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> resolve("SQLite", secretUrl, 3));
        assertTrue(ex.getMessage().contains("SQLite"));
        assertFalse(ex.getMessage().contains("secret"));
        assertFalse(ex.getMessage().contains("jdbc:"));
        // 产品名为 PostgreSQL，但 URL 不是 PostgreSQL 或已识别的变种
        assertThrows(IllegalStateException.class, () -> resolve("PostgreSQL", "jdbc:vastbase://127.0.0.1:5432/test", 9));
        // 产品名未登记，URL 也不是已识别的原生驱动；URL 中的 PostgreSQL 变种名称（库名、参数）不参与识别
        String secretVariantUrl = "jdbc:vastbase://127.0.0.1:5432/opengauss?user=root&password=secret";
        ex = assertThrows(IllegalStateException.class, () -> resolve("Vastbase", secretVariantUrl, 9));
        assertFalse(ex.getMessage().contains("secret"));
        assertFalse(ex.getMessage().contains("jdbc:"));
        assertThrows(IllegalStateException.class, () -> resolve("PostgreSQL", "jdbc:vastbase://127.0.0.1:5432/highgo", 9));
        // 未识别的 JDBC 协议：参数、路径、fragment 中的 :highgo:、:opengauss: 等不能激活 PostgreSQL 变种的识别
        for (String productName : new String[]{"PostgreSQL", "Vastbase"}) {
            for (String url : List.of("jdbc:vastbase://127.0.0.1:5432/db?ApplicationName=:opengauss:",
                    "jdbc:vastbase://127.0.0.1:5432/db?ApplicationName=:highgo:",
                    "jdbc:vastbase://127.0.0.1:5432/db:kingbase8:/x",
                    "jdbc:vastbase://127.0.0.1:5432/db#:mogdb:",
                    "jdbc:unknown:highgo://127.0.0.1:5866/highgo")) {
                assertThrows(IllegalStateException.class, () -> resolve(productName, url, 9), productName + " " + url);
                assertThrows(IllegalStateException.class, () -> resolveJson(productName, url, 9), productName + " " + url);
            }
        }
        // 产品名为空
        assertThrows(IllegalStateException.class, () -> resolve(null, "jdbc:unknown://127.0.0.1/test", 1));
    }

    @Test
    public void testGetDialect_cachedPerDataSource() {
        FakeDataSource postgresql = new FakeDataSource("PostgreSQL", "jdbc:postgresql://127.0.0.1:5432/a", 17);
        FakeDataSource mysql = new FakeDataSource("MySQL", "jdbc:mysql://127.0.0.1:3306/a", 8);

        assertEquals(Dialect.POSTGRESQL, SqlDialectUtils.getDialect(postgresql.getDataSource()));
        assertEquals(Dialect.ORIGINAL, SqlDialectUtils.getDialect(mysql.getDataSource()));
        assertEquals(Dialect.POSTGRESQL, SqlDialectUtils.getDialect(postgresql.getDataSource()));
        assertEquals(Dialect.ORIGINAL, SqlDialectUtils.getDialect(mysql.getDataSource()));
        assertEquals(1, postgresql.getConnectionCount());
        assertEquals(1, mysql.getConnectionCount());
    }

    @Test
    public void testGetDialect_failureNotCached() {
        FakeDataSource broken = new FakeDataSource("PostgreSQL", "jdbc:postgresql://127.0.0.1:5432/a", 17);
        broken.setFailOnConnection(true);
        assertThrows(IllegalStateException.class, () -> SqlDialectUtils.getDialect(broken.getDataSource()));
        assertThrows(IllegalStateException.class, () -> SqlDialectUtils.getDialect(broken.getDataSource()));
        assertEquals(2, broken.getConnectionCount());

        // 恢复后可以正常识别
        broken.setFailOnConnection(false);
        assertEquals(Dialect.POSTGRESQL, SqlDialectUtils.getDialect(broken.getDataSource()));
    }

    @Test
    public void testFormatDate_transactionConnectionDoesNotBorrowOrClose() throws SQLException {
        FakeDataSource h2 = new FakeDataSource("H2", "jdbc:h2:mem:single_connection", 2);
        Connection held = h2.getDataSource().getConnection();
        h2.setFailOnConnection(true); // 当前事务已占用唯一连接，任何额外借用都会失败
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("dataSource", h2.getDataSource());
        new SpringUtil().postProcessBeanFactory(beanFactory);
        TransactionSynchronizationManager.bindResource(h2.getDataSource(), new ConnectionHolder(held));
        try {
            assertEquals("DATE_FORMAT(create_time, '%Y-%m-%d')", SqlDialectUtils.formatDate("create_time"));
            assertEquals(1, h2.getConnectionCount());
            assertEquals(0, h2.closeCount);
        } finally {
            TransactionSynchronizationManager.unbindResource(h2.getDataSource());
            held.close();
        }
    }

    @Test
    public void testGetDialect_transactionUsesBoundConnectionWithoutPoisoningRouteCache() throws SQLException {
        FakeDataSource postgresql = new FakeDataSource("PostgreSQL", "jdbc:postgresql://probe/transaction", 17);
        FakeDataSource mysql = new FakeDataSource("MySQL", "jdbc:mysql://probe/other", 8);
        mockDynamicRoutingDataSource(Map.of("master", postgresql, "mysql", mysql));
        DynamicRoutingDataSource routing = SpringUtil.getBean(DynamicRoutingDataSource.class);
        Connection held = postgresql.getDataSource().getConnection();
        TransactionSynchronizationManager.bindResource(routing, new ConnectionHolder(held));
        DynamicDataSourceContextHolder.push("mysql");
        try {
            // Spring 事务的执行连接仍为 PG，切换线程路由键不会改变它
            assertEquals("CAST(EXTRACT(MONTH FROM birthday) AS INTEGER)", SqlDialectUtils.month("birthday"));
            assertEquals(1, postgresql.getConnectionCount());
            assertEquals(0, mysql.getConnectionCount());
            assertEquals(0, postgresql.closeCount);
        } finally {
            TransactionSynchronizationManager.unbindResource(routing);
            held.close();
        }
        // 事务结束后该路由真正使用 MySQL，不能命中错误的 PG 缓存
        assertEquals("MONTH(birthday)", SqlDialectUtils.month("birthday"));
        assertEquals(1, mysql.getConnectionCount());
    }

    @Test
    public void testFormatDate_followsDynamicRouting() {
        FakeDataSource postgresql = new FakeDataSource("PostgreSQL", "jdbc:postgresql://127.0.0.1:5432/a", 17);
        FakeDataSource mysql = new FakeDataSource("MySQL", "jdbc:mysql://127.0.0.1:3306/a", 8);
        FakeDataSource oracle = new FakeDataSource("Oracle", "jdbc:oracle:thin:@127.0.0.1:1521:xe", 21);
        mockDynamicRoutingDataSource(Map.of("master", postgresql, "mysql", mysql, "oracle", oracle));

        for (int i = 0; i < 3; i++) {
            // 未切换时为主库
            assertEquals("TO_CHAR(create_time, 'YYYY-MM-DD')", SqlDialectUtils.formatDate("create_time"));
            DynamicDataSourceContextHolder.push("mysql");
            assertEquals("DATE_FORMAT(create_time, '%Y-%m-%d')", SqlDialectUtils.formatDate("create_time"));
            DynamicDataSourceContextHolder.push("oracle");
            assertEquals("TO_CHAR(create_time, 'YYYY-MM-DD')", SqlDialectUtils.formatDate("create_time"));
            assertEquals("FROM DUAL", SqlDialectUtils.fromDual());
            DynamicDataSourceContextHolder.poll();
            assertEquals("", SqlDialectUtils.fromDual());
            DynamicDataSourceContextHolder.poll();
        }
        assertEquals(1, postgresql.getConnectionCount());
        assertEquals(1, mysql.getConnectionCount());
        assertEquals(1, oracle.getConnectionCount());
    }

    /**
     * 兼容 PostgreSQL 的原生驱动：动态路由后，普通片段使用 PostgreSQL 写法、不含 MySQL 专用函数；JSON 片段按 JSON 方言识别
     */
    @Test
    public void testFragments_postgreSQLVariantFollowsDynamicRouting() throws Exception {
        FakeDataSource mysql = new FakeDataSource("MySQL", "jdbc:mysql://127.0.0.1:3306/a", 8);
        Map<String, FakeDataSource> variants = new LinkedHashMap<>();
        variants.put("highgo", new FakeDataSource("HighGo", "jdbc:highgo://127.0.0.1:15866/highgo", 6));
        variants.put("kingbase", new FakeDataSource("KingbaseES", "jdbc:kingbase8://127.0.0.1:54321/test", 8));
        variants.put("opengauss", new FakeDataSource("openGauss", "jdbc:opengauss://127.0.0.1:5432/test", 5));
        variants.put("gaussdb", new FakeDataSource("GaussDB", "jdbc:gaussdb://127.0.0.1:8000/test", 5));
        variants.put("mogdb", new FakeDataSource("MogDB", "jdbc:mogdb://127.0.0.1:5432/test", 5));
        // PostgreSQL 官方驱动，参数中带 :opengauss:：按真实协议识别为 PostgreSQL
        variants.put("postgresqlParam", new FakeDataSource("PostgreSQL",
                "jdbc:postgresql://127.0.0.1:5432/a?ApplicationName=:opengauss:", 17));
        // 未识别的协议，参数中带 :highgo:：不能误路由为 PostgreSQL，抛出异常
        FakeDataSource fakeProtocol = new FakeDataSource("PostgreSQL",
                "jdbc:vastbase://127.0.0.1:5432/a?ApplicationName=:highgo:", 9);
        Map<String, FakeDataSource> dataSources = new HashMap<>(variants);
        dataSources.put("master", mysql);
        dataSources.put("fakeProtocol", fakeProtocol);
        mockDynamicRoutingDataSource(dataSources);
        // JSON 片段：PostgreSQL 官方驱动、瀚高原生驱动、人大金仓、openGauss、GaussDB（含 MogDB）使用 jsonb 写法
        Map<String, Dialect> jsonDialects = Map.of("highgo", Dialect.POSTGRESQL, "kingbase", Dialect.POSTGRESQL,
                "opengauss", Dialect.POSTGRESQL, "gaussdb", Dialect.POSTGRESQL, "mogdb", Dialect.POSTGRESQL,
                "postgresqlParam", Dialect.POSTGRESQL);
        Pattern mysqlFunction = Pattern.compile(
                "\\b(DATE_FORMAT|DATE|HOUR|MONTH|YEAR|DAYOFMONTH|FIND_IN_SET|TIMESTAMPDIFF|TRUNCATE)\\(");
        // 与 CmsVisitLogMapper 的小时统计相同
        SqlSource visitSource = new XMLLanguageDriver().createSqlSource(new Configuration(),
                "<script>SELECT " + P + "hour('t.create_time')} AS hourOfDay, COUNT(1) AS pv FROM cms_visit_log t"
                        + " WHERE t.create_time BETWEEN #{beginTime} AND #{endTime}"
                        + " GROUP BY " + P + "hour('t.create_time')} ORDER BY " + P + "hour('t.create_time')} ASC</script>",
                Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("beginTime", LocalDateTime.of(2026, 1, 1, 0, 0));
        params.put("endTime", LocalDateTime.of(2026, 1, 31, 23, 59, 59));
        String pgHour = "CAST(EXTRACT(HOUR FROM t.create_time) AS INTEGER)";
        String pgDayStart = "CAST(CAST(t.create_time AS DATE) AS TIMESTAMP)";

        for (int i = 0; i < 2; i++) {
            for (String name : variants.keySet()) {
                DynamicDataSourceContextHolder.push(name);
                List<String> fragments = List.of(SqlDialectUtils.formatDate("t.create_time"),
                        SqlDialectUtils.formatYearMonth("t.create_time"), SqlDialectUtils.formatYearDashMonth("t.create_time"),
                        SqlDialectUtils.date("t.create_time"), SqlDialectUtils.castDateText("t.create_time"),
                        SqlDialectUtils.castDate("t.create_time"), SqlDialectUtils.dayStart("t.create_time"),
                        SqlDialectUtils.month("t.create_time"), SqlDialectUtils.year("t.create_time"),
                        SqlDialectUtils.dayOfMonth("t.create_time"), SqlDialectUtils.hour("t.create_time"),
                        SqlDialectUtils.customerDealCycle("customer.create_time", "contract.order_date"),
                        SqlDialectUtils.findInSetColumn("cc.id", "cfur.contact_ids"),
                        SqlDialectUtils.limitRows(10, true), SqlDialectUtils.limitParam("limit", false),
                        SqlDialectUtils.deleteBatchByLessThan("infra_job_log", "create_time", "createTime", "limit"),
                        SqlDialectUtils.fromDual());
                List<String> expected = List.of(SqlDialectUtils.formatDate(Dialect.POSTGRESQL, "t.create_time"),
                        SqlDialectUtils.formatYearMonth(Dialect.POSTGRESQL, "t.create_time"),
                        SqlDialectUtils.formatYearDashMonth(Dialect.POSTGRESQL, "t.create_time"),
                        SqlDialectUtils.date(Dialect.POSTGRESQL, "t.create_time"),
                        SqlDialectUtils.castDateText(Dialect.POSTGRESQL, "t.create_time"),
                        SqlDialectUtils.castDate(Dialect.POSTGRESQL, "t.create_time"), pgDayStart,
                        SqlDialectUtils.month(Dialect.POSTGRESQL, "t.create_time"),
                        SqlDialectUtils.year(Dialect.POSTGRESQL, "t.create_time"),
                        SqlDialectUtils.dayOfMonth(Dialect.POSTGRESQL, "t.create_time"), pgHour,
                        SqlDialectUtils.customerDealCycle(Dialect.POSTGRESQL, "customer.create_time", "contract.order_date"),
                        SqlDialectUtils.findInSetColumn(Dialect.POSTGRESQL, "cc.id", "cfur.contact_ids"),
                        "LIMIT 10", "LIMIT #{limit}",
                        SqlDialectUtils.deleteBatchByLessThan(Dialect.POSTGRESQL, "infra_job_log", "create_time", "createTime", "limit"),
                        "");
                assertEquals(expected, fragments, name);
                for (String fragment : fragments) {
                    assertFalse(mysqlFunction.matcher(fragment).find(), name + " " + fragment);
                }

                // CMS 访问小时统计
                BoundSql boundSql = visitSource.getBoundSql(params);
                String sql = normalize(boundSql.getSql());
                assertEquals("SELECT " + pgHour + " AS hourOfDay, COUNT(1) AS pv FROM cms_visit_log t"
                        + " WHERE t.create_time BETWEEN ? AND ? GROUP BY " + pgHour + " ORDER BY " + pgHour + " ASC", sql);
                assertParameters(boundSql, "beginTime", "endTime");
                assertNotNull(CCJSqlParserUtil.parse(sql), name + " " + sql);
                // CMS 附件按天统计：与 CmsAttachmentMapper 相同，按 dayStart 分组
                String attachmentSql = "SELECT " + pgDayStart + " AS createDate, COUNT(1) AS total FROM cms_attachment t"
                        + " WHERE t.create_time >= ? GROUP BY " + pgDayStart;
                assertNotNull(CCJSqlParserUtil.parse(attachmentSql), name + " " + attachmentSql);

                // JSON 片段按 JSON 方言识别
                assertEquals(SqlDialectUtils.jsonArrayContains(jsonDialects.get(name), "label_ids", true),
                        SqlDialectUtils.jsonArrayContains("label_ids", true), name);
                DynamicDataSourceContextHolder.poll();
            }
            // 主库 MySQL 保持原有片段
            assertEquals("HOUR(t.create_time)", SqlDialectUtils.hour("t.create_time"));
            assertEquals("CAST(t.create_time AS DATE)", SqlDialectUtils.dayStart("t.create_time"));
            assertEquals("JSON_CONTAINS(label_ids, JSON_ARRAY({0}))", SqlDialectUtils.jsonArrayContains("label_ids", true));
        }
        // 普通片段与 JSON 片段分别按数据源缓存，各至少识别一次；缓存为弱引用的条目，GC 回收后会再次识别，
        // 因此只断言下限，不断言固定次数。每次识别借用的连接都已关闭，缓存未命中时也不泄漏
        for (Map.Entry<String, FakeDataSource> dataSource : dataSources.entrySet()) {
            if (dataSource.getValue() != fakeProtocol) {
                assertTrue(dataSource.getValue().getConnectionCount() >= 2, dataSource.getKey());
                assertEquals(dataSource.getValue().getConnectionCount(), dataSource.getValue().closeCount,
                        dataSource.getKey());
            }
        }

        // 未识别的协议：普通片段、JSON 片段均抛出异常
        DynamicDataSourceContextHolder.push("fakeProtocol");
        assertThrows(IllegalStateException.class, () -> SqlDialectUtils.hour("t.create_time"));
        assertThrows(IllegalStateException.class, () -> SqlDialectUtils.jsonArrayContains("label_ids", true));
        DynamicDataSourceContextHolder.poll();
    }

    @Test
    public void testFragments_postgreSQLVariantTransactionUsesBoundConnection() throws SQLException {
        FakeDataSource openGauss = new FakeDataSource("openGauss", "jdbc:opengauss://probe/transaction", 5);
        FakeDataSource mysql = new FakeDataSource("MySQL", "jdbc:mysql://probe/other", 8);
        mockDynamicRoutingDataSource(Map.of("master", openGauss, "mysql", mysql));
        DynamicRoutingDataSource routing = SpringUtil.getBean(DynamicRoutingDataSource.class);
        Connection held = openGauss.getDataSource().getConnection();
        TransactionSynchronizationManager.bindResource(routing, new ConnectionHolder(held));
        DynamicDataSourceContextHolder.push("mysql");
        try {
            // Spring 事务的执行连接仍为 openGauss：普通片段与 JSON 片段均按 PostgreSQL 处理
            assertEquals("CAST(EXTRACT(HOUR FROM t.create_time) AS INTEGER)", SqlDialectUtils.hour("t.create_time"));
            assertEquals("CAST(CAST(t.create_time AS DATE) AS TIMESTAMP)", SqlDialectUtils.dayStart("t.create_time"));
            assertEquals(postgreSQLJsonArrayContains("label_ids"), SqlDialectUtils.jsonArrayContains("label_ids", true));
            assertEquals(1, openGauss.getConnectionCount());
            assertEquals(0, mysql.getConnectionCount());
            assertEquals(0, openGauss.closeCount);
        } finally {
            TransactionSynchronizationManager.unbindResource(routing);
            held.close();
        }
        // 事务结束后该路由真正使用 MySQL，不能命中 openGauss 的方言
        assertEquals("HOUR(t.create_time)", SqlDialectUtils.hour("t.create_time"));
        assertEquals(1, mysql.getConnectionCount());
    }

    @Test
    public void testBoundSql_xmlScript() {
        FakeDataSource postgresql = new FakeDataSource("PostgreSQL", "jdbc:postgresql://127.0.0.1:5432/a", 17);
        FakeDataSource mysql = new FakeDataSource("MySQL", "jdbc:mysql://127.0.0.1:3306/a", 8);
        mockDynamicRoutingDataSource(Map.of("master", postgresql, "mysql", mysql));
        SqlSource sqlSource = new XMLLanguageDriver().createSqlSource(new Configuration(),
                "<script>SELECT " + P + "formatDate('create_time')} AS time, COUNT(*) AS customerCreateCount"
                        + " FROM crm_customer WHERE create_time BETWEEN #{beginTime} AND #{endTime}"
                        + " GROUP BY " + P + "formatDate('create_time')}</script>", Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("beginTime", LocalDateTime.of(2026, 1, 1, 0, 0));
        params.put("endTime", LocalDateTime.of(2026, 1, 31, 23, 59, 59));

        // 主库：PostgreSQL
        BoundSql boundSql = sqlSource.getBoundSql(params);
        assertEquals("SELECT TO_CHAR(create_time, 'YYYY-MM-DD') AS time, COUNT(*) AS customerCreateCount"
                + " FROM crm_customer WHERE create_time BETWEEN ? AND ?"
                + " GROUP BY TO_CHAR(create_time, 'YYYY-MM-DD')", normalize(boundSql.getSql()));
        assertParameters(boundSql, "beginTime", "endTime");

        // 同一个 SqlSource 在下一次执行时，跟随路由切换到 MySQL
        DynamicDataSourceContextHolder.push("mysql");
        boundSql = sqlSource.getBoundSql(params);
        assertEquals("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS time, COUNT(*) AS customerCreateCount"
                + " FROM crm_customer WHERE create_time BETWEEN ? AND ?"
                + " GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d')", normalize(boundSql.getSql()));
        assertParameters(boundSql, "beginTime", "endTime");
    }

    @Test
    public void testBoundSql_annotation() {
        FakeDataSource sqlServer = new FakeDataSource("Microsoft SQL Server", "jdbc:sqlserver://127.0.0.1:1433", 16);
        FakeDataSource h2 = new FakeDataSource("H2", "jdbc:h2:mem:testdb;MODE=MYSQL", 2);
        mockDynamicRoutingDataSource(Map.of("master", sqlServer, "h2", h2));
        // 与 @Select 注解相同：不使用 <script>，仅包含 ${} 时按动态 SQL 处理
        SqlSource sqlSource = new XMLLanguageDriver().createSqlSource(new Configuration(),
                "SELECT " + P + "date('send_time')} AS statDate, COUNT(*) AS count FROM im_private_message"
                        + " WHERE send_time >= #{beginTime} AND send_time < #{endTime}"
                        + " GROUP BY " + P + "date('send_time')}", Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("beginTime", LocalDateTime.of(2026, 1, 1, 0, 0));
        params.put("endTime", LocalDateTime.of(2026, 2, 1, 0, 0));

        BoundSql boundSql = sqlSource.getBoundSql(params);
        assertEquals("SELECT CONVERT(varchar(10), send_time, 23) AS statDate, COUNT(*) AS count FROM im_private_message"
                + " WHERE send_time >= ? AND send_time < ?"
                + " GROUP BY CONVERT(varchar(10), send_time, 23)", normalize(boundSql.getSql()));
        assertParameters(boundSql, "beginTime", "endTime");

        DynamicDataSourceContextHolder.push("h2");
        boundSql = sqlSource.getBoundSql(params);
        assertEquals("SELECT CAST(send_time AS DATE) AS statDate, COUNT(*) AS count FROM im_private_message"
                + " WHERE send_time >= ? AND send_time < ?"
                + " GROUP BY CAST(send_time AS DATE)", normalize(boundSql.getSql()));
        assertParameters(boundSql, "beginTime", "endTime");
    }

    /**
     * MySQL、达梦等原有路径：与 TradeOrderStatisticsMapper 的按月统计、ImStatisticsManagerMapper 的按天统计相同，
     * 不再输出达梦无法解析的单参数 CONCAT 与 DATE 函数；仅校验生成的 SQL，达梦上的实际执行由原生验收覆盖
     */
    @Test
    public void testBoundSql_originalDateFragments() throws Exception {
        FakeDataSource mysql = new FakeDataSource("MySQL", "jdbc:mysql://127.0.0.1:3306/a", 8);
        FakeDataSource dm = new FakeDataSource("DM DBMS", "jdbc:dm://127.0.0.1:5236", 8);
        mockDynamicRoutingDataSource(Map.of("master", mysql, "dm", dm));
        SqlSource monthSource = new XMLLanguageDriver().createSqlSource(new Configuration(),
                "<script>SELECT " + P + "formatYearDashMonth('pay_time')} AS statDate, COUNT(1) AS order_pay_count,"
                        + " SUM(pay_price) AS order_pay_price FROM trade_order"
                        + " WHERE pay_status = #{trueValue} AND pay_time BETWEEN #{beginTime} AND #{endTime}"
                        + " GROUP BY " + P + "formatYearDashMonth('pay_time')} ORDER BY statDate</script>", Map.class);
        SqlSource daySource = new XMLLanguageDriver().createSqlSource(new Configuration(),
                "SELECT " + P + "date('create_time')} AS statDate, COUNT(*) AS count FROM system_users"
                        + " WHERE create_time >= #{beginTime} AND create_time < #{endTime}"
                        + " GROUP BY " + P + "date('create_time')}", Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("trueValue", true);
        params.put("beginTime", LocalDateTime.of(2026, 1, 1, 0, 0));
        params.put("endTime", LocalDateTime.of(2026, 12, 31, 23, 59, 59));

        for (String route : List.of("master", "dm")) {
            DynamicDataSourceContextHolder.push(route);
            BoundSql boundSql = monthSource.getBoundSql(params);
            String sql = normalize(boundSql.getSql());
            assertEquals("SELECT DATE_FORMAT(pay_time, '%Y-%m') AS statDate, COUNT(1) AS order_pay_count,"
                    + " SUM(pay_price) AS order_pay_price FROM trade_order"
                    + " WHERE pay_status = ? AND pay_time BETWEEN ? AND ?"
                    + " GROUP BY DATE_FORMAT(pay_time, '%Y-%m') ORDER BY statDate", sql, route);
            assertParameters(boundSql, "trueValue", "beginTime", "endTime");
            assertNotNull(CCJSqlParserUtil.parse(sql), route + " " + sql);

            boundSql = daySource.getBoundSql(params);
            sql = normalize(boundSql.getSql());
            assertEquals("SELECT CAST(create_time AS DATE) AS statDate, COUNT(*) AS count FROM system_users"
                    + " WHERE create_time >= ? AND create_time < ?"
                    + " GROUP BY CAST(create_time AS DATE)", sql, route);
            assertParameters(boundSql, "beginTime", "endTime");
            assertNotNull(CCJSqlParserUtil.parse(sql), route + " " + sql);
            DynamicDataSourceContextHolder.poll();
        }
        assertEquals(1, mysql.getConnectionCount());
        assertEquals(1, dm.getConnectionCount());
    }

    /**
     * 达梦：普通片段只单独处理逗号分隔列表；神通：单独处理日期字符串、日期值、日部件与逗号分隔列表；
     * SQL Server 2012 以下的普通片段抛出异常。JSON 数组匹配、JSON 字段值的识别结果不随之变化
     */
    @Test
    public void testBoundSql_ordinaryDialectIsolatedFromJson() {
        mockDynamicRoutingDataSource(Map.of(
                "dm", new FakeDataSource("DM DBMS", "jdbc:dm://127.0.0.1:5236", 8),
                "oscar", new FakeDataSource("OSCAR", "jdbc:oscar://127.0.0.1:2003/osrdb", 7),
                "sqlserver2008", new FakeDataSource("Microsoft SQL Server", "jdbc:sqlserver://127.0.0.1:1433", 10)));
        Map<String, Dialect> routes = new LinkedHashMap<>();
        routes.put("dm", Dialect.DM);
        routes.put("oscar", Dialect.OSCAR);
        for (Map.Entry<String, Dialect> route : routes.entrySet()) {
            DynamicDataSourceContextHolder.push(route.getKey());
            try {
                Dialect dialect = route.getValue();
                assertEquals(SqlDialectUtils.findInSetColumn(dialect, "cc.id", "cfur.contact_ids"),
                        SqlDialectUtils.findInSetColumn("cc.id", "cfur.contact_ids"), route.getKey());
                assertNotEquals(SqlDialectUtils.findInSetColumn(Dialect.ORIGINAL, "cc.id", "cfur.contact_ids"),
                        SqlDialectUtils.findInSetColumn("cc.id", "cfur.contact_ids"), route.getKey());
                assertEquals(SqlDialectUtils.formatDate(dialect, "t.create_time"),
                        SqlDialectUtils.formatDate("t.create_time"), route.getKey());
                assertEquals(SqlDialectUtils.castDateText(dialect, "t.create_time"),
                        SqlDialectUtils.castDateText("t.create_time"), route.getKey());
                assertEquals(SqlDialectUtils.dayOfMonth(dialect, "t.create_time"),
                        SqlDialectUtils.dayOfMonth("t.create_time"), route.getKey());
                assertEquals(SqlDialectUtils.hour(Dialect.ORIGINAL, "t.create_time"),
                        SqlDialectUtils.hour("t.create_time"), route.getKey());
                assertEquals(SqlDialectUtils.stripHtmlTags(Dialect.ORIGINAL, "extension.html"),
                        SqlDialectUtils.stripHtmlTags("extension.html"), route.getKey());
                assertEquals(SqlDialectUtils.jsonArrayContains(Dialect.ORIGINAL, "t.label_ids", true),
                        SqlDialectUtils.jsonArrayContains("t.label_ids", true), route.getKey());
            } finally {
                DynamicDataSourceContextHolder.poll();
            }
        }
        // 神通：普通片段、日期运算均不含实库确认不支持的函数，也不用保留时分秒的 CAST AS DATE 按天截断
        Pattern oscarUnsupported = Pattern.compile(
                "\\b(DATE_FORMAT|DAY|DAYOFMONTH|FIND_IN_SET|TIMESTAMPDIFF|TIMESTAMPADD|TRUNCATE)\\(|AS DATE\\)|TIMESTAMP\\(9\\)"
                        + "|CAST\\(\\{1} AS FLOAT8\\)");
        DynamicDataSourceContextHolder.push("oscar");
        try {
            List<String> fragments = List.of(SqlDialectUtils.formatDate("t.create_time"),
                    SqlDialectUtils.formatYearMonth("t.create_time"), SqlDialectUtils.formatYearDashMonth("t.create_time"),
                    SqlDialectUtils.date("t.create_time"), SqlDialectUtils.castDateText("t.create_time"),
                    SqlDialectUtils.castDate("t.create_time"), SqlDialectUtils.dayStart("t.create_time"),
                    SqlDialectUtils.month("t.create_time"), SqlDialectUtils.year("t.create_time"),
                    SqlDialectUtils.dayOfMonth("t.create_time"), SqlDialectUtils.hour("t.create_time"),
                    SqlDialectUtils.customerDealCycle("customer.create_time", "contract.order_date"),
                    SqlDialectUtils.findInSetColumn("cc.id", "cfur.contact_ids"),
                    SqlDialectUtils.stripHtmlTags("extension.html"),
                    SqlDialectUtils.dateDecayScore("t.publish_time"), SqlDialectUtils.setMillisVersion("live_update_time"));
            for (String fragment : fragments) {
                assertFalse(oscarUnsupported.matcher(fragment).find(), fragment);
            }
            // DELETE 不支持 LIMIT：LIMIT 只出现在子查询中；SELECT 的 LIMIT 保持原有
            assertEquals(SqlDialectUtils.deleteBatchByLessThan(Dialect.POSTGRESQL, "infra_job_log", "create_time", "createTime", "limit"),
                    SqlDialectUtils.deleteBatchByLessThan("infra_job_log", "create_time", "createTime", "limit"));
            assertEquals("LIMIT #{limit}", SqlDialectUtils.limitParam("limit", true));
            assertEquals(SqlDialectUtils.dateDecayScore(DbType.OSCAR, "t.publish_time"), SqlDialectUtils.dateDecayScore("t.publish_time"));
            assertEquals(SqlDialectUtils.setMillisVersion(DbType.OSCAR, "live_update_time"),
                    SqlDialectUtils.setMillisVersion("live_update_time"));
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
        // JSON 字段值：仅达梦单独处理，神通仍为原有片段
        DynamicDataSourceContextHolder.push("dm");
        try {
            assertEquals(SqlDialectUtils.jsonText(Dialect.DM, "t.model_values", "amount"),
                    SqlDialectUtils.jsonText("t.model_values", "amount"));
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
        DynamicDataSourceContextHolder.push("oscar");
        try {
            assertEquals(SqlDialectUtils.jsonText(Dialect.ORIGINAL, "t.model_values", "amount"),
                    SqlDialectUtils.jsonText("t.model_values", "amount"));
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
        // SQL Server 2012 以下：普通片段不再输出无法执行的 MySQL 片段，JSON 片段保持原有片段
        DynamicDataSourceContextHolder.push("sqlserver2008");
        try {
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> SqlDialectUtils.formatDate("t.create_time"));
            assertTrue(causeMessages(ex).contains("SQL Server 2012"), causeMessages(ex));
            assertThrows(IllegalStateException.class, () -> SqlDialectUtils.findInSetColumn("cc.id", "cfur.contact_ids"));
            assertEquals(SqlDialectUtils.jsonArrayContains(Dialect.ORIGINAL, "t.label_ids", true),
                    SqlDialectUtils.jsonArrayContains("t.label_ids", true));
            assertEquals(SqlDialectUtils.jsonText(Dialect.ORIGINAL, "t.model_values", "amount"),
                    SqlDialectUtils.jsonText("t.model_values", "amount"));
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    @Test
    public void testBoundSql_deleteBatch() {
        FakeDataSource postgresql = new FakeDataSource("PostgreSQL", "jdbc:postgresql://127.0.0.1:5432/a", 17);
        FakeDataSource sqlServer = new FakeDataSource("Microsoft SQL Server", "jdbc:sqlserver://127.0.0.1:1433", 16);
        FakeDataSource mysql = new FakeDataSource("MySQL", "jdbc:mysql://127.0.0.1:3306/a", 8);
        FakeDataSource oracle = new FakeDataSource("Oracle", "jdbc:oracle:thin:@127.0.0.1:1521:xe", 21);
        mockDynamicRoutingDataSource(Map.of("master", postgresql, "sqlserver", sqlServer, "mysql", mysql, "oracle", oracle));
        // 与 @Delete 注解相同
        SqlSource sqlSource = new XMLLanguageDriver().createSqlSource(new Configuration(),
                P + "deleteBatchByLessThan('infra_job_log', 'create_time', 'createTime', 'limit')}", Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("createTime", LocalDateTime.of(2026, 1, 1, 0, 0));
        params.put("limit", 100);

        // 主库：PostgreSQL
        BoundSql boundSql = sqlSource.getBoundSql(params);
        assertEquals("DELETE FROM infra_job_log WHERE id IN (SELECT id FROM infra_job_log WHERE create_time < ? LIMIT ?)",
                normalize(boundSql.getSql()));
        assertParameters(boundSql, "createTime", "limit");

        DynamicDataSourceContextHolder.push("sqlserver");
        boundSql = sqlSource.getBoundSql(params);
        assertEquals("DELETE FROM infra_job_log WHERE id IN (SELECT TOP (?) id FROM infra_job_log WHERE create_time < ?)",
                normalize(boundSql.getSql()));
        assertParameters(boundSql, "limit", "createTime");

        DynamicDataSourceContextHolder.push("oracle");
        boundSql = sqlSource.getBoundSql(params);
        assertEquals("DELETE FROM infra_job_log WHERE create_time < ? AND ROWNUM <= ?", normalize(boundSql.getSql()));
        assertParameters(boundSql, "createTime", "limit");

        DynamicDataSourceContextHolder.push("mysql");
        boundSql = sqlSource.getBoundSql(params);
        assertEquals("DELETE FROM infra_job_log WHERE create_time < ? LIMIT ?", normalize(boundSql.getSql()));
        assertParameters(boundSql, "createTime", "limit");
    }

    @Test
    public void testBoundSql_limit() {
        FakeDataSource mysql = new FakeDataSource("MySQL", "jdbc:mysql://127.0.0.1:3306/a", 8);
        FakeDataSource oracle = new FakeDataSource("Oracle", "jdbc:oracle:thin:@127.0.0.1:1521:xe", 21);
        FakeDataSource sqlServer = new FakeDataSource("Microsoft SQL Server", "jdbc:sqlserver://127.0.0.1:1433", 16);
        mockDynamicRoutingDataSource(Map.of("master", mysql, "oracle", oracle, "sqlserver", sqlServer));
        // 与 @Select 注解相同：已有排序时使用参数绑定的行数，无排序时使用固定行数
        SqlSource orderedSource = new XMLLanguageDriver().createSqlSource(new Configuration(),
                "SELECT user_id, COUNT(*) AS messageCount FROM im_private_message WHERE send_time >= #{beginTime}"
                        + " GROUP BY user_id ORDER BY messageCount DESC " + P + "limitParam('limit', true)}", Map.class);
        SqlSource unorderedSource = new XMLLanguageDriver().createSqlSource(new Configuration(),
                "SELECT * FROM hrm_salary_option WHERE code = #{code} " + P + "limitRows(1, false)}", Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("beginTime", LocalDateTime.of(2026, 1, 1, 0, 0));
        params.put("limit", 10);
        params.put("code", 1);
        String ordered = "SELECT user_id, COUNT(*) AS messageCount FROM im_private_message WHERE send_time >= ?"
                + " GROUP BY user_id ORDER BY messageCount DESC ";

        // 主库：MySQL，保持原有 LIMIT
        BoundSql boundSql = orderedSource.getBoundSql(params);
        assertEquals(ordered + "LIMIT ?", normalize(boundSql.getSql()));
        assertParameters(boundSql, "beginTime", "limit");
        assertEquals("SELECT * FROM hrm_salary_option WHERE code = ? LIMIT 1",
                normalize(unorderedSource.getBoundSql(params).getSql()));

        DynamicDataSourceContextHolder.push("oracle");
        boundSql = orderedSource.getBoundSql(params);
        assertEquals(ordered + "FETCH FIRST ? ROWS ONLY", normalize(boundSql.getSql()));
        assertParameters(boundSql, "beginTime", "limit");
        assertEquals("SELECT * FROM hrm_salary_option WHERE code = ? FETCH FIRST 1 ROWS ONLY",
                normalize(unorderedSource.getBoundSql(params).getSql()));

        DynamicDataSourceContextHolder.push("sqlserver");
        boundSql = orderedSource.getBoundSql(params);
        assertEquals(ordered + "OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY", normalize(boundSql.getSql()));
        assertParameters(boundSql, "beginTime", "limit");
        boundSql = unorderedSource.getBoundSql(params);
        assertEquals("SELECT * FROM hrm_salary_option WHERE code = ? ORDER BY (SELECT NULL) OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY",
                normalize(boundSql.getSql()));
        assertParameters(boundSql, "code");
    }

    @Test
    public void testBoundSql_unsupportedProduct() {
        FakeDataSource sqlite = new FakeDataSource("SQLite", "jdbc:sqlite:/tmp/test.db", 3);
        mockDynamicRoutingDataSource(Map.of("master", sqlite));
        SqlSource sqlSource = new XMLLanguageDriver().createSqlSource(new Configuration(),
                "SELECT " + P + "month('order_date')} AS month FROM crm_contract"
                        + " WHERE " + P + "year('order_date')} = #{year}", Map.class);

        // OGNL 会包装异常，因此按异常链的信息断言
        Exception ex = assertThrows(Exception.class, () -> sqlSource.getBoundSql(Map.of("year", 2026)));
        assertTrue(causeMessages(ex).contains("不支持的数据库产品：SQLite"), causeMessages(ex));
    }

    // ========== JSON 字段值 ==========

    @Test
    public void testJsonValue_original() {
        String x = "JSON_EXTRACT(t.model_values, '$.amount')";
        String u = "JSON_UNQUOTE(" + x + ")";
        String number = "CASE WHEN " + u + " REGEXP '" + SqlDialectUtils.JSON_NUMBER_PATTERN + "' AND CHAR_LENGTH(" + u
                + ") <= 64 THEN CAST(" + u + " AS DECIMAL(30, 10)) END";
        for (Dialect dialect : List.of(Dialect.ORIGINAL, Dialect.H2)) {
            assertEquals("CASE WHEN JSON_TYPE(" + x + ") NOT IN ('NULL', 'ARRAY', 'OBJECT') THEN " + u + " END",
                    SqlDialectUtils.jsonText(dialect, "t.model_values", "amount"));
            assertEquals(number, SqlDialectUtils.jsonNumber(dialect, "t.model_values", "amount"));
            assertEquals("CASE WHEN " + u + " REGEXP '" + SqlDialectUtils.JSON_DATE_TIME_PATTERN + "' THEN CAST(" + u
                    + " AS DATETIME(6)) END", SqlDialectUtils.jsonDateTime(dialect, "t.model_values", "amount"));
            // 数字排序：先按空值分组，再按数值排序，与其它方言一致；非法值（JSON null 等）为 NULL，不被 CAST 转为 0，与空值组一致；
            // 排序表达式中正则的 ? 量词为 {0,1}，见 testJsonValue_orderByCountSql
            assertEquals(List.of(orderBy("CASE WHEN " + number + " IS NULL THEN 0 ELSE 1 END"), orderBy(number)),
                    SqlDialectUtils.jsonOrderBy(dialect, "t.model_values", "amount", BigDecimal.class));
            assertEquals(x + " = CAST({0} AS JSON)", SqlDialectUtils.jsonBooleanEquals(dialect, "t.model_values", "amount"));
            // IN：存储值为数组时不匹配（原有 JSON_CONTAINS 的子集规则会使 ["a"] 命中 IN ('a')）
            // 参数 {0} 为完整的 JSON 数组，按 JSON 类型比较，各值类型的片段相同；直接使用绑定的 JSON 文本，
            // 不 CAST(... AS JSON)（MariaDB 不支持），与达梦相同
            String notArray = "JSON_TYPE(" + x + ") <> 'ARRAY'";
            for (Class<?> valueType : List.of(String.class, BigDecimal.class, Boolean.class)) {
                String in = SqlDialectUtils.jsonIn(dialect, "t.model_values", "amount", valueType);
                assertEquals("(" + notArray + " AND JSON_CONTAINS({0}, " + x + "))", in, valueType.getName());
                assertFalse(in.contains("AS JSON"), in);
                // 数组任一匹配：JSON_OVERLAPS 只比较直接元素，JSON_CONTAINS 会递归匹配嵌套数组（[["java"]] 被 "java" 命中）
                assertEquals("JSON_OVERLAPS(" + x + ", {0})",
                        SqlDialectUtils.jsonArrayOverlaps(dialect, "t.model_values", "amount", valueType), valueType.getName());
            }
            // 问卷：题目编号以数字开头，路径需加引号；同样只比较直接元素，编号按数字与字符串两种形式
            String answer = "JSON_EXTRACT(answer_json, '$.\"11\"')";
            assertEquals("JSON_OVERLAPS(" + answer + ", JSON_ARRAY(CAST({0} AS SIGNED), CAST(CAST({0} AS SIGNED) AS CHAR)))",
                    SqlDialectUtils.jsonLongContains(dialect, "answer_json", "11"));
            for (String fragment : List.of(SqlDialectUtils.jsonArrayOverlaps(dialect, "t.model_values", "amount", String.class),
                    SqlDialectUtils.jsonLongContains(dialect, "answer_json", "11"))) {
                assertFalse(fragment.contains("JSON_CONTAINS"), fragment);
            }
            assertEquals("(JSON_TYPE(" + answer + ") = 'STRING' AND JSON_UNQUOTE(" + answer + ") REGEXP '[^[:space:]]')",
                    SqlDialectUtils.jsonTextNotBlank(dialect, "answer_json", "11"));
        }
    }

    /**
     * 达梦：没有中缀 REGEXP，JSON_UNQUOTE 的结果不能转为 DECIMAL，Long 参数不能 CAST(... AS JSON)；
     * 文本、布尔、IN 与原有片段相同
     */
    @Test
    public void testJsonValue_dm() {
        String x = "JSON_EXTRACT(t.model_values, '$.amount')";
        String u = "JSON_UNQUOTE(" + x + ")";
        String v = "JSON_VALUE(t.model_values, '$.amount')";
        String text = "CASE WHEN JSON_TYPE(" + x + ") NOT IN ('NULL', 'ARRAY', 'OBJECT') THEN " + u + " END";
        assertEquals(text, SqlDialectUtils.jsonText(Dialect.DM, "t.model_values", "amount"));
        // 格式校验在 CAST 的输入中：非法值先成为 NULL，达梦提前转换也不会报错
        // 先转为 DECIMAL(38, 10)，按 1E+20 的范围校验后再转为 DECIMAL(30, 10)：99999999999999999999.99999999995 舍入进位后超出范围，
        // 为 NULL 而不是溢出报错；两次校验都在 CAST 的输入中
        String decimal = "CAST(CASE WHEN REGEXP_LIKE(" + v + ", '" + SqlDialectUtils.DM_NUMBER_PATTERN + "') AND LENGTH(" + v
                + ") <= 64 THEN " + v + " END AS DECIMAL(38, 10))";
        String number = "CAST(CASE WHEN " + decimal + " > -100000000000000000000 AND " + decimal
                + " < 100000000000000000000 THEN " + decimal + " END AS DECIMAL(30, 10))";
        assertEquals(number, SqlDialectUtils.jsonNumber(Dialect.DM, "t.model_values", "amount"));
        String dateTime = "CAST(CASE WHEN REGEXP_LIKE(" + v + ", '" + SqlDialectUtils.JSON_DATE_TIME_PATTERN + "')"
                + " THEN SUBSTR(REPLACE(" + v + ", 'T', ' '), 1, 26) END AS DATETIME(6))";
        assertEquals(dateTime, SqlDialectUtils.jsonDateTime(Dialect.DM, "t.model_values", "amount"));
        // 数字排序不复用原有的 CAST(JSON_UNQUOTE(...) AS DECIMAL)
        assertEquals(List.of(orderBy("CASE WHEN " + number + " IS NULL THEN 0 ELSE 1 END"), orderBy(number)),
                SqlDialectUtils.jsonOrderBy(Dialect.DM, "t.model_values", "amount", BigDecimal.class));
        assertEquals(List.of(orderBy("CASE WHEN " + dateTime + " IS NULL THEN 0 ELSE 1 END"), orderBy(dateTime)),
                SqlDialectUtils.jsonOrderBy(Dialect.DM, "t.model_values", "amount", LocalDateTime.class));
        assertEquals(List.of("CASE WHEN " + text + " IS NULL THEN 0 ELSE 1 END", text),
                SqlDialectUtils.jsonOrderBy(Dialect.DM, "t.model_values", "amount", String.class));
        assertEquals(x + " = CAST({0} AS JSON)", SqlDialectUtils.jsonBooleanEquals(Dialect.DM, "t.model_values", "amount"));
        // IN 与数组任一匹配：直接使用绑定的 JSON 数组文本，不 CAST(... AS JSON)，按 JSON 类型比较
        for (Class<?> valueType : List.of(String.class, BigDecimal.class, Boolean.class)) {
            assertEquals("(JSON_TYPE(" + x + ") <> 'ARRAY' AND JSON_CONTAINS({0}, " + x + "))",
                    SqlDialectUtils.jsonIn(Dialect.DM, "t.model_values", "amount", valueType), valueType.getName());
            assertEquals("JSON_OVERLAPS(" + x + ", {0})",
                    SqlDialectUtils.jsonArrayOverlaps(Dialect.DM, "t.model_values", "amount", valueType), valueType.getName());
        }
        // 问卷：编号按数字与字符串两种形式匹配
        String answer = "JSON_EXTRACT(answer_json, '$.\"11\"')";
        assertEquals("JSON_OVERLAPS(" + answer + ", JSON_ARRAY(CAST({0} AS BIGINT), CAST(CAST({0} AS BIGINT) AS VARCHAR(20))))",
                SqlDialectUtils.jsonLongContains(Dialect.DM, "answer_json", "11"));
        // 问答：JSON_UNQUOTE 的 CLOB 不截断长文本（JSON_VALUE 会截断）；空白字符列举 Unicode White_Space 的 25 个字符，
        // 达梦的 [:space:] 实库漏 19 个字符
        assertEquals("(JSON_TYPE(" + answer + ") = 'STRING' AND REGEXP_LIKE(JSON_UNQUOTE(" + answer + "), '[^"
                        + whiteSpaces() + "]'))",
                SqlDialectUtils.jsonTextNotBlank(Dialect.DM, "answer_json", "11"));
    }

    @Test
    public void testJsonValue_postgreSQL() {
        String n = "jsonb_extract_path(CAST(t.model_values AS jsonb), 'amount')";
        String t = "jsonb_extract_path_text(CAST(t.model_values AS jsonb), 'amount')";
        String text = "CASE WHEN jsonb_typeof(" + n + ") IN ('string', 'number', 'boolean') THEN " + t + " END";
        assertEquals(text, SqlDialectUtils.jsonText(Dialect.POSTGRESQL, "t.model_values", "amount"));
        String number = "CASE WHEN " + t + " ~ '" + SqlDialectUtils.JSON_NUMBER_PATTERN + "' AND LENGTH(" + t
                + ") <= 64 THEN CAST(" + t + " AS NUMERIC) END";
        assertEquals(number, SqlDialectUtils.jsonNumber(Dialect.POSTGRESQL, "t.model_values", "amount"));
        assertEquals("CASE WHEN " + t + " ~ '" + SqlDialectUtils.JSON_DATE_TIME_PATTERN + "' THEN CAST(" + t
                + " AS TIMESTAMP) END", SqlDialectUtils.jsonDateTime(Dialect.POSTGRESQL, "t.model_values", "amount"));
        // PostgreSQL 默认 NULL 排在最后，空值分组使其与 MySQL 一致；排序表达式中正则的 ? 量词为 {0,1}
        assertEquals(List.of(orderBy("CASE WHEN " + number + " IS NULL THEN 0 ELSE 1 END"), orderBy(number)),
                SqlDialectUtils.jsonOrderBy(Dialect.POSTGRESQL, "t.model_values", "amount", BigDecimal.class));
        assertEquals(List.of("CASE WHEN " + text + " IS NULL THEN 0 ELSE 1 END", text),
                SqlDialectUtils.jsonOrderBy(Dialect.POSTGRESQL, "t.model_values", "amount", String.class));
        assertEquals(n + " = CAST({0} AS jsonb)", SqlDialectUtils.jsonBooleanEquals(Dialect.POSTGRESQL, "t.model_values", "amount"));
        // IN 与数组任一匹配：jsonb_array_elements 展开 {0} 绑定的 JSON 数组，按 jsonb 相等比较（类型不同不相等）；
        // 数组任一匹配时存储值为数组则展开一层，否则包装为单元素数组
        String param = "jsonb_array_elements(CAST({0} AS jsonb)) yd_jc";
        for (Class<?> valueType : List.of(String.class, BigDecimal.class, Boolean.class)) {
            assertEquals("EXISTS (SELECT 1 FROM " + param + " WHERE yd_jc.\"value\" = " + n + ")",
                    SqlDialectUtils.jsonIn(Dialect.POSTGRESQL, "t.model_values", "amount", valueType), valueType.getName());
            assertEquals("EXISTS (SELECT 1 FROM jsonb_array_elements(CASE WHEN jsonb_typeof(" + n + ") = 'array' THEN " + n
                            + " ELSE jsonb_build_array(" + n + ") END) yd_je, " + param + " WHERE yd_je.\"value\" = yd_jc.\"value\")",
                    SqlDialectUtils.jsonArrayOverlaps(Dialect.POSTGRESQL, "t.model_values", "amount", valueType), valueType.getName());
        }
        String answer = "jsonb_extract_path(CAST(answer_json AS jsonb), '11')";
        assertEquals("(" + answer + " @> to_jsonb(CAST({0} AS BIGINT)) OR " + answer
                        + " @> to_jsonb(CAST(CAST({0} AS BIGINT) AS VARCHAR)))",
                SqlDialectUtils.jsonLongContains(Dialect.POSTGRESQL, "answer_json", "11"));
        // 问答：列举 Unicode White_Space 的 25 个字符，[:space:] 取决于 locale，实库漏 U+0085、U+00A0、U+2007、U+202F
        assertEquals("(jsonb_typeof(" + answer + ") = 'string' AND jsonb_extract_path_text(CAST(answer_json AS jsonb), '11')"
                + " ~ '[^" + whiteSpaces() + "]')", SqlDialectUtils.jsonTextNotBlank(Dialect.POSTGRESQL, "answer_json", "11"));
    }

    @Test
    public void testJsonValue_oracle() throws Exception {
        String v = "JSON_VALUE(t.model_values, '$.\"amount\"')";
        String type = "JSON_VALUE(t.model_values, '$.\"amount\".type()')";
        // 文本：RETURNING CLOB，超过 4000 字节的文本不截断；排序不能使用 CLOB，分组与值均为 VARCHAR2(32767)，二者一致
        String clob = "JSON_VALUE(t.model_values, '$.\"amount\"' RETURNING CLOB)";
        assertEquals(clob, SqlDialectUtils.jsonText(Dialect.ORACLE, "t.model_values", "amount"));
        String sortText = "JSON_VALUE(t.model_values, '$.\"amount\"' RETURNING VARCHAR2(32767))";
        assertEquals(List.of("CASE WHEN " + sortText + " IS NULL THEN 0 ELSE 1 END", sortText),
                SqlDialectUtils.jsonOrderBy(Dialect.ORACLE, "t.model_values", "amount", String.class));
        String number = "CASE WHEN REGEXP_LIKE(" + v + ", '" + SqlDialectUtils.JSON_NUMBER_PATTERN + "') AND LENGTH(" + v
                + ") <= 64 THEN TO_NUMBER(REPLACE(" + v + ", '.', SUBSTR(TO_CHAR(0.5, 'FM0D0'), 2, 1))) END";
        assertEquals(number, SqlDialectUtils.jsonNumber(Dialect.ORACLE, "t.model_values", "amount"));
        String dateTime = "REPLACE(" + v + ", 'T', ' ')";
        assertEquals("CASE WHEN REGEXP_LIKE(" + v + ", '" + SqlDialectUtils.JSON_DATE_TIME_PATTERN + "') THEN CASE LENGTH(" + v + ")"
                        + " WHEN 10 THEN TO_TIMESTAMP(" + v + ", 'YYYY-MM-DD')"
                        + " WHEN 16 THEN TO_TIMESTAMP(" + dateTime + ", 'YYYY-MM-DD HH24:MI')"
                        + " WHEN 19 THEN TO_TIMESTAMP(" + dateTime + ", 'YYYY-MM-DD HH24:MI:SS')"
                        + " ELSE TO_TIMESTAMP(" + dateTime + ", 'YYYY-MM-DD HH24:MI:SS.FF') END END",
                SqlDialectUtils.jsonDateTime(Dialect.ORACLE, "t.model_values", "amount"));
        // 按 JSON 类型比较：字符串 "true"、"12" 不与布尔 true、数字 12 匹配
        assertEquals("(" + type + " = 'boolean' AND " + v + " = {0})",
                SqlDialectUtils.jsonBooleanEquals(Dialect.ORACLE, "t.model_values", "amount"));
        // IN 与数组任一匹配：JSON_TABLE 展开 {0} 绑定的 JSON 数组参数，参数按 CLOB 绑定（超过 32767 字符不报 ORA-01460），只绑定一次；
        // 数字按原文（FORMAT JSON WITH WRAPPER）的规范键比较，不经过 NUMBER 的有效数字转换（42 位的相邻整数不相等）；
        // 规范键为私有实现，这里只校验结构，语义由实库回归验证
        String param = "JSON_TABLE({0,jdbcType=CLOB,typeHandler=" + OracleClobTypeHandler.class.getName() + "}, '$[*]'";
        String stored = "JSON_TABLE(t.model_values, '$.\"amount\"[*]'";
        String raw = "FORMAT JSON WITH WRAPPER PATH '$'";
        Pattern numberColumn = Pattern.compile("yd_j[a-z]\\.n\\b");
        Map<Class<?>, String> oracleTypes = Map.of(String.class, "string", BigDecimal.class, "number", Boolean.class, "boolean");
        for (Map.Entry<Class<?>, String> oracleType : oracleTypes.entrySet()) {
            String typeName = oracleType.getValue();
            String in = SqlDialectUtils.jsonIn(Dialect.ORACLE, "t.model_values", "amount", oracleType.getKey());
            String overlaps = SqlDialectUtils.jsonArrayOverlaps(Dialect.ORACLE, "t.model_values", "amount", oracleType.getKey());
            // 按 JSON 类型比较：IN 的存储值只匹配标量，不展开数组；数组任一匹配展开一层，标量作为单个元素
            assertTrue(in.contains(type + " = '" + typeName + "'") && in.contains("yd_jc.t = '" + typeName + "'"), in);
            assertFalse(in.contains(stored), in);
            assertTrue(overlaps.startsWith("EXISTS (SELECT 1 FROM " + stored), overlaps);
            assertTrue(overlaps.contains("yd_jt.t = '" + typeName + "' AND yd_jc.t = '" + typeName + "'"), overlaps);
            for (String fragment : List.of(in, overlaps)) {
                assertEquals(1, fragment.split(Pattern.quote(param), -1).length - 1, fragment);
                assertEquals(List.of("{0}"), distinctPlaceholders(fragment.replaceAll("\\{0,[^}]+}", "{0}")), fragment);
                assertTrue(fragment.contains(raw), fragment);
                assertFalse(numberColumn.matcher(fragment).find(), fragment);
                assertNotNull(CCJSqlParserUtil.parse("SELECT id FROM cms_article t WHERE "
                        + fragment.replaceAll("\\{0(,[^}]+)?}", "?")), fragment);
            }
            if (oracleType.getKey() == BigDecimal.class) {
                // 数字：参数元素与存储值均取原文的规范键；IN 的存储值为 JSON_QUERY 包装后的原文，不复用 jsonNumber
                assertTrue(in.contains("yd_jc.r") && in.contains("JSON_QUERY(t.model_values, '$.\"amount\"'")
                        && in.contains("WITH WRAPPER)"), in);
                assertFalse(in.contains(number), in);
                assertTrue(overlaps.contains("yd_jt.r") && overlaps.contains("yd_jc.r"), overlaps);
            } else {
                // 字符串、布尔：仍按文本比较
                assertTrue(in.contains("yd_jc.v = " + v), in);
                assertTrue(overlaps.contains("yd_jt.v = yd_jc.v"), overlaps);
                assertFalse(in.contains("yd_jc.r") || overlaps.contains("yd_jt.r"), in + " " + overlaps);
            }
        }
        // 问卷：存储值按 JSON_TABLE 展开一层；编号只绑定一次，转为十进制文本的候选 yd_ji，参数为 NULL 时不匹配；
        // 数字按原文的规范键比较（不经过 NUMBER），字符串与十进制文本精确比较
        String oracleLongContains = SqlDialectUtils.jsonLongContains(Dialect.ORACLE, "answer_json", "11");
        assertTrue(oracleLongContains.startsWith("EXISTS (SELECT 1 FROM JSON_TABLE(answer_json, '$.\"11\"[*]'"), oracleLongContains);
        assertTrue(oracleLongContains.contains(raw), oracleLongContains);
        assertEquals(1, oracleLongContains.split("\\{0}", -1).length - 1, oracleLongContains);
        assertTrue(oracleLongContains.contains("CAST({0} AS NUMBER(19, 0))") && oracleLongContains.contains(") yd_ji"),
                oracleLongContains);
        assertTrue(oracleLongContains.contains("yd_ji.v IS NOT NULL"), oracleLongContains);
        assertTrue(oracleLongContains.contains("yd_jt.t = 'number'") && oracleLongContains.contains("yd_jt.r"), oracleLongContains);
        assertTrue(oracleLongContains.contains("yd_jt.t = 'string' AND yd_jt.v = yd_ji.v"), oracleLongContains);
        assertFalse(numberColumn.matcher(oracleLongContains).find(), oracleLongContains);
        // 真实 Wrapper：只绑定一个 Long 参数，生成的 SQL 可解析
        QueryWrapper<Object> oracleLongWrapper = new QueryWrapper<>();
        oracleLongWrapper.apply(oracleLongContains, 11L);
        BoundSql oracleLongSql = new XMLLanguageDriver().createSqlSource(new Configuration(),
                        "<script>SELECT id FROM cms_survey_record ${ew.customSqlSegment}</script>", Map.class)
                .getBoundSql(Map.of("ew", oracleLongWrapper));
        assertEquals(List.of(11L), getParameterValues(oracleLongSql, oracleLongWrapper));
        assertNotNull(CCJSqlParserUtil.parse(normalize(oracleLongSql.getSql())), oracleLongSql.getSql());
        // 问答：type() 限定为字符串，数字 123、数组 ["回答"] 不命中；空白字符为 25 个字符的 JSON 转义，SQL 文本只含 ASCII
        String oracleNotBlank = SqlDialectUtils.jsonTextNotBlank(Dialect.ORACLE, "answer_json", "11");
        assertEquals("(JSON_VALUE(answer_json, '$.\"11\".type()') = 'string'"
                        + " AND JSON_EXISTS(answer_json, '$.\"11\"?(@ like_regex \"[^" + oracleWhiteSpaces() + "]\")'))",
                oracleNotBlank);
        assertTrue(oracleNotBlank.chars().allMatch(c -> c >= 0x20 && c < 0x7F), oracleNotBlank);
        assertFalse(oracleNotBlank.contains("[:space:]"), oracleNotBlank);
    }

    @Test
    public void testJsonValue_sqlServer() throws Exception {
        String member = " FROM OPENJSON(CAST(t.model_values AS nvarchar(max))) yd_jo WHERE yd_jo.\"key\" = N'amount'";
        String sv = "JSON_VALUE(CAST(t.model_values AS nvarchar(max)), '$.\"amount\"')";
        assertEquals("(SELECT yd_jo.\"value\"" + member + " AND yd_jo.\"type\" IN (1, 2, 3))",
                SqlDialectUtils.jsonText(Dialect.SQLSERVER, "t.model_values", "amount"));
        // 数字：科学计数法按文本精确展开后 TRY_CAST，不经过 FLOAT
        String number = SqlDialectUtils.jsonNumber(Dialect.SQLSERVER, "t.model_values", "amount");
        // 前缀包含 -.5 这类负的纯小数（共享正则合法）
        assertEquals("CASE WHEN LEN(" + sv + ") <= 64 AND (" + sv + " LIKE N'[0-9]%' OR " + sv + " LIKE N'-[0-9]%' OR "
                        + sv + " LIKE N'.[0-9]%' OR " + sv + " LIKE N'-.[0-9]%') AND SUBSTRING(" + sv
                        + ", 2, 64) NOT LIKE N'%[^0-9.Ee+-]%' THEN " + sqlServerDecimal(sv) + " END", number);
        assertFalse(number.contains("FLOAT"), number);
        String date = "[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]";
        String second = date + "[ T][0-9][0-9]:[0-9][0-9]:[0-9][0-9]";
        assertEquals("CASE WHEN " + sv + " LIKE N'" + date + "' OR " + sv + " LIKE N'" + date + "[ T][0-9][0-9]:[0-9][0-9]'"
                        + " OR " + sv + " LIKE N'" + second + "' OR (" + sv + " LIKE N'" + second + ".[0-9]%' AND LEN(" + sv
                        + ") <= 29 AND SUBSTRING(" + sv + ", 21, 9) NOT LIKE N'%[^0-9]%')"
                        + " THEN TRY_CAST(LEFT(REPLACE(" + sv + ", N'T', N' '), 27) AS datetime2(7)) END",
                SqlDialectUtils.jsonDateTime(Dialect.SQLSERVER, "t.model_values", "amount"));
        assertEquals("EXISTS (SELECT 1" + member + " AND yd_jo.\"type\" = 3 AND yd_jo.\"value\" = {0})",
                SqlDialectUtils.jsonBooleanEquals(Dialect.SQLSERVER, "t.model_values", "amount"));
        // IN 与数组任一匹配：{0} 为完整的 JSON 数组，只在外层 OPENJSON 一次，按 type 与值比较
        // 数组任一匹配：只有数组才再次 OPENJSON，标量替换为空数组，避免优化器提前展开标量时报 JSON 格式错误
        String elements = "OPENJSON(CASE WHEN yd_jo.\"type\" = 4 THEN yd_jo.\"value\" ELSE N'[]' END) yd_je";
        String param = "OPENJSON(CAST({0} AS nvarchar(max))) yd_jc";
        Map<Class<?>, Integer> jsonTypes = Map.of(String.class, 1, BigDecimal.class, 2, Boolean.class, 3);
        for (Map.Entry<Class<?>, Integer> jsonType : jsonTypes.entrySet()) {
            String in = SqlDialectUtils.jsonIn(Dialect.SQLSERVER, "t.model_values", "amount", jsonType.getKey());
            String overlaps = SqlDialectUtils.jsonArrayOverlaps(Dialect.SQLSERVER, "t.model_values", "amount", jsonType.getKey());
            assertFalse(in.contains(elements), in);
            assertTrue(overlaps.contains(elements), overlaps);
            for (String fragment : List.of(in, overlaps)) {
                assertTrue(fragment.startsWith("EXISTS (SELECT 1" + member + " AND "), fragment);
                assertTrue(fragment.contains(param) && fragment.indexOf(param) == fragment.lastIndexOf(param), fragment);
                assertEquals(List.of("{0}"), distinctPlaceholders(fragment), fragment);
                assertTrue(fragment.contains("yd_jo.\"type\" = " + jsonType.getValue() + " AND yd_jc.\"type\" = "
                        + jsonType.getValue()), fragment);
                // 数字按规范键比较，不经过固定精度的 DECIMAL 与 FLOAT
                assertFalse(fragment.contains("DECIMAL") || fragment.contains("FLOAT"), fragment);
                assertNotNull(CCJSqlParserUtil.parse("SELECT id FROM cms_article t WHERE " + bindIndexParams(fragment)), fragment);
            }
        }
        // 字符串按原文精确相等：两侧先追加 N'.' 再按 BIN2 比较，区分大小写，尾部空格参与比较
        String exact = "(yd_jo.\"value\" + N'.') COLLATE Latin1_General_100_BIN2 = (yd_jc.\"value\" + N'.') COLLATE Latin1_General_100_BIN2";
        assertTrue(SqlDialectUtils.jsonIn(Dialect.SQLSERVER, "t.model_values", "amount", String.class).contains(exact));
        String stringOverlaps = SqlDialectUtils.jsonArrayOverlaps(Dialect.SQLSERVER, "t.model_values", "amount", String.class);
        assertTrue(stringOverlaps.contains(exact), stringOverlaps);
        assertTrue(stringOverlaps.contains(exact.replace("yd_jo.", "yd_je.")), stringOverlaps);
        String answer = " FROM OPENJSON(CAST(answer_json AS nvarchar(max))) yd_jo WHERE yd_jo.\"key\" = N'11'";
        // 问卷：数字编号按规范键比较，11.0、1.1e1 与 11 相等，11.00000000001 不相等；字符串编号按原文精确相等，
        // "11.0"、"123 "、全角数字不当作编号；语义由实库回归验证，这里只校验结构，不复制完整 SQL
        String longContains = SqlDialectUtils.jsonLongContains(Dialect.SQLSERVER, "answer_json", "11");
        assertTrue(longContains.startsWith("EXISTS ("), longContains);
        assertTrue(longContains.contains("FROM OPENJSON(CAST(answer_json AS nvarchar(max))) yd_jo"), longContains);
        assertTrue(longContains.contains("yd_jo.\"key\" = N'11'"), longContains);
        // {0} 只出现一次：在外层候选行按 BIGINT 转为十进制文本，标量与数组元素共用
        assertEquals(1, longContains.split("\\{0}", -1).length - 1, longContains);
        assertTrue(longContains.contains("CAST({0} AS BIGINT)"), longContains);
        // 只展开一层数组：外层成员与数组元素各一次 OPENJSON
        assertTrue(longContains.contains(elements), longContains);
        assertEquals(2, longContains.split("OPENJSON\\(", -1).length - 1, longContains);
        // 字符串（type 1）与数字（type 2）分开比较，标量与数组元素均如此
        assertFalse(longContains.contains("IN (1, 2)"), longContains);
        for (String alias : List.of("yd_jo", "yd_je")) {
            for (int type : new int[]{1, 2}) {
                assertTrue(longContains.contains(alias + ".\"type\" = " + type), alias + " " + type + " " + longContains);
            }
        }
        assertTrue(longContains.contains("COLLATE Latin1_General_100_BIN2") && longContains.contains(" + N'.'"), longContains);
        // 数字按规范键（CROSS APPLY 逐级计算）比较，不经过固定精度的 DECIMAL 与 FLOAT
        Pattern numberKey = Pattern.compile("\\(SELECT CASE WHEN LEN\\(yd_nk5\\.r\\).*?yd_nk6\\)");
        assertTrue(numberKey.matcher(longContains).find(), longContains);
        assertTrue(longContains.contains("CROSS APPLY"), longContains);
        assertFalse(longContains.contains("DECIMAL") || longContains.contains("FLOAT"), longContains);
        // 候选显式非 NULL 保护：去掉规范键自身的 IS NOT NULL 后仍存在
        assertTrue(numberKey.matcher(longContains).replaceAll("").contains("IS NOT NULL"), longContains);
        assertNotNull(CCJSqlParserUtil.parse("SELECT id FROM cms_survey_record WHERE " + bindIndexParams(longContains)),
                longContains);
        // 真实 Wrapper：只绑定一个 Long 参数，生成的 SQL 可解析
        QueryWrapper<Object> longWrapper = new QueryWrapper<>();
        longWrapper.apply(longContains, 11L);
        BoundSql longSql = new XMLLanguageDriver().createSqlSource(new Configuration(),
                        "<script>SELECT id FROM cms_survey_record ${ew.customSqlSegment}</script>", Map.class)
                .getBoundSql(Map.of("ew", longWrapper));
        assertEquals(List.of(11L), getParameterValues(longSql, longWrapper));
        assertNotNull(CCJSqlParserUtil.parse(normalize(longSql.getSql())), longSql.getSql());
        String notBlank = SqlDialectUtils.jsonTextNotBlank(Dialect.SQLSERVER, "answer_json", "11");
        String prefix = "EXISTS (SELECT 1" + answer + " AND yd_jo.\"type\" = 1"
                + " AND yd_jo.\"value\" COLLATE Latin1_General_100_BIN2 LIKE N'%[^";
        assertTrue(notBlank.startsWith(prefix), notBlank);
        assertTrue(notBlank.endsWith("]%')"), notBlank);
        // 字符类直接写在字面量中，为 Unicode White_Space 的全部 25 个字符（含空格）；不拼接 CHAR(n)、NCHAR(n)，
        // JSqlParser 5.4 会将其解析为数据类型的隐式转换
        int[] whiteSpaces = notBlank.substring(prefix.length(), notBlank.length() - "]%')".length()).codePoints().toArray();
        assertArrayEquals(new int[]{0x20, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x85, 0xA0, 0x1680,
                0x2000, 0x2001, 0x2002, 0x2003, 0x2004, 0x2005, 0x2006, 0x2007, 0x2008, 0x2009, 0x200A,
                0x2028, 0x2029, 0x202F, 0x205F, 0x3000}, whiteSpaces, notBlank);
        Pattern whiteSpace = Pattern.compile("\\p{IsWhite_Space}");
        List<Integer> expectedWhiteSpaces = new ArrayList<>();
        for (int codePoint = 0; codePoint <= 0xFFFF; codePoint++) {
            if (whiteSpace.matcher(String.valueOf((char) codePoint)).matches()) {
                expectedWhiteSpaces.add(codePoint);
            }
        }
        assertEquals(expectedWhiteSpaces, Arrays.stream(whiteSpaces).sorted().boxed().toList());
        assertFalse(notBlank.contains("CHAR("), notBlank);
        // 原样（不压缩空白）可被 JSqlParser 解析
        assertNotNull(CCJSqlParserUtil.parse("SELECT id FROM cms_survey_record WHERE " + notBlank), notBlank);
    }

    @Test
    public void testJsonValue_invalidArguments() {
        // 键：模型字段编码（最长 64 位）或 Long 编号的十进制文本，其它（路径、引号、超长）均拒绝
        String code64 = "a" + "b".repeat(63);
        for (String key : List.of("amount", "_", code64, "A_1", "11", "-1", "9223372036854775807")) {
            for (Dialect dialect : List.of(Dialect.ORIGINAL, Dialect.DM, Dialect.POSTGRESQL, Dialect.ORACLE, Dialect.SQLSERVER)) {
                assertNotNull(SqlDialectUtils.jsonText(dialect, "model_values", key), dialect + " " + key);
            }
        }
        List<String> invalidKeys = new ArrayList<>(List.of("", "1a", "a.b", "a'b", "a\"b", "a b", "a-b", "$.a", "-",
                code64 + "c", "12345678901234567890", "a[0]", "a*"));
        invalidKeys.add(null);
        for (String key : invalidKeys) {
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.jsonText(Dialect.ORIGINAL, "model_values", key), key);
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.jsonIn(Dialect.ORACLE, "model_values", key,
                    String.class), key);
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.jsonArrayOverlaps(Dialect.ORIGINAL,
                    "model_values", key, BigDecimal.class), key);
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.jsonLongContains(Dialect.SQLSERVER,
                    "answer_json", key), key);
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.jsonOrderBy(Dialect.POSTGRESQL,
                    "model_values", key, BigDecimal.class), key);
        }
        // 列名
        assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.jsonText(Dialect.ORIGINAL, "a; DROP", "amount"));
        assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.jsonTextNotBlank(Dialect.ORACLE, "(1)", "11"));
        assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.jsonIn(Dialect.POSTGRESQL, "a; DROP", "amount",
                String.class));
        assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.jsonArrayOverlaps(Dialect.SQLSERVER, "(1)",
                "amount", String.class));
        // 值类型：只支持 String、BigDecimal、Boolean
        for (Class<?> valueType : List.of(Integer.class, Long.class, LocalDateTime.class, Object.class)) {
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.jsonIn(Dialect.ORIGINAL, "model_values", "a",
                    valueType), valueType.getName());
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.jsonArrayOverlaps(Dialect.POSTGRESQL,
                    "model_values", "a", valueType), valueType.getName());
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.toJsonArrayParam(valueType, List.of(1)),
                    valueType.getName());
        }
        assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.jsonIn(Dialect.ORIGINAL, "model_values", "a", null));
        assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.jsonOrderBy(Dialect.ORIGINAL, "model_values",
                "a", Boolean.class));
    }

    @Test
    public void testJsonValue_placeholders() {
        for (Dialect dialect : List.of(Dialect.ORIGINAL, Dialect.DM, Dialect.POSTGRESQL, Dialect.ORACLE, Dialect.SQLSERVER)) {
            for (Class<?> valueType : List.of(String.class, BigDecimal.class, Boolean.class)) {
                for (String fragment : List.of(SqlDialectUtils.jsonIn(dialect, "model_values", "a", valueType),
                        SqlDialectUtils.jsonArrayOverlaps(dialect, "model_values", "a", valueType))) {
                    // 只有 {0} 一个编号参数（完整的 JSON 数组），与选项数量无关；Oracle 的 {0,jdbcType=CLOB,...} 为同一参数
                    assertEquals(List.of("{0}"), distinctPlaceholders(fragment.replaceAll("\\{0,[^}]+}", "{0}")),
                            dialect + " " + fragment);
                    assertFalse(fragment.contains("{1"), fragment);
                    if (dialect != Dialect.ORACLE) {
                        assertFalse(Pattern.compile("\\{\\d+,").matcher(fragment).find(), fragment);
                    }
                }
            }
            // 单参数片段只有 {0}，不含正则量词等其它花括号
            String longContains = SqlDialectUtils.jsonLongContains(dialect, "answer_json", "11");
            assertEquals(List.of("{0}"), distinctPlaceholders(longContains));
            assertFalse(longContains.replace("{0}", "").contains("{"), longContains);
            assertEquals(List.of("{0}"), distinctPlaceholders(SqlDialectUtils.jsonBooleanEquals(dialect, "model_values", "a")));
            // 取值、排序片段不含编号参数
            for (String fragment : List.of(SqlDialectUtils.jsonText(dialect, "model_values", "a"),
                    SqlDialectUtils.jsonNumber(dialect, "model_values", "a"),
                    SqlDialectUtils.jsonDateTime(dialect, "model_values", "a"),
                    SqlDialectUtils.jsonTextNotBlank(dialect, "answer_json", "11"))) {
                assertFalse(fragment.contains("{"), fragment);
            }
        }
    }

    /**
     * JSON 选项条件的参数：一个完整的 JSON 数组；数字保留为 JSON 数字（大 Long 不转为字符串），布尔严格校验，
     * 空集合为 []，不限制数量
     */
    @Test
    public void testToJsonArrayParam() {
        for (Class<?> valueType : List.of(String.class, BigDecimal.class, Boolean.class)) {
            assertEquals("[]", SqlDialectUtils.toJsonArrayParam(valueType, Collections.emptyList()), valueType.getName());
        }
        // 字符串：按 JSON 字符串转义，数值、布尔也写为字符串
        assertEquals("[\"a\\\"b\",\"x'y\",\"\\\\\",\"12\",\"9007199254740993\",\"true\"]",
                SqlDialectUtils.toJsonArrayParam(String.class, List.of("a\"b", "x'y", "\\", 12, 9007199254740993L, true)));
        // 数字：去掉末尾的 0，相邻的大 Long 与 11 位小数不丢精度
        assertEquals("[2.5,1000,0,9007199254740992,9007199254740993,0.00000000001,0.00000000002,-5,0.1]",
                SqlDialectUtils.toJsonArrayParam(BigDecimal.class, List.of(new BigDecimal("2.50"), "1E+3",
                        new BigDecimal("0.00"), 9007199254740992L, 9007199254740993L, new BigDecimal("0.00000000001"),
                        "2e-11", -5, 0.1)));
        // 十进制文本不超过 100 个字符时展开，超过时写为科学计数法，1e999999999 不展开为超长文本
        String plain100 = "1" + "0".repeat(99);
        assertEquals("[" + plain100 + "]", SqlDialectUtils.toJsonArrayParam(BigDecimal.class, List.of(plain100)));
        assertEquals("[1E+100]", SqlDialectUtils.toJsonArrayParam(BigDecimal.class, List.of(plain100 + "0")));
        assertEquals("[1E+999999999]", SqlDialectUtils.toJsonArrayParam(BigDecimal.class, List.of("1e999999999")));
        // 布尔：只接受 Boolean 与严格的 true、false 文本
        assertEquals("[true,false,true]", SqlDialectUtils.toJsonArrayParam(Boolean.class, List.of(true, "false", "true")));
        // 数量不限：1101 个选项为一个数组
        List<Long> ids = new ArrayList<>();
        for (long i = 0; i < 1101; i++) {
            ids.add(i);
        }
        String idsJson = SqlDialectUtils.toJsonArrayParam(BigDecimal.class, ids);
        assertTrue(idsJson.startsWith("[0,1,2,") && idsJson.endsWith(",1099,1100]"), idsJson);
        assertEquals(1101, idsJson.split(",").length);

        // 非法值均拒绝
        for (Object value : List.of("abc", "", " 1", "1,5", "NaN", Double.NaN, true, List.of(1), Map.of("a", 1))) {
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.toJsonArrayParam(BigDecimal.class,
                    List.of(value)), String.valueOf(value));
        }
        for (Object value : List.of("TRUE", "True", "1", " true", "yes", 1, List.of(true))) {
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.toJsonArrayParam(Boolean.class,
                    List.of(value)), String.valueOf(value));
        }
        for (Object value : List.of(List.of("a"), Map.of("a", "b"), new Object(), LocalDateTime.of(2026, 10, 31, 0, 0))) {
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.toJsonArrayParam(String.class,
                    List.of(value)), String.valueOf(value));
        }
        for (Class<?> valueType : List.of(String.class, BigDecimal.class, Boolean.class)) {
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.toJsonArrayParam(valueType, null));
            assertThrows(IllegalArgumentException.class, () -> SqlDialectUtils.toJsonArrayParam(valueType,
                    Arrays.asList("true", null)));
        }
    }

    /**
     * 排序表达式与 MyBatis Plus 分页的 count 优化：排序表达式不含 ?，autoCountSql 才会去掉 ORDER BY；
     * 否则保留 ORDER BY 并将查询列替换为 COUNT(*)，PostgreSQL 报 GROUP BY 错误
     */
    @Test
    public void testJsonValue_orderByCountSql() {
        // MyBatis Plus 分页插件实际的 count 优化（autoCountSql），不连接数据库
        PaginationInnerInterceptor interceptor = new PaginationInnerInterceptor();
        for (Dialect dialect : List.of(Dialect.ORIGINAL, Dialect.DM, Dialect.POSTGRESQL, Dialect.ORACLE, Dialect.SQLSERVER)) {
            for (Class<?> valueType : List.of(BigDecimal.class, LocalDateTime.class, String.class)) {
                List<String> orderBy = SqlDialectUtils.jsonOrderBy(dialect, "extension.model_values", "price", valueType);
                for (String expression : orderBy) {
                    assertFalse(expression.contains("?"), dialect + " " + expression);
                    assertTrue(distinctPlaceholders(expression).isEmpty(), dialect + " " + expression);
                }
                String sql = "SELECT t.id, t.title FROM cms_article t LEFT JOIN cms_article_extension extension"
                        + " ON extension.article_id = t.id WHERE t.site_id = ? ORDER BY " + String.join(" DESC, ", orderBy) + " DESC";
                String countSql = normalize(interceptor.autoCountSql(new Page<>(1, 10), sql));
                // 优化后的查询列为 COUNT(*)（可带别名 total）；解析失败时退化为 SELECT COUNT(*) FROM (原 SQL)，
                // 同样保留 ORDER BY，且不以 FROM cms_article 开头，因此也能发现解析问题
                assertTrue(Pattern.compile("^SELECT COUNT\\(\\*\\)( AS total)? FROM cms_article t ", Pattern.CASE_INSENSITIVE)
                        .matcher(countSql).find(), dialect + " " + valueType + " " + countSql);
                assertFalse(countSql.contains("ORDER BY"), dialect + " " + valueType + " " + countSql);
                assertTrue(countSql.contains("t.site_id = ?"), countSql);
            }
        }
        // 对照：排序表达式含正则的 ? 量词时，count 保留 ORDER BY（原问题）
        String number = SqlDialectUtils.jsonNumber(Dialect.POSTGRESQL, "extension.model_values", "price");
        assertTrue(number.contains("?"), number);
        assertTrue(normalize(interceptor.autoCountSql(new Page<>(1, 10), "SELECT t.id FROM cms_article t LEFT JOIN cms_article_extension extension"
                + " ON extension.article_id = t.id ORDER BY " + number + " DESC")).contains("ORDER BY"));
    }

    /**
     * 数字、日期正则在 Java 中的语义与 MySQL、PostgreSQL、Oracle 一致（只使用分组、字符类与 ?、+、* 量词）
     */
    @Test
    public void testJsonValue_patterns() {
        Pattern number = Pattern.compile(SqlDialectUtils.JSON_NUMBER_PATTERN);
        for (String value : List.of("0", "-5", "99.99", ".5", "-.5", "1.0E+7", "1e-7", "5E10", "1.5e99", "-0.0",
                "12345678901234567890.1234567890")) {
            assertTrue(number.matcher(value).find(), value);
        }
        for (String value : List.of("", "-", ".", "abc", "1.", "1..2", "+5", "1e300", "12e5", "1e", "1,5", " 1", "1 ",
                "0x10", "NaN", "Infinity", "--1", "true", "1-2")) {
            assertFalse(number.matcher(value).find(), value);
        }
        Pattern dateTime = Pattern.compile(SqlDialectUtils.JSON_DATE_TIME_PATTERN);
        for (String value : List.of("2024-02-29", "2000-02-29", "1600-02-29", "2400-02-29", "0004-02-29", "0001-01-01",
                "2026-10-31 23:59:59", "2026-10-31T08:30", "2026-10-31 08:30:15.123", "2026-10-31T08:30:15.123456789",
                "9999-12-31 23:59:59.999999999", "2026-04-30", "2026-01-31")) {
            assertTrue(dateTime.matcher(value).find(), value);
        }
        for (String value : List.of("2026-02-30", "2026-02-29", "1900-02-29", "2100-02-29", "0000-02-29", "0000-01-01",
                "2026-04-31", "2026-13-01", "2026-00-10", "2026-10-00", "2026-10-31 24:00", "2026-10-31 25:00:00",
                "2026-10-31 23:60", "2026-10-31 08:30:60", "2026-10-31 08:30:15+08:00", "2026-10-31T08:30:15Z",
                "2026-10-31 08:30:15.1234567890", "2026-10-31 08:30:15.", "2026-1-1", "20261031", "2026-10-31 8:30",
                " 2026-10-31", "2026-10-31 08", "2026/10/31")) {
            assertFalse(dateTime.matcher(value).find(), value);
        }
        // 达梦数字：在 JSON 数字的基础上限定小于 1E+20（DECIMAL(30, 10) 的整数部分），超出时为 NULL，不报溢出
        Pattern dmNumber = Pattern.compile(SqlDialectUtils.DM_NUMBER_PATTERN);
        for (String value : List.of("0", "-5", "99.99", ".5", "-.5", "1.0E+7", "1e-7", "1e-99", "9.9e19", "-1.5e19", "1E+19",
                "12345678901234567890", "-12345678901234567890.1234567890")) {
            assertTrue(dmNumber.matcher(value).find(), value);
        }
        for (String value : List.of("", "abc", "true", "1.", "+5", "123456789012345678901", "1e20", "1E+20", "1.5e99",
                "1e999", "12e5", "1e", " 1")) {
            assertFalse(dmNumber.matcher(value).find(), value);
        }
        // 不含 {n} 量词，避免被 Wrapper 当作编号参数；长度小于 Oracle 正则的 512 字节上限
        for (String pattern : List.of(SqlDialectUtils.JSON_NUMBER_PATTERN, SqlDialectUtils.JSON_DATE_TIME_PATTERN,
                SqlDialectUtils.DM_NUMBER_PATTERN)) {
            assertFalse(pattern.contains("{"), pattern);
            assertFalse(pattern.contains("'"), pattern);
            assertTrue(pattern.length() < 512, pattern);
        }
    }

    /**
     * JSON 字段值按普通方言路由：openGauss 等兼容 PostgreSQL 的数据库使用 jsonb 写法（与 JSON 数组匹配分开识别），
     * 值均为绑定参数，生成的 SQL 可被 JSqlParser 解析（租户、数据权限插件）
     */
    @Test
    public void testBoundSql_jsonValue() throws Exception {
        FakeDataSource mysql = new FakeDataSource("MySQL", "jdbc:mysql://127.0.0.1:3306/a", 8);
        FakeDataSource oracle = new FakeDataSource("Oracle", "jdbc:oracle:thin:@127.0.0.1:1521:xe", 21);
        FakeDataSource sqlServer = new FakeDataSource("Microsoft SQL Server", "jdbc:sqlserver://127.0.0.1:1433", 16);
        FakeDataSource kingbase = new FakeDataSource("KingbaseES", "jdbc:kingbase8://127.0.0.1:54321/test", 8);
        FakeDataSource openGauss = new FakeDataSource("openGauss", "jdbc:opengauss://127.0.0.1:5432/test", 5);
        FakeDataSource h2 = new FakeDataSource("H2", "jdbc:h2:mem:testdb;MODE=MYSQL", 2);
        FakeDataSource dm = new FakeDataSource("DM DBMS", "jdbc:dm://127.0.0.1:5236", 8);
        mockDynamicRoutingDataSource(Map.of("master", mysql, "oracle", oracle, "sqlserver", sqlServer,
                "kingbase", kingbase, "opengauss", openGauss, "h2", h2, "dm", dm));
        SqlSource sqlSource = new XMLLanguageDriver().createSqlSource(new Configuration(),
                "<script>SELECT t.id FROM cms_article t LEFT JOIN cms_article_extension extension ON extension.article_id = t.id"
                        + " ${ew.customSqlSegment}</script>", Map.class);
        Map<String, Dialect> routes = new LinkedHashMap<>();
        routes.put("master", Dialect.ORIGINAL);
        routes.put("oracle", Dialect.ORACLE);
        routes.put("sqlserver", Dialect.SQLSERVER);
        routes.put("kingbase", Dialect.POSTGRESQL);
        routes.put("opengauss", Dialect.POSTGRESQL);
        routes.put("h2", Dialect.H2);
        routes.put("dm", Dialect.DM);
        String column = "extension.model_values";

        for (Map.Entry<String, Dialect> route : routes.entrySet()) {
            DynamicDataSourceContextHolder.push(route.getKey());
            Dialect dialect = route.getValue();
            String like = SqlDialectUtils.jsonText(column, "title") + " LIKE CONCAT(CONCAT('%', {0}), '%') ESCAPE '!'";
            String number = SqlDialectUtils.jsonNumber(column, "price") + " >= {0}";
            String date = SqlDialectUtils.jsonDateTime(column, "eventTime") + " <= {0}";
            String equal = SqlDialectUtils.jsonBooleanEquals(column, "recommended");
            String in = SqlDialectUtils.jsonIn(column, "level", String.class);
            List<String> orderBy = SqlDialectUtils.jsonOrderBy(column, "price", BigDecimal.class);
            assertEquals(SqlDialectUtils.jsonText(dialect, column, "title") + " LIKE CONCAT(CONCAT('%', {0}), '%') ESCAPE '!'",
                    like, route.getKey());
            assertEquals(SqlDialectUtils.jsonIn(dialect, column, "level", String.class), in, route.getKey());
            assertEquals(SqlDialectUtils.jsonOrderBy(dialect, column, "price", BigDecimal.class), orderBy, route.getKey());
            String inJson = SqlDialectUtils.toJsonArrayParam(String.class, List.of("x'y", "z"));
            String topicsJson = SqlDialectUtils.toJsonArrayParam(BigDecimal.class, List.of(new BigDecimal("1"),
                    new BigDecimal("2.50"), 9007199254740993L));
            QueryWrapper<Object> wrapper = new QueryWrapper<>();
            wrapper.eq("t.site_id", 1L).apply(like, "50!%").apply(number, new BigDecimal("80.5"))
                    .apply(date, LocalDateTime.of(2026, 10, 31, 23, 59, 59, 999_000_000)).apply(equal, "true")
                    .apply(in, inJson);
            // Oracle 的 JSON_TABLE、JSON_VALUE ... RETURNING CLOB 需要 JSqlParser 5.4+ 才能解析
            wrapper.apply(SqlDialectUtils.jsonArrayOverlaps(column, "topics", BigDecimal.class), topicsJson);
            wrapper.apply(SqlDialectUtils.jsonLongContains("answer_json", "11"), 9007199254740993L)
                    .apply(SqlDialectUtils.jsonTextNotBlank("answer_json", "12"));
            for (String expression : SqlDialectUtils.jsonOrderBy(column, "title", String.class)) {
                wrapper.orderBy(true, true, expression);
            }
            for (String expression : orderBy) {
                wrapper.orderBy(true, false, expression);
            }
            BoundSql boundSql = sqlSource.getBoundSql(Map.of("ew", wrapper));
            String sql = normalize(boundSql.getSql());
            assertTrue(sql.contains(normalize(bindIndexParams(like))), route.getKey() + " " + sql);
            // Oracle 的 {0,jdbcType=CLOB,typeHandler=...} 同样由 Wrapper 转为一个 JDBC 占位符
            assertTrue(sql.contains(normalize(in.replaceAll("\\{0(,[^}]+)?}", "?"))), route.getKey() + " " + sql);
            assertTrue(sql.contains(normalize(bindIndexParams(
                    SqlDialectUtils.jsonLongContains(dialect, "answer_json", "11")))), route.getKey() + " " + sql);
            assertTrue(sql.endsWith(" DESC"), sql);
            // 值只通过参数绑定，不拼接到 SQL 中
            assertFalse(sql.contains("x'y") || sql.contains("50!%") || sql.contains("9007199254740993"), sql);
            List<Object> values = getParameterValues(boundSql, wrapper);
            assertEquals(1L, values.get(0));
            assertTrue(values.containsAll(List.of("50!%", new BigDecimal("80.5"), "true", "[\"x'y\",\"z\"]",
                    "[1,2.5,9007199254740993]", 9007199254740993L)), values.toString());
            assertNotNull(CCJSqlParserUtil.parse(sql), route.getKey() + " " + sql);
            // 选项条件：1101 个选项也只绑定一个完整的 JSON 数组参数，选项值不拼接到 SQL 中
            List<String> options = new ArrayList<>();
            for (int i = 0; i < 1101; i++) {
                options.add("v" + i);
            }
            String optionsJson = SqlDialectUtils.toJsonArrayParam(String.class, options);
            for (String condition : List.of(SqlDialectUtils.jsonIn(column, "level", String.class),
                    SqlDialectUtils.jsonArrayOverlaps(column, "tags", String.class))) {
                QueryWrapper<Object> optionWrapper = new QueryWrapper<>();
                optionWrapper.eq("t.site_id", 1L).apply(condition, optionsJson);
                BoundSql optionSql = sqlSource.getBoundSql(Map.of("ew", optionWrapper));
                assertEquals(List.of(1L, optionsJson), getParameterValues(optionSql, optionWrapper), route.getKey());
                assertFalse(optionSql.getSql().contains("v1100"), optionSql.getSql());
                if (dialect == Dialect.ORACLE) {
                    ParameterMapping mapping = optionSql.getParameterMappings().get(1);
                    assertEquals(JdbcType.CLOB, mapping.getJdbcType(), route.getKey());
                    assertInstanceOf(OracleClobTypeHandler.class, mapping.getTypeHandler(), route.getKey());
                }
                assertNotNull(CCJSqlParserUtil.parse(normalize(optionSql.getSql())), route.getKey() + " " + optionSql.getSql());
            }
            // 原样（不压缩空白）也可解析：SQL Server 问答的字符类中含有换行等空白字符
            assertTrue(boundSql.getSql().contains(SqlDialectUtils.jsonTextNotBlank(dialect, "answer_json", "12")), route.getKey());
            assertNotNull(CCJSqlParserUtil.parse(boundSql.getSql()), route.getKey() + " " + boundSql.getSql());
            DynamicDataSourceContextHolder.poll();
        }
        // 不断言连接次数：方言缓存为弱引用，本用例较长，GC 后可回收并重新识别；缓存行为由
        // testGetDialect_cachedPerDataSource 等专门用例验证
    }

    // ========== 工具方法 ==========

    private static Dialect resolveJson(String productName, String url, int majorVersion) throws SQLException {
        return SqlDialectUtils.resolveJsonDialect(new FakeDataSource(productName, url, majorVersion).getMetaData());
    }

    private static Dialect resolveJsonValue(String productName, String url, int majorVersion) throws SQLException {
        return SqlDialectUtils.resolveJsonValueDialect(new FakeDataSource(productName, url, majorVersion).getMetaData());
    }

    /**
     * 排序表达式的期望：正则的 ? 量词改为等价的 {0,1}，见 {@link #testJsonValue_orderByCountSql()}
     */
    private static String orderBy(String expression) {
        return expression.replace("?", "{0,1}");
    }

    /**
     * 问答空白字符的期望：Unicode White_Space 的 25 个字符（含空格），按码位生成
     */
    private static String whiteSpaces() {
        return new String(new int[]{0x20, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x85, 0xA0, 0x1680,
                0x2000, 0x2001, 0x2002, 0x2003, 0x2004, 0x2005, 0x2006, 0x2007, 0x2008, 0x2009, 0x200A,
                0x2028, 0x2029, 0x202F, 0x205F, 0x3000}, 0, 25);
    }

    /**
     * Oracle JSON 路径中空白字符的期望：空格原样，其它为 JSON 字符串的 Unicode 转义（反斜杠、u 与 4 位大写十六进制）
     */
    private static String oracleWhiteSpaces() {
        StringBuilder builder = new StringBuilder();
        whiteSpaces().codePoints().forEach(codePoint -> builder.append(codePoint == ' ' ? " "
                : "\\" + 'u' + String.format("%04X", codePoint)));
        return builder.toString();
    }

    /**
     * SQL Server 数字文本转 DECIMAL(38, 10) 的期望片段：十进制文本直接 TRY_CAST；科学计数法校验尾数、指数格式后，
     * 按指数移动小数点、补零展开为十进制文本再 TRY_CAST，不经过 FLOAT
     */
    private static String sqlServerDecimal(String text) {
        String p = "(yd_jnd.i + yd_jnd.x)";
        return "(SELECT CASE WHEN yd_jne.e = 0 THEN TRY_CAST(yd_jnt.t AS DECIMAL(38, 10))"
                + " WHEN (yd_jns.m LIKE N'[0-9]' OR (yd_jns.m LIKE N'[0-9].[0-9]%' AND SUBSTRING(yd_jns.m, 3, 64) NOT LIKE N'%[^0-9]%')"
                + " OR (yd_jns.m LIKE N'.[0-9]%' AND SUBSTRING(yd_jns.m, 2, 64) NOT LIKE N'%[^0-9]%'))"
                + " AND (yd_jns.xt LIKE N'[0-9]' OR yd_jns.xt LIKE N'[0-9][0-9]' OR yd_jns.xt LIKE N'[+-][0-9]'"
                + " OR yd_jns.xt LIKE N'[+-][0-9][0-9]')"
                + " THEN TRY_CAST(yd_jns.s + CASE WHEN " + p + " >= LEN(yd_jnd.d) THEN yd_jnd.d + REPLICATE(N'0', " + p
                + " - LEN(yd_jnd.d)) WHEN " + p + " <= 0 THEN N'0.' + REPLICATE(N'0', -" + p + ") + yd_jnd.d"
                + " ELSE LEFT(yd_jnd.d, ABS" + p + ") + N'.' + SUBSTRING(yd_jnd.d, " + p + " + 1, 64) END AS DECIMAL(38, 10)) END"
                + " FROM (SELECT " + text + " AS t) yd_jnt"
                + " CROSS APPLY (SELECT PATINDEX(N'%[eE]%', yd_jnt.t) AS e, CASE WHEN LEFT(yd_jnt.t, 1) = N'-' THEN 1 ELSE 0 END AS n) yd_jne"
                + " CROSS APPLY (SELECT CASE WHEN yd_jne.n = 1 THEN N'-' ELSE N'' END AS s,"
                + " SUBSTRING(yd_jnt.t, yd_jne.n + 1, CASE WHEN yd_jne.e > yd_jne.n + 1 THEN yd_jne.e - yd_jne.n - 1 ELSE 0 END) AS m,"
                + " SUBSTRING(yd_jnt.t, yd_jne.e + 1, 64) AS xt) yd_jns"
                + " CROSS APPLY (SELECT REPLACE(yd_jns.m, N'.', N'') AS d, CASE WHEN CHARINDEX(N'.', yd_jns.m) = 0 THEN LEN(yd_jns.m)"
                + " ELSE CHARINDEX(N'.', yd_jns.m) - 1 END AS i, TRY_CAST(yd_jns.xt AS INT) AS x) yd_jnd)";
    }

    /**
     * 在 Java 中复现神通日期衰减得分的舍入顺序：对数积按 FLOAT8 已知时，衰减部分先舍入为 Double（次正规区按 2^-1074 的整数倍
     * 就近舍入、平分取偶），再与权重按宽 NUMERIC 精确相乘，读取为 Double 时舍入一次
     *
     * @param boost 权重
     * @param lnScore 对数积
     * @return 得分
     */
    private static double oscarDateDecayScore(double boost, double lnScore) {
        if (lnScore <= -745.1332191019412) {
            return 0;
        }
        BigDecimal factor;
        if (lnScore < -708) {
            double ratio = Math.exp(744.4400719213812 - Math.max(708, 0 - Math.max(lnScore, -745.1332191019412)));
            long units = (long) ((ratio + 4503599627370496D) - 4503599627370496D);
            factor = BigDecimal.valueOf(units).multiply(new BigDecimal(Double.MIN_VALUE));
        } else {
            factor = new BigDecimal(Math.exp(Math.max(lnScore, -708)));
        }
        return new BigDecimal(boost).multiply(factor).doubleValue();
    }

    /**
     * PostgreSQL 及其兼容数据库的 JSON 数组包含片段：参数转为 BIGINT 后拼成单元素 JSON 数组，null 参数拼成 [null]
     *
     * @param column JSON 数组列名
     * @return 期望的 SQL 片段
     */
    private static String postgreSQLJsonArrayContains(String column) {
        return "CAST(" + column + " AS jsonb) @> CAST(CONCAT('[', COALESCE(CAST(CAST({0} AS BIGINT) AS TEXT), 'null'), ']') AS jsonb)";
    }

    /**
     * 在 Java 中复现 Oracle 片段：REPLACE 空白、REGEXP_LIKE 整体格式与溢出、去空白后 INSTR 匹配 ,元素,
     *
     * @param json 列值，null 表示 SQL NULL
     * @param value 绑定参数
     * @return 是否匹配
     */
    private static boolean oracleJsonContains(String json, Long value) {
        return jsonContains(SqlDialectUtils.ORACLE_LONG_ARRAY_PATTERN, json, value);
    }

    /**
     * 在 Java 中复现 H2 片段，规则同 {@link #oracleJsonContains(String, Long)}，整体格式使用 H2 专用的正则
     */
    private static boolean h2JsonContains(String json, Long value) {
        return jsonContains(SqlDialectUtils.H2_LONG_ARRAY_PATTERN, json, value);
    }

    private static boolean jsonContains(String arrayPattern, String json, Long value) {
        if (json == null) {
            return false;
        }
        String text = json.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
        if (!Pattern.compile(arrayPattern).matcher(text).find()) {
            return false;
        }
        if (Pattern.compile(SqlDialectUtils.LONG_OVERFLOW_PATTERN).matcher(json).find()) {
            return false;
        }
        String elements = text.replace(" ", "").replace('[', ',').replace(']', ',');
        return elements.contains("," + (value == null ? "null" : value.toString()) + ",");
    }

    /**
     * 在 H2 中执行 JSON 数组包含条件，列 j.label_ids 为内联的 CLOB
     *
     * @param connection H2 连接
     * @param condition 条件片段，{0} 已替换为 JDBC 参数
     * @param json 列值，null 表示 SQL NULL
     * @param value 绑定参数
     * @return 是否匹配
     */
    private static boolean h2Execute(Connection connection, String condition, String json, Long value) throws SQLException {
        // 条件中的参数在前，列值的参数在后
        String sql = "SELECT CASE WHEN " + condition + " THEN 1 ELSE 0 END FROM (SELECT CAST(? AS CLOB) AS label_ids) j";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (value == null) {
                statement.setNull(1, Types.BIGINT);
            } else {
                statement.setLong(1, value);
            }
            if (json == null) {
                statement.setNull(2, Types.CLOB);
            } else {
                statement.setString(2, json);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                return resultSet.getInt(1) == 1;
            }
        }
    }

    private static Dialect resolve(String productName, String url, int majorVersion) throws SQLException {
        return SqlDialectUtils.resolveDialect(new FakeDataSource(productName, url, majorVersion).getMetaData());
    }

    /**
     * 按 {@link JdbcUtils#getDbType()} 相同的识别规则取得数据库类型，再生成 extended 列的布尔判断
     */
    private static String isTrue(String productName, String url) {
        DbType dbType = JdbcUtils.getDbType(new FakeDataSource(productName, url, 1).getDataSource());
        return SqlDialectUtils.isTrue(dbType, "extended");
    }

    private static void mockDynamicRoutingDataSource(Map<String, FakeDataSource> dataSources) {
        DynamicRoutingDataSource dynamicRoutingDataSource = new DynamicRoutingDataSource(Collections.emptyList());
        dynamicRoutingDataSource.setPrimary("master");
        dataSources.forEach((name, dataSource) -> dynamicRoutingDataSource.addDataSource(name, dataSource.getDataSource()));
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("dynamicRoutingDataSource", dynamicRoutingDataSource);
        new SpringUtil().postProcessBeanFactory(beanFactory);
    }

    private static String normalize(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }

    /**
     * 将 #{} 参数替换为 JDBC 占位符，模拟 MyBatis 生成的最终 SQL
     */
    private static String bind(String sql) {
        return sql.replaceAll("#\\{[^}]+}", "?");
    }

    /**
     * 将 {0} 等编号参数替换为 JDBC 占位符，模拟 Wrapper 生成的最终 SQL
     */
    private static String bindIndexParams(String sql) {
        return sql.replaceAll("\\{\\d+}", "?");
    }

    private static List<String> distinctPlaceholders(String sql) {
        Matcher matcher = Pattern.compile("\\{\\d+}").matcher(sql);
        return matcher.results().map(result -> result.group()).distinct().sorted().toList();
    }

    /**
     * 按 BoundSql 的参数顺序，取得 Wrapper 中绑定的参数值
     */
    private static List<Object> getParameterValues(BoundSql boundSql, QueryWrapper<?> wrapper) {
        String prefix = "ew.paramNameValuePairs.";
        List<Object> values = new ArrayList<>();
        for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
            String property = parameterMapping.getProperty();
            assertTrue(property.startsWith(prefix), property);
            values.add(wrapper.getParamNameValuePairs().get(property.substring(prefix.length())));
        }
        return values;
    }

    private static void assertParameters(BoundSql boundSql, String... properties) {
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        assertEquals(properties.length, parameterMappings.size());
        for (int i = 0; i < properties.length; i++) {
            assertEquals(properties[i], parameterMappings.get(i).getProperty());
        }
    }

    private static String causeMessages(Throwable ex) {
        StringBuilder messages = new StringBuilder();
        for (Throwable cause = ex; cause != null && cause != cause.getCause(); cause = cause.getCause()) {
            messages.append(cause).append('\n');
        }
        return messages.toString();
    }

    /**
     * 基于 JDK 动态代理的数据源，只提供识别方言所需的元数据，并记录获取连接的次数
     */
    private static class FakeDataSource implements InvocationHandler {

        private final String productName;
        private final String url;
        private final int majorVersion;
        private final DataSource dataSource;
        private int connectionCount;
        private int closeCount;
        private boolean failOnConnection;

        FakeDataSource(String productName, String url, int majorVersion) {
            this.productName = productName;
            this.url = url;
            this.majorVersion = majorVersion;
            this.dataSource = proxy(DataSource.class);
        }

        DataSource getDataSource() {
            return dataSource;
        }

        DatabaseMetaData getMetaData() {
            return proxy(DatabaseMetaData.class);
        }

        int getConnectionCount() {
            return connectionCount;
        }

        void setFailOnConnection(boolean failOnConnection) {
            this.failOnConnection = failOnConnection;
        }

        @SuppressWarnings("unchecked")
        private <T> T proxy(Class<T> type) {
            return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{type}, this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            switch (method.getName()) {
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "equals":
                    return proxy == args[0];
                case "toString":
                    return "FakeDataSource(" + productName + ")";
                case "getConnection":
                    connectionCount++;
                    if (failOnConnection) {
                        throw new SQLException("connection refused");
                    }
                    return proxy(Connection.class);
                case "getMetaData":
                    return proxy(DatabaseMetaData.class);
                case "close":
                    closeCount++;
                    return null;
                case "getDatabaseProductName":
                    return productName;
                case "getURL":
                    return url;
                case "getDatabaseMajorVersion":
                    return majorVersion;
                default:
                    throw new UnsupportedOperationException(method.getName());
            }
        }

    }

}
