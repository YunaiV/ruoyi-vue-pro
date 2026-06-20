package cn.iocoder.yudao.module.iot.service.device.shadow;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.shadow.IotDeviceShadowReplyDTO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceShadowDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceShadowMapper;
import cn.iocoder.yudao.module.iot.dal.redis.device.DeviceShadowRedisDAO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DuplicateKeyException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link IotDeviceShadowServiceImpl} 的单元测试
 */
public class IotDeviceShadowServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private IotDeviceShadowServiceImpl service;

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

    @BeforeEach
    public void setUp() {
        reset(deviceShadowMapper, deviceShadowRedisDAO, deviceService,
                devicePropertyService, deviceMessageService);
    }

    @Test
    public void testGetOrCreateShadow_whenNotExists_insertNewRow() {
        // 影子不存在时懒插入；并发下 DuplicateKeyException 由 getOrCreateShadow 兜底
        IotDeviceDO device = buildDevice();
        when(deviceShadowRedisDAO.get(device.getId())).thenReturn(null);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(null);

        service.getOrCreateShadow(device);

        ArgumentCaptor<IotDeviceShadowDO> captor = ArgumentCaptor.forClass(IotDeviceShadowDO.class);
        verify(deviceShadowMapper).insert(captor.capture());
        assertEquals(device.getId(), captor.getValue().getDeviceId());
    }

    @Test
    public void testHandleDeviceShadowUpdate_get_replyFullShadow() {
        IotDeviceDO device = buildDevice();
        IotDeviceShadowDO shadow = buildShadow(device, 2L);
        when(deviceShadowRedisDAO.get(device.getId())).thenReturn(null);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(shadow);

        Map<String, Object> params = new HashMap<>();
        params.put("method", "get");
        IotDeviceMessage message = IotDeviceMessage.requestOf(device.getId(), 1L, "server-1",
                IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod(), params);

        service.handleDeviceShadowUpdate(device, message);

        ArgumentCaptor<IotDeviceMessage> captor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        verify(deviceMessageService).sendDeviceMessage(captor.capture(), eq(device), eq("server-1"));
        IotDeviceShadowReplyDTO reply = (IotDeviceShadowReplyDTO) captor.getValue().getParams();
        assertEquals("get_reply", reply.getMethod());
        assertEquals(2L, reply.getVersion());
    }

    @Test
    public void testHandleDeviceShadowUpdate_update_mergeReportedAndVersionIncrement() {
        IotDeviceDO device = buildDevice();
        IotDeviceShadowDO shadow = buildShadow(device, 1L);
        shadow.getReported().put("Temperature", 20);
        when(deviceShadowRedisDAO.get(device.getId())).thenReturn(null);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(shadow);
        when(deviceShadowMapper.updateByDeviceIdAndVersion(any(), eq(device.getId()), eq(1L))).thenReturn(1);

        Map<String, Object> reported = new HashMap<>();
        reported.put("Temperature", 25);
        reported.put("Humidity", 60);
        Map<String, Object> state = new HashMap<>();
        state.put("reported", reported);
        Map<String, Object> params = new HashMap<>();
        params.put("method", "update");
        params.put("state", state);
        params.put("version", 1L);
        IotDeviceMessage message = IotDeviceMessage.requestOf(device.getId(), 1L, "server-1",
                IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod(), params);

        service.handleDeviceShadowUpdate(device, message);

        ArgumentCaptor<IotDeviceShadowDO> updateCaptor = ArgumentCaptor.forClass(IotDeviceShadowDO.class);
        verify(deviceShadowMapper).updateByDeviceIdAndVersion(updateCaptor.capture(), eq(device.getId()), eq(1L));
        assertEquals(25, updateCaptor.getValue().getReported().get("Temperature"));
        assertEquals(60, updateCaptor.getValue().getReported().get("Humidity"));
        assertEquals(2L, updateCaptor.getValue().getVersion());
        verify(deviceShadowRedisDAO).delete(device.getId());
    }

    @Test
    public void testHandleDeviceShadowUpdate_versionGreaterThanLatest_send409() {
        IotDeviceDO device = buildDevice();
        IotDeviceShadowDO shadow = buildShadow(device, 1L);
        when(deviceShadowRedisDAO.get(device.getId())).thenReturn(null);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(shadow);

        Map<String, Object> reported = new HashMap<>();
        reported.put("Temperature", 25);
        Map<String, Object> state = new HashMap<>();
        state.put("reported", reported);
        Map<String, Object> params = new HashMap<>();
        params.put("method", "update");
        params.put("state", state);
        params.put("version", 3L);
        IotDeviceMessage message = IotDeviceMessage.requestOf(device.getId(), 1L, "server-1",
                IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod(), params);

        service.handleDeviceShadowUpdate(device, message);

        ArgumentCaptor<IotDeviceMessage> captor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        verify(deviceMessageService).sendDeviceMessage(captor.capture(), eq(device), eq("server-1"));
        IotDeviceShadowReplyDTO reply = (IotDeviceShadowReplyDTO) captor.getValue().getParams();
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) reply.getPayload();
        assertEquals(409, payload.get("code"));
        verify(deviceShadowMapper, never()).updateByDeviceIdAndVersion(any(), any(), any());
    }

    @Test
    public void testHandleDeviceShadowUpdate_versionNull_forceUpdateWithoutVersionCheck() {
        IotDeviceDO device = buildDevice();
        IotDeviceShadowDO shadow = buildShadow(device, 5L);
        when(deviceShadowRedisDAO.get(device.getId())).thenReturn(null);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(shadow);
        when(deviceShadowMapper.updateByDeviceIdAndVersion(any(), eq(device.getId()), eq(5L))).thenReturn(1);

        Map<String, Object> reported = new HashMap<>();
        reported.put("Temperature", 25);
        Map<String, Object> state = new HashMap<>();
        state.put("reported", reported);
        Map<String, Object> params = new HashMap<>();
        params.put("method", "update");
        params.put("state", state);
        // 不传 version → 强制更新，跳过版本精确匹配
        IotDeviceMessage message = IotDeviceMessage.requestOf(device.getId(), 1L, "server-1",
                IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod(), params);

        service.handleDeviceShadowUpdate(device, message);

        verify(deviceShadowMapper).updateByDeviceIdAndVersion(any(), eq(device.getId()), eq(5L));
    }

    @Test
    public void testHandleDeviceShadowUpdate_versionConflict_send409() {
        IotDeviceDO device = buildDevice();
        IotDeviceShadowDO shadow = buildShadow(device, 5L);
        when(deviceShadowRedisDAO.get(device.getId())).thenReturn(null);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(shadow);

        Map<String, Object> reported = new HashMap<>();
        reported.put("Temperature", 25);
        Map<String, Object> state = new HashMap<>();
        state.put("reported", reported);
        Map<String, Object> params = new HashMap<>();
        params.put("method", "update");
        params.put("state", state);
        params.put("version", 3L);
        IotDeviceMessage message = IotDeviceMessage.requestOf(device.getId(), 1L, "server-1",
                IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod(), params);

        service.handleDeviceShadowUpdate(device, message);

        ArgumentCaptor<IotDeviceMessage> captor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        verify(deviceMessageService).sendDeviceMessage(captor.capture(), eq(device), eq("server-1"));
        IotDeviceShadowReplyDTO reply = (IotDeviceShadowReplyDTO) captor.getValue().getParams();
        assertEquals("update_reply", reply.getMethod());
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) reply.getPayload();
        assertEquals(409, payload.get("code"));
        verify(deviceShadowMapper, never()).updateByDeviceIdAndVersion(any(), any(), any());
    }

    @Test
    public void testHandleDeviceShadowUpdate_versionMinusOne_resetShadow() {
        IotDeviceDO device = buildDevice();
        IotDeviceShadowDO shadow = buildShadow(device, 3L);
        shadow.getDesired().put("PowerSwitch", 1);
        shadow.getReported().put("Temperature", 20);
        when(deviceShadowRedisDAO.get(device.getId())).thenReturn(null);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(shadow);
        when(deviceShadowMapper.updateByDeviceIdAndVersion(any(), eq(device.getId()), eq(3L))).thenReturn(1);

        Map<String, Object> reported = new HashMap<>();
        reported.put("Temperature", 20);
        Map<String, Object> state = new HashMap<>();
        state.put("reported", reported);
        Map<String, Object> params = new HashMap<>();
        params.put("method", "update");
        params.put("state", state);
        params.put("version", -1L);
        IotDeviceMessage message = IotDeviceMessage.requestOf(device.getId(), 1L, "server-1",
                IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod(), params);

        service.handleDeviceShadowUpdate(device, message);

        ArgumentCaptor<IotDeviceShadowDO> updateCaptor = ArgumentCaptor.forClass(IotDeviceShadowDO.class);
        verify(deviceShadowMapper).updateByDeviceIdAndVersion(updateCaptor.capture(), eq(device.getId()), eq(3L));
        assertTrue(updateCaptor.getValue().getDesired().isEmpty());
        assertTrue(updateCaptor.getValue().getReported().isEmpty());
        assertEquals(0L, updateCaptor.getValue().getVersion());
    }

    @Test
    public void testHandleDeviceShadowUpdate_disabled_ignore() {
        IotDeviceDO device = buildDevice();
        IotDeviceShadowDO shadow = buildShadow(device, 0L, CommonStatusEnum.DISABLE.getStatus());
        when(deviceShadowRedisDAO.get(device.getId())).thenReturn(null);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(shadow);

        Map<String, Object> params = new HashMap<>();
        params.put("method", "get");
        IotDeviceMessage message = IotDeviceMessage.requestOf(device.getId(), 1L, "server-1",
                IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod(), params);

        service.handleDeviceShadowUpdate(device, message);

        verify(deviceMessageService, never()).sendDeviceMessage(any(), any(), any());
    }

    @Test
    public void testUpdateDesiredState_online_pushShadow() {
        Long deviceId = 2L;
        IotDeviceDO device = buildDevice();
        IotDeviceShadowDO shadow = buildShadow(device, 1L);
        when(deviceService.validateDeviceExists(deviceId)).thenReturn(device);
        when(deviceShadowRedisDAO.get(deviceId)).thenReturn(null);
        when(deviceShadowMapper.selectByDeviceId(deviceId)).thenReturn(shadow);
        when(deviceShadowMapper.updateByDeviceIdAndVersion(any(), eq(deviceId), eq(1L))).thenReturn(1);
        when(devicePropertyService.getDeviceServerId(deviceId)).thenReturn("server-1");

        Map<String, Object> desired = new HashMap<>();
        desired.put("PowerSwitch", 1);
        service.updateDesiredState(deviceId, desired);

        ArgumentCaptor<IotDeviceMessage> captor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        verify(deviceMessageService).sendDeviceMessage(captor.capture(), eq(device), eq("server-1"));
        IotDeviceShadowReplyDTO reply = (IotDeviceShadowReplyDTO) captor.getValue().getParams();
        assertEquals("get_reply", reply.getMethod());
        assertEquals(2L, reply.getVersion());
        // getOrCreateShadow + CAS 各一次 SELECT，推送不再额外查库
        verify(deviceShadowMapper, times(2)).selectByDeviceId(deviceId);
        verify(deviceShadowRedisDAO).delete(deviceId);
    }

    @Test
    public void testUpdateDesiredState_offline_noPush() {
        Long deviceId = 2L;
        IotDeviceDO device = buildDevice();
        IotDeviceShadowDO shadow = buildShadow(device, 0L);
        when(deviceService.validateDeviceExists(deviceId)).thenReturn(device);
        when(deviceShadowRedisDAO.get(deviceId)).thenReturn(null);
        when(deviceShadowMapper.selectByDeviceId(deviceId)).thenReturn(shadow);
        when(deviceShadowMapper.updateByDeviceIdAndVersion(any(), eq(deviceId), eq(0L))).thenReturn(1);
        when(devicePropertyService.getDeviceServerId(deviceId)).thenReturn(null);

        Map<String, Object> desired = new HashMap<>();
        desired.put("PowerSwitch", 1);
        service.updateDesiredState(deviceId, desired);

        verify(deviceMessageService, never()).sendDeviceMessage(any(), any(), any());
    }

    @Test
    public void testHandleDeviceShadowUpdate_methodMissing_send401() {
        IotDeviceDO device = buildDevice();
        IotDeviceShadowDO shadow = buildShadow(device, 3L);
        when(deviceShadowRedisDAO.get(device.getId())).thenReturn(null);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(shadow);

        Map<String, Object> params = new HashMap<>();
        params.put("state", Map.of("reported", Map.of("Temperature", 25)));
        IotDeviceMessage message = IotDeviceMessage.requestOf(device.getId(), 1L, "server-1",
                IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod(), params);

        service.handleDeviceShadowUpdate(device, message);

        ArgumentCaptor<IotDeviceMessage> captor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        verify(deviceMessageService).sendDeviceMessage(captor.capture(), eq(device), eq("server-1"));
        IotDeviceShadowReplyDTO reply = (IotDeviceShadowReplyDTO) captor.getValue().getParams();
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) reply.getPayload();
        assertEquals(401, payload.get("code"));
    }

    @Test
    public void testHandleDeviceShadowUpdate_update_retryReMergeFromLatest() {
        IotDeviceDO device = buildDevice();
        IotDeviceShadowDO staleShadow = buildShadow(device, 1L);
        staleShadow.getReported().put("Temperature", 20);

        IotDeviceShadowDO latestShadow = buildShadow(device, 2L);
        latestShadow.getReported().put("Temperature", 30);
        latestShadow.getReported().put("Humidity", 50);

        when(deviceShadowRedisDAO.get(device.getId())).thenReturn(null);
        AtomicInteger selectCount = new AtomicInteger(0);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenAnswer(invocation -> {
            // 前两次 select 返回 v=1 的快照，之后返回 v=2 的最新快照
            return selectCount.incrementAndGet() <= 2 ? staleShadow : latestShadow;
        });
        when(deviceShadowMapper.updateByDeviceIdAndVersion(any(), eq(device.getId()), anyLong())).thenAnswer(invocation -> {
            Long expectVersion = invocation.getArgument(2);
            if (expectVersion == 1L) {
                return 0;
            }
            if (expectVersion == 2L) {
                return 1;
            }
            return 0;
        });

        Map<String, Object> reported = new HashMap<>();
        reported.put("Temperature", 25);
        Map<String, Object> state = new HashMap<>();
        state.put("reported", reported);
        Map<String, Object> params = new HashMap<>();
        params.put("method", "update");
        params.put("state", state);
        // version=null：强制更新，CAS 重试时不受请求 version 约束
        IotDeviceMessage message = IotDeviceMessage.requestOf(device.getId(), 1L, "server-1",
                IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod(), params);

        service.handleDeviceShadowUpdate(device, message);

        ArgumentCaptor<IotDeviceShadowDO> updateCaptor = ArgumentCaptor.forClass(IotDeviceShadowDO.class);
        verify(deviceShadowMapper, atLeastOnce()).updateByDeviceIdAndVersion(updateCaptor.capture(), eq(device.getId()), any());
        IotDeviceShadowDO finalUpdate = updateCaptor.getAllValues().get(updateCaptor.getAllValues().size() - 1);
        // 重试时应基于 v=2 的最新 reported 合并，保留 Humidity=50
        assertEquals(25, finalUpdate.getReported().get("Temperature"));
        assertEquals(50, finalUpdate.getReported().get("Humidity"));
        assertEquals(3L, finalUpdate.getVersion());

        ArgumentCaptor<IotDeviceMessage> replyCaptor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        verify(deviceMessageService).sendDeviceMessage(replyCaptor.capture(), eq(device), eq("server-1"));
        IotDeviceShadowReplyDTO reply = (IotDeviceShadowReplyDTO) replyCaptor.getValue().getParams();
        assertEquals(3L, reply.getVersion());
    }

    @Test
    public void testUpdateStatus_whenShadowMissing_insertWithTargetStatus() {
        Long deviceId = 2L;
        IotDeviceDO device = buildDevice();
        when(deviceService.validateDeviceExists(deviceId)).thenReturn(device);
        when(deviceShadowRedisDAO.get(deviceId)).thenReturn(null);
        when(deviceShadowMapper.selectByDeviceId(deviceId)).thenReturn(null);

        service.updateStatus(deviceId, CommonStatusEnum.DISABLE.getStatus());

        ArgumentCaptor<IotDeviceShadowDO> captor = ArgumentCaptor.forClass(IotDeviceShadowDO.class);
        verify(deviceShadowMapper).insert(captor.capture());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), captor.getValue().getStatus());
        verify(deviceShadowRedisDAO, never()).delete(deviceId);
    }

    @Test
    public void testUpdateStatus_whenShadowMissing_duplicateKey_fallbackUpdateAndEvictCache() {
        Long deviceId = 2L;
        IotDeviceDO device = buildDevice();
        when(deviceService.validateDeviceExists(deviceId)).thenReturn(device);
        when(deviceShadowRedisDAO.get(deviceId)).thenReturn(null);
        when(deviceShadowMapper.selectByDeviceId(deviceId)).thenReturn(null);
        when(deviceShadowMapper.insert(any(IotDeviceShadowDO.class))).thenThrow(new DuplicateKeyException("duplicate"));
        when(deviceShadowMapper.updateStatusByDeviceId(deviceId, CommonStatusEnum.DISABLE.getStatus())).thenReturn(1);

        service.updateStatus(deviceId, CommonStatusEnum.DISABLE.getStatus());

        verify(deviceShadowMapper).updateStatusByDeviceId(deviceId, CommonStatusEnum.DISABLE.getStatus());
        verify(deviceShadowRedisDAO).delete(deviceId);
    }

    @Test
    public void testResetShadow_casConflict_throwException() {
        Long deviceId = 2L;
        IotDeviceDO device = buildDevice();
        IotDeviceShadowDO shadow = buildShadow(device, 2L);
        when(deviceService.validateDeviceExists(deviceId)).thenReturn(device);
        when(deviceShadowRedisDAO.get(deviceId)).thenReturn(null);
        when(deviceShadowMapper.selectByDeviceId(deviceId)).thenReturn(shadow);
        when(deviceShadowMapper.updateByDeviceIdAndVersion(any(), eq(deviceId), eq(2L))).thenReturn(0);

        assertThrows(ServiceException.class, () -> service.resetShadow(deviceId));
    }

    @Test
    public void testHandleDeviceShadowUpdate_catchBlock_dbFailureStillSendsReply() {
        IotDeviceDO device = buildDevice();
        when(deviceShadowRedisDAO.get(device.getId())).thenReturn(null);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenThrow(new RuntimeException("DB down"));

        Map<String, Object> params = new HashMap<>();
        params.put("method", "get");
        IotDeviceMessage message = IotDeviceMessage.requestOf(device.getId(), 1L, "server-1",
                IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod(), params);

        assertDoesNotThrow(() -> service.handleDeviceShadowUpdate(device, message));

        ArgumentCaptor<IotDeviceMessage> captor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        verify(deviceMessageService).sendDeviceMessage(captor.capture(), eq(device), eq("server-1"));
        IotDeviceShadowReplyDTO reply = (IotDeviceShadowReplyDTO) captor.getValue().getParams();
        assertEquals(0L, reply.getVersion());
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) reply.getPayload();
        assertEquals(500, payload.get("code"));
    }

    @Test
    public void testHandleDeviceShadowUpdate_casShadowNotFound_send500() {
        IotDeviceDO device = buildDevice();
        IotDeviceShadowDO shadow = buildShadow(device, 2L);
        when(deviceShadowRedisDAO.get(device.getId())).thenReturn(null);
        when(deviceShadowMapper.selectByDeviceId(device.getId())).thenReturn(shadow, (IotDeviceShadowDO) null);

        Map<String, Object> reported = new HashMap<>();
        reported.put("Temperature", 25);
        Map<String, Object> state = new HashMap<>();
        state.put("reported", reported);
        Map<String, Object> params = new HashMap<>();
        params.put("method", "update");
        params.put("state", state);
        params.put("version", 2L);
        IotDeviceMessage message = IotDeviceMessage.requestOf(device.getId(), 1L, "server-1",
                IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod(), params);

        service.handleDeviceShadowUpdate(device, message);

        ArgumentCaptor<IotDeviceMessage> captor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        verify(deviceMessageService).sendDeviceMessage(captor.capture(), eq(device), eq("server-1"));
        IotDeviceShadowReplyDTO reply = (IotDeviceShadowReplyDTO) captor.getValue().getParams();
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) reply.getPayload();
        assertEquals(500, payload.get("code"));
        verify(deviceShadowMapper, never()).updateByDeviceIdAndVersion(any(), any(), any());
    }

    private IotDeviceDO buildDevice() {
        return IotDeviceDO.builder()
                .id(2L)
                .productId(10L)
                .productKey("testProduct")
                .deviceName("testDevice")
                .build();
    }

    private IotDeviceShadowDO buildShadow(IotDeviceDO device, Long version) {
        return buildShadow(device, version, CommonStatusEnum.ENABLE.getStatus());
    }

    private IotDeviceShadowDO buildShadow(IotDeviceDO device, Long version, Integer status) {
        return IotDeviceShadowDO.builder()
                .id(100L)
                .deviceId(device.getId())
                .productId(device.getProductId())
                .productKey(device.getProductKey())
                .deviceName(device.getDeviceName())
                .desired(new HashMap<>())
                .reported(new HashMap<>())
                .metadata(new HashMap<>())
                .version(version)
                .shadowTime(1000L)
                .status(status)
                .build();
    }

}
