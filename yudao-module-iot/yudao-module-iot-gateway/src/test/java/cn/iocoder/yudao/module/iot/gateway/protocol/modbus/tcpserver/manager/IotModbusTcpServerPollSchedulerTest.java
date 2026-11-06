package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.IotModbusTcpServerConfig;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * {@link IotModbusTcpServerPollScheduler} 的单元测试
 *
 * @author 芋道源码
 */
public class IotModbusTcpServerPollSchedulerTest extends BaseMockitoUnitTest {

    @Mock
    private Vertx vertx;

    @Captor
    private ArgumentCaptor<Handler<Long>> timerCaptor;

    @Test
    public void testBindMinRequestInterval_default() {
        // 调用
        IotModbusTcpServerConfig config = bindConfig(null);
        // 断言
        assertEquals(1000, config.getMinRequestInterval());
    }

    @Test
    public void testBindMinRequestInterval_below100() {
        // 准备参数
        int minRequestInterval = 1;

        // 调用
        IotModbusTcpServerConfig config = bindConfig(minRequestInterval);
        // 断言
        assertEquals(minRequestInterval, config.getMinRequestInterval());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    public void testUpdatePolling_noRequestLimit(int minRequestInterval) {
        // 准备参数
        IotModbusDeviceConfigRespDTO device = createDeviceConfig();
        IotModbusTcpServerPollScheduler scheduler = createScheduler(bindConfig(minRequestInterval)
                .getMinRequestInterval());

        // 调用：连续触发两次点位轮询
        scheduler.updatePolling(device);
        verify(vertx).setPeriodic(eq(200L), timerCaptor.capture());
        Handler<Long> pollTimer = timerCaptor.getValue();
        pollTimer.handle(1L);
        pollTimer.handle(1L);
        // 断言：两次轮询均直接执行
        verify(scheduler, times(2)).pollPoint(1L, 10L);
        verify(vertx, never()).setTimer(anyLong(), any());
    }

    @Test
    public void testUpdatePolling_minRequestInterval() {
        // 准备参数
        int minRequestInterval = bindConfig(60_000).getMinRequestInterval();
        IotModbusDeviceConfigRespDTO device = createDeviceConfig();
        // mock 调度器和延迟定时器：手动触发回调，无需等待定时器到期
        IotModbusTcpServerPollScheduler scheduler = createScheduler(minRequestInterval);
        when(vertx.setTimer(anyLong(), any())).thenReturn(2L, 3L);

        // 调用：连续提交三次轮询，后两次进入队列
        scheduler.updatePolling(device);
        verify(vertx).setPeriodic(eq(200L), timerCaptor.capture());
        Handler<Long> pollTimer = timerCaptor.getValue();
        pollTimer.handle(1L);
        pollTimer.handle(1L);
        pollTimer.handle(1L);
        // 断言：首个请求立即执行，后续请求按配置间隔出队
        verify(scheduler).pollPoint(1L, 10L);
        InOrder timers = inOrder(vertx);
        timers.verify(vertx).setTimer(longThat(delay -> delay > 1000 && delay <= minRequestInterval),
                timerCaptor.capture());
        timerCaptor.getValue().handle(2L);
        verify(scheduler, times(2)).pollPoint(1L, 10L);
        timers.verify(vertx).setTimer(eq((long) minRequestInterval), timerCaptor.capture());
        timerCaptor.getValue().handle(3L);
        verify(scheduler, times(3)).pollPoint(1L, 10L);
    }

    @Test
    public void testStopPolling_cancelDelayTimer() {
        // 准备参数
        IotModbusDeviceConfigRespDTO device = createDeviceConfig();
        // mock 调度器和定时器：旧连接和重连后使用不同的定时器编号
        IotModbusTcpServerPollScheduler scheduler = createScheduler(60_000);
        when(vertx.setPeriodic(anyLong(), any())).thenReturn(1L, 3L);
        when(vertx.setTimer(anyLong(), any())).thenReturn(2L, 4L);

        // 调用：分别为旧连接和重连后的设备排队一个轮询请求
        InOrder timers = inOrder(vertx);
        scheduler.updatePolling(device);
        timers.verify(vertx).setPeriodic(eq(200L), timerCaptor.capture());
        Handler<Long> pollTimer = timerCaptor.getValue();
        pollTimer.handle(1L);
        pollTimer.handle(1L);
        timers.verify(vertx).setTimer(anyLong(), timerCaptor.capture());
        Handler<Long> oldDelayTimer = timerCaptor.getValue();
        scheduler.stopPolling(1L);
        verify(vertx).cancelTimer(2L);
        scheduler.updatePolling(device);
        timers.verify(vertx).setPeriodic(eq(200L), timerCaptor.capture());
        pollTimer = timerCaptor.getValue();
        pollTimer.handle(3L);
        pollTimer.handle(3L);
        timers.verify(vertx).setTimer(anyLong(), timerCaptor.capture());
        Handler<Long> newDelayTimer = timerCaptor.getValue();

        // 调用：模拟旧定时器已入队的回调比新定时器先执行
        oldDelayTimer.handle(2L);
        // 断言：旧回调不处理新请求，新回调仍能正常处理
        verify(scheduler, times(2)).pollPoint(1L, 10L);
        newDelayTimer.handle(4L);
        verify(scheduler, times(3)).pollPoint(1L, 10L);
    }

    private IotModbusTcpServerPollScheduler createScheduler(int minRequestInterval) {
        IotModbusTcpServerPollScheduler scheduler = spy(new IotModbusTcpServerPollScheduler(
                vertx, null, null, null, 5000, minRequestInterval, null));
        doNothing().when(scheduler).pollPoint(1L, 10L);
        return scheduler;
    }

    private static IotModbusDeviceConfigRespDTO createDeviceConfig() {
        IotModbusPointRespDTO point = new IotModbusPointRespDTO();
        point.setId(10L);
        point.setPollInterval(200);
        IotModbusDeviceConfigRespDTO device = new IotModbusDeviceConfigRespDTO();
        device.setDeviceId(1L);
        device.setPoints(List.of(point));
        return device;
    }

    private static IotModbusTcpServerConfig bindConfig(Integer minRequestInterval) {
        String prefix = "yudao.iot.gateway";
        String protocol = prefix + ".protocols[0].";
        Map<String, Object> properties = new HashMap<>();
        // 即使没有设置最小轮询请求间隔，也需要绑定出协议配置对象
        properties.put(protocol + "modbus-tcp-server.request-timeout", 5000);
        if (minRequestInterval != null) {
            properties.put(protocol + "modbus-tcp-server.min-request-interval", minRequestInterval);
        }
        return new Binder(new MapConfigurationPropertySource(properties))
                .bind(prefix, IotGatewayProperties.class).get().getProtocols().get(0).getModbusTcpServer();
    }

}
