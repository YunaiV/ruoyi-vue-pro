package cn.iocoder.yudao.module.report.service.goview;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.report.controller.admin.goview.vo.data.GoViewDataRespVO;
import com.alibaba.druid.DbType;
import com.baomidou.dynamic.datasource.tx.TransactionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.report.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.report.service.goview.GoViewDataServiceImpl.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link GoViewDataServiceImpl} 的单元测试
 *
 * 数据库只读事务、SQL_SELECT_LIMIT 复原的真实效果，需要在 MySQL、MariaDB、PostgreSQL 上验证；这里校验调用顺序与语句级设置
 *
 * @author 芋道源码
 */
public class GoViewDataServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private GoViewDataServiceImpl goViewDataService;

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private DatabaseMetaData databaseMetaData;
    @Mock
    private PreparedStatement preparedStatement;
    /**
     * 复原 SQL_SELECT_LIMIT 的语句
     */
    @Mock
    private Statement resetStatement;

    private final SimpleTransactionStatus transactionStatus = new SimpleTransactionStatus();

    /**
     * H2 内存数据库的连接，用于提供真实的 ResultSet
     */
    private Connection h2Connection;

    @BeforeEach
    public void setUp() throws SQLException {
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:go_view_data_test");
        // 模拟事务管理器开启事务后，数据源绑定到当前事务
        TransactionSynchronizationManager.bindResource(dataSource, new ConnectionHolder(connection));
    }

    @AfterEach
    public void tearDown() throws SQLException {
        TransactionSynchronizationManager.unbindResourceIfPossible(dataSource);
        TransactionContext.remove();
        h2Connection.close();
    }

    // ========== getDataBySQL ==========

    @Test
    @SuppressWarnings("unchecked")
    public void testGetDataBySQL_mysql() throws SQLException {
        // 准备参数
        String sql = "select id, name from system_users where deleted = 0 order by id";
        // mock 方法
        mockTransaction("MySQL");
        mockQuery("SELECT 1 AS \"id\", '芋道源码' AS \"name\" UNION ALL SELECT 2, '芋道' ORDER BY 1");
        mockResetSelectLimit();

        // 调用
        GoViewDataRespVO respVO = goViewDataService.getDataBySQL(sql);
        // 断言：结果
        assertEquals(Arrays.asList("id", "name"), respVO.getDimensions());
        assertEquals(2, respVO.getSource().size());
        assertEquals(1, ((Number) respVO.getSource().get(0).get("id")).intValue());
        assertEquals("芋道源码", respVO.getSource().get(0).get("name"));
        assertEquals(2, ((Number) respVO.getSource().get(1).get("id")).intValue());
        assertEquals("芋道", respVO.getSource().get(1).get("name"));
        // 断言：REQUIRES_NEW 的只读事务，结束后回滚
        ArgumentCaptor<TransactionDefinition> definitionCaptor = ArgumentCaptor.forClass(TransactionDefinition.class);
        verify(transactionManager).getTransaction(definitionCaptor.capture());
        assertEquals(TransactionDefinition.PROPAGATION_REQUIRES_NEW, definitionCaptor.getValue().getPropagationBehavior());
        assertTrue(definitionCaptor.getValue().isReadOnly());
        assertEquals(TransactionDefinition.TIMEOUT_DEFAULT, definitionCaptor.getValue().getTimeout());
        assertTrue(transactionStatus.isRollbackOnly());
        // 断言：先强制只读，再执行 Druid 输出的 SQL，最后复原 SQL_SELECT_LIMIT；查询、复原语句均单独设置超时
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        InOrder inOrder = inOrder(transactionManager, jdbcTemplate, preparedStatement, resetStatement);
        inOrder.verify(transactionManager).getTransaction(any());
        inOrder.verify(jdbcTemplate).execute(START_TRANSACTION_READ_ONLY_SQL);
        inOrder.verify(jdbcTemplate).execute(sqlCaptor.capture(), any(PreparedStatementCallback.class));
        inOrder.verify(preparedStatement).setQueryTimeout(QUERY_TIMEOUT_SECONDS);
        inOrder.verify(preparedStatement).setMaxRows(MAX_ROWS + 1);
        inOrder.verify(preparedStatement).executeQuery();
        inOrder.verify(jdbcTemplate).execute(any(StatementCallback.class));
        inOrder.verify(resetStatement).setQueryTimeout(QUERY_TIMEOUT_SECONDS);
        inOrder.verify(resetStatement).execute(RESET_SELECT_LIMIT_SQL);
        inOrder.verify(transactionManager).commit(transactionStatus);
        assertEquals(validateSql(sql, DbType.mysql), sqlCaptor.getValue());
    }

    @Test
    public void testGetDataBySQL_mariaDB() throws SQLException {
        // mock 方法
        mockTransaction("MariaDB");
        mockQuery("SELECT 1 AS \"id\"");
        mockResetSelectLimit();

        // 调用
        GoViewDataRespVO respVO = goViewDataService.getDataBySQL("SELECT id FROM system_users");
        // 断言：与 MySQL 一致，强制只读，并复原 SQL_SELECT_LIMIT
        assertEquals(1, respVO.getSource().size());
        verify(jdbcTemplate).execute(START_TRANSACTION_READ_ONLY_SQL);
        verify(resetStatement).execute(RESET_SELECT_LIMIT_SQL);
    }

    @Test
    public void testGetDataBySQL_postgreSQL() throws SQLException {
        // mock 方法
        mockTransaction("PostgreSQL");
        mockQuery("SELECT 1 AS \"id\"");

        // 调用
        GoViewDataRespVO respVO = goViewDataService.getDataBySQL("SELECT id FROM system_users");
        // 断言：强制只读；PostgreSQL 的 maxRows 不使用会话变量，无需复原
        assertEquals(Collections.singletonList("id"), respVO.getDimensions());
        assertEquals(1, respVO.getSource().size());
        verify(jdbcTemplate).execute(SET_TRANSACTION_READ_ONLY_SQL);
        verify(jdbcTemplate, never()).execute(any(StatementCallback.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetDataBySQL_queryAndResetFailed() throws SQLException {
        // mock 方法：查询失败，复原也失败
        QueryTimeoutException queryFailure = new QueryTimeoutException("queryFailure");
        DataAccessResourceFailureException resetFailure = new DataAccessResourceFailureException("resetFailure");
        mockTransaction("MySQL");
        doNothing().when(jdbcTemplate).execute(START_TRANSACTION_READ_ONLY_SQL);
        when(jdbcTemplate.execute(anyString(), any(PreparedStatementCallback.class))).thenThrow(queryFailure);
        when(jdbcTemplate.execute(any(StatementCallback.class))).thenThrow(resetFailure);

        // 调用，并断言：抛出原始的查询异常，复原的异常作为 suppressed
        QueryTimeoutException exception = assertThrows(QueryTimeoutException.class,
                () -> goViewDataService.getDataBySQL("SELECT id FROM system_users"));
        assertSame(queryFailure, exception);
        assertArrayEquals(new Throwable[]{resetFailure}, exception.getSuppressed());
        verify(transactionManager).rollback(transactionStatus);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetDataBySQL_queryError() throws SQLException {
        // mock 方法：查询以 Error 退出
        AssertionError queryError = new AssertionError("queryError");
        mockTransaction("MySQL");
        doNothing().when(jdbcTemplate).execute(START_TRANSACTION_READ_ONLY_SQL);
        when(jdbcTemplate.execute(anyString(), any(PreparedStatementCallback.class))).thenThrow(queryError);
        mockResetSelectLimit();

        // 调用，并断言：抛出原始的 Error
        AssertionError exception = assertThrows(AssertionError.class,
                () -> goViewDataService.getDataBySQL("SELECT id FROM system_users"));
        assertSame(queryError, exception);
        // 断言：仍复原 SQL_SELECT_LIMIT，并回滚
        InOrder inOrder = inOrder(resetStatement, transactionManager);
        inOrder.verify(resetStatement).setQueryTimeout(QUERY_TIMEOUT_SECONDS);
        inOrder.verify(resetStatement).execute(RESET_SELECT_LIMIT_SQL);
        inOrder.verify(transactionManager).rollback(transactionStatus);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetDataBySQL_resetFailed() throws SQLException {
        // mock 方法：查询成功，复原失败
        DataAccessResourceFailureException resetFailure = new DataAccessResourceFailureException("resetFailure");
        mockTransaction("MySQL");
        mockQuery("SELECT 1 AS \"id\"");
        when(jdbcTemplate.execute(any(StatementCallback.class))).thenThrow(resetFailure);

        // 调用，并断言：仍然抛出复原的异常，不返回结果
        DataAccessResourceFailureException exception = assertThrows(DataAccessResourceFailureException.class,
                () -> goViewDataService.getDataBySQL("SELECT id FROM system_users"));
        assertSame(resetFailure, exception);
        verify(transactionManager).rollback(transactionStatus);
    }

    @Test
    public void testGetDataBySQL_maxRows() throws SQLException {
        // mock 方法
        mockTransaction("MySQL");
        mockQuery("SELECT X AS \"id\" FROM SYSTEM_RANGE(1, " + MAX_ROWS + ")");
        mockResetSelectLimit();

        // 调用
        GoViewDataRespVO respVO = goViewDataService.getDataBySQL("SELECT id FROM system_users");
        // 断言：达到最大行数时，完整返回
        assertEquals(MAX_ROWS, respVO.getSource().size());
        verify(resetStatement).execute(RESET_SELECT_LIMIT_SQL);
    }

    @Test
    public void testGetDataBySQL_rowsExceed() throws SQLException {
        // mock 方法
        mockTransaction("MySQL");
        mockQuery("SELECT X AS \"id\" FROM SYSTEM_RANGE(1, " + (MAX_ROWS + 1) + ")");
        mockResetSelectLimit();

        // 调用，并断言：超过最大行数时报错，不截断返回
        assertServiceException(() -> goViewDataService.getDataBySQL("SELECT id FROM system_users"),
                GO_VIEW_SQL_ROWS_EXCEED, MAX_ROWS);
        // 断言：报错时，也复原 SQL_SELECT_LIMIT，并回滚
        verify(resetStatement).execute(RESET_SELECT_LIMIT_SQL);
        verify(transactionManager).rollback(transactionStatus);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Oracle", "Microsoft SQL Server", "DM DBMS", "KingbaseES", "H2"})
    public void testGetDataBySQL_dbNotSupported(String productName) throws SQLException {
        // mock 方法
        mockTransaction(productName);

        // 调用，并断言：不支持
        assertServiceException(() -> goViewDataService.getDataBySQL("SELECT 1"), GO_VIEW_SQL_DB_NOT_SUPPORTED, productName);
        // 断言：不执行任何语句，并回滚
        verifyNoStatementExecuted();
        verify(transactionManager).rollback(transactionStatus);
    }

    @Test
    public void testGetDataBySQL_invalidSql() throws SQLException {
        // mock 方法
        mockTransaction("MySQL");

        // 调用，并断言：校验不通过
        assertInvalidSql(() -> goViewDataService.getDataBySQL("DELETE FROM system_users"));
        // 断言：不执行任何语句，并回滚
        verifyNoStatementExecuted();
        verify(transactionManager).rollback(transactionStatus);
    }

    @Test
    public void testGetDataBySQL_dsTransactional() {
        // 准备参数：在 dynamic-datasource 的本地事务中
        TransactionContext.bind("test-xid");

        // 调用，并断言：拒绝，且不开启事务
        assertServiceException(() -> goViewDataService.getDataBySQL("SELECT 1"), GO_VIEW_SQL_TRANSACTION_NOT_SUPPORTED);
        verifyNoInteractions(transactionManager, jdbcTemplate);
    }

    // ========== validateSql ==========

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT id, nickname FROM system_users WHERE deleted = 0 ORDER BY id LIMIT 10 OFFSET 20",
            "SELECT u.id, d.name FROM system_users u LEFT JOIN system_dept d ON u.dept_id = d.id WHERE 1 = 1",
            "SELECT status, COUNT(*) AS total FROM system_users GROUP BY status HAVING COUNT(*) > 0",
            "SELECT DATE_FORMAT(create_time, '%Y-%m') AS ym, IFNULL(SUM(pay_price), 0) FROM trade_order GROUP BY ym",
            "SELECT id FROM system_users WHERE id IN (SELECT user_id FROM system_user_role)",
            "SELECT 1 AS id UNION ALL SELECT 2",
            "WITH t(id) AS (SELECT 1) SELECT id FROM t",
            "WITH RECURSIVE t(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM t WHERE n < 3) SELECT n FROM t",
            "SELECT table_name FROM information_schema.tables",
            "SELECT id FROM system_users LIMIT 0",
            "SELECT SUM(pay_price) OVER (PARTITION BY user_id ORDER BY id ROWS BETWEEN 1 PRECEDING AND CURRENT ROW) FROM trade_order",
            // 字符串中的注释、提示、赋值、函数名；列名恰好等于函数名
            "SELECT id FROM system_users WHERE name = '/*+ SET_VAR(sql_select_limit = 1) */'",
            "SELECT id FROM system_users WHERE name = ':=' OR remark = 'SLEEP(1)'",
            "SELECT t.sleep, get_lock FROM system_users t ORDER BY sleep"
    })
    public void testValidateSql_mysqlSuccess(String sql) {
        // 调用
        String executeSql = validateSql(sql, DbType.mysql);
        // 断言
        assertTrue(StrUtil.isNotBlank(executeSql));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // 写语句、非 SELECT 语句、多语句
            "DELETE FROM system_users",
            "UPDATE system_users SET status = 1",
            "INSERT INTO system_users (id) VALUES (1)",
            "REPLACE INTO system_users (id) VALUES (1)",
            "SELECT 1; DELETE FROM system_users",
            "SELECT 1; SELECT 2",
            "EXPLAIN SELECT id FROM system_users",
            "SHOW TABLES",
            "CALL test_procedure()",
            "SET autocommit = 1",
            "",
            "SELEC id FROM system_users",
            // 注释：Druid 可能保留在输出中，统一拒绝
            "SELECT id FROM system_users -- comment",
            "SELECT id FROM system_users /* comment */",
            "SELECT id /* where */ FROM system_users",
            // 用户变量赋值：回滚后仍保留在连接上；包括嵌套
            "SELECT @goview_probe := 'changed' AS probe_value",
            "SELECT id FROM system_users WHERE (@goview_probe := id) > 0",
            "SELECT (SELECT @goview_probe := 1) AS probe_value",
            // 提示：包括 MySQL 的可执行注释、查询块（含子查询、UNION）上的提示
            "SELECT id FROM system_users /*! , SLEEP(1) */",
            "SELECT /*! SQL_NO_CACHE */ id FROM system_users",
            "SELECT /*+ MAX_EXECUTION_TIME(1) */ id FROM system_users",
            "SELECT /*+ SET_VAR(sql_select_limit = 1) */ id FROM system_users",
            "SELECT id FROM (SELECT /*+ SET_VAR(sql_select_limit = 1) */ id FROM system_users) t",
            "SELECT id FROM system_users UNION ALL SELECT /*+ SET_VAR(sql_select_limit = 1) */ id FROM system_users",
            // INTO：包括顶层 FROM 系统库时
            "SELECT id INTO @id FROM system_users",
            "SELECT id FROM system_users INTO OUTFILE '/tmp/users.txt'",
            "SELECT table_name FROM information_schema.tables INTO OUTFILE '/tmp/tables.txt'",
            // 禁止的函数：所有位置，包括引号
            "SELECT SLEEP(1)",
            "SELECT `sleep`(1)",
            "SELECT id FROM system_users WHERE SLEEP(1) = 0",
            "SELECT COUNT(SLEEP(1)) FROM system_users",
            "SELECT id FROM (SELECT SLEEP(1) AS id) t",
            "SELECT table_name, SLEEP(1) FROM information_schema.tables",
            "SELECT BENCHMARK(1000000, MD5('a'))",
            "SELECT LOAD_FILE('/etc/passwd')",
            "SELECT GET_LOCK('lock', 10)",
            "SELECT RELEASE_ALL_LOCKS()",
            "SELECT LAST_INSERT_ID(987654321)",
            "SELECT `last_insert_id`(1)",
            "SELECT id FROM system_users UNION ALL SELECT GET_LOCK('lock', 10)",
            "SELECT `get_lock`('lock', 10)",
            "SELECT test.SLEEP(1)",
            "SELECT SUM(1) OVER (ROWS BETWEEN SLEEP(1) PRECEDING AND CURRENT ROW) FROM system_users"
    })
    public void testValidateSql_mysqlInvalid(String sql) {
        // 调用，并断言
        assertInvalidSql(() -> validateSql(sql, DbType.mysql));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT id, nickname FROM system_users WHERE deleted = 0 ORDER BY id LIMIT 10 OFFSET 20",
            "SELECT to_char(create_time, 'YYYY-MM') AS ym, COUNT(*) FROM system_users GROUP BY 1",
            "SELECT id::text FROM system_users",
            "WITH RECURSIVE t(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM t WHERE n < 3) SELECT n FROM t",
            "SELECT sum(1) OVER (ROWS BETWEEN 1 PRECEDING AND CURRENT ROW)",
            // 字符串中的函数名、赋值；列名恰好等于函数名
            "SELECT id AS pg_sleep, 'pg_advisory_lock(1)' AS remark, ':=' AS op FROM system_users"
    })
    public void testValidateSql_postgreSQLSuccess(String sql) {
        // 调用
        String executeSql = validateSql(sql, DbType.postgresql);
        // 断言
        assertTrue(StrUtil.isNotBlank(executeSql));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "WITH t AS (DELETE FROM system_users RETURNING id) SELECT id FROM t",
            "SELECT id FROM system_users; SELECT 1",
            "SELECT pg_sleep(1)",
            "SELECT pg_catalog.pg_sleep(1)",
            "SELECT \"pg_sleep\"(1)",
            "SELECT * FROM pg_sleep(1)",
            "SELECT pg_advisory_lock(1)",
            "SELECT * FROM pg_try_advisory_lock(1)",
            "SELECT set_config('default_transaction_read_only', 'off', false)",
            "SELECT dblink_exec('dbname=test', 'DELETE FROM system_users')",
            "SELECT query_to_xml('DELETE FROM system_users', true, true, '')",
            "SELECT pg_terminate_backend(1)",
            // 标准扩展的文件写入、远程连接与游标操作，也有事务外副作用
            "SELECT pg_file_write('/tmp/probe', 'content', false)",
            "SELECT pg_file_unlink('/tmp/probe')",
            "SELECT pg_file_rename('/tmp/probe', '/tmp/renamed')",
            "SELECT dblink_connect_u('c', 'dbname=test')",
            "SELECT dblink_open('c', 'SELECT 1')",
            "SELECT dblink_fetch('c', 1)",
            "SELECT public.\"dblink_close\"('c')",
            "SELECT dblink_disconnect('c')",
            // 窗口 frame 的起止表达式、UNION 右侧、加引号的 schema 限定
            "SELECT sum(1) OVER (ROWS BETWEEN pg_try_advisory_lock(987654321)::integer PRECEDING AND CURRENT ROW)",
            "SELECT sum(1) OVER (ROWS BETWEEN CAST(pg_try_advisory_lock(987654321) AS integer) PRECEDING AND CURRENT ROW)",
            "SELECT id FROM system_users UNION ALL SELECT pg_advisory_lock(1)::integer",
            "SELECT \"pg_catalog\".\"pg_advisory_lock\"(1)"
    })
    public void testValidateSql_postgreSQLInvalid(String sql) {
        // 调用，并断言
        assertInvalidSql(() -> validateSql(sql, DbType.postgresql));
    }

    @Test
    public void testValidateSql_maxLength() {
        // 准备参数：字符串字面量使 SQL 总长恰好达到上限
        String sql = "SELECT '" + StrUtil.repeat('a', MAX_SQL_LENGTH - "SELECT ''".length()) + "'";

        // 调用，并断言：两种数据库语法均允许达到上限的 SQL
        assertDoesNotThrow(() -> validateSql(sql, DbType.mysql));
        assertDoesNotThrow(() -> validateSql(sql, DbType.postgresql));
    }

    @Test
    public void testValidateSql_tooLong() {
        // 准备参数
        String sql = "SELECT '" + StrUtil.repeat('a', MAX_SQL_LENGTH - "SELECT ''".length() + 1) + "'";

        // 调用，并断言
        assertServiceException(() -> validateSql(sql, DbType.mysql), GO_VIEW_SQL_INVALID, "长度不能超过 " + MAX_SQL_LENGTH);
    }

    @SuppressWarnings("unchecked")
    private void mockTransaction(String productName) throws SQLException {
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseProductName()).thenReturn(productName);
        when(jdbcTemplate.execute(any(ConnectionCallback.class))).thenAnswer(invocation ->
                ((ConnectionCallback<?>) invocation.getArgument(0)).doInConnection(connection));
    }

    /**
     * mock 强制只读、查询的执行，结果来自 H2 执行的 h2Sql
     *
     * stub 强制只读语句的 execute(String)，查询回调使用另一个重载
     */
    @SuppressWarnings("unchecked")
    private void mockQuery(String h2Sql) throws SQLException {
        doNothing().when(jdbcTemplate).execute(anyString());
        when(preparedStatement.executeQuery()).thenAnswer(invocation -> h2Connection.createStatement().executeQuery(h2Sql));
        when(jdbcTemplate.execute(anyString(), any(PreparedStatementCallback.class))).thenAnswer(invocation ->
                ((PreparedStatementCallback<?>) invocation.getArgument(1)).doInPreparedStatement(preparedStatement));
    }

    /**
     * mock 复原 SQL_SELECT_LIMIT（MySQL、MariaDB）的执行，在 {@link #resetStatement} 上执行
     */
    @SuppressWarnings("unchecked")
    private void mockResetSelectLimit() {
        when(jdbcTemplate.execute(any(StatementCallback.class))).thenAnswer(invocation ->
                ((StatementCallback<?>) invocation.getArgument(0)).doInStatement(resetStatement));
    }

    @SuppressWarnings("unchecked")
    private void verifyNoStatementExecuted() {
        verify(jdbcTemplate, never()).execute(anyString());
        verify(jdbcTemplate, never()).execute(anyString(), any(PreparedStatementCallback.class));
        verify(jdbcTemplate, never()).execute(any(StatementCallback.class));
    }

    /**
     * 断言 SQL 不合法：提示来自 Druid 的违规信息，只校验错误码
     */
    private static void assertInvalidSql(Executable executable) {
        ServiceException exception = assertThrows(ServiceException.class, executable);
        assertEquals(GO_VIEW_SQL_INVALID.getCode(), exception.getCode());
    }

}
