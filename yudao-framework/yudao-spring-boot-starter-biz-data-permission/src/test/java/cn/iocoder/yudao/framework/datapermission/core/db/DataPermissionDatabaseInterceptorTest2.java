package cn.iocoder.yudao.framework.datapermission.core.db;

import cn.iocoder.yudao.framework.datapermission.core.rule.DataPermissionRule;
import cn.iocoder.yudao.framework.datapermission.core.rule.DataPermissionRuleFactory;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link DataPermissionDatabaseInterceptor} 的单元测试
 * 主要复用了 MyBatis Plus 的 TenantLineInnerInterceptorTest 的单元测试
 * 不过它的单元测试不是很规范，考虑到是复用的，所以暂时不进行修改~
 *
 * @author 芋道源码
 */
public class DataPermissionDatabaseInterceptorTest2 extends BaseMockitoUnitTest {

    @InjectMocks
    private DataPermissionDatabaseInterceptor interceptor;

    @Mock
    private DataPermissionRuleFactory ruleFactory;

    @BeforeEach
    public void setUp() {
        // 租户的数据权限规则
        DataPermissionRule tenantRule = new DataPermissionRule() {

            private static final String COLUMN = "tenant_id";

            @Override
            public Set<String> getTableNames() {
                return asSet("entity", "entity1", "entity2", "entity3", "t1", "t2", "sys_dict_item", // 支持 MyBatis Plus 的单元测试
                        "t_user", "t_role"); // 满足自己的单元测试
            }

            @Override
            public Expression getExpression(String tableName, Alias tableAlias) {
                Column column = MyBatisUtils.buildColumn(tableName, tableAlias, COLUMN);
                LongValue value = new LongValue(1L);
                return new EqualsTo(column, value);
            }

        };
        // 部门的数据权限规则
        DataPermissionRule deptRule = new DataPermissionRule() {

            private static final String COLUMN = "dept_id";

            @Override
            public Set<String> getTableNames() {
                return asSet("t_user");  // 满足自己的单元测试
            }

            @Override
            public Expression getExpression(String tableName, Alias tableAlias) {
                Column column = MyBatisUtils.buildColumn(tableName, tableAlias, COLUMN);
                ExpressionList values = new ExpressionList(new LongValue(10L),
                        new LongValue(20L));
                return new InExpression(column, values);
            }

        };
        // 设置到上下文，保证
        DataPermissionDatabaseInterceptor.ContextHolder.init(Arrays.asList(tenantRule, deptRule));
    }

    @Test
    void delete() {
        assertSql("delete from entity where id = ?",
                "DELETE FROM entity WHERE id = ? AND entity.tenant_id = 1");
    }

    @Test
    void update() {
        assertSql("update entity set name = ? where id = ?",
                "UPDATE entity SET name = ? WHERE id = ? AND entity.tenant_id = 1");
    }

    @Test
    void selectSingle() {
        // 单表
        assertSql("select * from entity where id = ?",
                "SELECT * FROM entity WHERE id = ? AND entity.tenant_id = 1");

        assertSql("select * from entity where id = ? or name = ?",
                "SELECT * FROM entity WHERE (id = ? OR name = ?) AND entity.tenant_id = 1");

        assertSql("SELECT * FROM entity WHERE (id = ? OR name = ?)",
                "SELECT * FROM entity WHERE (id = ? OR name = ?) AND entity.tenant_id = 1");

        /* not */
        assertSql("SELECT * FROM entity WHERE not (id = ? OR name = ?)",
                "SELECT * FROM entity WHERE NOT (id = ? OR name = ?) AND entity.tenant_id = 1");
    }

