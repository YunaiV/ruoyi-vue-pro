package cn.iocoder.yudao.module.iot.gateway.protocol;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotGatewayServerAliveReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link IotProtocolManager} 的单元测试：协议实例的存活上报
 *
 * @author 芋道源码
 */
public class IotProtocolManagerTest {

    private static final String THREAD_NAME_PREFIX = "iot-gateway-alive-";

    private IotDeviceCommonApi deviceApi;

    private IotProtocol mqttProtocol;
    private IotProtocol tcpProtocol;
    private IotProtocol emqxProtocol;
    private IotProtocolManager protocolManager;

    @BeforeEach
    public void setUp() {
        deviceApi = mock(IotDeviceCommonApi.class);
        when(deviceApi.keepAliveGatewayServers(any())).thenReturn(success(true));
        // 协议实例：MQTT、TCP 运行中；EMQX 不上报存活标记
        mqttProtocol = mockProtocol("mqtt-1", "server-mqtt", IotProtocolTypeEnum.MQTT);
        tcpProtocol = mockProtocol("tcp-1", "server-tcp", IotProtocolTypeEnum.TCP);
        emqxProtocol = mockProtocol("emqx-1", "server-emqx", IotProtocolTypeEnum.EMQX);
        Map<String, IotProtocol> protocols = new HashMap<>();
        protocols.put("mqtt-1", mqttProtocol);
        protocols.put("tcp-1", tcpProtocol);
        protocols.put("emqx-1", emqxProtocol);
        IotGatewayProperties gatewayProperties = new IotGatewayProperties();
        gatewayProperties.setProtocols(Arrays.asList(buildConfig("mqtt-1"), buildConfig("tcp-1"), buildConfig("emqx-1")));
        protocolManager = new IotProtocolManager(gatewayProperties, deviceApi) {

            @Override
            IotProtocol createProtocol(IotGatewayProperties.ProtocolProperties config) {
                return protocols.get(config.getId());
            }

        };
    }

    @AfterEach
    public void tearDown() {
        protocolManager.stop();
    }

    @Test
    public void testStart_writeAlive() {
        // 调用
        long before = System.currentTimeMillis();
        protocolManager.start();
        long after = System.currentTimeMillis();

        // 断言：启动后立即上报一次，一次批量包含全部运行中的非 EMQX 协议实例
        Map<String, Long> startTimes = awaitFirstWriteAlive().getStartTimes();
        assertEquals(new HashSet<>(Arrays.asList("server-mqtt", "server-tcp")), startTimes.keySet());
        // 断言：值为协议实例的启动时间
        assertTrue(startTimes.get("server-mqtt") >= before && startTimes.get("server-mqtt") <= after);
        assertTrue(startTimes.get("server-tcp") >= before && startTimes.get("server-tcp") <= after);
    }

    @Test
    public void testWriteAlive_startTimeNotChange() {
        // 准备参数
        protocolManager.start();
        Map<String, Long> firstStartTimes = awaitFirstWriteAlive().getStartTimes();

        // 调用：续期两轮
        protocolManager.writeAlive();
        protocolManager.writeAlive();
        // 断言：每轮只调用一次，启动时间不随续期变化
        ArgumentCaptor<IotGatewayServerAliveReqDTO> reqCaptor = ArgumentCaptor.forClass(IotGatewayServerAliveReqDTO.class);
        verify(deviceApi, times(2)).keepAliveGatewayServers(reqCaptor.capture());
        List<IotGatewayServerAliveReqDTO> reqDTOs = reqCaptor.getAllValues();
        assertEquals(firstStartTimes, reqDTOs.get(0).getStartTimes());
        assertEquals(firstStartTimes, reqDTOs.get(1).getStartTimes());
    }

    @Test
    public void testWriteAlive_skipNotRunning() {
        // mock 方法：TCP 已停止
        when(tcpProtocol.isRunning()).thenReturn(false);

        // 调用
        protocolManager.start();
        // 断言：只上报运行中的协议实例
        assertEquals(Collections.singleton("server-mqtt"), awaitFirstWriteAlive().getStartTimes().keySet());
    }

