package cn.iocoder.yudao.module.iot.protocol.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotHttpTopicUtils} 单元测试
 *
 * @author haohao
 */
class IotHttpTopicUtilsTest {

    @Test
    void testBuildAuthPath() {
        assertEquals("/auth", IotHttpTopicUtils.buildAuthPath());
    }

    @Test
    void testBuildTopicPath() {
        // 测试正常路径
        assertEquals("/topic/sys/test/device/thing/service/property/set",
                IotHttpTopicUtils.buildTopicPath("/sys/test/device/thing/service/property/set"));

        // 测试空路径
        assertNull(IotHttpTopicUtils.buildTopicPath(null));
        assertNull(IotHttpTopicUtils.buildTopicPath(""));
    }

    @Test
    void testBuildPropertySetPath() {
        String result = IotHttpTopicUtils.buildPropertySetPath("testProduct", "testDevice");
        assertEquals("/topic/sys/testProduct/testDevice/thing/service/property/set", result);

        // 测试无效参数
        assertNull(IotHttpTopicUtils.buildPropertySetPath(null, "testDevice"));
        assertNull(IotHttpTopicUtils.buildPropertySetPath("testProduct", null));
        assertNull(IotHttpTopicUtils.buildPropertySetPath("", "testDevice"));
        assertNull(IotHttpTopicUtils.buildPropertySetPath("testProduct", ""));
    }

    @Test
    void testBuildPropertyGetPath() {
        String result = IotHttpTopicUtils.buildPropertyGetPath("testProduct", "testDevice");
        assertEquals("/topic/sys/testProduct/testDevice/thing/service/property/get", result);
    }

    @Test
    void testBuildPropertyPostPath() {
        String result = IotHttpTopicUtils.buildPropertyPostPath("testProduct", "testDevice");
        assertEquals("/topic/sys/testProduct/testDevice/thing/event/property/post", result);
    }

    @Test
    void testBuildEventPostPath() {
        String result = IotHttpTopicUtils.buildEventPostPath("testProduct", "testDevice", "alarm");
        assertEquals("/topic/sys/testProduct/testDevice/thing/event/alarm/post", result);

        // 测试无效参数
        assertNull(IotHttpTopicUtils.buildEventPostPath(null, "testDevice", "alarm"));
        assertNull(IotHttpTopicUtils.buildEventPostPath("testProduct", null, "alarm"));
        assertNull(IotHttpTopicUtils.buildEventPostPath("testProduct", "testDevice", null));
    }

    @Test
    void testBuildServiceInvokePath() {
        String result = IotHttpTopicUtils.buildServiceInvokePath("testProduct", "testDevice", "reboot");
        assertEquals("/topic/sys/testProduct/testDevice/thing/service/reboot", result);

        // 测试无效参数
        assertNull(IotHttpTopicUtils.buildServiceInvokePath(null, "testDevice", "reboot"));
        assertNull(IotHttpTopicUtils.buildServiceInvokePath("testProduct", null, "reboot"));
        assertNull(IotHttpTopicUtils.buildServiceInvokePath("testProduct", "testDevice", null));
    }

    @Test
    void testBuildCustomTopicPath() {
        String result = IotHttpTopicUtils.buildCustomTopicPath("testProduct", "testDevice", "user/get");
        assertEquals("/topic/testProduct/testDevice/user/get", result);

        // 测试无效参数
        assertNull(IotHttpTopicUtils.buildCustomTopicPath(null, "testDevice", "user/get"));
        assertNull(IotHttpTopicUtils.buildCustomTopicPath("testProduct", null, "user/get"));
        assertNull(IotHttpTopicUtils.buildCustomTopicPath("testProduct", "testDevice", null));
    }

    @Test
    void testExtractActualTopic() {
        // 测试正常提取
        String actualTopic = IotHttpTopicUtils
                .extractActualTopic("/topic/sys/testProduct/testDevice/thing/service/property/set");
        assertEquals("/sys/testProduct/testDevice/thing/service/property/set", actualTopic);

        // 测试无效路径
        assertNull(IotHttpTopicUtils.extractActualTopic("/auth"));
        assertNull(IotHttpTopicUtils.extractActualTopic("/unknown/path"));
        assertNull(IotHttpTopicUtils.extractActualTopic(null));
        assertNull(IotHttpTopicUtils.extractActualTopic(""));
    }

