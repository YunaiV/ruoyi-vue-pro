package cn.iocoder.yudao.module.iot.framework.storage.core.util;

import cn.hutool.core.util.StrUtil;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotStorageFieldUtils} 的单元测试
 *
 * @author 芋道源码
 */
public class IotStorageFieldUtilsTest {

    @Test
    public void testBuildFieldName_success() {
        assertEquals("ua", IotStorageFieldUtils.buildFieldName("Ua"));
        assertEquals("pf_t", IotStorageFieldUtils.buildFieldName("PfT"));
        assertEquals("pt", IotStorageFieldUtils.buildFieldName("PT"));
        assertEquals("pa", IotStorageFieldUtils.buildFieldName("PA"));
        assertEquals("geo_location", IotStorageFieldUtils.buildFieldName("GeoLocation"));
        assertEquals("light_status", IotStorageFieldUtils.buildFieldName("LightStatus"));
        assertEquals("foo_bar", IotStorageFieldUtils.buildFieldName("foo_bar"));
    }

    @Test
    public void testBuildFieldName_blank() {
        // 空白原样返回，由 isPropertyFieldName、validatePropertyIdentifiers 判定为不合法
        assertNull(IotStorageFieldUtils.buildFieldName(null));
        assertEquals("", IotStorageFieldUtils.buildFieldName(""));
        assertEquals(" ", IotStorageFieldUtils.buildFieldName(" "));
    }

    @Test
    public void testIsPropertyFieldName_true() {
        assertTrue(IotStorageFieldUtils.isPropertyFieldName("Temperature"));
        assertTrue(IotStorageFieldUtils.isPropertyFieldName("PfT"));
        assertTrue(IotStorageFieldUtils.isPropertyFieldName("a1_b2"));
        assertTrue(IotStorageFieldUtils.isPropertyFieldName(StrUtil.repeat('a', 64)));
    }

    @Test
    public void testIsPropertyFieldName_false() {
        // 保留字段：归一化后与系统字段相同
        assertFalse(IotStorageFieldUtils.isPropertyFieldName("ts"));
        assertFalse(IotStorageFieldUtils.isPropertyFieldName("TS"));
        assertFalse(IotStorageFieldUtils.isPropertyFieldName("reportTime"));
        assertFalse(IotStorageFieldUtils.isPropertyFieldName("report_time"));
        assertFalse(IotStorageFieldUtils.isPropertyFieldName("deviceId"));
        // 格式不合法：不能安全地拼接到 SQL 结构名
        assertFalse(IotStorageFieldUtils.isPropertyFieldName(null));
        assertFalse(IotStorageFieldUtils.isPropertyFieldName(""));
        assertFalse(IotStorageFieldUtils.isPropertyFieldName(" "));
        assertFalse(IotStorageFieldUtils.isPropertyFieldName("1abc"));
        assertFalse(IotStorageFieldUtils.isPropertyFieldName("温度"));
        assertFalse(IotStorageFieldUtils.isPropertyFieldName("a-b"));
        assertFalse(IotStorageFieldUtils.isPropertyFieldName("a\"b"));
        assertFalse(IotStorageFieldUtils.isPropertyFieldName("a` OR 1=1"));
        assertFalse(IotStorageFieldUtils.isPropertyFieldName(StrUtil.repeat('a', 65)));
    }

    @Test
    public void testValidatePropertyIdentifiers_success() {
        // 调用，并断言
        assertDoesNotThrow(() -> IotStorageFieldUtils.validatePropertyIdentifiers(
                Arrays.asList("Temperature", "PfT", "geo_location")));
        assertDoesNotThrow(() -> IotStorageFieldUtils.validatePropertyIdentifiers(Collections.emptyList()));
    }

    @Test
    public void testValidatePropertyIdentifiers_invalid() {
        assertServiceException(() -> IotStorageFieldUtils.validatePropertyIdentifiers(
                Arrays.asList("Temperature", "温度")), DEVICE_PROPERTY_FIELD_NAME_INVALID, "温度");
        assertServiceException(() -> IotStorageFieldUtils.validatePropertyIdentifiers(
                Collections.singletonList(null)), DEVICE_PROPERTY_FIELD_NAME_INVALID, (Object) null);
    }

    @Test
    public void testValidatePropertyIdentifiers_reserved() {
        assertServiceException(() -> IotStorageFieldUtils.validatePropertyIdentifiers(
                Collections.singletonList("reportTime")), DEVICE_PROPERTY_FIELD_NAME_RESERVED, "reportTime", "report_time");
        assertServiceException(() -> IotStorageFieldUtils.validatePropertyIdentifiers(
                Collections.singletonList("deviceId")), DEVICE_PROPERTY_FIELD_NAME_RESERVED, "deviceId", "device_id");
    }

    @Test
    public void testValidatePropertyIdentifiers_duplicate() {
        assertServiceException(() -> IotStorageFieldUtils.validatePropertyIdentifiers(
                Arrays.asList("fooBar", "foo_bar")), DEVICE_PROPERTY_FIELD_NAME_DUPLICATE, "foo_bar", "fooBar", "foo_bar");
    }

}
