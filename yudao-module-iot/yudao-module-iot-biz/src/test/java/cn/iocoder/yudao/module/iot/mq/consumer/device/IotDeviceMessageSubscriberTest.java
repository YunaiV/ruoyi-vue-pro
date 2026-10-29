package cn.iocoder.yudao.module.iot.mq.consumer.device;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.device.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.QueryTimeoutException;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 * {@link IotDeviceMessageSubscriber} 的单元测试类
 *
 * @author 芋道源码
 */
public class IotDeviceMessageSubscriberTest extends BaseMockitoUnitTest {

    @InjectMocks
    private IotDeviceMessageSubscriber deviceMessageSubscriber;

    @Mock
    private IotDeviceService deviceService;
    @Mock
    private IotDevicePropertyService devicePropertyService;
    @Mock
    private IotDeviceMessageService deviceMessageService;
    @Mock
    private IotMessageBus messageBus;

    @Test
    public void testOnMessage_success() {
        // 准备参数
        IotDeviceMessage message = buildPropertyPostMessage();
        // mock 方法：设备在线
        IotDeviceDO device = new IotDeviceDO().setId(message.getDeviceId())
                .setState(IotDeviceStateEnum.ONLINE.getState());
        when(deviceService.getDeviceFromCache(eq(message.getDeviceId()))).thenReturn(device);

        // 调用
        deviceMessageSubscriber.onMessage(message);
        // 断言
        verify(devicePropertyService).updateDeviceReportTimeAsync(eq(device.getId()), any(LocalDateTime.class));
        verify(devicePropertyService).updateDeviceServerIdAsync(eq(device.getId()), eq(message.getServerId()));
        verify(deviceMessageService).handleUpstreamDeviceMessage(eq(message), eq(device));
    }

    @Test
    public void testOnMessage_deviceNotExists() {
        // 准备参数：设备已删除时，消息仍在途
        IotDeviceMessage message = buildPropertyPostMessage();
        // mock 方法
        when(deviceService.getDeviceFromCache(eq(message.getDeviceId()))).thenReturn(null);

        // 调用：正常返回，由消息总线 ack，不再重投
        assertDoesNotThrow(() -> deviceMessageSubscriber.onMessage(message));
        // 断言
        verifyNoInteractions(devicePropertyService, deviceMessageService);
    }

    @Test
    public void testOnMessage_queryException() {
        // 准备参数
        IotDeviceMessage message = buildPropertyPostMessage();
        // mock 方法：查询设备时 Redis 超时
        when(deviceService.getDeviceFromCache(eq(message.getDeviceId())))
                .thenThrow(new QueryTimeoutException("Redis 超时"));

        // 调用，并断言异常：可恢复异常继续抛出，消息不 ack，由重投任务重试
        assertThrows(QueryTimeoutException.class, () -> deviceMessageSubscriber.onMessage(message));
        verifyNoInteractions(devicePropertyService, deviceMessageService);
    }

    @Test
    public void testOnMessage_replyForceOnline() {
        // 准备参数：设备实际返回的回复（例如 Modbus 异常响应），按原逻辑作为设备活动
        IotDeviceMessage message = new IotDeviceMessage().setDeviceId(randomLongId()).setTenantId(randomLongId())
                .setServerId("test-server").setMethod(IotDeviceMessageMethodEnum.PROPERTY_SET.getMethod())
                .setCode(1_051_003_111).setReportTime(LocalDateTime.of(2026, 10, 29, 10, 0, 0));
        // mock 方法：设备离线
        IotDeviceDO device = new IotDeviceDO().setId(message.getDeviceId())
                .setState(IotDeviceStateEnum.OFFLINE.getState());
        when(deviceService.getDeviceFromCache(eq(message.getDeviceId()))).thenReturn(device);

        // 调用
        deviceMessageSubscriber.onMessage(message);
        // 断言：更新上报时间、连接 server，并强制上线（事件时间沿用源消息的上报时间）
        verify(devicePropertyService).updateDeviceReportTimeAsync(eq(device.getId()), any(LocalDateTime.class));
        verify(devicePropertyService).updateDeviceServerIdAsync(eq(device.getId()), eq(message.getServerId()));
        verify(deviceMessageService).sendDeviceMessage(argThat((IotDeviceMessage m) ->
                IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod().equals(m.getMethod())),
                same(device), isNull(), eq(message.getReportTime()));
        verify(deviceMessageService).handleUpstreamDeviceMessage(eq(message), eq(device));
    }

    @Test
    public void testOnMessage_stateOffline() {
        // 准备参数：网关连接断开的离线
        IotDeviceMessage message = IotDeviceMessage.buildStateOffline().setDeviceId(randomLongId())
                .setTenantId(randomLongId()).setServerId("old-server");
        // mock 方法：设备在线
        IotDeviceDO device = new IotDeviceDO().setId(message.getDeviceId())
                .setState(IotDeviceStateEnum.ONLINE.getState());
        when(deviceService.getDeviceFromCache(eq(message.getDeviceId()))).thenReturn(device);

        // 调用
        deviceMessageSubscriber.onMessage(message);
        // 断言：离线不算活跃，也不改变连接的 server
        verify(devicePropertyService, never()).updateDeviceReportTimeAsync(any(), any());
        verify(devicePropertyService, never()).updateDeviceServerIdAsync(any(), any());
        verify(deviceMessageService).handleUpstreamDeviceMessage(eq(message), eq(device));
    }

    @Test
    public void testOnMessage_oldMessageKeepServerId() {
        // 准备参数：早于当前上线时间的旧消息（例如说，旧连接上迟到的属性上报）
        IotDeviceMessage message = buildPropertyPostMessage().setServerId("old-server")
                .setReportTime(LocalDateTime.of(2026, 10, 29, 10, 0, 0));
        // mock 方法：设备已经在新连接上线
        IotDeviceDO device = new IotDeviceDO().setId(message.getDeviceId())
                .setState(IotDeviceStateEnum.ONLINE.getState()).setOnlineTime(LocalDateTime.of(2026, 10, 29, 10, 1, 0));
        when(deviceService.getDeviceFromCache(eq(message.getDeviceId()))).thenReturn(device);

        // 调用
        deviceMessageSubscriber.onMessage(message);
        // 断言：照常记录活跃，但不改路由
        verify(devicePropertyService).updateDeviceReportTimeAsync(eq(device.getId()), any(LocalDateTime.class));
        verify(devicePropertyService, never()).updateDeviceServerIdAsync(any(), any());
        verify(deviceMessageService).handleUpstreamDeviceMessage(eq(message), eq(device));
    }

    private static IotDeviceMessage buildPropertyPostMessage() {
        return new IotDeviceMessage().setDeviceId(randomLongId()).setTenantId(randomLongId())
                .setServerId("test-server").setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod());
    }

}
