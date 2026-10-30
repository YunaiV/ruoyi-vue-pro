package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.device.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants;
import cn.iocoder.yudao.module.iot.dal.storage.IotDeviceMessageDAO;
import cn.iocoder.yudao.module.iot.enums.product.IotProductDeviceTypeEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionOperatorEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionTypeEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.message.IotDeviceMessageServiceImpl;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import cn.iocoder.yudao.module.iot.service.device.shadow.IotDeviceShadowService;
import cn.iocoder.yudao.module.iot.service.ota.IotOtaTaskRecordService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.service.rule.data.IotDataRuleService;
import cn.iocoder.yudao.module.iot.service.rule.scene.IotSceneRuleService;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotSceneRuleMatcherManager;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.condition.IotDeviceStateConditionMatcher;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.trigger.IotDeviceStateUpdateTriggerMatcher;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * {@link IotDeviceServiceImpl} 的单元测试
 *
 * 设备状态相关的用例，同时使用真实的 {@link IotDeviceMessageServiceImpl}：经过 Spring 代理开启事务，
 * 在 H2 上验证条件更新、状态事务的提交与回滚、缓存清理时机、网关下线联动子设备；
 * 规则匹配相关的用例，使用真实的 {@link IotSceneRuleMatcherManager} 与设备状态的触发器、条件匹配器
 *
 * @author 芋道源码
 */
@Import({IotDeviceServiceImpl.class, IotDeviceMessageServiceImpl.class, IotDeviceServiceImplTest.CacheConfiguration.class,
        IotSceneRuleMatcherManager.class, IotDeviceStateUpdateTriggerMatcher.class, IotDeviceStateConditionMatcher.class})
public class IotDeviceServiceImplTest extends BaseDbUnitTest {

