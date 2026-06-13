package cn.iocoder.yudao.module.iot.service.rule.data.action;

import cn.iocoder.yudao.module.iot.enums.rule.IotDataSinkTypeEnum;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * {@link IotMqttDataRuleAction} 的单元测试
 *
 * @author HUIHUI
 */
class IotMqttDataRuleActionTest {

    private IotMqttDataRuleAction mqttDataRuleAction;

    @Mock
    private MqttClient mqttClient;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mqttDataRuleAction = new IotMqttDataRuleAction();
    }

    @Test
    public void testGetType() {
        // 调用 & 断言：返回 MQTT 类型枚举值
        assertEquals(IotDataSinkTypeEnum.MQTT.getType(), mqttDataRuleAction.getType());
    }

    @Test
    public void testCloseProducer_whenConnected() throws Exception {
        // 准备：连接中状态
        when(mqttClient.isConnected()).thenReturn(true);

        // 调用
        mqttDataRuleAction.closeProducer(mqttClient);

        // 断言：先 disconnect 再 close
        verify(mqttClient, times(1)).disconnect();
        verify(mqttClient, times(1)).close();
    }

    @Test
    public void testCloseProducer_whenAlreadyDisconnected() throws Exception {
        // 准备：已断开状态
        when(mqttClient.isConnected()).thenReturn(false);

        // 调用
        mqttDataRuleAction.closeProducer(mqttClient);

        // 断言：跳过 disconnect，仅 close
        verify(mqttClient, never()).disconnect();
        verify(mqttClient, times(1)).close();
    }

}
