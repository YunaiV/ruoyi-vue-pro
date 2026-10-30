package cn.iocoder.yudao.module.iot.service.rule.scene;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotSceneRuleSaveReqVO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleActionTypeEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.dal.mysql.rule.IotSceneRuleMapper;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.service.rule.scene.action.IotSceneRuleAction;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotSceneRuleMatcherManager;
import cn.iocoder.yudao.module.iot.service.rule.scene.timer.IotSceneRuleTimerHandler;
import cn.iocoder.yudao.module.iot.service.rule.scene.timer.IotTimerConditionEvaluator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link IotSceneRuleServiceImpl} 的简化单元测试类
 * 使用 Mockito 进行纯单元测试，不依赖 Spring 容器
 *
 * @author 芋道源码
 */
public class IotSceneRuleServiceSimpleTest extends BaseMockitoUnitTest {

    @InjectMocks
    private IotSceneRuleServiceImpl sceneRuleService;

    @Mock
    private IotSceneRuleMapper sceneRuleMapper;
    @Mock
    private List<IotSceneRuleAction> sceneRuleActions;
    @Mock
    private IotSceneRuleTimerHandler timerHandler;
    @Mock
    private IotTimerConditionEvaluator timerConditionEvaluator;
    @Mock
    private IotSceneRuleMatcherManager sceneRuleMatcherManager;
    @Mock
    private IotProductService productService;
    @Mock
    private IotDeviceService deviceService;
    @Mock
    private IotSceneRuleAction sceneRuleAction;

    @Test
    public void testCreateScene_rule_success() {
        // 准备参数
        IotSceneRuleSaveReqVO createReqVO = randomPojo(IotSceneRuleSaveReqVO.class, o -> {
            o.setId(null);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setTriggers(Collections.singletonList(randomPojo(IotSceneRuleDO.Trigger.class)));
            o.setActions(Collections.singletonList(randomPojo(IotSceneRuleDO.Action.class)));
        });

        // Mock 行为
        Long expectedId = randomLongId();
        when(sceneRuleMapper.insert(any(IotSceneRuleDO.class))).thenAnswer(invocation -> {
            IotSceneRuleDO sceneRule = invocation.getArgument(0);
            sceneRule.setId(expectedId);
            return 1;
        });

        // 调用
        Long sceneRuleId = sceneRuleService.createSceneRule(createReqVO);

        // 断言
        assertEquals(expectedId, sceneRuleId);
        verify(sceneRuleMapper, times(1)).insert(any(IotSceneRuleDO.class));
    }

    @Test
    public void testUpdateScene_rule_success() {
        // 准备参数
        Long id = randomLongId();
        IotSceneRuleSaveReqVO updateReqVO = randomPojo(IotSceneRuleSaveReqVO.class, o -> {
            o.setId(id);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setTriggers(Collections.singletonList(randomPojo(IotSceneRuleDO.Trigger.class)));
            o.setActions(Collections.singletonList(randomPojo(IotSceneRuleDO.Action.class)));
        });

        // Mock 行为
        IotSceneRuleDO existingSceneRule = randomPojo(IotSceneRuleDO.class, o -> o.setId(id));
        when(sceneRuleMapper.selectById(id)).thenReturn(existingSceneRule);
        when(sceneRuleMapper.updateById(any(IotSceneRuleDO.class))).thenReturn(1);

        // 调用
        assertDoesNotThrow(() -> sceneRuleService.updateSceneRule(updateReqVO));

        // 验证
        verify(sceneRuleMapper, times(1)).selectById(id);
        verify(sceneRuleMapper, times(1)).updateById(any(IotSceneRuleDO.class));
    }

    @Test
    public void testDeleteSceneRule_success() {
        // 准备参数
        Long id = randomLongId();

        // Mock 行为
        IotSceneRuleDO existingSceneRule = randomPojo(IotSceneRuleDO.class, o -> o.setId(id));
        when(sceneRuleMapper.selectById(id)).thenReturn(existingSceneRule);
        when(sceneRuleMapper.deleteById(id)).thenReturn(1);

        // 调用
        assertDoesNotThrow(() -> sceneRuleService.deleteSceneRule(id));

        // 验证
        verify(sceneRuleMapper, times(1)).selectById(id);
        verify(sceneRuleMapper, times(1)).deleteById(id);
    }