    @Test
    void selectSubSelectIn() {
        /* in */
        assertSql("SELECT * FROM entity e WHERE e.id IN (select e1.id from entity1 e1 where e1.id = ?)",
                "SELECT * FROM entity e WHERE e.id IN (SELECT e1.id FROM entity1 e1 WHERE e1.id = ? AND e1.tenant_id = 1) AND e.tenant_id = 1");
        // 在最前
        assertSql("SELECT * FROM entity e WHERE e.id IN " +
                        "(select e1.id from entity1 e1 where e1.id = ?) and e.id = ?",
                "SELECT * FROM entity e WHERE e.id IN " +
                        "(SELECT e1.id FROM entity1 e1 WHERE e1.id = ? AND e1.tenant_id = 1) AND e.id = ? AND e.tenant_id = 1");
        // 在最后
        assertSql("SELECT * FROM entity e WHERE e.id = ? and e.id IN " +
                        "(select e1.id from entity1 e1 where e1.id = ?)",
                "SELECT * FROM entity e WHERE e.id = ? AND e.id IN " +
                        "(SELECT e1.id FROM entity1 e1 WHERE e1.id = ? AND e1.tenant_id = 1) AND e.tenant_id = 1");
        // 在中间
        assertSql("SELECT * FROM entity e WHERE e.id = ? and e.id IN " +
                        "(select e1.id from entity1 e1 where e1.id = ?) and e.id = ?",
                "SELECT * FROM entity e WHERE e.id = ? AND e.id IN " +
                        "(SELECT e1.id FROM entity1 e1 WHERE e1.id = ? AND e1.tenant_id = 1) AND e.id = ? AND e.tenant_id = 1");
    }

    @Test
    void selectSubSelectEq() {
        /* = */
        assertSql("SELECT * FROM entity e WHERE e.id = (select e1.id from entity1 e1 where e1.id = ?)",
                "SELECT * FROM entity e WHERE e.id = (SELECT e1.id FROM entity1 e1 WHERE e1.id = ? AND e1.tenant_id = 1) AND e.tenant_id = 1");
    }

    @Test
    void selectSubSelectInnerNotEq() {
        /* inner not = */
        assertSql("SELECT * FROM entity e WHERE not (e.id = (select e1.id from entity1 e1 where e1.id = ?))",
                "SELECT * FROM entity e WHERE NOT (e.id = (SELECT e1.id FROM entity1 e1 WHERE e1.id = ? AND e1.tenant_id = 1)) AND e.tenant_id = 1");

        assertSql("SELECT * FROM entity e WHERE not (e.id = (select e1.id from entity1 e1 where e1.id = ?) and e.id = ?)",
                "SELECT * FROM entity e WHERE NOT (e.id = (SELECT e1.id FROM entity1 e1 WHERE e1.id = ? AND e1.tenant_id = 1) AND e.id = ?) AND e.tenant_id = 1");
    }

    @Test
    void selectSubSelectExists() {
        /* EXISTS */
        assertSql("SELECT * FROM entity e WHERE EXISTS (select e1.id from entity1 e1 where e1.id = ?)",
                "SELECT * FROM entity e WHERE EXISTS (SELECT e1.id FROM entity1 e1 WHERE e1.id = ? AND e1.tenant_id = 1) AND e.tenant_id = 1");


        /* NOT EXISTS */
        assertSql("SELECT * FROM entity e WHERE NOT EXISTS (select e1.id from entity1 e1 where e1.id = ?)",
                "SELECT * FROM entity e WHERE NOT EXISTS (SELECT e1.id FROM entity1 e1 WHERE e1.id = ? AND e1.tenant_id = 1) AND e.tenant_id = 1");
    }

    @Test
    void selectSubSelect() {
        /* >= */
        assertSql("SELECT * FROM entity e WHERE e.id >= (select e1.id from entity1 e1 where e1.id = ?)",
                "SELECT * FROM entity e WHERE e.id >= (SELECT e1.id FROM entity1 e1 WHERE e1.id = ? AND e1.tenant_id = 1) AND e.tenant_id = 1");


        /* <= */
        assertSql("SELECT * FROM entity e WHERE e.id <= (select e1.id from entity1 e1 where e1.id = ?)",
                "SELECT * FROM entity e WHERE e.id <= (SELECT e1.id FROM entity1 e1 WHERE e1.id = ? AND e1.tenant_id = 1) AND e.tenant_id = 1");


        /* <> */
        assertSql("SELECT * FROM entity e WHERE e.id <> (select e1.id from entity1 e1 where e1.id = ?)",
                "SELECT * FROM entity e WHERE e.id <> (SELECT e1.id FROM entity1 e1 WHERE e1.id = ? AND e1.tenant_id = 1) AND e.tenant_id = 1");
    }

