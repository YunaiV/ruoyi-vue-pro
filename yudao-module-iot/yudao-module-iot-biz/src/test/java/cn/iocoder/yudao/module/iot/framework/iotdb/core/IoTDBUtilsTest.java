package cn.iocoder.yudao.module.iot.framework.iotdb.core;

import cn.hutool.core.map.MapUtil;
import org.apache.iotdb.common.rpc.thrift.TSStatus;
import org.apache.iotdb.jdbc.IoTDBSQLException;
import org.apache.iotdb.rpc.StatementExecutionException;
import org.apache.iotdb.rpc.TSStatusCode;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.UncategorizedSQLException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IoTDBUtils} 的单元测试
 *
 * @author 芋道源码
 */
public class IoTDBUtilsTest {

    // ========== isTableNotExists ==========

    @Test
    public void testIsTableNotExists_statusCode() {
        assertEquals(TSStatusCode.TABLE_NOT_EXISTS.getStatusCode(), IoTDBUtils.STATUS_CODE_TABLE_NOT_EXISTS);
    }

    @Test
    public void testIsTableNotExists_true() {
        // 驱动抛出的异常：errorCode 为 IoTDB 状态码
        IoTDBSQLException sqlException = new IoTDBSQLException("550: Table 'db.product_property_1' does not exist",
                new TSStatus(TSStatusCode.TABLE_NOT_EXISTS.getStatusCode()));
        assertTrue(IoTDBUtils.isTableNotExists(sqlException));
        // Spring、MyBatis 包装后的异常：沿 cause 链查找
        assertTrue(IoTDBUtils.isTableNotExists(new UncategorizedSQLException("select", "SELECT 1", sqlException)));
        assertTrue(IoTDBUtils.isTableNotExists(new RuntimeException(new RuntimeException(sqlException))));
        // Session 风格的异常
        assertTrue(IoTDBUtils.isTableNotExists(new RuntimeException(
                new StatementExecutionException(new TSStatus(TSStatusCode.TABLE_NOT_EXISTS.getStatusCode())))));
    }

    @Test
    public void testIsTableNotExists_false() {
        // 列不存在、类型不匹配：不是表不存在，需要原样抛出
        assertFalse(IoTDBUtils.isTableNotExists(new IoTDBSQLException("616",
                new TSStatus(TSStatusCode.COLUMN_NOT_EXISTS.getStatusCode()))));
        assertFalse(IoTDBUtils.isTableNotExists(new StatementExecutionException(
                new TSStatus(TSStatusCode.DATA_TYPE_MISMATCH.getStatusCode()))));
        // 只靠异常文案，不识别为表不存在
        assertFalse(IoTDBUtils.isTableNotExists(new RuntimeException("Table does not exist")));
        assertFalse(IoTDBUtils.isTableNotExists(null));
    }

    @Test
    public void testIsTableNotExists_causeLoop() {
        // 准备参数：cause 链成环
        RuntimeException a = new RuntimeException("a");
        RuntimeException b = new RuntimeException("b");
        a.initCause(b);
        b.initCause(a);

        // 调用，并断言：不会死循环
        assertFalse(IoTDBUtils.isTableNotExists(a));
    }

    // ========== validateTimestampPrecision ==========

    @Test
    public void testValidateTimestampPrecision_success() {
        List<Map<String, Object>> variables = Arrays.asList(
                row("Variable", "ClusterName", "Value", "defaultCluster"),
                row("variable", "TimestampPrecision", "value", "ms"));
        assertDoesNotThrow(() -> IoTDBUtils.validateTimestampPrecision(variables));
    }

    @Test
    public void testValidateTimestampPrecision_failure() {
        // 精度不是 ms
        assertThrows(IllegalStateException.class, () -> IoTDBUtils.validateTimestampPrecision(
                Collections.singletonList(row("Variable", "TimestampPrecision", "Value", "ns"))));
        // 无法读取
        assertThrows(IllegalStateException.class, () -> IoTDBUtils.validateTimestampPrecision(
                Collections.singletonList(row("Variable", "ClusterName", "Value", "defaultCluster"))));
        assertThrows(IllegalStateException.class, () -> IoTDBUtils.validateTimestampPrecision(Collections.emptyList()));
    }

    // ========== validateDataNodes ==========

    @Test
    public void testValidateDataNodes_success() {
        List<Map<String, Object>> dataNodes = Arrays.asList(
                row("NodeID", 1, "Status", "Running"),
                row("NodeID", 3, "Status", "running"));
        assertEquals(Arrays.asList(1, 3), IoTDBUtils.validateDataNodes(dataNodes));
    }

    @Test
    public void testValidateDataNodes_failure() {
        // 没有 DataNode
        assertThrows(IllegalStateException.class, () -> IoTDBUtils.validateDataNodes(Collections.emptyList()));
        assertThrows(IllegalStateException.class, () -> IoTDBUtils.validateDataNodes(null));
        // 有 DataNode 不是 Running：无法确认它的配置
        assertThrows(IllegalStateException.class, () -> IoTDBUtils.validateDataNodes(Arrays.asList(
                row("NodeID", 1, "Status", "Running"),
                row("NodeID", 2, "Status", "Unknown"))));
        // 编号无法识别
        assertThrows(IllegalStateException.class, () -> IoTDBUtils.validateDataNodes(
                Collections.singletonList(row("NodeID", "abc", "Status", "Running"))));
        assertThrows(IllegalStateException.class, () -> IoTDBUtils.validateDataNodes(
                Collections.singletonList(row("Status", "Running"))));
    }

    // ========== validatePartialInsertDisabled ==========

    @Test
    public void testValidatePartialInsertDisabled_success() {
        List<Map<String, Object>> configurations = Arrays.asList(
                row("name", "dn_rpc_port", "value", "6667", "default_value", "6667"),
                row("name", "enable_partial_insert", "value", "false", "default_value", "true"));
        assertDoesNotThrow(() -> IoTDBUtils.validatePartialInsertDisabled(1, configurations));
    }

    @Test
    public void testValidatePartialInsertDisabled_failure() {
        // 开启了部分写入
        assertThrows(IllegalStateException.class, () -> IoTDBUtils.validatePartialInsertDisabled(1,
                Collections.singletonList(row("name", "enable_partial_insert", "value", "true"))));
        // 无法读取配置
        assertThrows(IllegalStateException.class, () -> IoTDBUtils.validatePartialInsertDisabled(1,
                Collections.singletonList(row("name", "dn_rpc_port", "value", "6667"))));
        assertThrows(IllegalStateException.class, () -> IoTDBUtils.validatePartialInsertDisabled(1,
                Collections.singletonList(row("name", "enable_partial_insert"))));
    }

    // ========== getIgnoreCase ==========

    @Test
    public void testGetIgnoreCase() {
        Map<String, Object> row = row("NodeID", 1);
        assertEquals(1, IoTDBUtils.getIgnoreCase(row, "nodeid"));
        assertNull(IoTDBUtils.getIgnoreCase(row, "Status"));
    }

    private static Map<String, Object> row(Object... keyValues) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            row.put((String) keyValues[i], keyValues[i + 1]);
        }
        return MapUtil.unmodifiable(row);
    }

}
