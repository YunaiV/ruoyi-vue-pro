package cn.iocoder.yudao.module.iot.service.device.shadow;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceShadowDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceShadowMapper;
import cn.iocoder.yudao.module.iot.dal.redis.device.DeviceShadowRedisDAO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link IotDeviceShadowServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
public class IotDeviceShadowServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private IotDeviceShadowServiceImpl deviceShadowService;

    @Mock
    private IotDeviceShadowMapper deviceShadowMapper;
    @Mock
    private DeviceShadowRedisDAO deviceShadowRedisDAO;
    @Mock
    private IotDeviceService deviceService;
    @Mock
    private IotDevicePropertyService devicePropertyService;
    @Mock
    private IotDeviceMessageService deviceMessageService;

    // ========== getOrCreateShadow ==========

    @Test
    public void testGetOrCreateShadow_cacheHit() {
        // mock 数据
        IotDeviceDO device = buildDevice();
        IotDeviceShadowDO cacheShadow = buildShadow(device, 2L);
        // mock 方法
        when(deviceShadowRedisDAO.get(device.getId())).thenReturn(cacheShadow);

        // 调用
        IotDeviceShadowDO shadow = deviceShadowService.getOrCreateShadow(device);
        // 断言
        assertSame(cacheShadow, shadow);
        verifyNoInteractions(deviceShadowMapper);
    }

    @Test
    public void testGetOrCreateShadow_notExists() {
        // 准备参数
        IotDeviceDO device = buildDevice();
        // mock 方法
        when(deviceShadowRedisDAO.get(device.getId())).thenReturn(null);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(null);

        // 调用
        IotDeviceShadowDO shadow = deviceShadowService.getOrCreateShadow(device);
        // 断言：插入启用的空影子，并回填缓存
        verify(deviceShadowMapper).insert(shadow);
        assertEquals(device.getId(), shadow.getDeviceId());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), shadow.getStatus());
        assertEquals(0L, shadow.getVersion());
        assertTrue(shadow.getReported().isEmpty());
        verify(deviceShadowRedisDAO).set(shadow);
    }

    // ========== handleDeviceShadowUpdate：get ==========

    @Test
    public void testHandleDeviceShadowUpdate_get() {
        // mock 数据
        IotDeviceDO device = buildDevice();
        IotDeviceShadowDO dbShadow = buildShadow(device, 2L);
        dbShadow.getDesired().put("PowerSwitch", 1);
        // mock 方法
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(dbShadow);
        // 准备参数
        IotDeviceMessage message = buildMessage(device, Map.of("method", "get"));

        // 调用
        deviceShadowService.handleDeviceShadowUpdate(device, message);
        // 断言：读 DB 回复全量影子
        IotDeviceMessage reply = captureReply(device);
        assertEquals(IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod(), reply.getMethod());
        assertEquals("req-1", reply.getRequestId());
        assertEquals(0, reply.getCode());
        assertEquals(2L, getReplyVersion(reply));
        assertEquals(Map.of("PowerSwitch", 1), getReplyState(reply).get("desired"));
        verify(deviceShadowRedisDAO, never()).get(any());
    }

    @Test
    public void testHandleDeviceShadowUpdate_disabledInDb() {
        // mock 数据：缓存为启用的旧影子，DB 已关闭
        IotDeviceDO device = buildDevice();
        lenient().when(deviceShadowRedisDAO.get(device.getId())).thenReturn(buildShadow(device, 0L));
        when(deviceShadowMapper.selectByDeviceId(device.getId()))
                .thenReturn(buildShadow(device, 0L, CommonStatusEnum.DISABLE.getStatus()));
        // 准备参数
        IotDeviceMessage message = buildMessage(device, buildUpdateParams(Map.of("Temperature", 25), null));

        // 调用
        deviceShadowService.handleDeviceShadowUpdate(device, message);
        // 断言：以 DB 为准，忽略上行
        verify(deviceShadowMapper, never()).updateByDeviceIdAndVersion(any(), any(), any());
        verify(deviceMessageService, never()).sendDeviceMessage(any(), any(), any());
    }

    // ========== handleDeviceShadowUpdate：update ==========

    @Test
    public void testHandleDeviceShadowUpdate_update_success() {
        // mock 数据
        IotDeviceDO device = buildDevice();
        IotDeviceShadowDO dbShadow = buildShadow(device, 1L);
        dbShadow.getReported().put("Temperature", 20);
        dbShadow.getDesired().put("PowerSwitch", 1);
        // mock 方法
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(dbShadow);
        when(deviceShadowMapper.updateByDeviceIdAndVersion(any(), eq(device.getId()), eq(1L))).thenReturn(1);
        // 准备参数
        IotDeviceMessage message = buildMessage(device,
                buildUpdateParams(Map.of("Temperature", 25, "Humidity", 60), 1L));

        // 调用
        deviceShadowService.handleDeviceShadowUpdate(device, message);
        // 断言：合并 reported，保留 desired，version + 1
        IotDeviceShadowDO update = captureUpdate(device, 1L);
        assertEquals(Map.of("Temperature", 25, "Humidity", 60), update.getReported());
        assertEquals(Map.of("PowerSwitch", 1), update.getDesired());
        assertEquals(2L, update.getVersion());
        verify(deviceShadowRedisDAO).delete(device.getId());
        // 断言：回复新版本
        IotDeviceMessage reply = captureReply(device);
        assertEquals(0, reply.getCode());
        assertEquals(2L, getReplyVersion(reply));
    }

    @Test
    public void testHandleDeviceShadowUpdate_update_staleCacheUseDbVersion() {
        // mock 数据：缓存为 v1 的旧影子，DB 已是 v2
        IotDeviceDO device = buildDevice();
        lenient().when(deviceShadowRedisDAO.get(device.getId())).thenReturn(buildShadow(device, 1L));
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(buildShadow(device, 2L));
        when(deviceShadowMapper.updateByDeviceIdAndVersion(any(), eq(device.getId()), eq(2L))).thenReturn(1);
        // 准备参数：设备携带 DB 当前版本 v2
        IotDeviceMessage message = buildMessage(device, buildUpdateParams(Map.of("Temperature", 25), 2L));

        // 调用
        deviceShadowService.handleDeviceShadowUpdate(device, message);
        // 断言：按 DB 版本写入成功
        assertEquals(3L, captureUpdate(device, 2L).getVersion());
        assertEquals(0, captureReply(device).getCode());
        verify(deviceShadowRedisDAO, never()).get(any());
    }

    @Test
    public void testHandleDeviceShadowUpdate_update_versionConflict() {
        // mock 数据
        IotDeviceDO device = buildDevice();
        // mock 方法
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(buildShadow(device, 5L));
        // 准备参数：设备携带旧版本 v3
        IotDeviceMessage message = buildMessage(device, buildUpdateParams(Map.of("Temperature", 25), 3L));

        // 调用
        deviceShadowService.handleDeviceShadowUpdate(device, message);
        // 断言：不写入，回复 409 及 DB 当前版本
        verify(deviceShadowMapper, never()).updateByDeviceIdAndVersion(any(), any(), any());
        IotDeviceMessage reply = captureReply(device);
        assertEquals(409, reply.getCode());
        assertEquals(5L, getReplyVersion(reply));
    }

    @Test
    public void testHandleDeviceShadowUpdate_update_versionNull() {
        // mock 数据
        IotDeviceDO device = buildDevice();
        // mock 方法
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(buildShadow(device, 5L));
        when(deviceShadowMapper.updateByDeviceIdAndVersion(any(), eq(device.getId()), eq(5L))).thenReturn(1);
        // 准备参数：不传 version，强制更新
        IotDeviceMessage message = buildMessage(device, buildUpdateParams(Map.of("Temperature", 25), null));

        // 调用
        deviceShadowService.handleDeviceShadowUpdate(device, message);
        // 断言
        assertEquals(6L, captureUpdate(device, 5L).getVersion());
    }

    @Test
    public void testHandleDeviceShadowUpdate_update_retryFromLatest() {
        // mock 数据：前两次读到 v1，之后读到 v2
        IotDeviceDO device = buildDevice();
        IotDeviceShadowDO staleShadow = buildShadow(device, 1L);
        staleShadow.getReported().put("Temperature", 20);
        IotDeviceShadowDO latestShadow = buildShadow(device, 2L);
        latestShadow.getReported().put("Temperature", 30);
        latestShadow.getReported().put("Humidity", 50);
        // mock 方法：v1 写入冲突，v2 写入成功
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(staleShadow, staleShadow, latestShadow);
        when(deviceShadowMapper.updateByDeviceIdAndVersion(any(), eq(device.getId()), eq(1L))).thenReturn(0);
        when(deviceShadowMapper.updateByDeviceIdAndVersion(any(), eq(device.getId()), eq(2L))).thenReturn(1);
        // 准备参数
        IotDeviceMessage message = buildMessage(device, buildUpdateParams(Map.of("Temperature", 25), null));

        // 调用
        deviceShadowService.handleDeviceShadowUpdate(device, message);
        // 断言：重试时基于 v2 合并，保留 Humidity
        IotDeviceShadowDO update = captureUpdate(device, 2L);
        assertEquals(Map.of("Temperature", 25, "Humidity", 50), update.getReported());
        assertEquals(3L, update.getVersion());
        assertEquals(3L, getReplyVersion(captureReply(device)));
    }

    @Test
    public void testHandleDeviceShadowUpdate_update_retryExhausted() {
        // mock 数据
        IotDeviceDO device = buildDevice();
        // mock 方法：每次写入都冲突
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(buildShadow(device, 4L));
        when(deviceShadowMapper.updateByDeviceIdAndVersion(any(), eq(device.getId()), eq(4L))).thenReturn(0);
        // 准备参数
        IotDeviceMessage message = buildMessage(device, buildUpdateParams(Map.of("Temperature", 25), null));

        // 调用
        deviceShadowService.handleDeviceShadowUpdate(device, message);
        // 断言：重试 3 次后，回复 409
        verify(deviceShadowMapper, times(3)).updateByDeviceIdAndVersion(any(), eq(device.getId()), eq(4L));
        verify(deviceShadowRedisDAO, never()).delete(any());
        IotDeviceMessage reply = captureReply(device);
        assertEquals(409, reply.getCode());
        assertEquals(4L, getReplyVersion(reply));
    }

    @Test
    public void testHandleDeviceShadowUpdate_update_propertyCountExceeded() {
        // mock 数据：已有 128 个属性
        IotDeviceDO device = buildDevice();
        IotDeviceShadowDO dbShadow = buildShadow(device, 1L);
        for (int i = 0; i < 128; i++) {
            dbShadow.getReported().put("p" + i, i);
        }
        // mock 方法
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(dbShadow);
        // 准备参数
        IotDeviceMessage message = buildMessage(device, buildUpdateParams(Map.of("Temperature", 25), 1L));

        // 调用
        deviceShadowService.handleDeviceShadowUpdate(device, message);
        // 断言：不写入，回复 408
        verify(deviceShadowMapper, never()).updateByDeviceIdAndVersion(any(), any(), any());
        IotDeviceMessage reply = captureReply(device);
        assertEquals(408, reply.getCode());
        assertEquals(1L, getReplyVersion(reply));
    }

    @Test
    public void testHandleDeviceShadowUpdate_resetWithoutState() {
        // mock 数据
        IotDeviceDO device = buildDevice();
        // mock 方法
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(buildShadow(device, 3L));
        when(deviceShadowMapper.updateByDeviceIdAndVersion(any(), eq(device.getId()), eq(3L))).thenReturn(1);
        // 准备参数：version 为 -1，且不传 state
        IotDeviceMessage message = buildMessage(device, buildResetParams());

        // 调用
        deviceShadowService.handleDeviceShadowUpdate(device, message);
        // 断言：按 DB 版本清空影子，version 归零
        IotDeviceShadowDO update = captureUpdate(device, 3L);
        assertTrue(update.getDesired().isEmpty());
        assertTrue(update.getReported().isEmpty());
        assertTrue(update.getMetadata().isEmpty());
        assertEquals(0L, update.getVersion());
        verify(deviceShadowRedisDAO).delete(device.getId());
        // 断言：回复成功
        IotDeviceMessage reply = captureReply(device);
        assertEquals(0, reply.getCode());
        assertEquals(0L, getReplyVersion(reply));
    }

    @Test
    public void testHandleDeviceShadowUpdate_resetRetryExhausted() {
        // mock 数据
        IotDeviceDO device = buildDevice();
        // mock 方法：每次清空都冲突
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(buildShadow(device, 3L));
        when(deviceShadowMapper.updateByDeviceIdAndVersion(any(), eq(device.getId()), eq(3L))).thenReturn(0);
        // 准备参数
        IotDeviceMessage message = buildMessage(device, buildResetParams());

        // 调用
        deviceShadowService.handleDeviceShadowUpdate(device, message);
        // 断言：重试 3 次后，回复 409 及 DB 当前版本
        verify(deviceShadowMapper, times(3)).updateByDeviceIdAndVersion(any(), eq(device.getId()), eq(3L));
        verify(deviceShadowRedisDAO, never()).delete(any());
        IotDeviceMessage reply = captureReply(device);
        assertEquals(409, reply.getCode());
        assertEquals(3L, getReplyVersion(reply));
    }

    @Test
    public void testHandleDeviceShadowUpdate_stateMissing() {
        // mock 数据
        IotDeviceDO device = buildDevice();
        // mock 方法
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(buildShadow(device, 3L));
        // 准备参数
        IotDeviceMessage message = buildMessage(device, Map.of("method", "update", "version", 3L));

        // 调用
        deviceShadowService.handleDeviceShadowUpdate(device, message);
        // 断言
        IotDeviceMessage reply = captureReply(device);
        assertEquals(402, reply.getCode());
        assertEquals(3L, getReplyVersion(reply));
    }

    @Test
    public void testHandleDeviceShadowUpdate_invalidParams() {
        // 准备参数
        IotDeviceDO device = buildDevice();
        IotDeviceMessage message = buildMessage(device, null);

        // 调用
        deviceShadowService.handleDeviceShadowUpdate(device, message);
        // 断言
        assertEquals(400, captureReply(device).getCode());
    }

    @Test
    public void testHandleDeviceShadowUpdate_methodMissing() {
        // mock 数据
        IotDeviceDO device = buildDevice();
        // mock 方法
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(buildShadow(device, 3L));
        // 准备参数
        IotDeviceMessage message = buildMessage(device, Map.of("state", Map.of("reported", Map.of("Temperature", 25))));

        // 调用
        deviceShadowService.handleDeviceShadowUpdate(device, message);
        // 断言
        assertEquals(401, captureReply(device).getCode());
    }

    @Test
    public void testHandleDeviceShadowUpdate_dbFailure() {
        // mock 数据
        IotDeviceDO device = buildDevice();
        // mock 方法
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenThrow(new RuntimeException("DB down"));
        // 准备参数
        IotDeviceMessage message = buildMessage(device, Map.of("method", "get"));

        // 调用
        assertDoesNotThrow(() -> deviceShadowService.handleDeviceShadowUpdate(device, message));
        // 断言：仍回复 500
        IotDeviceMessage reply = captureReply(device);
        assertEquals(500, reply.getCode());
        assertEquals(0L, getReplyVersion(reply));
    }

    @Test
    public void testHandleDeviceShadowUpdate_shadowDeletedDuringUpdate() {
        // mock 数据
        IotDeviceDO device = buildDevice();
        // mock 方法：校验时存在，更新时已被删除
        when(deviceShadowMapper.selectByDeviceId(device.getId()))
                .thenReturn(buildShadow(device, 2L), (IotDeviceShadowDO) null);
        // 准备参数
        IotDeviceMessage message = buildMessage(device, buildUpdateParams(Map.of("Temperature", 25), 2L));

        // 调用
        deviceShadowService.handleDeviceShadowUpdate(device, message);
        // 断言
        verify(deviceShadowMapper, never()).updateByDeviceIdAndVersion(any(), any(), any());
        assertEquals(500, captureReply(device).getCode());
    }

    // ========== updateDesiredState ==========

    @Test
    public void testUpdateDesiredState_online() {
        // mock 数据：缓存为关闭的旧影子，DB 已启用
        IotDeviceDO device = buildDevice();
        IotDeviceShadowDO dbShadow = buildShadow(device, 1L);
        dbShadow.getReported().put("Temperature", 20);
        lenient().when(deviceShadowRedisDAO.get(device.getId()))
                .thenReturn(buildShadow(device, 1L, CommonStatusEnum.DISABLE.getStatus()));
        when(deviceService.validateDeviceExists(device.getId())).thenReturn(device);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(dbShadow);
        when(deviceShadowMapper.updateByDeviceIdAndVersion(any(), eq(device.getId()), eq(1L))).thenReturn(1);
        when(devicePropertyService.getDeviceServerId(device.getId())).thenReturn("server-1");
        // 准备参数：数值、结构体、数组保持原类型
        Map<String, Object> desired = Map.of("Temperature", 25, "Location", Map.of("lat", 31.2),
                "Modes", List.of(1, 2));

        // 调用
        deviceShadowService.updateDesiredState(device.getId(), desired);
        // 断言：合并 desired，保留 reported
        IotDeviceShadowDO update = captureUpdate(device, 1L);
        assertEquals(desired, update.getDesired());
        assertEquals(Map.of("Temperature", 20), update.getReported());
        assertEquals(2L, update.getVersion());
        verify(deviceShadowRedisDAO).delete(device.getId());
        // 断言：推送全量影子，desired 类型不变
        IotDeviceMessage push = captureReply(device);
        assertEquals(IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod(), push.getMethod());
        assertEquals(0, push.getCode());
        assertEquals(2L, getReplyVersion(push));
        assertEquals(desired, getReplyState(push).get("desired"));
        assertEquals(Map.of("Temperature", 20), getReplyState(push).get("reported"));
    }

    @Test
    public void testUpdateDesiredState_offline() {
        // mock 数据
        IotDeviceDO device = buildDevice();
        // mock 方法
        when(deviceService.validateDeviceExists(device.getId())).thenReturn(device);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(buildShadow(device, 0L));
        when(deviceShadowMapper.updateByDeviceIdAndVersion(any(), eq(device.getId()), eq(0L))).thenReturn(1);
        when(devicePropertyService.getDeviceServerId(device.getId())).thenReturn(null);

        // 调用
        deviceShadowService.updateDesiredState(device.getId(), Map.of("PowerSwitch", 1));
        // 断言
        verify(deviceMessageService, never()).sendDeviceMessage(any(), any(), any());
    }

    @Test
    public void testUpdateDesiredState_disabledInDb() {
        // mock 数据：缓存为启用的旧影子，DB 已关闭
        IotDeviceDO device = buildDevice();
        lenient().when(deviceShadowRedisDAO.get(device.getId())).thenReturn(buildShadow(device, 0L));
        when(deviceService.validateDeviceExists(device.getId())).thenReturn(device);
        when(deviceShadowMapper.selectByDeviceId(device.getId()))
                .thenReturn(buildShadow(device, 0L, CommonStatusEnum.DISABLE.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> deviceShadowService.updateDesiredState(device.getId(), Map.of("PowerSwitch", 1)),
                DEVICE_SHADOW_NOT_ENABLED);
        verify(deviceShadowMapper, never()).updateByDeviceIdAndVersion(any(), any(), any());
    }

    @Test
    public void testUpdateDesiredState_propertyCountExceeded() {
        // mock 数据：已有 128 个属性
        IotDeviceDO device = buildDevice();
        IotDeviceShadowDO dbShadow = buildShadow(device, 1L);
        for (int i = 0; i < 128; i++) {
            dbShadow.getDesired().put("p" + i, i);
        }
        // mock 方法
        when(deviceService.validateDeviceExists(device.getId())).thenReturn(device);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(dbShadow);

        // 调用，并断言异常
        assertServiceException(() -> deviceShadowService.updateDesiredState(device.getId(), Map.of("PowerSwitch", 1)),
                DEVICE_SHADOW_PROPERTY_COUNT_EXCEEDED);
        verify(deviceShadowMapper, never()).updateByDeviceIdAndVersion(any(), any(), any());
    }

    @Test
    public void testUpdateDesiredState_retryExhausted() {
        // mock 数据
        IotDeviceDO device = buildDevice();
        // mock 方法：每次写入都冲突
        when(deviceService.validateDeviceExists(device.getId())).thenReturn(device);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(buildShadow(device, 1L));
        when(deviceShadowMapper.updateByDeviceIdAndVersion(any(), eq(device.getId()), eq(1L))).thenReturn(0);

        // 调用，并断言异常
        assertServiceException(() -> deviceShadowService.updateDesiredState(device.getId(), Map.of("PowerSwitch", 1)),
                DEVICE_SHADOW_VERSION_CONFLICT);
        verify(deviceShadowMapper, times(3)).updateByDeviceIdAndVersion(any(), eq(device.getId()), eq(1L));
        verify(deviceMessageService, never()).sendDeviceMessage(any(), any(), any());
    }

    // ========== resetShadow ==========

    @Test
    public void testResetShadow() {
        // mock 数据
        IotDeviceDO device = buildDevice();
        // mock 方法
        when(deviceService.validateDeviceExists(device.getId())).thenReturn(device);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(buildShadow(device, 2L));
        when(deviceShadowMapper.updateByDeviceIdAndVersion(any(), eq(device.getId()), eq(2L))).thenReturn(1);

        // 调用
        deviceShadowService.resetShadow(device.getId());
        // 断言
        IotDeviceShadowDO update = captureUpdate(device, 2L);
        assertTrue(update.getDesired().isEmpty());
        assertTrue(update.getReported().isEmpty());
        assertEquals(0L, update.getVersion());
        verify(deviceShadowRedisDAO).delete(device.getId());
    }

    @Test
    public void testResetShadow_notExists() {
        // mock 数据
        IotDeviceDO device = buildDevice();
        // mock 方法
        when(deviceService.validateDeviceExists(device.getId())).thenReturn(device);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(null);

        // 调用
        deviceShadowService.resetShadow(device.getId());
        // 断言：影子不存在，无需清空
        verify(deviceShadowMapper, never()).updateByDeviceIdAndVersion(any(), any(), any());
        verify(deviceShadowRedisDAO, never()).delete(any());
    }

    @Test
    public void testResetShadow_retryExhausted() {
        // mock 数据
        IotDeviceDO device = buildDevice();
        // mock 方法：每次清空都冲突
        when(deviceService.validateDeviceExists(device.getId())).thenReturn(device);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(buildShadow(device, 2L));
        when(deviceShadowMapper.updateByDeviceIdAndVersion(any(), eq(device.getId()), eq(2L))).thenReturn(0);

        // 调用，并断言异常
        assertServiceException(() -> deviceShadowService.resetShadow(device.getId()), DEVICE_SHADOW_VERSION_CONFLICT);
        verify(deviceShadowMapper, times(3)).updateByDeviceIdAndVersion(any(), eq(device.getId()), eq(2L));
        verify(deviceShadowRedisDAO, never()).delete(any());
    }

    // ========== updateStatus ==========

    @Test
    public void testUpdateStatus_shadowMissing() {
        // mock 数据
        IotDeviceDO device = buildDevice();
        // mock 方法
        when(deviceService.validateDeviceExists(device.getId())).thenReturn(device);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(null);

        // 调用
        deviceShadowService.updateStatus(device.getId(), CommonStatusEnum.DISABLE.getStatus());
        // 断言：直接按目标状态创建
        ArgumentCaptor<IotDeviceShadowDO> insertCaptor = ArgumentCaptor.forClass(IotDeviceShadowDO.class);
        verify(deviceShadowMapper).insert(insertCaptor.capture());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), insertCaptor.getValue().getStatus());
        verify(deviceShadowMapper, never()).updateByDeviceId(any(), any());
    }

    @Test
    public void testUpdateStatus_duplicateKey() {
        // mock 数据
        IotDeviceDO device = buildDevice();
        // mock 方法：并发创建，插入冲突后读到已启用的影子
        when(deviceService.validateDeviceExists(device.getId())).thenReturn(device);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(null, buildShadow(device, 0L));
        when(deviceShadowMapper.insert(any(IotDeviceShadowDO.class))).thenThrow(new DuplicateKeyException("duplicate"));

        // 调用
        deviceShadowService.updateStatus(device.getId(), CommonStatusEnum.DISABLE.getStatus());
        // 断言：改为更新状态，并删除缓存
        ArgumentCaptor<IotDeviceShadowDO> updateCaptor = ArgumentCaptor.forClass(IotDeviceShadowDO.class);
        verify(deviceShadowMapper).updateByDeviceId(updateCaptor.capture(), eq(device.getId()));
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), updateCaptor.getValue().getStatus());
        assertNull(updateCaptor.getValue().getVersion());
        verify(deviceShadowRedisDAO).delete(device.getId());
    }

    @Test
    public void testUpdateStatus_inTransaction() {
        // mock 数据
        IotDeviceDO device = buildDevice();
        // mock 方法
        when(deviceService.validateDeviceExists(device.getId())).thenReturn(device);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(buildShadow(device, 0L));
        // mock 事务
        TransactionSynchronizationManager.initSynchronization();
        try {
            // 调用
            deviceShadowService.updateStatus(device.getId(), CommonStatusEnum.DISABLE.getStatus());
            // 断言：提交前不删除缓存
            verify(deviceShadowMapper).updateByDeviceId(any(), eq(device.getId()));
            verify(deviceShadowRedisDAO, never()).delete(any());
            // 断言：提交后删除缓存
            TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
            verify(deviceShadowRedisDAO).delete(device.getId());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    // ========== deleteByDeviceId ==========

    @Test
    public void testDeleteByDeviceId_inTransaction() {
        // 准备参数
        Long deviceId = 2L;
        // mock 事务
        TransactionSynchronizationManager.initSynchronization();
        try {
            // 调用
            deviceShadowService.deleteByDeviceId(deviceId);
            // 断言：提交前不删除缓存
            verify(deviceShadowMapper).deleteByDeviceId(deviceId);
            verify(deviceShadowRedisDAO, never()).delete(any());
            // 断言：回滚时不删除缓存
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(sync -> sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
            verify(deviceShadowRedisDAO, never()).delete(any());
            // 断言：提交后删除缓存
            TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
            verify(deviceShadowRedisDAO).delete(deviceId);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    // ========== 私有方法 ==========

    private IotDeviceDO buildDevice() {
        return IotDeviceDO.builder().id(2L).productId(10L).productKey("testProduct").deviceName("testDevice").build();
    }

    private IotDeviceShadowDO buildShadow(IotDeviceDO device, Long version) {
        return buildShadow(device, version, CommonStatusEnum.ENABLE.getStatus());
    }

    private IotDeviceShadowDO buildShadow(IotDeviceDO device, Long version, Integer status) {
        return new IotDeviceShadowDO().setId(100L).setDeviceId(device.getId()).setProductId(device.getProductId())
                .setProductKey(device.getProductKey()).setDeviceName(device.getDeviceName())
                .setDesired(new HashMap<>()).setReported(new HashMap<>()).setMetadata(new HashMap<>())
                .setVersion(version).setShadowTime(1000L).setStatus(status);
    }

    private static Map<String, Object> buildUpdateParams(Map<String, Object> reported, Long version) {
        Map<String, Object> params = new HashMap<>();
        params.put("method", "update");
        params.put("state", Map.of("reported", reported));
        params.put("version", version);
        return params;
    }

    private static Map<String, Object> buildResetParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("method", "update");
        params.put("version", -1L);
        return params;
    }

    private static IotDeviceMessage buildMessage(IotDeviceDO device, Object params) {
        return IotDeviceMessage.requestOf(device.getId(), 1L, "server-1",
                IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod(), params).setRequestId("req-1");
    }

    private IotDeviceShadowDO captureUpdate(IotDeviceDO device, Long expectVersion) {
        ArgumentCaptor<IotDeviceShadowDO> updateCaptor = ArgumentCaptor.forClass(IotDeviceShadowDO.class);
        verify(deviceShadowMapper).updateByDeviceIdAndVersion(updateCaptor.capture(), eq(device.getId()),
                eq(expectVersion));
        return updateCaptor.getValue();
    }

    private IotDeviceMessage captureReply(IotDeviceDO device) {
        ArgumentCaptor<IotDeviceMessage> replyCaptor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        verify(deviceMessageService).sendDeviceMessage(replyCaptor.capture(), eq(device), eq("server-1"));
        return replyCaptor.getValue();
    }

    @SuppressWarnings("unchecked")
    private static Long getReplyVersion(IotDeviceMessage reply) {
        Object version = ((Map<String, Object>) reply.getData()).get("version");
        return version instanceof Number ? ((Number) version).longValue() : null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getReplyState(IotDeviceMessage reply) {
        return (Map<String, Object>) ((Map<String, Object>) reply.getData()).get("state");
    }

}
