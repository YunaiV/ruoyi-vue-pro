package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.util;

import org.eclipse.milo.opcua.stack.core.NodeIds;
import org.eclipse.milo.opcua.stack.core.OpcUaDataType;
import org.eclipse.milo.opcua.stack.core.encoding.DefaultEncodingContext;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.structured.Range;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link IotOpcUaValueConverter} 的单元测试
 *
 * @author 芋道源码
 */
public class IotOpcUaValueConverterTest {

    @Test
    public void testConvert_scalar() {
        assertNull(IotOpcUaValueConverter.convert(null));
        assertEquals(true, IotOpcUaValueConverter.convert(true));
        assertEquals("a", IotOpcUaValueConverter.convert("a"));
        assertEquals(-1, IotOpcUaValueConverter.convert((byte) -1));
        assertEquals(-2, IotOpcUaValueConverter.convert((short) -2));
        assertEquals(3, IotOpcUaValueConverter.convert(3));
        assertEquals(4L, IotOpcUaValueConverter.convert(4L));
        assertEquals(255, IotOpcUaValueConverter.convert(ubyte(255)));
        assertEquals(65535, IotOpcUaValueConverter.convert(ushort(65535)));
        assertEquals(4294967295L, IotOpcUaValueConverter.convert(uint(4294967295L)));
        assertEquals("18446744073709551615", IotOpcUaValueConverter.convert(ulong("18446744073709551615")));
        assertEquals(1.5F, IotOpcUaValueConverter.convert(1.5F));
        assertEquals(2.5D, IotOpcUaValueConverter.convert(2.5D));
    }

    @Test
    public void testConvert_nanAndInfinity() {
        assertNull(IotOpcUaValueConverter.convert(Float.NaN));
        assertNull(IotOpcUaValueConverter.convert(Double.POSITIVE_INFINITY));
        assertNull(IotOpcUaValueConverter.convert(Double.NEGATIVE_INFINITY));
    }

