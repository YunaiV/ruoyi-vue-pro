package cn.iocoder.yudao.module.iot.core.util;

import org.junit.jupiter.api.Test;
import cn.hutool.core.util.StrUtil;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link IotOpcUaUtils} 的单元测试
 *
 * @author 芋道源码
 */
public class IotOpcUaUtilsTest {

    @Test
    public void testIsValidNodeId_valid() {
        assertTrue(IotOpcUaUtils.isValidNodeId("i=2258"));
        assertTrue(IotOpcUaUtils.isValidNodeId("ns=2;s=Channel1.Device1.Tag1"));
        assertTrue(IotOpcUaUtils.isValidNodeId("ns=65535;i=4294967295"));
        assertTrue(IotOpcUaUtils.isValidNodeId("nsu=urn:yudao:test;s=温度"));
        assertTrue(IotOpcUaUtils.isValidNodeId("ns=1;g=09087e75-8e5e-499b-954f-f2a9603db28a"));
        assertTrue(IotOpcUaUtils.isValidNodeId("g=09087E75-8E5E-499B-954F-F2A9603DB28A"));
        assertTrue(IotOpcUaUtils.isValidNodeId("ns=1;b=M/RbKBsRVkePCePcx24oRA=="));
    }

    @Test
    public void testIsValidNodeId_invalid() {
        assertFalse(IotOpcUaUtils.isValidNodeId(null));
        assertFalse(IotOpcUaUtils.isValidNodeId(""));
        assertFalse(IotOpcUaUtils.isValidNodeId(" i=1"));
        assertFalse(IotOpcUaUtils.isValidNodeId("ns=65536;i=1"));
        assertFalse(IotOpcUaUtils.isValidNodeId("ns=a;i=1"));
        assertFalse(IotOpcUaUtils.isValidNodeId("ns=1"));
        assertFalse(IotOpcUaUtils.isValidNodeId("nsu=;s=a"));
        assertFalse(IotOpcUaUtils.isValidNodeId("nsu=   ;s=a"));
        assertFalse(IotOpcUaUtils.isValidNodeId("nsu= urn:a;s=a"));
        assertFalse(IotOpcUaUtils.isValidNodeId("nsu=urn:a ;s=a"));
        assertFalse(IotOpcUaUtils.isValidNodeId("i=4294967296"));
        assertFalse(IotOpcUaUtils.isValidNodeId("i=-1"));
        assertFalse(IotOpcUaUtils.isValidNodeId("i=١٢"));
        assertFalse(IotOpcUaUtils.isValidNodeId("s="));
        assertFalse(IotOpcUaUtils.isValidNodeId("g=not-a-guid"));
        assertFalse(IotOpcUaUtils.isValidNodeId("g=09087e758e5e499b954ff2a9603db28a"));
        assertFalse(IotOpcUaUtils.isValidNodeId("i=+1"));
        assertFalse(IotOpcUaUtils.isValidNodeId("b=***"));
        assertFalse(IotOpcUaUtils.isValidNodeId("x=1"));
        assertFalse(IotOpcUaUtils.isValidNodeId("ns=1;s=" + StrUtil.repeat('a', IotOpcUaUtils.NODE_ID_MAX_LENGTH)));
    }

    /**
     * b= 的 Base64 口径（前端 DeviceOpcUaPointForm 的 NODE_ID_PATTERN 与此保持一致）
     */
    @Test
    public void testIsValidNodeId_base64() {
        // 正例：带填充、省略填充（末尾分组 2 / 3 个字符）、非规范尾位（JDK 解码器不校验）
        for (String value : new String[]{"AQID", "AA==", "AAA=", "AA", "AAA", "AQIDBA==", "AQIDBAU", "+/+/", "AB=="}) {
            assertTrue(IotOpcUaUtils.isValidNodeId("ns=1;b=" + value), value);
        }
        // 反例：空、末尾分组只有 1 个字符、填充数量不对、填充后还有字符、非 Base64 字符（含 URL 安全字符、空白）
        for (String value : new String[]{"", "A", "A=", "A==", "AA=", "AA=A", "AAAAA", "AAAA=", "=", "==", "====",
                "AQID====", "AQ==AQ==", "AQ ID", "AQ-_", "AQID!"}) {
            assertFalse(IotOpcUaUtils.isValidNodeId("ns=1;b=" + value), value);
        }
    }

    @Test
    public void testIsNamespaceIndexNodeId() {
        assertTrue(IotOpcUaUtils.isNamespaceIndexNodeId("ns=2;s=a"));
        assertFalse(IotOpcUaUtils.isNamespaceIndexNodeId("nsu=urn:a;s=a"));
        assertFalse(IotOpcUaUtils.isNamespaceIndexNodeId("i=1"));
        assertFalse(IotOpcUaUtils.isNamespaceIndexNodeId(null));
    }

}
