package cn.iocoder.yudao.framework.mybatis.core.interceptor;

import cn.iocoder.yudao.framework.mybatis.core.injector.SelectForUpdateMethod;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.injector.methods.SelectList;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import lombok.Data;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link ForUpdateInnerInterceptor} 的单元测试
 *
 * 使用 {@link SelectForUpdateMethod} 实际注入的 MappedStatement 生成 BoundSql，连接为桩对象
 *
 * @author 芋道源码
 */
public class ForUpdateInnerInterceptorTest {

    private static final String SQL_SERVER = "Microsoft SQL Server";
    private static final String MYSQL = "MySQL";
    private static final String ORACLE = "Oracle";

    private final ForUpdateInnerInterceptor interceptor = new ForUpdateInnerInterceptor();

    private MybatisConfiguration configuration;

    @BeforeEach
    public void setUp() {
        configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        assistant.setCurrentNamespace(LockMapper.class.getName());
        TableInfo tableInfo = TableInfoHelper.initTableInfo(assistant, LockDO.class);
        new SelectList().inject(assistant, LockMapper.class, LockDO.class, tableInfo);
        for (SelectForUpdateMethod.Kind kind : SelectForUpdateMethod.Kind.values()) {
            new SelectForUpdateMethod(kind).inject(assistant, LockMapper.class, LockDO.class, tableInfo);
        }
    }

    // ========== 内置查询 ==========

    @Test
    public void testMappedStatement() {
        for (SelectForUpdateMethod.Kind kind : SelectForUpdateMethod.Kind.values()) {
            MappedStatement ms = getMappedStatement(kind.getMethod());
            // 断言：SqlSource 标记、缓存
            SelectForUpdateMethod.LockSqlSource sqlSource = assertInstanceOf(SelectForUpdateMethod.LockSqlSource.class, ms.getSqlSource());
            assertSame(kind, sqlSource.getKind());
            assertTrue(ms.isFlushCacheRequired());
            assertFalse(ms.isUseCache());
            // 断言：加锁查询返回实体，主键查询返回主键类型
            Class<?> resultType = ms.getResultMaps().get(0).getType();
            assertSame(kind == SelectForUpdateMethod.Kind.FIRST_IDS ? Long.class : LockDO.class, resultType);
        }
    }

    @Test
    public void testBoundSql() {
        // 准备参数：OR 条件
        LambdaQueryWrapper<LockDO> wrapper = new LambdaQueryWrapper<LockDO>()
                .eq(LockDO::getName, "a").or().eq(LockDO::getStatus, 1);

        // 调用、断言：加锁查询，OR 条件整体加括号后，再限制主键与逻辑删除；未设置排序时不排序
        String lock = normalize(getBoundSql(SelectForUpdateMethod.Kind.LOCK, wrapper, 1L).getSql());
        assertTrue(Pattern.compile("WHERE \\(\\s*\\(name = \\? OR status = \\?\\)\\s*\\) AND id = \\? AND deleted\\s*=\\s*0 FOR UPDATE$")
                .matcher(lock).find(), lock);
        assertFalse(lock.contains("ORDER BY"), lock);
        // 调用、断言：加锁查询，不传主键时不限制主键
        String lockWithoutId = normalize(getBoundSql(SelectForUpdateMethod.Kind.LOCK, wrapper, null).getSql());
        assertFalse(lockWithoutId.contains("id = ?"), lockWithoutId);
        // 调用、断言：首行加锁查询，未设置排序时不排序
        String lockFirst = normalize(getBoundSql(SelectForUpdateMethod.Kind.LOCK_FIRST, wrapper, null).getSql());
        assertTrue(lockFirst.endsWith("AND deleted=0 LIMIT 1 FOR UPDATE"), lockFirst);
        assertFalse(lockFirst.contains("ORDER BY"), lockFirst);
        // 调用、断言：主键查询，未设置排序时按主键排序，不加锁
        String firstIds = normalize(getBoundSql(SelectForUpdateMethod.Kind.FIRST_IDS, wrapper, null).getSql());
        assertTrue(firstIds.startsWith("SELECT id FROM infra_lock_demo"), firstIds);
        assertTrue(firstIds.endsWith("ORDER BY id"), firstIds);
        // 调用、断言：设置排序时使用 Wrapper 的排序
        wrapper.orderByDesc(LockDO::getStatus);
        assertTrue(normalize(getBoundSql(SelectForUpdateMethod.Kind.LOCK, wrapper, null).getSql())
                .endsWith("ORDER BY status DESC FOR UPDATE"));
        assertTrue(normalize(getBoundSql(SelectForUpdateMethod.Kind.FIRST_IDS, wrapper, null).getSql())
                .endsWith("ORDER BY status DESC"));
    }