    @Test
    void selectFromSelect() {
        assertSql("SELECT * FROM (select e.id from entity e WHERE e.id = (select e1.id from entity1 e1 where e1.id = ?))",
                "SELECT * FROM (SELECT e.id FROM entity e WHERE e.id = (SELECT e1.id FROM entity1 e1 WHERE e1.id = ? AND e1.tenant_id = 1) AND e.tenant_id = 1)");
    }

    @Test
    void selectBodySubSelect() {
        assertSql("select t1.col1,(select t2.col2 from t2 t2 where t1.col1=t2.col1) from t1 t1",
                "SELECT t1.col1, (SELECT t2.col2 FROM t2 t2 WHERE t1.col1 = t2.col1 AND t2.tenant_id = 1) FROM t1 t1 WHERE t1.tenant_id = 1");
    }

    @Test
    void selectLeftJoin() {
        // left join
        assertSql("SELECT * FROM entity e " +
                        "left join entity1 e1 on e1.id = e.id " +
                        "WHERE e.id = ? OR e.name = ?",
                "SELECT * FROM entity e " +
                        "LEFT JOIN entity1 e1 ON e1.id = e.id AND e1.tenant_id = 1 " +
                        "WHERE (e.id = ? OR e.name = ?) AND e.tenant_id = 1");

        assertSql("SELECT * FROM entity e " +
                        "left join entity1 e1 on e1.id = e.id " +
                        "WHERE (e.id = ? OR e.name = ?)",
                "SELECT * FROM entity e " +
                        "LEFT JOIN entity1 e1 ON e1.id = e.id AND e1.tenant_id = 1 " +
                        "WHERE (e.id = ? OR e.name = ?) AND e.tenant_id = 1");

        assertSql("SELECT * FROM entity e " +
                        "left join entity1 e1 on e1.id = e.id " +
                        "left join entity2 e2 on e1.id = e2.id",
                "SELECT * FROM entity e " +
                        "LEFT JOIN entity1 e1 ON e1.id = e.id AND e1.tenant_id = 1 " +
                        "LEFT JOIN entity2 e2 ON e1.id = e2.id AND e2.tenant_id = 1 " +
                        "WHERE e.tenant_id = 1");
    }

    @Test
    void selectRightJoin() {
        // right join
        assertSql("SELECT * FROM entity e " +
                        "right join entity1 e1 on e1.id = e.id",
                "SELECT * FROM entity e " +
                        "RIGHT JOIN entity1 e1 ON e1.id = e.id AND e.tenant_id = 1 " +
                        "WHERE e1.tenant_id = 1");

        assertSql("SELECT * FROM with_as_1 e " +
                        "right join entity1 e1 on e1.id = e.id",
                "SELECT * FROM with_as_1 e " +
                        "RIGHT JOIN entity1 e1 ON e1.id = e.id " +
                        "WHERE e1.tenant_id = 1");

        assertSql("SELECT * FROM entity e " +
                        "right join entity1 e1 on e1.id = e.id " +
                        "WHERE e.id = ? OR e.name = ?",
                "SELECT * FROM entity e " +
                        "RIGHT JOIN entity1 e1 ON e1.id = e.id AND e.tenant_id = 1 " +
                        "WHERE (e.id = ? OR e.name = ?) AND e1.tenant_id = 1");

        assertSql("SELECT * FROM entity e " +
                        "right join entity1 e1 on e1.id = e.id " +
                        "right join entity2 e2 on e1.id = e2.id ",
                "SELECT * FROM entity e " +
                        "RIGHT JOIN entity1 e1 ON e1.id = e.id AND e.tenant_id = 1 " +
                        "RIGHT JOIN entity2 e2 ON e1.id = e2.id AND e1.tenant_id = 1 " +
                        "WHERE e2.tenant_id = 1");
    }

