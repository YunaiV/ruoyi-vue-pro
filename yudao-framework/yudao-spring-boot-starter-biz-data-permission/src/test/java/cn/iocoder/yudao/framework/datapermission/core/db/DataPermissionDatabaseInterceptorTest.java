package cn.iocoder.yudao.framework.datapermission.core.db;

import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.framework.datapermission.core.rule.DataPermissionRule;
import cn.iocoder.yudao.framework.datapermission.core.rule.DataPermissionRuleFactory;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.util.*;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link DataPermissionDatabaseInterceptor} 的单元测试
 * 主要测试 {@link DataPermissionDatabaseInterceptor#beforePrepare(StatementHandler, Connection, Integer)}
 * 和 {@link DataPermissionDatabaseInterceptor#beforeUpdate(Executor, MappedStatement, Object)}
 * 以及在这个过程中，ContextHolder 和 MappedStatementCache
 *
 * @author 芋道源码
 */
public class DataPermissionDatabaseInterceptorTest extends BaseMockitoUnitTest {

    @InjectMocks
    private DataPermissionDatabaseInterceptor interceptor;

    @Mock
    private DataPermissionRuleFactory ruleFactory;

    @BeforeEach
    public void setUp() {
        // 清理上下文
        DataPermissionDatabaseInterceptor.ContextHolder.clear();
        // 清空缓存
        interceptor.getMappedStatementCache().clear();
    }

    @Test // 不存在规则，且不匹配
    public void testBeforeQuery_withoutRule() {
        try (MockedStatic<PluginUtils> pluginUtilsMock = mockStatic(PluginUtils.class)) {
            // 准备参数
            MappedStatement mappedStatement = mock(MappedStatement.class);
            BoundSql boundSql = mock(BoundSql.class);

            // 调用
            interceptor.beforeQuery(null, mappedStatement, null, null, null, boundSql);
            // 断言
            pluginUtilsMock.verify(() -> PluginUtils.mpBoundSql(boundSql), never());
        }
    }

    @Test // 存在规则，且不匹配
    public void testBeforeQuery_withMatchRule() {
        try (MockedStatic<PluginUtils> pluginUtilsMock = mockStatic(PluginUtils.class)) {
            // 准备参数
            MappedStatement mappedStatement = mock(MappedStatement.class);
            BoundSql boundSql = mock(BoundSql.class);
            // mock 方法(数据权限)
            when(ruleFactory.getDataPermissionRule(same(mappedStatement.getId())))
                    .thenReturn(singletonList(new DeptDataPermissionRule()));
            // mock 方法(MPBoundSql)
            PluginUtils.MPBoundSql mpBs = mock(PluginUtils.MPBoundSql.class);
            pluginUtilsMock.when(() -> PluginUtils.mpBoundSql(same(boundSql))).thenReturn(mpBs);
            // mock 方法(SQL)
            String sql = "select * from t_user where id = 1";
            when(mpBs.sql()).thenReturn(sql);
            // 针对 ContextHolder 和 MappedStatementCache 暂时不 mock，主要想校验过程中，数据是否正确

            // 调用
            interceptor.beforeQuery(null, mappedStatement, null, null, null, boundSql);
            // 断言
            verify(mpBs, times(1)).sql(
                    eq("SELECT * FROM t_user WHERE id = 1 AND dept_id = 100"));
            // 断言缓存
            assertTrue(interceptor.getMappedStatementCache().getNoRewritableMappedStatements().isEmpty());
        }
    }

    @Test // 存在规则，但不匹配
    public void testBeforeQuery_withoutMatchRule() {
        try (MockedStatic<PluginUtils> pluginUtilsMock = mockStatic(PluginUtils.class)) {
            // 准备参数
            MappedStatement mappedStatement = mock(MappedStatement.class);
            BoundSql boundSql = mock(BoundSql.class);
            // mock 方法(数据权限)
            when(ruleFactory.getDataPermissionRule(same(mappedStatement.getId())))
                    .thenReturn(singletonList(new DeptDataPermissionRule()));
            // mock 方法(MPBoundSql)
            PluginUtils.MPBoundSql mpBs = mock(PluginUtils.MPBoundSql.class);
            pluginUtilsMock.when(() -> PluginUtils.mpBoundSql(same(boundSql))).thenReturn(mpBs);
            // mock 方法(SQL)
            String sql = "select * from t_role where id = 1";
            when(mpBs.sql()).thenReturn(sql);
            // 针对 ContextHolder 和 MappedStatementCache 暂时不 mock，主要想校验过程中，数据是否正确

            // 调用
            interceptor.beforeQuery(null, mappedStatement, null, null, null, boundSql);
            // 断言
            verify(mpBs, times(1)).sql(
                    eq("SELECT * FROM t_role WHERE id = 1"));
            // 断言缓存
            assertFalse(interceptor.getMappedStatementCache().getNoRewritableMappedStatements().isEmpty());
        }
    }

    @Test
    public void testAddNoRewritable() {
        // 准备参数
        MappedStatement ms = mock(MappedStatement.class);
        List<DataPermissionRule> rules = singletonList(new DeptDataPermissionRule());
        // mock 方法
        when(ms.getId()).thenReturn("selectById");

        // 调用
        interceptor.getMappedStatementCache().addNoRewritable(ms, rules);
        // 断言
        Map<Class<? extends DataPermissionRule>, Set<String>> noRewritableMappedStatements =
                interceptor.getMappedStatementCache().getNoRewritableMappedStatements();
        assertEquals(1, noRewritableMappedStatements.size());
        assertEquals(SetUtils.asSet("selectById"), noRewritableMappedStatements.get(DeptDataPermissionRule.class));
    }

    @Test
    public void testNoRewritable() {
        // 准备参数
        MappedStatement ms = mock(MappedStatement.class);
        // mock 方法
        when(ms.getId()).thenReturn("selectById");
        // mock 数据
        List<DataPermissionRule> rules = singletonList(new DeptDataPermissionRule());
        interceptor.getMappedStatementCache().addNoRewritable(ms, rules);

        // 场景一，rules 为空
        assertTrue(interceptor.getMappedStatementCache().noRewritable(ms, null));
        // 场景二，rules 非空，可重写
        assertFalse(interceptor.getMappedStatementCache().noRewritable(ms, singletonList(new EmptyDataPermissionRule())));
        // 场景三，rule 非空，不可重写
        assertTrue(interceptor.getMappedStatementCache().noRewritable(ms, rules));
    }

    private static class DeptDataPermissionRule implements DataPermissionRule {

        private static final String COLUMN = "dept_id";

        @Override
        public Set<String> getTableNames() {
            return SetUtils.asSet("t_user");
        }

        @Override
        public Expression getExpression(String tableName, Alias tableAlias) {
            Column column = MyBatisUtils.buildColumn(tableName, tableAlias, COLUMN);
            LongValue value = new LongValue(100L);
            return new EqualsTo(column, value);
        }

    }

    private static class EmptyDataPermissionRule implements DataPermissionRule {

        @Override
        public Set<String> getTableNames() {
            return Collections.emptySet();
        }

        @Override
        public Expression getExpression(String tableName, Alias tableAlias) {
            return null;
        }

    }

}