    @Test
    public void testGetSceneRule() {
        // 准备参数
        Long id = randomLongId();
        IotSceneRuleDO expectedSceneRule = randomPojo(IotSceneRuleDO.class, o -> o.setId(id));

        // Mock 行为
        when(sceneRuleMapper.selectById(id)).thenReturn(expectedSceneRule);

        // 调用
        IotSceneRuleDO result = sceneRuleService.getSceneRule(id);

        // 断言
        assertEquals(expectedSceneRule, result);
        verify(sceneRuleMapper, times(1)).selectById(id);
    }

    @Test
    public void testUpdateSceneRuleStatus_success() {
        // 准备参数
        Long id = randomLongId();
        Integer status = CommonStatusEnum.DISABLE.getStatus();

        // Mock 行为
        IotSceneRuleDO existingSceneRule = randomPojo(IotSceneRuleDO.class, o -> {
            o.setId(id);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(sceneRuleMapper.selectById(id)).thenReturn(existingSceneRule);
        when(sceneRuleMapper.updateById(any(IotSceneRuleDO.class))).thenReturn(1);

        // 调用
        assertDoesNotThrow(() -> sceneRuleService.updateSceneRuleStatus(id, status));

        // 验证
        verify(sceneRuleMapper, times(1)).selectById(id);
        verify(sceneRuleMapper, times(1)).updateById(any(IotSceneRuleDO.class));
    }

    @Test
    public void testExecuteSceneRuleByTimer_success() {
        // 准备参数
        Long id = randomLongId();

        // Mock 行为
        IotSceneRuleDO sceneRule = randomPojo(IotSceneRuleDO.class, o -> {
            o.setId(id);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(sceneRuleMapper.selectById(id)).thenReturn(sceneRule);

        // 调用
        assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(id));

        // 验证
        verify(sceneRuleMapper, times(1)).selectById(id);
    }

    @Test
    public void testExecuteSceneRuleByTimer_notExists() {
        // 准备参数
        Long id = randomLongId();

        // Mock 行为
        when(sceneRuleMapper.selectById(id)).thenReturn(null);

        // 调用 - 不存在的场景规则应该不会抛异常，只是记录日志
        assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(id));

        // 验证
        verify(sceneRuleMapper, times(1)).selectById(id);
    }

    @Test
    public void testExecuteSceneRuleByTimer_disabled() {
        // 准备参数
        Long id = randomLongId();

        // Mock 行为
        IotSceneRuleDO sceneRule = randomPojo(IotSceneRuleDO.class, o -> {
            o.setId(id);
            o.setStatus(CommonStatusEnum.DISABLE.getStatus());
        });
        when(sceneRuleMapper.selectById(id)).thenReturn(sceneRule);

        // 调用 - 禁用的场景规则应该不会执行，只是记录日志
        assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(id));

        // 验证
        verify(sceneRuleMapper, times(1)).selectById(id);
    }

    @Test
    public void testExecuteSceneRuleByDevice_deviceNotExists() {
        // 准备参数：设备已删除时，消息仍在途
        IotDeviceMessage message = new IotDeviceMessage().setDeviceId(randomLongId())
                .setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod());

        // Mock 行为
        when(deviceService.getDeviceFromCache(message.getDeviceId())).thenReturn(null);

        // 调用 - 设备不存在时跳过，不抛出异常
        assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByDevice(message));

