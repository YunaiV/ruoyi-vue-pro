package cn.iocoder.yudao.framework.common.util.jdbc;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link JdbcExceptionUtils} 的单元测试
 *
 * @author 芋道源码
 */
public class JdbcExceptionUtilsTest {

    // ========== 表不存在：SQLState、错误码 ==========

    @Test
    public void testIsTableNotExists_mysql() {
        assertTrue(JdbcExceptionUtils.isTableNotExists(
                new SQLException("Table 'ruoyi-vue-pro.bpm_category' doesn't exist", "42S02", 1146)));
        // SQLState 识别，不依赖文案
        assertTrue(JdbcExceptionUtils.isTableNotExists(new SQLException("unknown", "42S02", 1146)));
    }

    @Test
    public void testIsTableNotExists_postgresql() {
        assertTrue(JdbcExceptionUtils.isTableNotExists(
                new SQLException("ERROR: relation \"bpm_category\" does not exist", "42P01")));
    }

    @Test
    public void testIsTableNotExists_oracle() {
        assertTrue(JdbcExceptionUtils.isTableNotExists(
                new SQLException("ORA-00942: table or view does not exist\n", "42000", 942)));
    }

    @Test
    public void testIsTableNotExists_sqlServer() {
        // mssql-jdbc
        assertTrue(JdbcExceptionUtils.isTableNotExists(
                new SQLException("Invalid object name 'bpm_category'.", "S0002", 208)));
        // jTDS
        assertTrue(JdbcExceptionUtils.isTableNotExists(
                new SQLException("Invalid object name 'bpm_category'.", "42S02", 208)));
    }

    @Test
    public void testIsTableNotExists_messageFallback() {
        // 未携带 SQLState 时，兜底匹配 MySQL 文案
        assertTrue(JdbcExceptionUtils.isTableNotExists(
                new SQLException("Table 'ruoyi-vue-pro.mp_account' doesn't exist")));
        assertTrue(JdbcExceptionUtils.isTableNotExists(
                new RuntimeException("### Cause: Table 'ruoyi-vue-pro.mp_account' doesn't exist")));
    }

    // ========== 表不存在：异常链 ==========

    @Test
    public void testIsTableNotExists_causeChain() {
        SQLException sqlException = new SQLException("ERROR: relation \"mp_account\" does not exist", "42P01");
        // 外层包装的文案不包含“表不存在”，需要沿 cause 链查找
        assertTrue(JdbcExceptionUtils.isTableNotExists(
                new RuntimeException("nested exception", new IllegalStateException(sqlException))));
    }

    @Test
    public void testIsTableNotExists_nextExceptionChain() {
        SQLException batchException = new SQLException("Batch entry 0 was aborted", "25P02");
        batchException.setNextException(new SQLException("ERROR: relation \"mp_account\" does not exist", "42P01"));
        assertTrue(JdbcExceptionUtils.isTableNotExists(new RuntimeException(batchException)));
    }

    // ========== 非表不存在：原样返回 false ==========

    @Test
    public void testIsTableNotExists_columnNotExists() {
        // MySQL
        assertFalse(JdbcExceptionUtils.isTableNotExists(
                new SQLException("Unknown column 'xxx' in 'field list'", "42S22", 1054)));
        // PostgreSQL
        assertFalse(JdbcExceptionUtils.isTableNotExists(
                new SQLException("ERROR: column \"xxx\" does not exist", "42703")));
        // Oracle
        assertFalse(JdbcExceptionUtils.isTableNotExists(
                new SQLException("ORA-00904: \"XXX\": invalid identifier\n", "42000", 904)));
        // SQL Server
        assertFalse(JdbcExceptionUtils.isTableNotExists(
                new SQLException("Invalid column name 'xxx'.", "S0001", 207)));
    }