    // ========== 拦截 ==========

    @Test
    public void testBeforePrepare_notLockStatement() {
        // 准备参数：普通查询，即使 SQL 包含 FOR UPDATE
        Map<String, Object> params = new HashMap<>();
        params.put("ew", new LambdaQueryWrapper<LockDO>().eq(LockDO::getName, "for update").last("FOR UPDATE"));
        MappedStatement ms = getMappedStatement("selectList");
        BoundSql boundSql = ms.getBoundSql(params);
        String sql = boundSql.getSql();
        Connection connection = mock(Connection.class);

        // 调用
        interceptor.beforePrepare(newHandler(ms, params, boundSql), connection, null);
        // 断言：SQL 不变，不访问连接
        assertSame(sql, boundSql.getSql());
        verifyNoInteractions(connection);
    }

    @Test
    public void testBeforePrepare_sqlServerLock() throws SQLException {
        // 准备参数
        BoundSql boundSql = beforePrepare(SelectForUpdateMethod.Kind.LOCK,
                new LambdaQueryWrapper<LockDO>().eq(LockDO::getName, "a").orderByAsc(LockDO::getId), 1L,
                mockConnection(SQL_SERVER, false, Connection.TRANSACTION_READ_COMMITTED));
        String original = getBoundSql(SelectForUpdateMethod.Kind.LOCK,
                new LambdaQueryWrapper<LockDO>().eq(LockDO::getName, "a").orderByAsc(LockDO::getId), 1L).getSql();

        // 断言：删除 FOR UPDATE，表之后增加提示
        assertEquals(normalize(original.replace("FROM infra_lock_demo", "FROM infra_lock_demo WITH (UPDLOCK, ROWLOCK)")
                .replace("FOR UPDATE", "")), normalize(boundSql.getSql()));
    }

    @Test
    public void testBeforePrepare_sqlServerLockFirst() throws SQLException {
        // 准备参数
        BoundSql boundSql = beforePrepare(SelectForUpdateMethod.Kind.LOCK_FIRST,
                new LambdaQueryWrapper<LockDO>().eq(LockDO::getName, "a").orderByAsc(LockDO::getId), null,
                mockConnection(SQL_SERVER, false, Connection.TRANSACTION_REPEATABLE_READ));
        String original = getBoundSql(SelectForUpdateMethod.Kind.LOCK_FIRST,
                new LambdaQueryWrapper<LockDO>().eq(LockDO::getName, "a").orderByAsc(LockDO::getId), null).getSql();

        // 断言：LIMIT 1 改为 TOP (1)，删除 FOR UPDATE，表之后增加提示
        assertEquals(normalize(original.replaceFirst("SELECT", "SELECT TOP (1)")
                .replace("FROM infra_lock_demo", "FROM infra_lock_demo WITH (UPDLOCK, ROWLOCK)")
                .replace("LIMIT 1 FOR UPDATE", "")), normalize(boundSql.getSql()));
    }

    @Test
    public void testBeforePrepare_sqlServerMarkerAfterCopy() throws SQLException {
        // 准备参数：与 MyBatis Plus Join 一样，复制 MappedStatement 时保留同一个 SqlSource
        MappedStatement origin = getMappedStatement(SelectForUpdateMethod.Kind.LOCK.getMethod());
        MappedStatement ms = new MappedStatement.Builder(configuration, origin.getId(), origin.getSqlSource(),
                origin.getSqlCommandType()).resultMaps(origin.getResultMaps()).build();
        Map<String, Object> params = buildParams(new LambdaQueryWrapper<LockDO>().eq(LockDO::getName, "a"), null);
        BoundSql boundSql = ms.getBoundSql(params);

        // 调用
        interceptor.beforePrepare(newHandler(ms, params, boundSql),
                mockConnection(SQL_SERVER, false, Connection.TRANSACTION_READ_COMMITTED), null);
        // 断言
        assertTrue(boundSql.getSql().contains("FROM infra_lock_demo WITH (UPDLOCK, ROWLOCK)"), boundSql.getSql());
        assertFalse(boundSql.getSql().contains("FOR UPDATE"), boundSql.getSql());
    }

