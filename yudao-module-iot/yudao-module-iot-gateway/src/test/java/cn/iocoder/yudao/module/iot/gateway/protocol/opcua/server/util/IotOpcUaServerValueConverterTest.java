package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.server.util;

import org.eclipse.milo.opcua.stack.core.NodeIds;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotOpcUaServerValueConverter} 的单元测试
 *
 * @author 芋道源码
 */
public class IotOpcUaServerValueConverterTest {

    @Test
    public void testToDataTypeId() {
        assertEquals(NodeIds.Int32, IotOpcUaServerValueConverter.toDataTypeId("int"));
        assertEquals(NodeIds.Int32, IotOpcUaServerValueConverter.toDataTypeId("enum"));
        assertEquals(NodeIds.Float, IotOpcUaServerValueConverter.toDataTypeId("float"));
        assertEquals(NodeIds.Double, IotOpcUaServerValueConverter.toDataTypeId("double"));
        assertEquals(NodeIds.Boolean, IotOpcUaServerValueConverter.toDataTypeId("bool"));
        assertEquals(NodeIds.DateTime, IotOpcUaServerValueConverter.toDataTypeId("date"));
        assertEquals(NodeIds.String, IotOpcUaServerValueConverter.toDataTypeId("text"));
        assertEquals(NodeIds.String, IotOpcUaServerValueConverter.toDataTypeId("struct"));
        assertEquals(NodeIds.String, IotOpcUaServerValueConverter.toDataTypeId(null));
    }

    @Test
    public void testToVariant() {
        assertEquals(12, value(12, "int"));
        assertEquals(12, value("12", "int"));
        assertEquals(3, value(3, "enum"));
        assertEquals(1.5F, value(1.5D, "float"));
        assertEquals(25.5D, value("25.5", "double"));
        assertEquals(true, value(true, "bool"));
        assertEquals(false, value(0, "bool"));
        assertEquals("pump", value("pump", "text"));
        assertEquals(new DateTime(Instant.ofEpochMilli(1700000000000L)), value(1700000000000L, "date"));
        assertEquals(new DateTime(Instant.parse("2026-11-21T00:00:00Z")), value("2026-11-21T00:00:00Z", "date"));
        assertEquals("{\"a\":1}", value(Collections.singletonMap("a", 1), "struct"));
        assertEquals("[1,2]", value(Arrays.asList(1, 2), "array"));
    }

    @Test
    public void testToVariant_invalid() {
        assertNull(IotOpcUaServerValueConverter.toVariant(null, "int"));
        // int 超出范围、有小数、非数字
        assertNull(IotOpcUaServerValueConverter.toVariant(3_000_000_000L, "int"));
        assertNull(IotOpcUaServerValueConverter.toVariant(1.5D, "int"));
        assertNull(IotOpcUaServerValueConverter.toVariant("abc", "double"));
        assertNull(IotOpcUaServerValueConverter.toVariant("on", "bool"));
        assertNull(IotOpcUaServerValueConverter.toVariant("not-a-date", "date"));
    }

    @Test
    public void testToVariant_overflowAndUnderflow() {
        // float：溢出为 Infinity、非零值下溢为 0
        assertNull(IotOpcUaServerValueConverter.toVariant(1e39D, "float"));
        assertNull(IotOpcUaServerValueConverter.toVariant(-1e39D, "float"));
        assertNull(IotOpcUaServerValueConverter.toVariant(Double.MAX_VALUE, "float"));
        assertNull(IotOpcUaServerValueConverter.toVariant(1e-50D, "float"));
        // double：溢出、下溢
        assertNull(IotOpcUaServerValueConverter.toVariant("1e309", "double"));
        assertNull(IotOpcUaServerValueConverter.toVariant("-1e309", "double"));
        assertNull(IotOpcUaServerValueConverter.toVariant("1e-400", "double"));
        assertNull(IotOpcUaServerValueConverter.toVariant(new BigDecimal("1e400"), "double"));
        // 超大 BigDecimal、BigInteger、指数超出范围的字符串：不抛出异常
        BigDecimal huge = new BigDecimal("1E+999999999");
        assertNull(IotOpcUaServerValueConverter.toVariant(huge, "int"));
        assertNull(IotOpcUaServerValueConverter.toVariant(huge, "float"));
        assertNull(IotOpcUaServerValueConverter.toVariant(huge, "double"));
        assertNull(IotOpcUaServerValueConverter.toVariant(huge, "date"));
        assertNull(IotOpcUaServerValueConverter.toVariant(new BigDecimal("1E-999999999"), "double"));
        assertNull(IotOpcUaServerValueConverter.toVariant(BigInteger.TEN.pow(400), "double"));
        assertNull(IotOpcUaServerValueConverter.toVariant("1e99999999999", "double"));
        // NaN、Infinity
        assertNull(IotOpcUaServerValueConverter.toVariant(Double.NaN, "double"));
        assertNull(IotOpcUaServerValueConverter.toVariant(Double.POSITIVE_INFINITY, "float"));
        // 断言：零和边界内的值正常转换
        assertEquals(0F, value(0, "float"));
        assertEquals(0D, value("0.0", "double"));
        assertEquals(Float.MAX_VALUE, value(new BigDecimal(Float.toString(Float.MAX_VALUE)), "float"));
        assertEquals(Double.MIN_VALUE, value(Double.MIN_VALUE, "double"));
        assertEquals(12, value(new BigDecimal("12"), "int"));
    }