    @Test
    public void testIsTableNotExists_permissionDenied() {
        // MySQL
        assertFalse(JdbcExceptionUtils.isTableNotExists(
                new SQLException("SELECT command denied to user 'u'@'localhost' for table 'bpm_category'", "42000", 1142)));
        // PostgreSQL
        assertFalse(JdbcExceptionUtils.isTableNotExists(
                new SQLException("ERROR: permission denied for table bpm_category", "42501")));
        // Oracle
        assertFalse(JdbcExceptionUtils.isTableNotExists(
                new SQLException("ORA-01031: insufficient privileges\n", "42000", 1031)));
    }

    @Test
    public void testIsTableNotExists_sqlStateTakesPrecedenceOverLegacyMessage() {
        // 用户名或外层包装信息可能包含旧文案，明确的权限/列错误仍应保留。
        assertFalse(JdbcExceptionUtils.isTableNotExists(new SQLException(
                "SELECT command denied to user 'doesn't exist'@'localhost' for table 'erp_product'",
                "42000", 1142)));
        assertFalse(JdbcExceptionUtils.isTableNotExists(new RuntimeException(
                "Previous lookup: Table 'erp_product' doesn't exist",
                new SQLException("column missing", "42703"))));
    }

    @Test
    public void testIsTableNotExists_otherErrors() {
        // 语法错误：与 Oracle ORA-00942 同为 42000，但错误码不同
        assertFalse(JdbcExceptionUtils.isTableNotExists(
                new SQLException("You have an error in your SQL syntax", "42000", 1064)));
        assertFalse(JdbcExceptionUtils.isTableNotExists(
                new SQLException("ERROR: syntax error at or near \"FROM\"", "42601")));
        // 错误码相同，但 SQLState 不是“表不存在”：Oracle ORA-01146、SQL Server 942
        assertFalse(JdbcExceptionUtils.isTableNotExists(
                new SQLException("ORA-01146: cannot start online backup", "72000", 1146)));
        assertFalse(JdbcExceptionUtils.isTableNotExists(
                new SQLException("Database 'xxx' cannot be opened because it is offline.", "S0001", 942)));
        // 唯一键冲突、连接异常
        assertFalse(JdbcExceptionUtils.isTableNotExists(
                new SQLException("Duplicate entry '1' for key 'PRIMARY'", "23000", 1062)));
        assertFalse(JdbcExceptionUtils.isTableNotExists(new SQLException("Connection refused", "08001")));
        // 非 MySQL 文案的“does not exist”，且无 SQLState
        assertFalse(JdbcExceptionUtils.isTableNotExists(new RuntimeException("Table does not exist")));
    }

    @Test
    public void testIsTableNotExists_nullAndEmpty() {
        assertFalse(JdbcExceptionUtils.isTableNotExists(null));
        assertFalse(JdbcExceptionUtils.isTableNotExists(new RuntimeException((String) null)));
        assertFalse(JdbcExceptionUtils.isTableNotExists(new SQLException((String) null)));
        assertFalse(JdbcExceptionUtils.isTableNotExists(
                new RuntimeException(null, new SQLException(null, null, 0))));
    }

    @Test
    public void testIsTableNotExists_cycle() {
        // cause 链循环
        RuntimeException a = new RuntimeException("a");
        RuntimeException b = new RuntimeException("b", a);
        a.initCause(b);
        assertFalse(JdbcExceptionUtils.isTableNotExists(a));

        // nextException 链循环
        SQLException s1 = new SQLException("s1", "HY000", 1);
        SQLException s2 = new SQLException("s2", "HY000", 2);
        s1.setNextException(s2);
        s2.setNextException(s1);
        assertFalse(JdbcExceptionUtils.isTableNotExists(s1));

        // 循环链中存在“表不存在”时，仍可识别
        SQLException s3 = new SQLException("s3", "HY000", 3);
        SQLException s4 = new SQLException("ERROR: relation \"mp_account\" does not exist", "42P01");
        s3.setNextException(s4);
        s4.setNextException(s3);
        assertTrue(JdbcExceptionUtils.isTableNotExists(s4));
        assertTrue(JdbcExceptionUtils.isTableNotExists(s3));
    }

}