    @Test
    public void testBeforePrepare_mysql() throws SQLException {
        LambdaQueryWrapper<LockDO> wrapper = new LambdaQueryWrapper<LockDO>().eq(LockDO::getName, "a");
        for (SelectForUpdateMethod.Kind kind : new SelectForUpdateMethod.Kind[]{
                SelectForUpdateMethod.Kind.LOCK, SelectForUpdateMethod.Kind.LOCK_FIRST}) {
            // 准备参数：MySQL 默认的 REPEATABLE READ
            Connection connection = mockConnection(MYSQL, false, Connection.TRANSACTION_REPEATABLE_READ);

            // 调用
            BoundSql boundSql = beforePrepare(kind, wrapper, null, connection);
            // 断言：SQL 不变，不校验隔离级别
            assertEquals(getBoundSql(kind, wrapper, null).getSql(), boundSql.getSql());
            verify(connection, never()).getTransactionIsolation();
        }
    }

    @Test
    public void testBeforePrepare_autoCommit() throws SQLException {
        LambdaQueryWrapper<LockDO> wrapper = new LambdaQueryWrapper<LockDO>().eq(LockDO::getName, "a");
        for (SelectForUpdateMethod.Kind kind : SelectForUpdateMethod.Kind.values()) {
            // 调用，并断言异常
            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> beforePrepare(kind, wrapper, null,
                    mockConnection(MYSQL, true, Connection.TRANSACTION_READ_COMMITTED)));
            assertTrue(ex.getMessage().contains("自动提交"));
        }
    }

    @Test
    public void testBeforePrepare_firstIds() throws SQLException {
        LambdaQueryWrapper<LockDO> wrapper = new LambdaQueryWrapper<LockDO>().eq(LockDO::getName, "a");

        // 调用，并断言异常：非 READ COMMITTED
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> beforePrepare(SelectForUpdateMethod.Kind.FIRST_IDS,
                wrapper, null, mockConnection(ORACLE, false, Connection.TRANSACTION_SERIALIZABLE)));
        assertTrue(ex.getMessage().contains("READ COMMITTED"));
        // 调用：READ COMMITTED
        BoundSql boundSql = beforePrepare(SelectForUpdateMethod.Kind.FIRST_IDS, wrapper, null,
                mockConnection(ORACLE, false, Connection.TRANSACTION_READ_COMMITTED));
        // 断言：SQL 不变
        assertEquals(getBoundSql(SelectForUpdateMethod.Kind.FIRST_IDS, wrapper, null).getSql(), boundSql.getSql());
    }

    @Test
    public void testBeforePrepare_oracle() throws SQLException {
        LambdaQueryWrapper<LockDO> wrapper = new LambdaQueryWrapper<LockDO>().eq(LockDO::getName, "a");

        // 调用：加锁查询，SQL 不变
        BoundSql boundSql = beforePrepare(SelectForUpdateMethod.Kind.LOCK, wrapper, 1L,
                mockConnection(ORACLE, false, Connection.TRANSACTION_READ_COMMITTED));
        assertEquals(getBoundSql(SelectForUpdateMethod.Kind.LOCK, wrapper, 1L).getSql(), boundSql.getSql());
        // 调用，并断言异常：首行加锁查询
        assertThrows(IllegalStateException.class, () -> beforePrepare(SelectForUpdateMethod.Kind.LOCK_FIRST, wrapper, null,
                mockConnection(ORACLE, false, Connection.TRANSACTION_READ_COMMITTED)));
    }

    // ========== SQL Server 改写 ==========

    @Test
    public void testRewriteForSqlServer() {
        // 字符串中的 for update 不改写
        assertEquals("SELECT id FROM t WITH (UPDLOCK, ROWLOCK) WHERE name = 'for update' ",
                ForUpdateInnerInterceptor.rewriteForSqlServer("SELECT id FROM t WHERE name = 'for update' FOR UPDATE", false));
        // 子查询的表不加提示
        assertEquals("SELECT id FROM a WITH (UPDLOCK, ROWLOCK) WHERE (b IN (SELECT id FROM c WHERE site_id = 1)) ORDER BY id ASC ",
                ForUpdateInnerInterceptor.rewriteForSqlServer(
                        "SELECT id FROM a WHERE (b IN (SELECT id FROM c WHERE site_id = 1)) ORDER BY id ASC FOR UPDATE", false));
        // 首行
        assertEquals("SELECT TOP (1) id FROM t WITH (UPDLOCK, ROWLOCK) WHERE (a = ?) ORDER BY id ASC  ",
                ForUpdateInnerInterceptor.rewriteForSqlServer("SELECT id FROM t WHERE (a = ?) ORDER BY id ASC LIMIT 1 FOR UPDATE", true));
    }

    @Test
    public void testRewriteForSqlServer_unsupported() {
        assertThrows(IllegalStateException.class, () -> ForUpdateInnerInterceptor.rewriteForSqlServer(
                "SELECT a.id FROM a JOIN b ON a.id = b.id FOR UPDATE", false));
        assertThrows(IllegalStateException.class, () -> ForUpdateInnerInterceptor.rewriteForSqlServer(
                "SELECT id FROM t FOR UPDATE NOWAIT", false));
        assertThrows(IllegalStateException.class, () -> ForUpdateInnerInterceptor.rewriteForSqlServer(
                "SELECT id FROM t", false));
        assertThrows(IllegalStateException.class, () -> ForUpdateInnerInterceptor.rewriteForSqlServer(
                "SELECT id FROM t LIMIT 1 FOR UPDATE", false));
        assertThrows(IllegalStateException.class, () -> ForUpdateInnerInterceptor.rewriteForSqlServer(
                "SELECT id FROM t LIMIT 2 FOR UPDATE", true));
        assertThrows(IllegalStateException.class, () -> ForUpdateInnerInterceptor.rewriteForSqlServer(
                "SELECT DISTINCT id FROM t LIMIT 1 FOR UPDATE", true));
    }

    // ========== 工具方法 ==========

    private BoundSql beforePrepare(SelectForUpdateMethod.Kind kind, LambdaQueryWrapper<LockDO> wrapper, Object id,
                                   Connection connection) {
        MappedStatement ms = getMappedStatement(kind.getMethod());
        Map<String, Object> params = buildParams(wrapper, id);
        BoundSql boundSql = ms.getBoundSql(params);
        interceptor.beforePrepare(newHandler(ms, params, boundSql), connection, null);
        return boundSql;
    }

    private BoundSql getBoundSql(SelectForUpdateMethod.Kind kind, LambdaQueryWrapper<LockDO> wrapper, Object id) {
        return getMappedStatement(kind.getMethod()).getBoundSql(buildParams(wrapper, id));
    }

    private static Map<String, Object> buildParams(LambdaQueryWrapper<LockDO> wrapper, Object id) {
        Map<String, Object> params = new HashMap<>();
        params.put("ew", wrapper);
        params.put(SelectForUpdateMethod.PARAM_ID, id);
        return params;
    }

    private MappedStatement getMappedStatement(String method) {
        return configuration.getMappedStatement(LockMapper.class.getName() + "." + method);
    }

    private static RoutingStatementHandler newHandler(MappedStatement ms, Object params, BoundSql boundSql) {
        return new RoutingStatementHandler(mock(Executor.class), ms, params, RowBounds.DEFAULT, null, boundSql);
    }

    private static Connection mockConnection(String productName, boolean autoCommit, int isolation) throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getDatabaseProductName()).thenReturn(productName);
        when(connection.getAutoCommit()).thenReturn(autoCommit);
        when(connection.getTransactionIsolation()).thenReturn(isolation);
        return connection;
    }

    private static String normalize(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }

    @Data
    @TableName("infra_lock_demo")
    public static class LockDO {

        @TableId
        private Long id;
        private String name;
        private Integer status;
        @TableLogic
        private Boolean deleted;

    }

    public interface LockMapper extends BaseMapper<LockDO> {
    }

}