    @Test
    void selectMixJoin() {
        assertSql("SELECT * FROM entity e " +
                        "right join entity1 e1 on e1.id = e.id " +
                        "left join entity2 e2 on e1.id = e2.id",
                "SELECT * FROM entity e " +
                        "RIGHT JOIN entity1 e1 ON e1.id = e.id AND e.tenant_id = 1 " +
                        "LEFT JOIN entity2 e2 ON e1.id = e2.id AND e2.tenant_id = 1 " +
                        "WHERE e1.tenant_id = 1");

        assertSql("SELECT * FROM entity e " +
                        "left join entity1 e1 on e1.id = e.id " +
                        "right join entity2 e2 on e1.id = e2.id",
                "SELECT * FROM entity e " +
                        "LEFT JOIN entity1 e1 ON e1.id = e.id AND e1.tenant_id = 1 " +
                        "RIGHT JOIN entity2 e2 ON e1.id = e2.id AND e1.tenant_id = 1 " +
                        "WHERE e2.tenant_id = 1");

        assertSql("SELECT * FROM entity e " +
                        "left join entity1 e1 on e1.id = e.id " +
                        "inner join entity2 e2 on e1.id = e2.id",
                "SELECT * FROM entity e " +
                        "LEFT JOIN entity1 e1 ON e1.id = e.id AND e1.tenant_id = 1 " +
                        "INNER JOIN entity2 e2 ON e1.id = e2.id AND e.tenant_id = 1 AND e2.tenant_id = 1");
    }


    @Test
    void selectJoinSubSelect() {
        assertSql("select * from (select * from entity) e1 " +
                        "left join entity2 e2 on e1.id = e2.id",
                "SELECT * FROM (SELECT * FROM entity WHERE entity.tenant_id = 1) e1 " +
                        "LEFT JOIN entity2 e2 ON e1.id = e2.id AND e2.tenant_id = 1");

        assertSql("select * from entity1 e1 " +
                        "left join (select * from entity2) e2 " +
                        "on e1.id = e2.id",
                "SELECT * FROM entity1 e1 " +
                        "LEFT JOIN (SELECT * FROM entity2 WHERE entity2.tenant_id = 1) e2 " +
                        "ON e1.id = e2.id " +
                        "WHERE e1.tenant_id = 1");
    }

