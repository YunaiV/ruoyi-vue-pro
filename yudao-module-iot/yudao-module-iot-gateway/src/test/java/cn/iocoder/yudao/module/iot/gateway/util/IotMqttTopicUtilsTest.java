package cn.iocoder.yudao.module.iot.gateway.util;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link IotMqttTopicUtils} 的单元测试
 *
 * @author 芋道源码
 */
public class IotMqttTopicUtilsTest {

    @Test
    public void testBuildTopicByMethod_shadowUpdateReply() {
        // 准备参数
        String method = IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod();

        // 调用
        String topic = IotMqttTopicUtils.buildTopicByMethod(method, "pk", "dn", true);
        // 断言
        assertEquals("/sys/pk/dn/thing/shadow/update_reply", topic);
    }

    @Test
    public void testBuildTopicByMethod_serviceInvokeReply() {
        // 准备参数
        String method = IotDeviceMessageMethodEnum.SERVICE_INVOKE.getMethod();

        // 调用
        String topic = IotMqttTopicUtils.buildTopicByMethod(method, "pk", "dn", true);
        // 断言
        assertEquals("/sys/pk/dn/thing/service/invoke_reply", topic);
    }

    @Test
    public void testBuildRawPushTopic() {
        // 调用
        String topic = IotMqttTopicUtils.buildRawPushTopic("pk", "dn");
        // 断言
        assertEquals("/sys/pk/dn/thing/raw/push", topic);
    }

    @Test
    public void testIsRawPostTopic() {
        // 正确格式
        assertTrue(IotMqttTopicUtils.isRawPostTopic("/sys/pk/dn/thing/raw/post"));
        // 其它主题
        assertFalse(IotMqttTopicUtils.isRawPostTopic("/sys/pk/dn/thing/raw/push"));
        assertFalse(IotMqttTopicUtils.isRawPostTopic("/sys/pk/dn/thing/property/post"));
        assertFalse(IotMqttTopicUtils.isRawPostTopic("/sys/pk/dn/thing/raw/post_reply"));
        // 格式不正确：缺少 / 多出段、productKey 或 deviceName 为空、前缀不对
        assertFalse(IotMqttTopicUtils.isRawPostTopic("/sys/pk/thing/raw/post"));
        assertFalse(IotMqttTopicUtils.isRawPostTopic("/sys/pk/dn/x/thing/raw/post"));
        assertFalse(IotMqttTopicUtils.isRawPostTopic("/sys//dn/thing/raw/post"));
        assertFalse(IotMqttTopicUtils.isRawPostTopic("/sys/pk//thing/raw/post"));
        assertFalse(IotMqttTopicUtils.isRawPostTopic("/abc/pk/dn/thing/raw/post"));
        assertFalse(IotMqttTopicUtils.isRawPostTopic("/sys/pk/dn/thing/raw/post/"));
        assertFalse(IotMqttTopicUtils.isRawPostTopic(""));
        assertFalse(IotMqttTopicUtils.isRawPostTopic(null));
    }

    @Test
    public void testIsRawPushTopic() {
        assertTrue(IotMqttTopicUtils.isRawPushTopic("/sys/pk/dn/thing/raw/push"));
        assertFalse(IotMqttTopicUtils.isRawPushTopic("/sys/pk/dn/thing/raw/post"));
        assertFalse(IotMqttTopicUtils.isRawPushTopic("/sys/pk/thing/raw/push"));
        assertFalse(IotMqttTopicUtils.isRawPushTopic(null));
    }

    @Test
    public void testIsTopicPublishAllowed_rawPost() {
        // 自己的 raw/post：允许
        assertTrue(IotMqttTopicUtils.isTopicPublishAllowed("/sys/pk/dn/thing/raw/post", "pk", "dn"));
        // 别的设备的 raw/post：拒绝
        assertFalse(IotMqttTopicUtils.isTopicPublishAllowed("/sys/pk/other/thing/raw/post", "pk", "dn"));
        assertFalse(IotMqttTopicUtils.isTopicPublishAllowed("/sys/other/dn/thing/raw/post", "pk", "dn"));
    }

    @Test
    public void testIsTopicSubscribeAllowed_rawPush() {
        // 自己的 raw/push：允许
        assertTrue(IotMqttTopicUtils.isTopicSubscribeAllowed("/sys/pk/dn/thing/raw/push", "pk", "dn"));
        // 别的设备的 raw/push：拒绝
        assertFalse(IotMqttTopicUtils.isTopicSubscribeAllowed("/sys/pk/other/thing/raw/push", "pk", "dn"));
        assertFalse(IotMqttTopicUtils.isTopicSubscribeAllowed("/sys/+/+/thing/raw/push", "pk", "dn"));
    }

}
