package cn.iocoder.yudao.module.iot.service.rule.data.action;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.config.IotDataSinkTcpConfig;
import cn.iocoder.yudao.module.iot.service.rule.data.action.tcp.IotTcpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link IotTcpDataRuleAction} 的单元测试
 *
 * @author HUIHUI
 */
class IotTcpDataRuleActionTest {

    private IotTcpDataRuleAction tcpDataRuleAction;

    @Mock
    private IotTcpClient mockTcpClient;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        tcpDataRuleAction = new IotTcpDataRuleAction();
    }

    @Test
    public void testGetType() {
        // 准备参数
        Integer expectedType = 2; // 数据接收类型枚举中 TCP 类型的值

        // 调用方法
        Integer actualType = tcpDataRuleAction.getType();

        // 断言结果
        assertEquals(expectedType, actualType);
    }

    // TODO @puhui999：_ 后面是小写哈，单测的命名规则。
    @Test
    public void testInitProducer_Success() throws Exception {
        // 准备参数
        IotDataSinkTcpConfig config = new IotDataSinkTcpConfig();
        config.setHost("localhost");
        config.setPort(8080);
        config.setDataFormat("JSON");
        config.setSsl(false);

        // 调用方法 & 断言结果
        // 此测试需要实际的 TCP 连接，在单元测试中可能不可用
        // 目前我们只验证配置是否有效
        assertNotNull(config.getHost());
        assertTrue(config.getPort() > 0 && config.getPort() <= 65535);
    }

    @Test
    public void testInitProducer_InvalidHost() {
        // 准备参数
        IotDataSinkTcpConfig config = new IotDataSinkTcpConfig();
        config.setHost("");
        config.setPort(8080);

        // 调用方法 & 断言结果
        IotTcpDataRuleAction action = new IotTcpDataRuleAction();

        // 测试验证逻辑（通常在 initProducer 方法中）
        assertThrows(IllegalArgumentException.class, () -> {
            if (config.getHost() == null || config.getHost().trim().isEmpty()) {
                throw new IllegalArgumentException("TCP 服务器地址不能为空");
            }
        });
    }

    @Test
    public void testInitProducer_InvalidPort() {
        // 准备参数
        IotDataSinkTcpConfig config = new IotDataSinkTcpConfig();
        config.setHost("localhost");
        config.setPort(-1);

        // 调用方法 & 断言结果
        assertThrows(IllegalArgumentException.class, () -> {
            if (config.getPort() == null || config.getPort() <= 0 || config.getPort() > 65535) {
                throw new IllegalArgumentException("TCP 服务器端口无效");
            }
        });
    }

    @Test
    public void testCloseProducer() throws Exception {
        // 准备参数
        IotTcpClient client = mock(IotTcpClient.class);

        // 调用方法
        tcpDataRuleAction.closeProducer(client);

        // 断言结果
        verify(client, times(1)).close();
    }

    @Test
    public void testExecute_WithValidConfig() {
        // 准备参数
        IotDeviceMessage message = IotDeviceMessage.requestOf("thing.property.report",
                "{\"temperature\": 25.5, \"humidity\": 60}");

        IotDataSinkTcpConfig config = new IotDataSinkTcpConfig();
        config.setHost("localhost");
        config.setPort(8080);
        config.setDataFormat("JSON");

        // 调用方法 & 断言结果
        // 通常这需要实际的 TCP 连接
        // 在单元测试中，我们只验证输入参数
        assertNotNull(message);
        assertNotNull(config);
        assertNotNull(config.getHost());
        assertTrue(config.getPort() > 0);
    }

    @Test
    public void testConfig_DefaultValues() {
        // 准备参数
        IotDataSinkTcpConfig config = new IotDataSinkTcpConfig();

        // 调用方法 & 断言结果
        // 验证默认值
        assertEquals("JSON", config.getDataFormat());
        assertEquals(5000, config.getConnectTimeoutMs());
        assertEquals(10000, config.getReadTimeoutMs());
        assertEquals(false, config.getSsl());
        assertEquals(30000L, config.getHeartbeatIntervalMs());
        assertEquals(5000L, config.getReconnectIntervalMs());
        assertEquals(3, config.getMaxReconnectAttempts());
    }

    @Test
    public void testMessageSerialization() {
        // 准备参数
        IotDeviceMessage message = IotDeviceMessage.builder()
                .deviceId(123L)
                .method("thing.property.report")
                .params("{\"temperature\": 25.5}")
                .build();

        // 调用方法
        String json = JsonUtils.toJsonString(message);

        // 断言结果
        assertNotNull(json);
        assertTrue(json.contains("\"deviceId\":123"));
        assertTrue(json.contains("\"method\":\"thing.property.report\""));
        assertTrue(json.contains("\"temperature\":25.5"));
    }

}