    @Test
    void testParseProductKeyFromTopic() {
        // 测试系统主题
        assertEquals("testProduct",
                IotHttpTopicUtils.parseProductKeyFromTopic("/sys/testProduct/testDevice/thing/service/property/set"));

        // 测试自定义主题
        assertEquals("testProduct", IotHttpTopicUtils.parseProductKeyFromTopic("/testProduct/testDevice/user/get"));

        // 测试无效主题
        assertNull(IotHttpTopicUtils.parseProductKeyFromTopic("/sys"));
        assertNull(IotHttpTopicUtils.parseProductKeyFromTopic("/single"));
        assertNull(IotHttpTopicUtils.parseProductKeyFromTopic(""));
        assertNull(IotHttpTopicUtils.parseProductKeyFromTopic(null));
    }

    @Test
    void testParseDeviceNameFromTopic() {
        // 测试系统主题
        assertEquals("testDevice",
                IotHttpTopicUtils.parseDeviceNameFromTopic("/sys/testProduct/testDevice/thing/service/property/set"));

        // 测试自定义主题
        assertEquals("testDevice", IotHttpTopicUtils.parseDeviceNameFromTopic("/testProduct/testDevice/user/get"));

        // 测试无效主题
        assertNull(IotHttpTopicUtils.parseDeviceNameFromTopic("/sys/testProduct"));
        assertNull(IotHttpTopicUtils.parseDeviceNameFromTopic("/testProduct"));
        assertNull(IotHttpTopicUtils.parseDeviceNameFromTopic(""));
        assertNull(IotHttpTopicUtils.parseDeviceNameFromTopic(null));
    }

    @Test
    void testIsAuthPath() {
        assertTrue(IotHttpTopicUtils.isAuthPath("/auth"));
        assertFalse(IotHttpTopicUtils.isAuthPath("/topic/test"));
        assertFalse(IotHttpTopicUtils.isAuthPath("/unknown"));
        assertFalse(IotHttpTopicUtils.isAuthPath(null));
        assertFalse(IotHttpTopicUtils.isAuthPath(""));
    }

    @Test
    void testIsTopicPath() {
        assertTrue(IotHttpTopicUtils.isTopicPath("/topic/sys/test/device/property"));
        assertTrue(IotHttpTopicUtils.isTopicPath("/topic/test"));
        assertFalse(IotHttpTopicUtils.isTopicPath("/auth"));
        assertFalse(IotHttpTopicUtils.isTopicPath("/unknown"));
        assertFalse(IotHttpTopicUtils.isTopicPath(null));
        assertFalse(IotHttpTopicUtils.isTopicPath(""));
    }

    @Test
    void testIsValidHttpPath() {
        assertTrue(IotHttpTopicUtils.isValidHttpPath("/auth"));
        assertTrue(IotHttpTopicUtils.isValidHttpPath("/topic/test"));
        assertFalse(IotHttpTopicUtils.isValidHttpPath("/unknown"));
        assertFalse(IotHttpTopicUtils.isValidHttpPath(null));
        assertFalse(IotHttpTopicUtils.isValidHttpPath(""));
    }

    @Test
    void testIsSystemTopic() {
        assertTrue(IotHttpTopicUtils.isSystemTopic("/sys/testProduct/testDevice/thing/service/property/set"));
        assertFalse(IotHttpTopicUtils.isSystemTopic("/testProduct/testDevice/user/get"));
        assertFalse(IotHttpTopicUtils.isSystemTopic("/unknown"));
        assertFalse(IotHttpTopicUtils.isSystemTopic(null));
        assertFalse(IotHttpTopicUtils.isSystemTopic(""));
    }

    @Test
    void testBuildReplyPath() {
        // 测试系统主题响应路径
        String requestPath = "/topic/sys/testProduct/testDevice/thing/service/property/set";
        String replyPath = IotHttpTopicUtils.buildReplyPath(requestPath);
        assertEquals("/topic/sys/testProduct/testDevice/thing/service/property/set_reply", replyPath);

        // 测试非系统主题
        String customPath = "/topic/testProduct/testDevice/user/get";
        assertNull(IotHttpTopicUtils.buildReplyPath(customPath));

        // 测试无效路径
        assertNull(IotHttpTopicUtils.buildReplyPath("/auth"));
        assertNull(IotHttpTopicUtils.buildReplyPath("/unknown"));
        assertNull(IotHttpTopicUtils.buildReplyPath(null));
    }
}