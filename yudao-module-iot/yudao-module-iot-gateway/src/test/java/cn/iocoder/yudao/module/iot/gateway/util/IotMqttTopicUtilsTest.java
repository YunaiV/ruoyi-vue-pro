package cn.iocoder.yudao.module.iot.gateway.util;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

}
