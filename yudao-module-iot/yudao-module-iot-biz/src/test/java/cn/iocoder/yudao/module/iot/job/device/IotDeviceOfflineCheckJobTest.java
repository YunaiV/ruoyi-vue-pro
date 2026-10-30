package cn.iocoder.yudao.module.iot.job.device;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.enums.device.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.enums.product.IotProductDeviceTypeEnum;
import cn.iocoder.yudao.module.iot.framework.iot.config.YudaoIotProperties;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.data.redis.RedisConnectionFailureException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

/**
 * {@link IotDeviceOfflineCheckJob} 的单元测试
 *
 * @author 芋道源码
 */
public class IotDeviceOfflineCheckJobTest extends BaseMockitoUnitTest {

    /**
     * 最后上报时间：默认超时为 10 分钟 × 1.5 = 15 分钟
     */
    private static final LocalDateTime REPORT_TIME = LocalDateTime.of(2026, 10, 29, 10, 0, 0);

    @InjectMocks
    private IotDeviceOfflineCheckJob job;

    @Spy
    private YudaoIotProperties iotProperties = new YudaoIotProperties();
    @Mock
    private IotDeviceService deviceService;
    @Mock
    private IotDevicePropertyService devicePropertyService;
    @Mock
    private IotDeviceMessageService deviceMessageService;
    @Mock
    private IotProductService productService;

    @Test
    public void testExecute_activityBased() {
        // mock 数据：HTTP 直连设备、MQTT 网关的子设备已超时；另一个 HTTP 设备未超时
        IotDeviceDO httpDevice = buildDevice(1L, 10L, IotProductDeviceTypeEnum.DIRECT, REPORT_TIME);
        IotDeviceDO subDevice = buildDevice(2L, 20L, IotProductDeviceTypeEnum.GATEWAY_SUB, REPORT_TIME);
        IotDeviceDO activeDevice = buildDevice(3L, 10L, IotProductDeviceTypeEnum.DIRECT, REPORT_TIME);
        when(deviceService.getDeviceListByState(eq(IotDeviceStateEnum.ONLINE.getState())))
                .thenReturn(Arrays.asList(httpDevice, subDevice, activeDevice));
        mockTimeoutDevices(httpDevice, subDevice);
        mockProduct(10L, IotProtocolTypeEnum.HTTP);

        // 调用
        job.execute(null);
        // 断言：超时即离线，事件时间 = 最后上报时间 + 超时时间；子设备不检查网关存活
        verifyOffline(httpDevice);
        verifyOffline(subDevice);
        verify(deviceMessageService, never()).sendDeviceMessage(any(), same(activeDevice), any(), any());
        verify(devicePropertyService, never()).getDeviceServerId(any());
    }

    @Test
    public void testExecute_connectionBased() {
        // mock 数据：4 个已超时的 MQTT 设备，网关 server-1 在 REPORT_TIME - 1h 的 .100 启动
        // device1：在 server-1 启动后上线，连接仍在 → 保持在线
        // device2：在 server-1 启动前上线（之后网关重启过） → 离线
        // device3：网关 server-2 的存活标记已过期 → 离线
        // device4：与 server-1 启动在同一秒上线（实际 .200，按秒存储为 .000） → 保持在线
        LocalDateTime startTime = REPORT_TIME.minusHours(1);
        IotDeviceDO device1 = buildDevice(1L, 10L, IotProductDeviceTypeEnum.DIRECT, REPORT_TIME);
        IotDeviceDO device2 = buildDevice(2L, 10L, IotProductDeviceTypeEnum.DIRECT, REPORT_TIME.minusHours(2));
        IotDeviceDO device3 = buildDevice(3L, 10L, IotProductDeviceTypeEnum.DIRECT, REPORT_TIME);
        IotDeviceDO device4 = buildDevice(4L, 10L, IotProductDeviceTypeEnum.DIRECT, startTime);
        when(deviceService.getDeviceListByState(eq(IotDeviceStateEnum.ONLINE.getState())))
                .thenReturn(Arrays.asList(device1, device2, device3, device4));
        mockTimeoutDevices(device1, device2, device3, device4);
        mockProduct(10L, IotProtocolTypeEnum.MQTT);
        when(devicePropertyService.getDeviceServerId(eq(1L))).thenReturn("server-1");
        when(devicePropertyService.getDeviceServerId(eq(2L))).thenReturn("server-1");
        when(devicePropertyService.getDeviceServerId(eq(3L))).thenReturn("server-2");
        when(devicePropertyService.getDeviceServerId(eq(4L))).thenReturn("server-1");
        when(devicePropertyService.getGatewayServerStartTime(eq("server-1")))
                .thenReturn(LocalDateTimeUtil.toEpochMilli(startTime) + 100);
        when(devicePropertyService.getGatewayServerStartTime(eq("server-2"))).thenReturn(null);

        // 调用
        job.execute(null);
        // 断言
        verify(deviceMessageService, never()).sendDeviceMessage(any(), same(device1), any(), any());
        verify(deviceMessageService, never()).sendDeviceMessage(any(), same(device4), any(), any());
        verifyOffline(device2);
        verifyOffline(device3);
        // 断言：同一个 serverId，本轮只读取一次
        verify(devicePropertyService).getGatewayServerStartTime(eq("server-1"));
    }

