package cn.iocoder.yudao.module.iot.gateway.util;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link IotMqttTopicUtils} 单元测试
 */
class IotMqttTopicUtilsTest {

    private static final String PRODUCT_KEY = "pk";
    private static final String DEVICE_NAME = "dn";

    @Test
    void testBuildTopicByMethod_shadowUpdateReplyAppendsReplySuffix() {
        String topic = IotMqttTopicUtils.buildTopicByMethod(
                IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod(), PRODUCT_KEY, DEVICE_NAME, true);
        assertEquals("/sys/pk/dn/thing/shadow/update_reply", topic);
    }

    @Test
    void testBuildTopicByMethod_serviceInvokeReplyAppendsReplySuffix() {
        String topic = IotMqttTopicUtils.buildTopicByMethod(
                IotDeviceMessageMethodEnum.SERVICE_INVOKE.getMethod(), PRODUCT_KEY, DEVICE_NAME, true);
        assertEquals("/sys/pk/dn/thing/service/invoke_reply", topic);
    }

}
