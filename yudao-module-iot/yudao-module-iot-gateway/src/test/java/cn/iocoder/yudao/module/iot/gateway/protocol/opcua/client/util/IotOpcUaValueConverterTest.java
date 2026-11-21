package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.util;

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

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

}