    @Test
    void selectSubJoin() {

        assertSql("select * FROM " +
                        "(entity1 e1 right JOIN entity2 e2 ON e1.id = e2.id)",
                "SELECT * FROM " +
                        "(entity1 e1 RIGHT JOIN entity2 e2 ON e1.id = e2.id AND e1.tenant_id = 1) " +
                        "WHERE e2.tenant_id = 1");

        assertSql("select * FROM " +
                        "(entity1 e1 LEFT JOIN entity2 e2 ON e1.id = e2.id)",
                "SELECT * FROM " +
                        "(entity1 e1 LEFT JOIN entity2 e2 ON e1.id = e2.id AND e2.tenant_id = 1) " +
                        "WHERE e1.tenant_id = 1");


        assertSql("select * FROM " +
                        "(entity1 e1 LEFT JOIN entity2 e2 ON e1.id = e2.id) " +
                        "right join entity3 e3 on e1.id = e3.id",
                "SELECT * FROM " +
                        "(entity1 e1 LEFT JOIN entity2 e2 ON e1.id = e2.id AND e2.tenant_id = 1) " +
                        "RIGHT JOIN entity3 e3 ON e1.id = e3.id AND e1.tenant_id = 1 " +
                        "WHERE e3.tenant_id = 1");


        assertSql("select * FROM entity e " +
                        "LEFT JOIN (entity1 e1 right join entity2 e2 ON e1.id = e2.id) " +
                        "on e.id = e2.id",
                "SELECT * FROM entity e " +
                        "LEFT JOIN (entity1 e1 RIGHT JOIN entity2 e2 ON e1.id = e2.id AND e1.tenant_id = 1) " +
                        "ON e.id = e2.id AND e2.tenant_id = 1 " +
                        "WHERE e.tenant_id = 1");

        assertSql("select * FROM entity e " +
                        "LEFT JOIN (entity1 e1 left join entity2 e2 ON e1.id = e2.id) " +
                        "on e.id = e2.id",
                "SELECT * FROM entity e " +
                        "LEFT JOIN (entity1 e1 LEFT JOIN entity2 e2 ON e1.id = e2.id AND e2.tenant_id = 1) " +
                        "ON e.id = e2.id AND e1.tenant_id = 1 " +
                        "WHERE e.tenant_id = 1");

        assertSql("select * FROM entity e " +
                        "RIGHT JOIN (entity1 e1 left join entity2 e2 ON e1.id = e2.id) " +
                        "on e.id = e2.id",
                "SELECT * FROM entity e " +
                        "RIGHT JOIN (entity1 e1 LEFT JOIN entity2 e2 ON e1.id = e2.id AND e2.tenant_id = 1) " +
                        "ON e.id = e2.id AND e.tenant_id = 1 " +
                        "WHERE e1.tenant_id = 1");
    }


    @Test
    void selectLeftJoinMultipleTrailingOn() {
        // 多个 on 尾缀的
        assertSql("SELECT * FROM entity e " +
                        "LEFT JOIN entity1 e1 " +
                        "LEFT JOIN entity2 e2 ON e2.id = e1.id " +
                        "ON e1.id = e.id " +
                        "WHERE (e.id = ? OR e.NAME = ?)",
                "SELECT * FROM entity e " +
                        "LEFT JOIN entity1 e1 " +
                        "LEFT JOIN entity2 e2 ON e2.id = e1.id AND e2.tenant_id = 1 " +
                        "ON e1.id = e.id AND e1.tenant_id = 1 " +
                        "WHERE (e.id = ? OR e.NAME = ?) AND e.tenant_id = 1");

        assertSql("SELECT * FROM entity e " +
                        "LEFT JOIN entity1 e1 " +
                        "LEFT JOIN with_as_A e2 ON e2.id = e1.id " +
                        "ON e1.id = e.id " +
                        "WHERE (e.id = ? OR e.NAME = ?)",
                "SELECT * FROM entity e " +
                        "LEFT JOIN entity1 e1 " +
                        "LEFT JOIN with_as_A e2 ON e2.id = e1.id " +
                        "ON e1.id = e.id AND e1.tenant_id = 1 " +
                        "WHERE (e.id = ? OR e.NAME = ?) AND e.tenant_id = 1");
    }

