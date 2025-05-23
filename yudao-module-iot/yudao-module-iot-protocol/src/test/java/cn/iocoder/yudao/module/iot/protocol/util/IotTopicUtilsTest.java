package cn.iocoder.yudao.module.iot.protocol.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link IotTopicUtils} 单元测试
 *
 * @author haohao
 */
class IotTopicUtilsTest {

    @Test
    void testBuildPropertySetTopic() {
        String topic = IotTopicUtils.buildPropertySetTopic("testProduct", "testDevice");
        assertEquals("/sys/testProduct/testDevice/thing/service/property/set", topic);
    }

    @Test
    void testBuildPropertyGetTopic() {
        String topic = IotTopicUtils.buildPropertyGetTopic("testProduct", "testDevice");
        assertEquals("/sys/testProduct/testDevice/thing/service/property/get", topic);
    }

    @Test
    void testBuildEventPostTopic() {
        String topic = IotTopicUtils.buildEventPostTopic("testProduct", "testDevice", "temperature");
        assertEquals("/sys/testProduct/testDevice/thing/event/temperature/post", topic);
    }

    @Test
    void testGetReplyTopic() {
        String requestTopic = "/sys/testProduct/testDevice/thing/service/property/set";
        String replyTopic = IotTopicUtils.getReplyTopic(requestTopic);
        assertEquals("/sys/testProduct/testDevice/thing/service/property/set_reply", replyTopic);
    }

    @Test
    void testParseProductKeyFromTopic() {
        String topic = "/sys/testProduct/testDevice/thing/service/property/set";
        String productKey = IotTopicUtils.parseProductKeyFromTopic(topic);
        assertEquals("testProduct", productKey);
    }

    @Test
    void testParseDeviceNameFromTopic() {
        String topic = "/sys/testProduct/testDevice/thing/service/property/set";
        String deviceName = IotTopicUtils.parseDeviceNameFromTopic(topic);
        assertEquals("testDevice", deviceName);
    }

    @Test
    void testParseMethodFromTopic() {
        // 测试属性设置
        String topic1 = "/sys/testProduct/testDevice/thing/service/property/set";
        String method1 = IotTopicUtils.parseMethodFromTopic(topic1);
        assertEquals("property.set", method1);

        // 测试事件上报
        String topic2 = "/sys/testProduct/testDevice/thing/event/temperature/post";
        String method2 = IotTopicUtils.parseMethodFromTopic(topic2);
        assertEquals("event.temperature.post", method2);

        // 测试无效主题
        String method3 = IotTopicUtils.parseMethodFromTopic("/invalid/topic");
        assertNull(method3);
    }

    @Test
    void testParseInvalidTopic() {
        // 测试空主题
        assertNull(IotTopicUtils.parseProductKeyFromTopic(""));
        assertNull(IotTopicUtils.parseProductKeyFromTopic(null));

        // 测试格式错误的主题
        assertNull(IotTopicUtils.parseProductKeyFromTopic("/invalid"));
        assertNull(IotTopicUtils.parseDeviceNameFromTopic("/sys/product"));
    }
}