    @Test
    public void testExecute_connectionBasedRedisFailed() {
        // mock 数据：已超时的 TCP 设备
        IotDeviceDO device = buildDevice(1L, 10L, IotProductDeviceTypeEnum.DIRECT, REPORT_TIME);
        when(deviceService.getDeviceListByState(eq(IotDeviceStateEnum.ONLINE.getState())))
                .thenReturn(Arrays.asList(device));
        mockTimeoutDevices(device);
        mockProduct(10L, IotProtocolTypeEnum.TCP);
        when(devicePropertyService.getDeviceServerId(eq(1L))).thenReturn("server-1");
        when(devicePropertyService.getGatewayServerStartTime(eq("server-1")))
                .thenThrow(new RedisConnectionFailureException("Redis 不可用"));

        // 调用
        job.execute(null);
        // 断言：读取失败不等于标记不存在，本轮不判离线
        verify(deviceMessageService, never()).sendDeviceMessage(any(), any(), any(), any());
    }

    @Test
    public void testExecute_emqx() {
        // mock 数据：已超时的 EMQX 设备
        IotDeviceDO device = buildDevice(1L, 10L, IotProductDeviceTypeEnum.DIRECT, REPORT_TIME);
        when(deviceService.getDeviceListByState(eq(IotDeviceStateEnum.ONLINE.getState())))
                .thenReturn(Arrays.asList(device));
        mockTimeoutDevices(device);
        mockProduct(10L, IotProtocolTypeEnum.EMQX);

        // 调用
        job.execute(null);
        // 断言：连接由 EMQX 维护，不使用存活标记，也不判离线
        verify(deviceMessageService, never()).sendDeviceMessage(any(), any(), any(), any());
        verify(devicePropertyService, never()).getGatewayServerStartTime(any());
    }

    // ========== 辅助方法 ==========

    private void mockTimeoutDevices(IotDeviceDO... devices) {
        Map<Long, LocalDateTime> timeoutDevices = new HashMap<>();
        for (IotDeviceDO device : devices) {
            timeoutDevices.put(device.getId(), REPORT_TIME);
        }
        when(devicePropertyService.getDeviceReportTimeMapByReportTime(any())).thenReturn(timeoutDevices);
    }

    private void mockProduct(Long productId, IotProtocolTypeEnum protocolType) {
        when(productService.getProductFromCache(eq(productId)))
                .thenReturn(new IotProductDO().setId(productId).setProtocolType(protocolType.getType()));
    }

    private void verifyOffline(IotDeviceDO device) {
        verify(deviceMessageService).sendDeviceMessage(argThat((IotDeviceMessage m) ->
                        IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod().equals(m.getMethod())
                                && String.valueOf(IotDeviceStateEnum.OFFLINE.getState()).equals(IotDeviceMessageUtils.getIdentifier(m))),
                same(device), isNull(), eq(REPORT_TIME.plusMinutes(15)));
    }

    private static IotDeviceDO buildDevice(Long id, Long productId, IotProductDeviceTypeEnum deviceType,
                                           LocalDateTime onlineTime) {
        return IotDeviceDO.builder().id(id).productId(productId).deviceType(deviceType.getType())
                .state(IotDeviceStateEnum.ONLINE.getState()).onlineTime(onlineTime).build();
    }

}