        // 验证：不再匹配、执行场景规则
        verify(deviceService, times(1)).getDeviceFromCache(message.getDeviceId());
        verifyNoInteractions(sceneRuleMapper, productService, sceneRuleMatcherManager, sceneRuleActions);
    }

    @Test
    public void testExecuteSceneRuleByDevice_queryException() {
        // 准备参数
        IotDeviceMessage message = new IotDeviceMessage().setDeviceId(randomLongId())
                .setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod());

        // Mock 行为：查询设备时 Redis 超时
        when(deviceService.getDeviceFromCache(message.getDeviceId()))
                .thenThrow(new QueryTimeoutException("Redis 超时"));

        // 调用，并断言异常：可恢复异常继续抛出，由消息重投重试
        assertThrows(QueryTimeoutException.class, () -> sceneRuleService.executeSceneRuleByDevice(message));
        verifyNoInteractions(sceneRuleMapper, productService, sceneRuleMatcherManager, sceneRuleActions);
    }

    // ========== executeSceneRuleByDevice：状态事务内的数据访问异常 ==========
    // 说明：以下用例只标记“存在实际事务”，验证各 catch 的判断与原语义；不是真实的数据库事务，
    //      真实的回滚由 IotDeviceServiceImplTest 覆盖

    @Test
    public void testExecuteSceneRuleByDevice_triggerMatchDataAccessException() {
        // 准备参数
        IotDeviceMessage message = buildPropertyPostMessage();
        IotSceneRuleDO sceneRule = buildSceneRule(null);
        // mock 方法：触发器匹配时访问数据失败
        when(sceneRuleMatcherManager.isMatched(eq(message), any())).thenThrow(new QueryTimeoutException("matcher failed"));

        try (MockedStatic<SpringUtil> ignored = mockMatchedSceneRule(message, sceneRule)) {
            // 调用，并断言异常：状态事务内，不吞掉
            assertThrows(QueryTimeoutException.class,
                    () -> executeInActualTransaction(() -> sceneRuleService.executeSceneRuleByDevice(message)));
            // 调用：非事务内，与原逻辑一致，记录日志后视为不匹配
            assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByDevice(message));
        }
        // 断言：没有执行动作
        verifyNoInteractions(sceneRuleAction);
        verify(sceneRuleMatcherManager, never()).isConditionMatched(any(), any());
    }

    @Test
    public void testExecuteSceneRuleByDevice_conditionMatchDataAccessException() {
        // 准备参数：触发器带一个条件
        IotDeviceMessage message = buildPropertyPostMessage();
        IotSceneRuleDO sceneRule = buildSceneRule(Collections.singletonList(
                Collections.singletonList(new IotSceneRuleDO.TriggerCondition())));
        // mock 方法：触发器匹配；条件匹配时访问数据失败
        when(sceneRuleMatcherManager.isMatched(eq(message), any())).thenReturn(true);
        when(sceneRuleMatcherManager.isConditionMatched(eq(message), any())).thenThrow(new QueryTimeoutException("condition failed"));

        try (MockedStatic<SpringUtil> ignored = mockMatchedSceneRule(message, sceneRule)) {
            // 调用，并断言异常：状态事务内，不吞掉
            assertThrows(QueryTimeoutException.class,
                    () -> executeInActualTransaction(() -> sceneRuleService.executeSceneRuleByDevice(message)));
            // 调用：非事务内，与原逻辑一致，视为条件不满足
            assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByDevice(message));
        }
        // 断言：没有执行动作
        verifyNoInteractions(sceneRuleAction);
    }

    @Test
    public void testExecuteSceneRuleByDevice_actionDataAccessException() throws Exception {
        // 准备参数
        IotDeviceMessage message = buildPropertyPostMessage();
        IotSceneRuleDO sceneRule = buildSceneRule(null);
        // mock 方法：触发器匹配；动作执行时访问数据失败
        when(sceneRuleMatcherManager.isMatched(eq(message), any())).thenReturn(true);
        mockSceneRuleAction();
        doThrow(new QueryTimeoutException("action failed")).when(sceneRuleAction).execute(any(), any(), any());

        try (MockedStatic<SpringUtil> ignored = mockMatchedSceneRule(message, sceneRule)) {
            // 调用，并断言异常：状态事务内，不吞掉，也不再更新最后触发时间
            assertThrows(QueryTimeoutException.class,
                    () -> executeInActualTransaction(() -> sceneRuleService.executeSceneRuleByDevice(message)));
            verify(sceneRuleMapper, never()).updateById(any(IotSceneRuleDO.class));
            // 调用：非事务内，与原逻辑一致，记录日志后继续更新最后触发时间
            assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByDevice(message));
        }
        verify(sceneRuleMapper, times(1)).updateById(any(IotSceneRuleDO.class));
    }

    @Test
    public void testExecuteSceneRuleByDevice_updateLastTriggerTimeDataAccessException() throws Exception {
        // 准备参数
        IotDeviceMessage message = buildPropertyPostMessage();
        IotSceneRuleDO sceneRule = buildSceneRule(null);
        // mock 方法：动作执行成功；更新最后触发时间失败（例如说热点行的锁等待超时）
        when(sceneRuleMatcherManager.isMatched(eq(message), any())).thenReturn(true);
        mockSceneRuleAction();
        when(sceneRuleMapper.updateById(any(IotSceneRuleDO.class))).thenThrow(new QueryTimeoutException("update failed"));

        try (MockedStatic<SpringUtil> ignored = mockMatchedSceneRule(message, sceneRule)) {
            // 调用，并断言异常：状态事务内，不吞掉
            assertThrows(QueryTimeoutException.class,
                    () -> executeInActualTransaction(() -> sceneRuleService.executeSceneRuleByDevice(message)));
            // 调用：非事务内，与原逻辑一致，记录日志后结束
            assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByDevice(message));
        }
        verify(sceneRuleAction, times(2)).execute(same(message), same(sceneRule), any());
    }

    // ========== 辅助方法 ==========

    /**
     * 标记“存在实际事务”后执行：只用于验证 catch 的判断条件，不是真实的数据库事务
     */
    private static void executeInActualTransaction(Executable executable) throws Throwable {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            executable.execute();
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }

    /**
     * mock 消息对应的设备、产品，以及匹配的规则场景（getSelf 的缓存方法）
     */
    private MockedStatic<SpringUtil> mockMatchedSceneRule(IotDeviceMessage message, IotSceneRuleDO sceneRule) {
        IotDeviceDO device = IotDeviceDO.builder().id(message.getDeviceId()).productId(randomLongId()).build();
        device.setTenantId(1L);
        when(deviceService.getDeviceFromCache(message.getDeviceId())).thenReturn(device);
        when(productService.getProductFromCache(device.getProductId()))
                .thenReturn(IotProductDO.builder().id(device.getProductId()).build());
        IotSceneRuleServiceImpl self = mock(IotSceneRuleServiceImpl.class);
        when(self.getSceneRuleListByProductIdAndDeviceIdFromCache(device.getProductId(), device.getId()))
                .thenReturn(Collections.singletonList(sceneRule));
        MockedStatic<SpringUtil> springUtilMock = mockStatic(SpringUtil.class);
        springUtilMock.when(() -> SpringUtil.getBean(IotSceneRuleServiceImpl.class)).thenReturn(self);
        return springUtilMock;
    }

    private void mockSceneRuleAction() {
        when(sceneRuleAction.getType()).thenReturn(IotSceneRuleActionTypeEnum.ALERT_TRIGGER);
        ReflectUtil.setFieldValue(sceneRuleService, "sceneRuleActions", Collections.singletonList(sceneRuleAction));
    }

    private static IotDeviceMessage buildPropertyPostMessage() {
        return new IotDeviceMessage().setDeviceId(randomLongId())
                .setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod());
    }

    private static IotSceneRuleDO buildSceneRule(List<List<IotSceneRuleDO.TriggerCondition>> conditionGroups) {
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger()
                .setType(IotSceneRuleTriggerTypeEnum.DEVICE_PROPERTY_POST.getType()).setConditionGroups(conditionGroups);
        IotSceneRuleDO.Action action = new IotSceneRuleDO.Action().setType(IotSceneRuleActionTypeEnum.ALERT_TRIGGER.getType());
        return new IotSceneRuleDO().setId(randomLongId()).setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setTriggers(Collections.singletonList(trigger)).setActions(Collections.singletonList(action));
    }

}