    @Test
    public void testWriteAlive_empty() {
        // mock 方法：MQTT、TCP 都已停止，只剩下 EMQX
        when(mqttProtocol.isRunning()).thenReturn(false);
        when(tcpProtocol.isRunning()).thenReturn(false);
        protocolManager.start();

        // 调用
        protocolManager.writeAlive();
        // 断言：没有需要上报的协议实例，不调用
        verify(deviceApi, never()).keepAliveGatewayServers(any());
    }

    @Test
    public void testWriteAlive_failNotThrow() {
        // 准备参数
        protocolManager.start();
        awaitFirstWriteAlive();
        // mock 方法：依次返回 error、部分失败、直接抛出异常（同 JVM 直接调用时），最后恢复成功
        when(deviceApi.keepAliveGatewayServers(any())).thenReturn(CommonResult.error(INTERNAL_SERVER_ERROR))
                .thenReturn(success(false)).thenThrow(new IllegalStateException("biz 不可用")).thenReturn(success(true));

        // 调用：异常不抛出（否则周期任务会永久停止），每轮都照常上报
        for (int i = 0; i < 4; i++) {
            assertDoesNotThrow(() -> protocolManager.writeAlive());
        }
        // 断言
        verify(deviceApi, times(4)).keepAliveGatewayServers(any());
    }

    @Test
    public void testStopAndRestart() throws Exception {
        // 调用：启动
        protocolManager.start();
        long firstStartTime = awaitFirstWriteAlive().getStartTimes().get("server-mqtt");

        // 调用：停止，重复停止
        protocolManager.stop();
        protocolManager.stop();
        // 断言：协议只停止一次，续期线程已退出
        verify(mqttProtocol).stop();
        assertFalse(protocolManager.isRunning());
        assertNoAliveThread();
        // 断言：停止后不再上报
        protocolManager.writeAlive();
        verify(deviceApi, never()).keepAliveGatewayServers(any());

        // 调用：同 serverId 重新启动
        Thread.sleep(5);
        protocolManager.start();
        // 断言：启动时间已更新
        assertTrue(awaitFirstWriteAlive().getStartTimes().get("server-mqtt") > firstStartTime);
        verify(mqttProtocol, times(2)).start();
    }

    // ========== 辅助方法 ==========

    /**
     * 等待启动后立即执行的那一轮上报，并清空调用记录；下一轮在 20 秒后，不会干扰后续断言
     */
    private IotGatewayServerAliveReqDTO awaitFirstWriteAlive() {
        ArgumentCaptor<IotGatewayServerAliveReqDTO> reqCaptor = ArgumentCaptor.forClass(IotGatewayServerAliveReqDTO.class);
        verify(deviceApi, timeout(2000)).keepAliveGatewayServers(reqCaptor.capture());
        clearInvocations(deviceApi);
        return reqCaptor.getValue();
    }

    private static void assertNoAliveThread() throws InterruptedException {
        // 线程池终止后，工作线程的退出稍有延迟
        for (int i = 0; i < 100; i++) {
            boolean alive = Thread.getAllStackTraces().keySet().stream()
                    .anyMatch(thread -> thread.getName().startsWith(THREAD_NAME_PREFIX) && thread.isAlive());
            if (!alive) {
                return;
            }
            Thread.sleep(10);
        }
        fail("续期线程没有退出");
    }

    private static IotProtocol mockProtocol(String id, String serverId, IotProtocolTypeEnum type) {
        IotProtocol protocol = mock(IotProtocol.class);
        when(protocol.getId()).thenReturn(id);
        when(protocol.getServerId()).thenReturn(serverId);
        when(protocol.getType()).thenReturn(type);
        when(protocol.isRunning()).thenReturn(true);
        return protocol;
    }

    private static IotGatewayProperties.ProtocolProperties buildConfig(String id) {
        IotGatewayProperties.ProtocolProperties config = new IotGatewayProperties.ProtocolProperties();
        config.setId(id);
        return config;
    }

}
