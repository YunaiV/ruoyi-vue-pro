package cn.iocoder.yudao.framework.mybatis.core.util;

import cn.hutool.core.exceptions.UtilException;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.mybatis.core.util.SqlDialectUtils.Dialect;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link SqlDialectUtils} 的单元测试
 *
 * @author 芋道源码
 */
public class SqlDialectUtilsTest {

    private static final String P = "${@cn.iocoder.yudao.framework.mybatis.core.util.SqlDialectUtils@";

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
    }

    @Test
    public void testFormatYearMonth() {
        assertEquals("DATE_FORMAT(order_date, '%Y%m')", SqlDialectUtils.formatYearMonth(Dialect.ORIGINAL, "order_date"));
        assertEquals("TO_CHAR(order_date, 'YYYYMM')", SqlDialectUtils.formatYearMonth(Dialect.POSTGRESQL, "order_date"));
        assertEquals("TO_CHAR(order_date, 'YYYYMM')", SqlDialectUtils.formatYearMonth(Dialect.ORACLE, "order_date"));
        assertEquals("CONVERT(varchar(6), order_date, 112)", SqlDialectUtils.formatYearMonth(Dialect.SQLSERVER, "order_date"));
    }

    @Test
    public void testFormatYearDashMonth() {
        assertEquals("CONCAT(EXTRACT(YEAR FROM pay_time), '-', LPAD(CONCAT(EXTRACT(MONTH FROM pay_time)), 2, '0'))",
                SqlDialectUtils.formatYearDashMonth(Dialect.ORIGINAL, "pay_time"));
        assertEquals("TO_CHAR(pay_time, 'YYYY-MM')", SqlDialectUtils.formatYearDashMonth(Dialect.POSTGRESQL, "pay_time"));
        assertEquals("TO_CHAR(pay_time, 'YYYY-MM')", SqlDialectUtils.formatYearDashMonth(Dialect.ORACLE, "pay_time"));
        assertEquals("CONVERT(varchar(7), pay_time, 120)", SqlDialectUtils.formatYearDashMonth(Dialect.SQLSERVER, "pay_time"));
    }

    @Test
    public void testDate() {
        assertEquals("DATE(send_time)", SqlDialectUtils.date(Dialect.ORIGINAL, "send_time"));
        assertEquals("TO_CHAR(send_time, 'YYYY-MM-DD')", SqlDialectUtils.date(Dialect.POSTGRESQL, "send_time"));
        assertEquals("TO_CHAR(send_time, 'YYYY-MM-DD')", SqlDialectUtils.date(Dialect.ORACLE, "send_time"));
        assertEquals("CONVERT(varchar(10), send_time, 23)", SqlDialectUtils.date(Dialect.SQLSERVER, "send_time"));
    }

    @Test
    public void testCastDateText() {
        assertEquals("CAST(create_time AS DATE)", SqlDialectUtils.castDateText(Dialect.ORIGINAL, "create_time"));
        assertEquals("TO_CHAR(create_time, 'YYYY-MM-DD')", SqlDialectUtils.castDateText(Dialect.POSTGRESQL, "create_time"));
        assertEquals("TO_CHAR(create_time, 'YYYY-MM-DD')", SqlDialectUtils.castDateText(Dialect.ORACLE, "create_time"));
        assertEquals("CONVERT(varchar(10), create_time, 23)", SqlDialectUtils.castDateText(Dialect.SQLSERVER, "create_time"));
    }

    @Test
    public void testCastDate() {
        assertEquals("CAST(create_time AS DATE)", SqlDialectUtils.castDate(Dialect.ORIGINAL, "create_time"));
        assertEquals("CAST(create_time AS DATE)", SqlDialectUtils.castDate(Dialect.POSTGRESQL, "create_time"));
        assertEquals("TRUNC(create_time)", SqlDialectUtils.castDate(Dialect.ORACLE, "create_time"));
        assertEquals("CAST(create_time AS DATE)", SqlDialectUtils.castDate(Dialect.SQLSERVER, "create_time"));
    }

    @Test
    public void testMonth() {
        assertEquals("MONTH(order_date)", SqlDialectUtils.month(Dialect.ORIGINAL, "order_date"));
        assertEquals("CAST(EXTRACT(MONTH FROM order_date) AS INTEGER)", SqlDialectUtils.month(Dialect.POSTGRESQL, "order_date"));
        assertEquals("EXTRACT(MONTH FROM order_date)", SqlDialectUtils.month(Dialect.ORACLE, "order_date"));
        assertEquals("MONTH(order_date)", SqlDialectUtils.month(Dialect.SQLSERVER, "order_date"));
    }

    @Test
    public void testYear() {
        assertEquals("YEAR(return_time)", SqlDialectUtils.year(Dialect.ORIGINAL, "return_time"));
        assertEquals("CAST(EXTRACT(YEAR FROM return_time) AS INTEGER)", SqlDialectUtils.year(Dialect.POSTGRESQL, "return_time"));
        assertEquals("EXTRACT(YEAR FROM return_time)", SqlDialectUtils.year(Dialect.ORACLE, "return_time"));
        assertEquals("YEAR(return_time)", SqlDialectUtils.year(Dialect.SQLSERVER, "return_time"));
    }

    @Test
    public void testDayOfMonth() {
        assertEquals("DAYOFMONTH(birthday)", SqlDialectUtils.dayOfMonth(Dialect.ORIGINAL, "birthday"));
        assertEquals("CAST(EXTRACT(DAY FROM birthday) AS INTEGER)", SqlDialectUtils.dayOfMonth(Dialect.POSTGRESQL, "birthday"));
        assertEquals("EXTRACT(DAY FROM birthday)", SqlDialectUtils.dayOfMonth(Dialect.ORACLE, "birthday"));
        assertEquals("DAY(birthday)", SqlDialectUtils.dayOfMonth(Dialect.SQLSERVER, "birthday"));
    }

    @Test
    public void testCustomerDealCycle() {
        String original = "COALESCE(TRUNCATE(AVG(TIMESTAMPDIFF(DAY, customer.create_time, contract.order_date)), 1), 0)";
        assertEquals(original, SqlDialectUtils.customerDealCycle(Dialect.ORIGINAL, "customer.create_time", "contract.order_date"));
        // PostgreSQL：两个 TIMESTAMP 相减为 interval，EXTRACT(DAY) 即完整天数（向零截断、保留负号），TRUNC 截断保留 1 位
        assertEquals("COALESCE(TRUNC(AVG(CAST(EXTRACT(DAY FROM (CAST(contract.order_date AS TIMESTAMP) - "
                        + "CAST(customer.create_time AS TIMESTAMP))) AS NUMERIC)), 1), 0)",
                SqlDialectUtils.customerDealCycle(Dialect.POSTGRESQL, "customer.create_time", "contract.order_date"));
        // Oracle、SQL Server：完整天数表达式依赖实际列类型，尚未验收，保持原有片段
        assertEquals(original, SqlDialectUtils.customerDealCycle(Dialect.ORACLE, "customer.create_time", "contract.order_date"));
        assertEquals(original, SqlDialectUtils.customerDealCycle(Dialect.SQLSERVER, "customer.create_time", "contract.order_date"));
    }

    @Test
    public void testFromDual() {
        assertEquals("", SqlDialectUtils.fromDual(Dialect.ORIGINAL));
        assertEquals("", SqlDialectUtils.fromDual(Dialect.POSTGRESQL));
        assertEquals("FROM DUAL", SqlDialectUtils.fromDual(Dialect.ORACLE));
        assertEquals("", SqlDialectUtils.fromDual(Dialect.SQLSERVER));
    }

    @Test
    public void testInvalidColumnName() {
        List<BiFunction<Dialect, String, String>> fragments = List.of(SqlDialectUtils::formatDate,
                SqlDialectUtils::formatYearMonth, SqlDialectUtils::formatYearDashMonth, SqlDialectUtils::date,
                SqlDialectUtils::castDateText, SqlDialectUtils::castDate, SqlDialectUtils::month,
                SqlDialectUtils::year, SqlDialectUtils::dayOfMonth);
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
    }

    @Test
    public void testLimitRows_zeroAndNegative() {
        assertEquals("LIMIT 0", SqlDialectUtils.limitRows(Dialect.ORIGINAL, 0, true));
        assertEquals("LIMIT 0", SqlDialectUtils.limitRows(Dialect.POSTGRESQL, 0, true));
        assertEquals("FETCH FIRST 0 ROWS ONLY", SqlDialectUtils.limitRows(Dialect.ORACLE, 0, true));
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
        assertEquals(Dialect.ORIGINAL, resolve("OceanBase", "jdbc:oceanbase://127.0.0.1:2881/ruoyi-vue-pro", 4));
        // 已有旧路径：H2、达梦、神通、人大金仓
        assertEquals(Dialect.ORIGINAL, resolve("H2", "jdbc:h2:mem:testdb;MODE=MYSQL", 2));
        assertEquals(Dialect.ORIGINAL, resolve("DM DBMS", "jdbc:dm://127.0.0.1:5236", 8));
        assertEquals(Dialect.ORIGINAL, resolve("OSCAR", "jdbc:oscar://127.0.0.1:2003/osrdb", 7));
        assertEquals(Dialect.ORIGINAL, resolve("KingbaseES", "jdbc:kingbase8://127.0.0.1:54321/test", 8));
        // 未验收的 PostgreSQL 变种：原生产品名
        assertEquals(Dialect.ORIGINAL, resolve("openGauss", "jdbc:opengauss://127.0.0.1:5432/test", 5));
        assertEquals(Dialect.ORIGINAL, resolve("GaussDB", "jdbc:gaussdb://127.0.0.1:8000/test", 5));
        // 未验收的 PostgreSQL 变种：产品名未登记或与 PostgreSQL 相同，按原生驱动 URL 识别
        assertEquals(Dialect.ORIGINAL, resolve("HighGo", "jdbc:highgo://127.0.0.1:5866/highgo", 6));
        assertEquals(Dialect.ORIGINAL, resolve("PostgreSQL", "jdbc:highgo://127.0.0.1:5866/highgo", 6));
        assertEquals(Dialect.ORIGINAL, resolve("PostgreSQL", "jdbc:opengauss://127.0.0.1:5432/test", 9));
        assertEquals(Dialect.ORIGINAL, resolve("PostgreSQL", "jdbc:mogdb://127.0.0.1:5432/test", 9));
        assertEquals(Dialect.ORIGINAL, resolve("PostgreSQL", "jdbc:gaussdb://127.0.0.1:8000/test", 9));
        assertEquals(Dialect.ORIGINAL, resolve("PostgreSQL", "jdbc:kingbase8://127.0.0.1:54321/test", 12));
        // 未验收的 SQL Server 版本
        assertEquals(Dialect.ORIGINAL, resolve("Microsoft SQL Server", "jdbc:sqlserver://127.0.0.1:1433", 10));
        assertEquals(Dialect.ORIGINAL, resolve("Microsoft SQL Server 2005", "jdbc:jtds:sqlserver://127.0.0.1:1433", 9));
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
        assertEquals("SELECT DATE(send_time) AS statDate, COUNT(*) AS count FROM im_private_message"
                + " WHERE send_time >= ? AND send_time < ?"
                + " GROUP BY DATE(send_time)", normalize(boundSql.getSql()));
        assertParameters(boundSql, "beginTime", "endTime");
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

    // ========== 工具方法 ==========

    private static Dialect resolve(String productName, String url, int majorVersion) throws SQLException {
        return SqlDialectUtils.resolveDialect(new FakeDataSource(productName, url, majorVersion).getMetaData());
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