    @Test
    public void testToVariant_intScaleAndRange() {
        // 断言：普通数值语义不变（整数值的小数、科学计数法、int 边界）
        assertEquals(12, value(new BigDecimal("12.000"), "int"));
        assertEquals(100, value("1e2", "int"));
        assertEquals(1_000_000_000, value(new BigDecimal("1E+9"), "int"));
        assertEquals(Integer.MAX_VALUE, value(BigDecimal.valueOf(Integer.MAX_VALUE), "int"));
        assertEquals(Integer.MIN_VALUE, value(String.valueOf(Integer.MIN_VALUE), "enum"));
        assertEquals(0, value(new BigDecimal("0E-1000000000"), "int"));
        assertEquals(-7, value(new BigDecimal("-7.0"), "enum"));
        // 断言：超出 int 范围、带非零小数、0 < |x| < 1 的小数返回 null
        assertNull(IotOpcUaServerValueConverter.toVariant(BigDecimal.valueOf(Integer.MAX_VALUE + 1L), "int"));
        assertNull(IotOpcUaServerValueConverter.toVariant(BigDecimal.valueOf(Integer.MIN_VALUE - 1L), "enum"));
        assertNull(IotOpcUaServerValueConverter.toVariant(new BigDecimal("9999999999"), "int"));
        assertNull(IotOpcUaServerValueConverter.toVariant(new BigDecimal("12.5"), "int"));
        assertNull(IotOpcUaServerValueConverter.toVariant(new BigDecimal("-0.5"), "int"));

        // 断言：极端指数、超长有效数字先按规模拒绝，不展开、不抛出异常
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            for (String dataType : Arrays.asList("int", "enum")) {
                assertNull(IotOpcUaServerValueConverter.toVariant(new BigDecimal("1e1000000000"), dataType));
                assertNull(IotOpcUaServerValueConverter.toVariant(new BigDecimal("-1E+2147483647"), dataType));
                assertNull(IotOpcUaServerValueConverter.toVariant(new BigDecimal("1E-1000000000"), dataType));
                assertNull(IotOpcUaServerValueConverter.toVariant(new BigDecimal("1E-2147483647"), dataType));
                assertNull(IotOpcUaServerValueConverter.toVariant(new BigDecimal(BigInteger.TEN.pow(100_000)), dataType));
                assertNull(IotOpcUaServerValueConverter.toVariant("1e1000000000", dataType));
            }
            // date：超出 long 范围按不匹配处理；0 < |x| < 1 的小数截断为 0
            assertNull(IotOpcUaServerValueConverter.toVariant(new BigDecimal("1e1000000000"), "date"));
            assertEquals(new DateTime(Instant.ofEpochMilli(0)), value(new BigDecimal("1E-1000000000"), "date"));
        });
        // 断言：date 的普通小数仍按原语义截断
        assertEquals(new DateTime(Instant.ofEpochMilli(1700000000000L)), value(new BigDecimal("1700000000000.9"), "date"));
    }

    private static Object value(Object value, String dataType) {
        Variant variant = IotOpcUaServerValueConverter.toVariant(value, dataType);
        assertNotNull(variant);
        return variant.getValue();
    }

}