    /**
     * 基准时间：H2 的时间精度有限，使用整秒
     */
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 10, 29, 10, 0, 0);

    @Resource
    private IotDeviceServiceImpl deviceService;
    @Resource
    private IotDeviceMessageServiceImpl deviceMessageService;

    @Resource
    private IotDeviceMapper deviceMapper;
    @Resource
    private PlatformTransactionManager transactionManager;
    @SpyBean
    private CacheManager cacheManager;
    @Resource
    private IotSceneRuleMatcherManager sceneRuleMatcherManager;

    @MockBean
    private IotProductService productService;
    @MockBean
    private IotDeviceGroupService deviceGroupService;
    @MockBean
    private IotDeviceShadowService deviceShadowService;
    @MockBean
    private IotDevicePropertyService devicePropertyService;
    @MockBean
    private IotOtaTaskRecordService otaTaskRecordService;
    @MockBean
    private IotSceneRuleService sceneRuleService;
    @MockBean
    private IotDataRuleService dataRuleService;
    @MockBean
    private IotDeviceMessageDAO deviceMessageDAO;
    @MockBean
    private IotDeviceMessageProducer deviceMessageProducer;

    /**
     * 与线上一致：缓存的写入、清理，在事务提交后才生效
     */
    @Configuration
    @EnableCaching
    public static class CacheConfiguration {

        @Bean
        public CacheManager cacheManager() {
            return new TransactionAwareCacheManagerProxy(new ConcurrentMapCacheManager());
        }

    }

    @BeforeEach
    public void setUp() {
        getDeviceCache().clear();
    }

    @Test
    public void testDeleteDevice_success() {
        // mock 数据
        IotDeviceDO dbDevice = buildDevice();
        deviceMapper.insert(dbDevice);
        // 准备参数
        Long id = dbDevice.getId();

        // 调用
        deviceService.deleteDevice(id);
        // 断言
        assertNull(deviceMapper.selectById(id));
        verify(deviceShadowService).deleteByDeviceId(id);
    }

    @Test
    public void testDeleteDevice_shadowDeleteFailed() {
        // mock 数据
        IotDeviceDO dbDevice = buildDevice();
        deviceMapper.insert(dbDevice);
        // 准备参数
        Long id = dbDevice.getId();
        // mock 影子删除失败
        RuntimeException failure = new RuntimeException("shadow delete failed");
        doThrow(failure).when(deviceShadowService).deleteByDeviceId(id);

        // 调用，并断言异常
        assertSame(failure, assertThrows(RuntimeException.class, () -> deviceService.deleteDevice(id)));
        // 断言：设备删除已回滚
        assertNotNull(deviceMapper.selectById(id));
    }

    // ========== 设备状态：条件更新 ==========

    @Test
    public void testUpdateDeviceState_firstOnline() {
        // mock 数据：未激活
        IotDeviceDO device = insertDevice(IotDeviceStateEnum.INACTIVE.getState(), null, null);

        // 调用
        boolean changed = deviceService.updateDeviceState(device, IotDeviceStateEnum.ONLINE.getState(), T0, false);
        // 断言：上线，并且首次激活
        assertTrue(changed);
        IotDeviceDO dbDevice = deviceMapper.selectById(device.getId());
        assertEquals(IotDeviceStateEnum.ONLINE.getState(), dbDevice.getState());
        assertEquals(T0, dbDevice.getOnlineTime());
        assertEquals(T0, dbDevice.getActiveTime());
    }

    @Test
    public void testUpdateDeviceState_reOnlineKeepActiveTime() {
        // mock 数据：已激活、已离线
        IotDeviceDO device = insertDevice(IotDeviceStateEnum.OFFLINE.getState(), T0, T0.plusMinutes(1));

        // 调用
        boolean changed = deviceService.updateDeviceState(device, IotDeviceStateEnum.ONLINE.getState(),
                T0.plusMinutes(2), false);
        // 断言：重新上线，激活时间不变
        assertTrue(changed);
        IotDeviceDO dbDevice = deviceMapper.selectById(device.getId());
        assertEquals(IotDeviceStateEnum.ONLINE.getState(), dbDevice.getState());
        assertEquals(T0.plusMinutes(2), dbDevice.getOnlineTime());
        assertEquals(T0, dbDevice.getActiveTime());
    }

    @Test
    public void testUpdateDeviceState_duplicateOnline() {
        // mock 数据：已在线
        IotDeviceDO device = insertDevice(IotDeviceStateEnum.ONLINE.getState(), T0, null);

        // 调用：网关连接的重复上线（refresh = true），推进上线时间，但不算状态变化
        assertFalse(deviceService.updateDeviceState(device, IotDeviceStateEnum.ONLINE.getState(),
                T0.plusMinutes(1), true));
        assertEquals(T0.plusMinutes(1), deviceMapper.selectById(device.getId()).getOnlineTime());
        // 调用：其它来源的重复上线（refresh = false），不推进上线时间
        assertFalse(deviceService.updateDeviceState(device, IotDeviceStateEnum.ONLINE.getState(),
                T0.plusMinutes(2), false));
        assertEquals(T0.plusMinutes(1), deviceMapper.selectById(device.getId()).getOnlineTime());
        // 调用：更早的上线，不回退上线时间
        assertFalse(deviceService.updateDeviceState(device, IotDeviceStateEnum.ONLINE.getState(),
                T0, true));
        assertEquals(T0.plusMinutes(1), deviceMapper.selectById(device.getId()).getOnlineTime());
    }

    @Test
    public void testUpdateDeviceState_lateOnlineIgnored() {
        // mock 数据：已离线，离线时间为 T0 + 10
        IotDeviceDO device = insertDevice(IotDeviceStateEnum.OFFLINE.getState(), T0, T0.plusMinutes(10));

        // 调用：迟到的上线（早于离线）
        boolean changed = deviceService.updateDeviceState(device, IotDeviceStateEnum.ONLINE.getState(),
                T0.plusMinutes(5), true);
        // 断言：保持离线
        assertFalse(changed);
        IotDeviceDO dbDevice = deviceMapper.selectById(device.getId());
        assertEquals(IotDeviceStateEnum.OFFLINE.getState(), dbDevice.getState());
        assertEquals(T0, dbDevice.getOnlineTime());
    }

    @Test
    public void testUpdateDeviceState_lateOfflineIgnored() {
        // mock 数据：已在线，上线时间为 T0 + 10
        IotDeviceDO device = insertDevice(IotDeviceStateEnum.ONLINE.getState(), T0.plusMinutes(10), null);

        // 调用：迟到的离线（早于上线）
        boolean changed = deviceService.updateDeviceState(device, IotDeviceStateEnum.OFFLINE.getState(),
                T0.plusMinutes(5), false);
        // 断言：保持在线
        assertFalse(changed);
        IotDeviceDO dbDevice = deviceMapper.selectById(device.getId());
        assertEquals(IotDeviceStateEnum.ONLINE.getState(), dbDevice.getState());
        assertNull(dbDevice.getOfflineTime());
    }

    @Test
    public void testUpdateDeviceState_sameTimeOffline() {
        // mock 数据：已在线，上线时间为 T0
        IotDeviceDO device = insertDevice(IotDeviceStateEnum.ONLINE.getState(), T0, null);

        // 调用：同一时间的离线
        boolean changed = deviceService.updateDeviceState(device, IotDeviceStateEnum.OFFLINE.getState(), T0, false);
        // 断言：离线生效
        assertTrue(changed);
        IotDeviceDO dbDevice = deviceMapper.selectById(device.getId());
        assertEquals(IotDeviceStateEnum.OFFLINE.getState(), dbDevice.getState());
        assertEquals(T0, dbDevice.getOfflineTime());
    }

    @Test
    public void testUpdateDeviceState_sameSecondReconnect() {
        // mock 数据：已在线
        IotDeviceDO device = insertDevice(IotDeviceStateEnum.ONLINE.getState(), T0, null);

        // 调用：同一秒内断开（.600）又重连（.800）
        assertTrue(deviceService.updateDeviceState(device, IotDeviceStateEnum.OFFLINE.getState(),
                T0.plusNanos(600_000_000), false));
        assertTrue(deviceService.updateDeviceState(device, IotDeviceStateEnum.ONLINE.getState(),
                T0.plusNanos(800_000_000), true));
        // 断言：重连生效；时间按秒存储，不会因为毫秒进位被误判为迟到
        IotDeviceDO dbDevice = deviceMapper.selectById(device.getId());
        assertEquals(IotDeviceStateEnum.ONLINE.getState(), dbDevice.getState());
        assertEquals(T0, dbDevice.getOfflineTime());
        assertEquals(T0, dbDevice.getOnlineTime());
    }

    @Test
    public void testUpdateDeviceState_duplicateOffline() {
        // mock 数据：已离线，离线时间为 T0
        IotDeviceDO device = insertDevice(IotDeviceStateEnum.OFFLINE.getState(), null, T0);

        // 调用：更晚的离线，推进离线水位，但不算状态变化
        assertFalse(deviceService.updateDeviceState(device, IotDeviceStateEnum.OFFLINE.getState(),
                T0.plusMinutes(5), false));
        assertEquals(T0.plusMinutes(5), deviceMapper.selectById(device.getId()).getOfflineTime());
        // 调用：更早的离线，不回退离线水位
        assertFalse(deviceService.updateDeviceState(device, IotDeviceStateEnum.OFFLINE.getState(),
                T0, false));
        assertEquals(T0.plusMinutes(5), deviceMapper.selectById(device.getId()).getOfflineTime());
    }

    // ========== 设备状态：状态事务 ==========

    @Test
    public void testHandleStateUpdate_commit() {
        // mock 数据：已离线
        IotDeviceDO device = insertDevice(IotDeviceStateEnum.OFFLINE.getState(), null, null);
        // 准备参数
        IotDeviceMessage message = buildStateMessage(device, IotDeviceStateEnum.ONLINE.getState(), T0, "server-1");
        // mock 方法：规则在状态事务内执行，此时已能读到新状态
        doAnswer(invocation -> {
            assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
            assertEquals(IotDeviceStateEnum.ONLINE.getState(), deviceMapper.selectById(device.getId()).getState());
            return null;
        }).when(sceneRuleService).executeSceneRuleByDevice(any());

        // 调用
        deviceMessageService.handleUpstreamDeviceMessage(message, device);
        // 断言：已提交
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        IotDeviceDO dbDevice = deviceMapper.selectById(device.getId());
        assertEquals(IotDeviceStateEnum.ONLINE.getState(), dbDevice.getState());
        assertEquals(T0, dbDevice.getOnlineTime());
        verify(sceneRuleService).executeSceneRuleByDevice(same(message));
        verify(dataRuleService).executeDataRule(same(message));
    }

    @Test
    public void testHandleStateUpdate_notChangedSkipRule() {
        // mock 数据：已在线
        IotDeviceDO device = insertDevice(IotDeviceStateEnum.ONLINE.getState(), T0, null);
        // 准备参数：重复的上线
        IotDeviceMessage message = buildStateMessage(device, IotDeviceStateEnum.ONLINE.getState(),
                T0.plusMinutes(1), "server-1");

        // 调用
        deviceMessageService.handleUpstreamDeviceMessage(message, device);
        // 断言：只推进上线时间，不触发规则
        assertEquals(T0.plusMinutes(1), deviceMapper.selectById(device.getId()).getOnlineTime());
        verify(sceneRuleService, never()).executeSceneRuleByDevice(any());
        verify(dataRuleService, never()).executeDataRule(any());
    }

    @Test
    public void testHandleStateUpdate_ruleDataAccessExceptionRollback() {
        // mock 数据：已离线
        IotDeviceDO device = insertDevice(IotDeviceStateEnum.OFFLINE.getState(), null, null);
        // 准备参数
        IotDeviceMessage message = buildStateMessage(device, IotDeviceStateEnum.ONLINE.getState(), T0, null);
        // mock 方法：规则访问数据失败（例如说 Redis 超时），异常不被吞掉
        QueryTimeoutException failure = new QueryTimeoutException("rule data access failed");
        doThrow(failure).when(sceneRuleService).executeSceneRuleByDevice(any());

        // 调用，并断言异常：抛给消息总线
        assertSame(failure, assertThrows(QueryTimeoutException.class,
                () -> deviceMessageService.handleUpstreamDeviceMessage(message, device)));
        // 断言：状态已回滚
        IotDeviceDO dbDevice = deviceMapper.selectById(device.getId());
        assertEquals(IotDeviceStateEnum.OFFLINE.getState(), dbDevice.getState());
        assertNull(dbDevice.getOnlineTime());
        verify(dataRuleService, never()).executeDataRule(any());
    }

    @Test
    public void testHandleStateUpdate_rollbackOnly() {
        // mock 数据：已离线
        IotDeviceDO device = insertDevice(IotDeviceStateEnum.OFFLINE.getState(), null, null);
        // 准备参数
        IotDeviceMessage message = buildStateMessage(device, IotDeviceStateEnum.ONLINE.getState(), T0, null);
        // mock 方法：规则内部的事务方法失败、被 catch 吞掉，状态事务被标记为 rollback-only
        doAnswer(invocation -> {
            try {
                new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                    throw new IllegalArgumentException("inner failed");
                });
            } catch (IllegalArgumentException ignored) {
            }
            return null;
        }).when(dataRuleService).executeDataRule(any());

        // 调用，并断言异常：显式失败，而不是静默回滚
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> deviceMessageService.handleUpstreamDeviceMessage(message, device));
        assertEquals("状态事务已被标记回滚", ex.getMessage());
        // 断言：状态已回滚
        assertEquals(IotDeviceStateEnum.OFFLINE.getState(), deviceMapper.selectById(device.getId()).getState());
    }

    @Test
    public void testHandleStateUpdate_localRollbackOnly() {
        // mock 数据：网关在线；子设备在线
        IotDeviceDO gateway = insertGateway();
        IotDeviceDO subDevice = insertSubDevice(gateway, IotDeviceStateEnum.ONLINE.getState(), T0);
        // 准备参数
        IotDeviceMessage message = buildStateMessage(gateway, IotDeviceStateEnum.OFFLINE.getState(),
                T0.plusMinutes(10), "server-1");
        // mock 方法：规则直接把当前事务标记为 rollback-only 后正常返回（只有本地标记）
        // 说明：本地 rollback-only 时，commit 会静默回滚、不抛异常；如果不显式失败，调用方会误以为已经提交
        doAnswer(invocation -> {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return null;
        }).when(dataRuleService).executeDataRule(any());

        // 调用，并断言异常：显式失败
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> deviceMessageService.handleUpstreamDeviceMessage(message, gateway));
        assertEquals("状态事务已被标记回滚", ex.getMessage());
        // 断言：网关状态已回滚，不联动子设备
        assertEquals(IotDeviceStateEnum.ONLINE.getState(), deviceMapper.selectById(gateway.getId()).getState());
        assertEquals(IotDeviceStateEnum.ONLINE.getState(), deviceMapper.selectById(subDevice.getId()).getState());
        verify(deviceMessageProducer, never()).sendDeviceMessage(any());
    }

    @Test
    public void testHandleStateUpdate_rejectExistingTransaction() {
        // mock 数据：已离线
        IotDeviceDO device = insertDevice(IotDeviceStateEnum.OFFLINE.getState(), null, null);
        // 准备参数
        IotDeviceMessage message = buildStateMessage(device, IotDeviceStateEnum.ONLINE.getState(), T0, null);

        // 调用，并断言异常：在已有事务中处理状态消息，直接拒绝
        new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                assertThrows(IllegalStateException.class,
                        () -> deviceMessageService.handleUpstreamDeviceMessage(message, device)));
        // 断言：没有更新状态，也没有触发规则
        assertEquals(IotDeviceStateEnum.OFFLINE.getState(), deviceMapper.selectById(device.getId()).getState());
        verify(sceneRuleService, never()).executeSceneRuleByDevice(any());
        verify(dataRuleService, never()).executeDataRule(any());
    }

    @Test
    public void testHandleStateUpdate_evictCacheAfterCommit() {
        // mock 数据：已离线，并且已经缓存
        IotDeviceDO device = insertDevice(IotDeviceStateEnum.OFFLINE.getState(), null, null);
        getDeviceCache().put(device.getId(), device);
        // 准备参数
        IotDeviceMessage message = buildStateMessage(device, IotDeviceStateEnum.ONLINE.getState(), T0, null);
        // mock 方法：事务内，缓存还没有清理
        doAnswer(invocation -> {
            assertNotNull(getDeviceCache().get(device.getId()));
            return null;
        }).when(sceneRuleService).executeSceneRuleByDevice(any());

        // 调用
        deviceMessageService.handleUpstreamDeviceMessage(message, device);
        // 断言：提交后，缓存已清理
        verify(sceneRuleService).executeSceneRuleByDevice(any());
        assertNull(getDeviceCache().get(device.getId()));
    }

    @Test
    public void testHandleStateUpdate_rollbackKeepCache() {
        // mock 数据：已离线，并且已经缓存
        IotDeviceDO device = insertDevice(IotDeviceStateEnum.OFFLINE.getState(), null, null);
        getDeviceCache().put(device.getId(), device);
        // 准备参数
        IotDeviceMessage message = buildStateMessage(device, IotDeviceStateEnum.ONLINE.getState(), T0, null);
        // mock 方法：规则访问数据失败
        doThrow(new QueryTimeoutException("rule data access failed")).when(dataRuleService).executeDataRule(any());

        // 调用
        assertThrows(QueryTimeoutException.class, () -> deviceMessageService.handleUpstreamDeviceMessage(message, device));
        // 断言：回滚后，缓存没有被清理（与数据库一致）
        assertNotNull(getDeviceCache().get(device.getId()));
    }

    // ========== 设备状态：网关下线联动子设备 ==========

    @Test
    public void testHandleStateUpdate_gatewayOffline() {
        // mock 数据：网关在线；子设备 1、2 在线，子设备 3 在网关离线之后重连，子设备 4 已离线
        IotDeviceDO gateway = insertGateway();
        IotDeviceDO subDevice1 = insertSubDevice(gateway, IotDeviceStateEnum.ONLINE.getState(), T0);
        IotDeviceDO subDevice2 = insertSubDevice(gateway, IotDeviceStateEnum.ONLINE.getState(), T0);
        IotDeviceDO subDevice3 = insertSubDevice(gateway, IotDeviceStateEnum.ONLINE.getState(), T0.plusMinutes(20));
        IotDeviceDO subDevice4 = insertSubDevice(gateway, IotDeviceStateEnum.OFFLINE.getState(), T0);
        // 准备参数：网关在 T0 + 10 离线
        IotDeviceMessage message = buildStateMessage(gateway, IotDeviceStateEnum.OFFLINE.getState(),
                T0.plusMinutes(10), "server-1");
        // mock 方法：模拟 Local 总线，在发布线程上同步处理子设备消息
        mockLocalMessageBus();

        // 调用
        deviceMessageService.handleUpstreamDeviceMessage(message, gateway);
        // 断言：网关离线
        assertEquals(IotDeviceStateEnum.OFFLINE.getState(), deviceMapper.selectById(gateway.getId()).getState());
        // 断言：在线的 3 个子设备，各发送一条离线消息，事件时间为网关离线的时间
        ArgumentCaptor<IotDeviceMessage> captor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        verify(deviceMessageProducer, times(3)).sendDeviceMessage(captor.capture());
        captor.getAllValues().forEach(subMessage -> {
            assertEquals(IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod(), subMessage.getMethod());
            assertEquals(String.valueOf(IotDeviceStateEnum.OFFLINE.getState()), IotDeviceMessageUtils.getIdentifier(subMessage));
            assertEquals(T0.plusMinutes(10), subMessage.getReportTime());
            assertNull(subMessage.getServerId());
        });
        // 断言：子设备 1、2 离线；子设备 3 的重连晚于网关离线，保持在线
        assertEquals(IotDeviceStateEnum.OFFLINE.getState(), deviceMapper.selectById(subDevice1.getId()).getState());
        assertEquals(IotDeviceStateEnum.OFFLINE.getState(), deviceMapper.selectById(subDevice2.getId()).getState());
        assertEquals(IotDeviceStateEnum.ONLINE.getState(), deviceMapper.selectById(subDevice3.getId()).getState());
        assertNull(deviceMapper.selectById(subDevice4.getId()).getOfflineTime());
        // 断言：规则触发 3 次（网关、子设备 1、2）
        verify(sceneRuleService, times(3)).executeSceneRuleByDevice(any());
    }

    @Test
    public void testHandleStateUpdate_gatewayOfflineSubDeviceFailed() {
        // mock 数据：网关在线；子设备 1、2 在线
        IotDeviceDO gateway = insertGateway();
        IotDeviceDO subDevice1 = insertSubDevice(gateway, IotDeviceStateEnum.ONLINE.getState(), T0);
        IotDeviceDO subDevice2 = insertSubDevice(gateway, IotDeviceStateEnum.ONLINE.getState(), T0);
        // 准备参数
        IotDeviceMessage message = buildStateMessage(gateway, IotDeviceStateEnum.OFFLINE.getState(),
                T0.plusMinutes(10), "server-1");
        // mock 方法：模拟 Local 总线；子设备 1 的规则访问数据失败
        mockLocalMessageBus();
        doThrow(new QueryTimeoutException("rule data access failed")).when(sceneRuleService)
                .executeSceneRuleByDevice(argThat(m -> subDevice1.getId().equals(m.getDeviceId())));

        // 调用
        deviceMessageService.handleUpstreamDeviceMessage(message, gateway);
        // 断言：子设备 1 自己的事务回滚，不影响已提交的网关和其它子设备
        assertEquals(IotDeviceStateEnum.OFFLINE.getState(), deviceMapper.selectById(gateway.getId()).getState());
        assertEquals(IotDeviceStateEnum.ONLINE.getState(), deviceMapper.selectById(subDevice1.getId()).getState());
        assertEquals(IotDeviceStateEnum.OFFLINE.getState(), deviceMapper.selectById(subDevice2.getId()).getState());
    }

    @Test
    public void testHandleStateUpdate_gatewayRollbackNoSubDeviceMessage() {
        // mock 数据：网关在线；子设备在线
        IotDeviceDO gateway = insertGateway();
        IotDeviceDO subDevice = insertSubDevice(gateway, IotDeviceStateEnum.ONLINE.getState(), T0);
        // 准备参数
        IotDeviceMessage message = buildStateMessage(gateway, IotDeviceStateEnum.OFFLINE.getState(),
                T0.plusMinutes(10), "server-1");
        // mock 方法：网关的规则访问数据失败
        doThrow(new QueryTimeoutException("rule data access failed")).when(sceneRuleService).executeSceneRuleByDevice(any());

        // 调用
        assertThrows(QueryTimeoutException.class, () -> deviceMessageService.handleUpstreamDeviceMessage(message, gateway));
        // 断言：网关状态回滚，不联动子设备
        assertEquals(IotDeviceStateEnum.ONLINE.getState(), deviceMapper.selectById(gateway.getId()).getState());
        assertEquals(IotDeviceStateEnum.ONLINE.getState(), deviceMapper.selectById(subDevice.getId()).getState());
        verify(deviceMessageProducer, never()).sendDeviceMessage(any());
    }

    @Test
    public void testHandleStateUpdate_afterCommitFailedSkipSubDevices() {
        // mock 数据：网关在线；子设备在线
        IotDeviceDO gateway = insertGateway();
        IotDeviceDO subDevice = insertSubDevice(gateway, IotDeviceStateEnum.ONLINE.getState(), T0);
        // 准备参数
        IotDeviceMessage message = buildStateMessage(gateway, IotDeviceStateEnum.OFFLINE.getState(),
                T0.plusMinutes(10), "server-1");
        // mock 方法：提交后的回调失败（例如说清理缓存失败）
        doAnswer(invocation -> {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                @Override
                public void afterCommit() {
                    throw new IllegalStateException("after commit failed");
                }

            });
            return null;
        }).when(sceneRuleService).executeSceneRuleByDevice(any());

        // 调用，并断言异常
        assertThrows(IllegalStateException.class, () -> deviceMessageService.handleUpstreamDeviceMessage(message, gateway));
        // 断言：网关状态已提交，但没有联动子设备
        assertEquals(IotDeviceStateEnum.OFFLINE.getState(), deviceMapper.selectById(gateway.getId()).getState());
        verify(deviceMessageProducer, never()).sendDeviceMessage(any());

        // 调用：重投同一条消息
        deviceMessageService.handleUpstreamDeviceMessage(message, deviceMapper.selectById(gateway.getId()));
        // 断言：已知边界，状态不再变化，子设备仍不联动（由离线检测兜底）
        verify(deviceMessageProducer, never()).sendDeviceMessage(any());
        assertEquals(IotDeviceStateEnum.ONLINE.getState(), deviceMapper.selectById(subDevice.getId()).getState());
    }

    // ========== 设备状态：规则匹配的数据访问异常 ==========

    @Test
    public void testHandleStateUpdate_triggerMatchDataAccessExceptionRollback() {
        // mock 数据：网关在线；子设备在线
        IotDeviceDO gateway = insertGateway();
        IotDeviceDO subDevice = insertSubDevice(gateway, IotDeviceStateEnum.ONLINE.getState(), T0);
        // 准备参数：网关离线；按产品配置的状态触发器，匹配时读取网关的设备缓存
        IotDeviceMessage message = buildStateMessage(gateway, IotDeviceStateEnum.OFFLINE.getState(),
                T0.plusMinutes(10), "server-1");
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.DEVICE_STATE_UPDATE.getType());
        trigger.setProductId(gateway.getProductId());
        trigger.setOperator(IotSceneRuleConditionOperatorEnum.EQUALS.getOperator());
        trigger.setValue(String.valueOf(IotDeviceStateEnum.OFFLINE.getState()));
        // mock 方法：场景联动执行真实的触发器匹配；读取网关的设备缓存失败
        doAnswer(invocation -> sceneRuleMatcherManager.isMatched(invocation.getArgument(0), trigger))
                .when(sceneRuleService).executeSceneRuleByDevice(any());
        mockDeviceCacheGetFailed(gateway.getId());

        // 调用，并断言异常：状态事务内，匹配器的数据访问异常不吞掉
        assertThrows(QueryTimeoutException.class, () -> deviceMessageService.handleUpstreamDeviceMessage(message, gateway));
        // 断言：网关状态回滚，不联动子设备
        assertEquals(IotDeviceStateEnum.ONLINE.getState(), deviceMapper.selectById(gateway.getId()).getState());
        assertEquals(IotDeviceStateEnum.ONLINE.getState(), deviceMapper.selectById(subDevice.getId()).getState());
        verify(deviceMessageProducer, never()).sendDeviceMessage(any());
        // 调用：非事务内，与原逻辑一致，按不匹配处理
        assertFalse(sceneRuleMatcherManager.isMatched(message, trigger));
    }

    @Test
    public void testHandleStateUpdate_conditionMatchDataAccessExceptionRollback() {
        // mock 数据：网关在线；子设备在线
        IotDeviceDO gateway = insertGateway();
        IotDeviceDO subDevice = insertSubDevice(gateway, IotDeviceStateEnum.ONLINE.getState(), T0);
        // 准备参数：网关离线；按产品配置的设备状态条件，匹配时读取网关的设备缓存
        IotDeviceMessage message = buildStateMessage(gateway, IotDeviceStateEnum.OFFLINE.getState(),
                T0.plusMinutes(10), "server-1");
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(IotSceneRuleConditionTypeEnum.DEVICE_STATE.getType());
        condition.setProductId(gateway.getProductId());
        condition.setOperator(IotSceneRuleConditionOperatorEnum.EQUALS.getOperator());
        condition.setParam(String.valueOf(IotDeviceStateEnum.OFFLINE.getState()));
        // mock 方法：场景联动执行真实的条件匹配；读取网关的设备缓存失败
        doAnswer(invocation -> sceneRuleMatcherManager.isConditionMatched(invocation.getArgument(0), condition))
                .when(sceneRuleService).executeSceneRuleByDevice(any());
        mockDeviceCacheGetFailed(gateway.getId());

        // 调用，并断言异常：状态事务内，匹配器的数据访问异常不吞掉
        assertThrows(QueryTimeoutException.class, () -> deviceMessageService.handleUpstreamDeviceMessage(message, gateway));
        // 断言：网关状态回滚，不联动子设备
        assertEquals(IotDeviceStateEnum.ONLINE.getState(), deviceMapper.selectById(gateway.getId()).getState());
        assertEquals(IotDeviceStateEnum.ONLINE.getState(), deviceMapper.selectById(subDevice.getId()).getState());
        verify(deviceMessageProducer, never()).sendDeviceMessage(any());
        // 调用：非事务内，与原逻辑一致，按不匹配处理
        assertFalse(sceneRuleMatcherManager.isConditionMatched(message, condition));
    }

    @Test
    public void testHandleTopoDeleteMessage_subDeviceOffline() {
        // mock 数据：网关；在线的子设备
        IotDeviceDO gateway = insertGateway();
        IotDeviceDO subDevice = insertSubDevice(gateway, IotDeviceStateEnum.ONLINE.getState(), T0);
        // 准备参数
        IotDeviceMessage message = IotDeviceMessage.requestOf(IotDeviceMessageMethodEnum.TOPO_DELETE.getMethod(),
                Collections.singletonMap("subDevices",
                        singletonList(new IotDeviceIdentity(subDevice.getProductKey(), subDevice.getDeviceName()))));

        // 调用
        List<IotDeviceIdentity> result = deviceService.handleTopoDeleteMessage(message, gateway);
        // 断言：解绑
        assertEquals(1, result.size());
        assertNull(deviceMapper.selectById(subDevice.getId()).getGatewayId());
        // 断言：子设备离线走消息
        verify(deviceMessageProducer).sendDeviceMessage(argThat(subMessage ->
                subDevice.getId().equals(subMessage.getDeviceId())
                        && IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod().equals(subMessage.getMethod())
                        && String.valueOf(IotDeviceStateEnum.OFFLINE.getState()).equals(IotDeviceMessageUtils.getIdentifier(subMessage))
                        && subMessage.getReportTime() != null));
    }

    // ========== 辅助方法 ==========

    /**
     * 模拟 Local 总线：发布的消息，在发布线程上同步处理；订阅者的异常被吞掉（与 IotLocalMessageBus 一致）
     */
    private void mockLocalMessageBus() {
        doAnswer(invocation -> {
            IotDeviceMessage subMessage = invocation.getArgument(0);
            // 断言：在网关的状态事务提交之后，才发送子设备消息
            assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
            try {
                deviceMessageService.handleUpstreamDeviceMessage(subMessage, deviceMapper.selectById(subMessage.getDeviceId()));
            } catch (Exception ignored) {
            }
            return null;
        }).when(deviceMessageProducer).sendDeviceMessage(any());
    }

    private Cache getDeviceCache() {
        return cacheManager.getCache(RedisKeyConstants.DEVICE);
    }

    /**
     * 模拟读取指定设备的缓存失败（例如说 Redis 超时）：异常经 Spring 缓存抛出，与线上 Redis 缓存故障一致
     */
    private void mockDeviceCacheGetFailed(Long deviceId) {
        Cache deviceCache = spy(getDeviceCache());
        doThrow(new QueryTimeoutException("redis read timeout")).when(deviceCache).get(deviceId);
        doReturn(deviceCache).when(cacheManager).getCache(RedisKeyConstants.DEVICE);
    }

    private IotDeviceDO insertDevice(Integer state, LocalDateTime onlineTime, LocalDateTime offlineTime) {
        IotDeviceDO device = buildDevice();
        device.setState(state);
        device.setOnlineTime(onlineTime);
        device.setOfflineTime(offlineTime);
        device.setActiveTime(onlineTime);
        deviceMapper.insert(device);
        return deviceMapper.selectById(device.getId());
    }

    private IotDeviceDO insertGateway() {
        IotDeviceDO gateway = IotDeviceDO.builder().deviceName("testGateway").productId(20L).productKey("testGatewayProduct")
                .deviceType(IotProductDeviceTypeEnum.GATEWAY.getType())
                .state(IotDeviceStateEnum.ONLINE.getState()).onlineTime(T0).activeTime(T0).build();
        deviceMapper.insert(gateway);
        return deviceMapper.selectById(gateway.getId());
    }

    private IotDeviceDO insertSubDevice(IotDeviceDO gateway, Integer state, LocalDateTime onlineTime) {
        IotDeviceDO subDevice = IotDeviceDO.builder().deviceName("testSubDevice" + System.nanoTime())
                .productId(30L).productKey("testSubProduct")
                .deviceType(IotProductDeviceTypeEnum.GATEWAY_SUB.getType()).gatewayId(gateway.getId())
                .state(state).onlineTime(onlineTime).activeTime(onlineTime).build();
        deviceMapper.insert(subDevice);
        return deviceMapper.selectById(subDevice.getId());
    }

    private static IotDeviceMessage buildStateMessage(IotDeviceDO device, Integer state,
                                                      LocalDateTime reportTime, String serverId) {
        IotDeviceMessage message = IotDeviceStateEnum.ONLINE.getState().equals(state)
                ? IotDeviceMessage.buildStateUpdateOnline() : IotDeviceMessage.buildStateOffline();
        message.setId("msg-" + System.nanoTime());
        message.setDeviceId(device.getId());
        message.setReportTime(reportTime);
        message.setServerId(serverId);
        return message;
    }

    private static IotDeviceDO buildDevice() {
        return IotDeviceDO.builder().deviceName("testDevice").productId(10L).productKey("testProduct")
                .deviceType(IotProductDeviceTypeEnum.DIRECT.getType()).build();
    }

}