    @Test
    void selectInnerJoin() {
        // inner join
        assertSql("SELECT * FROM entity e " +
                        "inner join entity1 e1 on e1.id = e.id " +
                        "WHERE e.id = ? OR e.name = ?",
                "SELECT * FROM entity e " +
                        "INNER JOIN entity1 e1 ON e1.id = e.id AND e.tenant_id = 1 AND e1.tenant_id = 1 " +
                        "WHERE e.id = ? OR e.name = ?");

        assertSql("SELECT * FROM entity e " +
                        "inner join entity1 e1 on e1.id = e.id " +
                        "WHERE (e.id = ? OR e.name = ?)",
                "SELECT * FROM entity e " +
                        "INNER JOIN entity1 e1 ON e1.id = e.id AND e.tenant_id = 1 AND e1.tenant_id = 1 " +
                        "WHERE (e.id = ? OR e.name = ?)");

        // 隐式内连接
        assertSql("SELECT * FROM entity,entity1 " +
                        "WHERE entity.id = entity1.id",
                "SELECT * FROM entity, entity1 " +
                        "WHERE entity.id = entity1.id AND entity.tenant_id = 1 AND entity1.tenant_id = 1");

        // 隐式内连接
        assertSql("SELECT * FROM entity a, with_as_entity1 b " +
                        "WHERE a.id = b.id",
                "SELECT * FROM entity a, with_as_entity1 b " +
                        "WHERE a.id = b.id AND a.tenant_id = 1");

        assertSql("SELECT * FROM with_as_entity a, with_as_entity1 b " +
                        "WHERE a.id = b.id",
                "SELECT * FROM with_as_entity a, with_as_entity1 b " +
                        "WHERE a.id = b.id");

        // SubJoin with 隐式内连接
        assertSql("SELECT * FROM (entity,entity1) " +
                        "WHERE entity.id = entity1.id",
                "SELECT * FROM (entity, entity1) " +
                        "WHERE entity.id = entity1.id " +
                        "AND entity.tenant_id = 1 AND entity1.tenant_id = 1");

        assertSql("SELECT * FROM ((entity,entity1),entity2) " +
                        "WHERE entity.id = entity1.id and entity.id = entity2.id",
                "SELECT * FROM ((entity, entity1), entity2) " +
                        "WHERE entity.id = entity1.id AND entity.id = entity2.id " +
                        "AND entity.tenant_id = 1 AND entity1.tenant_id = 1 AND entity2.tenant_id = 1");

        assertSql("SELECT * FROM (entity,(entity1,entity2)) " +
                        "WHERE entity.id = entity1.id and entity.id = entity2.id",
                "SELECT * FROM (entity, (entity1, entity2)) " +
                        "WHERE entity.id = entity1.id AND entity.id = entity2.id " +
                        "AND entity.tenant_id = 1 AND entity1.tenant_id = 1 AND entity2.tenant_id = 1");

        // 沙雕的括号写法
        assertSql("SELECT * FROM (((entity,entity1))) " +
                        "WHERE entity.id = entity1.id",
                "SELECT * FROM (((entity, entity1))) " +
                        "WHERE entity.id = entity1.id " +
                        "AND entity.tenant_id = 1 AND entity1.tenant_id = 1");

    }


    @Test
    void selectWithAs() {
        assertSql("with with_as_A as (select * from entity) select * from with_as_A",
                "WITH with_as_A AS (SELECT * FROM entity WHERE entity.tenant_id = 1) SELECT * FROM with_as_A");
    }


    @Test
    void selectIgnoreTable() {
        assertSql(" SELECT dict.dict_code, item.item_text AS \"text\", item.item_value AS \"value\" FROM sys_dict_item item INNER JOIN sys_dict dict ON dict.id = item.dict_id WHERE dict.dict_code IN (1, 2, 3) AND item.item_value IN (1, 2, 3)",
                "SELECT dict.dict_code, item.item_text AS \"text\", item.item_value AS \"value\" FROM sys_dict_item item INNER JOIN sys_dict dict ON dict.id = item.dict_id AND item.tenant_id = 1 WHERE dict.dict_code IN (1, 2, 3) AND item.item_value IN (1, 2, 3)");
    }

    private void assertSql(String sql, String targetSql) {
        assertEquals(targetSql, interceptor.parserSingle(sql, null));
    }


    // ========== 额外的测试 ==========

    @Test
    public void testSelectSingle() {
        // 单表
        assertSql("select * from t_user where id = ?",
                "SELECT * FROM t_user WHERE id = ? AND t_user.tenant_id = 1 AND t_user.dept_id IN (10, 20)");

        assertSql("select * from t_user where id = ? or name = ?",
                "SELECT * FROM t_user WHERE (id = ? OR name = ?) AND t_user.tenant_id = 1 AND t_user.dept_id IN (10, 20)");

        assertSql("SELECT * FROM t_user WHERE (id = ? OR name = ?)",
                "SELECT * FROM t_user WHERE (id = ? OR name = ?) AND t_user.tenant_id = 1 AND t_user.dept_id IN (10, 20)");

        /* not */
        assertSql("SELECT * FROM t_user WHERE not (id = ? OR name = ?)",
                "SELECT * FROM t_user WHERE NOT (id = ? OR name = ?) AND t_user.tenant_id = 1 AND t_user.dept_id IN (10, 20)");
    }

