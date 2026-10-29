package cn.iocoder.yudao.framework.mybatis.core.mapper;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.DialectFactory;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link BaseMapperX} 限量查询的单元测试
 *
 * 不连接数据库：通过 JDK 动态代理执行 default 方法，记录传给 MyBatis Plus 的分页参数
 *
 * @author 芋道源码
 */
public class BaseMapperXTest {

    @Test
    public void testSelectListLimitN_limit() {
        FakeMapper fake = new FakeMapper(List.of("a", "b"));
        QueryWrapper<String> wrapper = new QueryWrapper<String>()
                .select("id", "name")
                .eq("status", 1)
                .orderByDesc("id");
        String sqlSegment = wrapper.getSqlSegment();

        // 调用
        List<String> list = fake.mapper.selectListLimitN(wrapper, 5);
        // 断言：第 1 页、每页 5 条、不执行 COUNT、不追加排序
        assertEquals(List.of("a", "b"), list);
        assertEquals(1, fake.invocations);
        assertEquals(1, fake.page.getCurrent());
        assertEquals(5, fake.page.getSize());
        assertFalse(fake.page.searchCount());
        assertTrue(fake.page.orders().isEmpty());
        // 断言：条件、排序、投影均不变，行数限制交给分页插件生成
        assertSame(wrapper, fake.wrapper);
        assertEquals(sqlSegment, wrapper.getSqlSegment());
        assertEquals("id,name", wrapper.getSqlSelect());
        assertFalse(wrapper.getSqlSegment().toUpperCase().contains("LIMIT"));
    }

    @Test
    public void testSelectListLimitN_zero() {
        FakeMapper fake = new FakeMapper(List.of("a"));

        // 调用，并断言：不查询，避免分页插件按不分页处理而查询全表
        List<String> list = fake.mapper.selectListLimitN(new QueryWrapper<String>().orderByDesc("id"), 0);
        assertTrue(list.isEmpty());
        assertEquals(0, fake.invocations);
    }

    @Test
    public void testSelectListLimitN_negative() {
        FakeMapper fake = new FakeMapper(List.of("a"));

        // 调用，并断言：分页插件会把负数的 size 视为不分页，因此直接拒绝
        assertThrows(IllegalArgumentException.class,
                () -> fake.mapper.selectListLimitN(new QueryWrapper<String>().orderByDesc("id"), -1));
        assertEquals(0, fake.invocations);
    }

    @Test
    public void testSelectFirstOne() {
        FakeMapper fake = new FakeMapper(List.of("a", "b"));
        QueryWrapper<String> wrapper = new QueryWrapper<String>().eq("status", 1).orderByAsc("id");

        // 调用，并断言：只取 1 条，返回第 1 条
        assertEquals("a", fake.mapper.selectFirstOne(wrapper));
        assertEquals(1, fake.invocations);
        assertEquals(1, fake.page.getCurrent());
        assertEquals(1, fake.page.getSize());
        assertFalse(fake.page.searchCount());
        assertSame(wrapper, fake.wrapper);
    }

    @Test
    public void testSelectFirstOne_empty() {
        FakeMapper fake = new FakeMapper(Collections.emptyList());

        // 调用，并断言：不存在时返回 null
        assertNull(fake.mapper.selectFirstOne(new QueryWrapper<String>().orderByAsc("id")));
        assertEquals(1, fake.invocations);
    }

    /**
     * 分页插件按执行连接识别的数据库类型生成的分页语句（第 1 页的 offset 为 0），均可被 JSqlParser 解析
     *
     * 注意：SQL Server 的 OFFSET FETCH 要求原语句有 ORDER BY，因此调用方必须设置排序
     */
    @Test
    public void testPaginationDialect() throws Exception {
        String sql = "SELECT id, name FROM system_users WHERE status = ? ORDER BY id DESC";

        // MySQL、OSCAR 神通等：LIMIT
        assertEquals(sql + " LIMIT ?", paginate(DbType.MYSQL, sql));
        assertEquals(sql + " LIMIT ?", paginate(DbType.OSCAR, sql));
        // PostgreSQL、H2、人大金仓、openGauss、瀚高等：LIMIT
        assertEquals(sql + " LIMIT ?", paginate(DbType.POSTGRE_SQL, sql));
        assertEquals(sql + " LIMIT ?", paginate(DbType.H2, sql));
        assertEquals(sql + " LIMIT ?", paginate(DbType.KINGBASE_ES, sql));
        assertEquals(sql + " LIMIT ?", paginate(DbType.OPENGAUSS, sql));
        assertEquals(sql + " LIMIT ?", paginate(DbType.HIGH_GO, sql));
        // Oracle、达梦：ROWNUM 包裹原语句，先排序后取行，兼容 Oracle 11g
        String oracle = "SELECT * FROM ( SELECT TMP.*, ROWNUM ROW_ID FROM ( " + sql
                + " ) TMP WHERE ROWNUM <=?) WHERE ROW_ID > ?";
        assertEquals(oracle, paginate(DbType.ORACLE, sql));
        assertEquals(oracle, paginate(DbType.DM, sql));
        // SQL Server 2012+：OFFSET FETCH 直接追加在 ORDER BY 之后
        assertEquals(sql + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY", paginate(DbType.SQL_SERVER, sql));

        for (DbType dbType : List.of(DbType.MYSQL, DbType.POSTGRE_SQL, DbType.ORACLE, DbType.SQL_SERVER)) {
            assertNotNull(CCJSqlParserUtil.parse(paginate(dbType, sql)), dbType.name());
        }
    }

    private static String paginate(DbType dbType, String sql) {
        return DialectFactory.getDialect(dbType).buildPaginationSql(sql, 0, 5).getDialectSql();
    }

    /**
     * 基于 JDK 动态代理的 {@link BaseMapperX}：default 方法真实执行，selectList(IPage, Wrapper) 记录参数并返回固定结果
     *
     * @author 芋道源码
     */
    private static class FakeMapper implements InvocationHandler {

        private final List<String> records;
        private final BaseMapperX<String> mapper;
        private IPage<?> page;
        private Wrapper<?> wrapper;
        private int invocations;

        @SuppressWarnings("unchecked")
        FakeMapper(List<String> records) {
            this.records = records;
            this.mapper = (BaseMapperX<String>) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[]{BaseMapperX.class}, this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.isDefault()) {
                return InvocationHandler.invokeDefault(proxy, method, args);
            }
            if ("selectList".equals(method.getName()) && args != null && args.length == 2
                    && args[0] instanceof IPage && args[1] instanceof Wrapper) {
                invocations++;
                page = (IPage<?>) args[0];
                wrapper = (Wrapper<?>) args[1];
                return records;
            }
            throw new UnsupportedOperationException(method.getName());
        }

    }

}