    @Test
    public void testConvert_builtinTypes() {
        DateTime dateTime = new DateTime(Instant.ofEpochMilli(1700000000000L));
        assertEquals(1700000000000L, IotOpcUaValueConverter.convert(dateTime));
        assertEquals("AQI=", IotOpcUaValueConverter.convert(ByteString.of(new byte[]{1, 2})));
        assertEquals("hello", IotOpcUaValueConverter.convert(LocalizedText.english("hello")));
        assertEquals("name", IotOpcUaValueConverter.convert(new QualifiedName(1, "name")));
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid.toString(), IotOpcUaValueConverter.convert(uuid));
        assertEquals("ns=2;s=a", IotOpcUaValueConverter.convert(new NodeId(2, "a")));
        assertEquals(0L, IotOpcUaValueConverter.convert(StatusCode.GOOD));
    }

    @Test
    public void testConvert_array() {
        assertEquals(Arrays.asList(1, 2, 3), IotOpcUaValueConverter.convert(new Integer[]{1, 2, 3}));
        assertEquals(Arrays.asList(1L, 2L), IotOpcUaValueConverter.convert(new long[]{1L, 2L}));
        // 多维数组、有元素不能转换的数组：丢弃
        assertNull(IotOpcUaValueConverter.convert(new Integer[][]{{1}, {2}}));
        assertNull(IotOpcUaValueConverter.convert(new Double[]{1.0D, Double.NaN}));
    }

    @Test
    public void testConvert_unsupported() {
        assertNull(IotOpcUaValueConverter.convert(ExtensionObject.encode(DefaultEncodingContext.INSTANCE,
                new Range(0.0, 1.0))));
        assertNull(IotOpcUaValueConverter.convert(new Object()));
    }

    // ========== 写入 ==========

    @Test
    public void testResolveWritableDataType() {
        // 内置标量类型
        assertEquals(OpcUaDataType.Int16, IotOpcUaValueConverter.resolveWritableDataType(NodeIds.Int16));
        assertEquals(OpcUaDataType.UInt64, IotOpcUaValueConverter.resolveWritableDataType(NodeIds.UInt64));
        assertEquals(OpcUaDataType.String, IotOpcUaValueConverter.resolveWritableDataType(NodeIds.String));
        // 常见子类型
        assertEquals(OpcUaDataType.DateTime, IotOpcUaValueConverter.resolveWritableDataType(NodeIds.UtcTime));
        assertEquals(OpcUaDataType.Double, IotOpcUaValueConverter.resolveWritableDataType(NodeIds.Duration));
        // 不支持：抽象类型、ByteString、结构体、自定义类型、null
        assertNull(IotOpcUaValueConverter.resolveWritableDataType(NodeIds.BaseDataType));
        assertNull(IotOpcUaValueConverter.resolveWritableDataType(NodeIds.Number));
        assertNull(IotOpcUaValueConverter.resolveWritableDataType(NodeIds.ByteString));
        assertNull(IotOpcUaValueConverter.resolveWritableDataType(NodeIds.Range));
        assertNull(IotOpcUaValueConverter.resolveWritableDataType(new NodeId(2, "MyType")));
        assertNull(IotOpcUaValueConverter.resolveWritableDataType(null));
    }

    @Test
    public void testToVariant_boolean() {
        assertEquals(true, toVariantValue(true, OpcUaDataType.Boolean));
        assertEquals(false, toVariantValue(" FALSE ", OpcUaDataType.Boolean));
        assertEquals(true, toVariantValue(1, OpcUaDataType.Boolean));
        assertEquals(false, toVariantValue(0.0D, OpcUaDataType.Boolean));
        assertInvalid(2, OpcUaDataType.Boolean);
        assertInvalid("on", OpcUaDataType.Boolean);
    }

    @Test
    public void testToVariant_integer() {
        // 有符号：类型与边界
        assertEquals((byte) -128, toVariantValue(-128, OpcUaDataType.SByte));
        assertInvalid(128, OpcUaDataType.SByte);
        assertEquals((short) 32767, toVariantValue("32767", OpcUaDataType.Int16));
        assertInvalid(32768, OpcUaDataType.Int16);
        assertEquals(Integer.MIN_VALUE, toVariantValue(-2147483648L, OpcUaDataType.Int32));
        assertInvalid(2147483648L, OpcUaDataType.Int32);
        assertEquals(Long.MAX_VALUE, toVariantValue("9223372036854775807", OpcUaDataType.Int64));
        assertInvalid("9223372036854775808", OpcUaDataType.Int64);
        // 无符号：类型与边界，负数不允许
        assertEquals(ubyte(255), toVariantValue(255, OpcUaDataType.Byte));
        assertInvalid(-1, OpcUaDataType.Byte);
        assertEquals(ushort(65535), toVariantValue(65535, OpcUaDataType.UInt16));
        assertInvalid(65536, OpcUaDataType.UInt16);
        assertEquals(uint(4294967295L), toVariantValue(4294967295L, OpcUaDataType.UInt32));
        assertInvalid(-1, OpcUaDataType.UInt32);
        assertEquals(ulong("18446744073709551615"), toVariantValue(new BigInteger("18446744073709551615"), OpcUaDataType.UInt64));
        assertInvalid("18446744073709551616", OpcUaDataType.UInt64);
        // 整数值的小数可以，带小数部分的不截断
        assertEquals(12, toVariantValue(12.0D, OpcUaDataType.Int32));
        assertEquals(1200, toVariantValue("1.2e3", OpcUaDataType.Int32));
        assertEquals(0, toVariantValue("0E-100", OpcUaDataType.Int32));
        assertInvalid(12.5D, OpcUaDataType.Int32);
        // 极大、极小的科学计数法：按位数直接拒绝，不分配巨大的 BigInteger
        assertInvalid("1e1000000000", OpcUaDataType.Int64);
        assertInvalid("-1e1000000000", OpcUaDataType.UInt64);
        assertInvalid("1e-1000000000", OpcUaDataType.Int32);
        // 非数值
        assertInvalid(true, OpcUaDataType.Int32);
        assertInvalid("0x10", OpcUaDataType.Int32);
        assertInvalid("abc", OpcUaDataType.Int32);
    }

    @Test
    public void testToVariant_floatAndDouble() {
        assertEquals(1.5F, toVariantValue(1.5D, OpcUaDataType.Float));
        assertEquals(2.25D, toVariantValue("2.25", OpcUaDataType.Double));
        assertInvalid(1e39D, OpcUaDataType.Float);
        assertInvalid("1e400", OpcUaDataType.Double);
        // 边界：Float.MAX_VALUE 的十进制写法、double 写法都是合法的最大值
        assertEquals(Float.MAX_VALUE, toVariantValue("3.4028235E38", OpcUaDataType.Float));
        assertEquals(-Float.MAX_VALUE, toVariantValue((double) -Float.MAX_VALUE, OpcUaDataType.Float));
        assertEquals(Double.MAX_VALUE, toVariantValue(Double.MAX_VALUE, OpcUaDataType.Double));
        assertEquals(Float.MIN_VALUE, toVariantValue((double) Float.MIN_VALUE, OpcUaDataType.Float));
        // 下溢：非零值变成 0 时失败；0 本身可以
        assertInvalid(1e-50D, OpcUaDataType.Float);
        assertInvalid("-1e-400", OpcUaDataType.Double);
        assertEquals(0F, toVariantValue("0.000", OpcUaDataType.Float));
        assertEquals(0D, toVariantValue(0, OpcUaDataType.Double));
        assertInvalid(Double.NaN, OpcUaDataType.Double);
        assertInvalid("NaN", OpcUaDataType.Double);
    }

    @Test
    public void testToVariant_stringAndDateTime() {
        // 字符串：只接受字符串，不把数字转为字符串
        assertEquals("on", toVariantValue("on", OpcUaDataType.String));
        assertInvalid(1, OpcUaDataType.String);
        // 日期时间：毫秒时间戳、数字字符串、ISO-8601
        DateTime expected = new DateTime(Instant.ofEpochMilli(1700000000000L));
        assertEquals(expected, toVariantValue(1700000000000L, OpcUaDataType.DateTime));
        assertEquals(expected, toVariantValue("1700000000000", OpcUaDataType.DateTime));
        assertEquals(expected, toVariantValue("2023-11-14T22:13:20Z", OpcUaDataType.DateTime));
        assertInvalid("2023-11-14 22:13:20", OpcUaDataType.DateTime);
        assertInvalid(Long.MAX_VALUE, OpcUaDataType.DateTime);
    }

    @Test
    public void testToVariant_invalid() {
        // 空值、数组、结构体
        assertInvalid(null, OpcUaDataType.Int32);
        assertInvalid(Arrays.asList(1, 2), OpcUaDataType.Int32);
        assertInvalid(new int[]{1}, OpcUaDataType.Int32);
        assertInvalid(Collections.singletonMap("a", 1), OpcUaDataType.String);
        // 不支持的类型
        assertInvalid(new byte[0], OpcUaDataType.ByteString);
        assertInvalid("a", OpcUaDataType.ByteString);
    }

    private static Object toVariantValue(Object value, OpcUaDataType dataType) {
        return IotOpcUaValueConverter.toVariant(value, dataType).getValue();
    }

    private static void assertInvalid(Object value, OpcUaDataType dataType) {
        assertThrows(IllegalArgumentException.class, () -> IotOpcUaValueConverter.toVariant(value, dataType));
    }

}