    @Test
    public void testSelectLeftJoin() {
        // left join
        assertSql("SELECT * FROM t_user e " +
                        "left join t_role e1 on e1.id = e.id " +
                        "WHERE e.id = ? OR e.name = ?",
                "SELECT * FROM t_user e " +
                        "LEFT JOIN t_role e1 ON e1.id = e.id AND e1.tenant_id = 1 " +
                        "WHERE (e.id = ? OR e.name = ?) AND e.tenant_id = 1 AND e.dept_id IN (10, 20)");

        // 条件 e.id = ? OR e.name = ? 带括号
        assertSql("SELECT * FROM t_user e " +
                        "left join t_role e1 on e1.id = e.id " +
                        "WHERE (e.id = ? OR e.name = ?)",
                "SELECT * FROM t_user e " +
                        "LEFT JOIN t_role e1 ON e1.id = e.id AND e1.tenant_id = 1 " +
                        "WHERE (e.id = ? OR e.name = ?) AND e.tenant_id = 1 AND e.dept_id IN (10, 20)");
    }

    @Test
    public void testSelectRightJoin() {
        // right join
        assertSql("SELECT * FROM t_user e " +
                        "right join t_role e1 on e1.id = e.id " +
                        "WHERE e.id = ? OR e.name = ?",
                "SELECT * FROM t_user e " +
                        "RIGHT JOIN t_role e1 ON e1.id = e.id AND e.tenant_id = 1 AND e.dept_id IN (10, 20) " +
                        "WHERE (e.id = ? OR e.name = ?) AND e1.tenant_id = 1");

        // 条件 e.id = ? OR e.name = ? 带括号
        assertSql("SELECT * FROM t_user e " +
                        "right join t_role e1 on e1.id = e.id " +
                        "WHERE (e.id = ? OR e.name = ?)",
                "SELECT * FROM t_user e " +
                        "RIGHT JOIN t_role e1 ON e1.id = e.id AND e.tenant_id = 1 AND e.dept_id IN (10, 20) " +
                        "WHERE (e.id = ? OR e.name = ?) AND e1.tenant_id = 1");
    }

    @Test
    public void testSelectInnerJoin() {
        // inner join
        assertSql("SELECT * FROM t_user e " +
                        "inner join entity1 e1 on e1.id = e.id " +
                        "WHERE e.id = ? OR e.name = ?",
                "SELECT * FROM t_user e " +
                        "INNER JOIN entity1 e1 ON e1.id = e.id AND e.tenant_id = 1 AND e.dept_id IN (10, 20) AND e1.tenant_id = 1 " +
                        "WHERE e.id = ? OR e.name = ?");

        // 条件 e.id = ? OR e.name = ? 带括号
        assertSql("SELECT * FROM t_user e " +
                        "inner join entity1 e1 on e1.id = e.id " +
                        "WHERE (e.id = ? OR e.name = ?)",
                "SELECT * FROM t_user e " +
                        "INNER JOIN entity1 e1 ON e1.id = e.id AND e.tenant_id = 1 AND e.dept_id IN (10, 20) AND e1.tenant_id = 1 " +
                        "WHERE (e.id = ? OR e.name = ?)");

        // 没有 On 的 inner join
        assertSql("SELECT * FROM entity,entity1 " +
                "WHERE entity.id = entity1.id",
            "SELECT * FROM entity, entity1 " +
                    "WHERE entity.id = entity1.id AND entity.tenant_id = 1 AND entity1.tenant_id = 1");
    }

}
