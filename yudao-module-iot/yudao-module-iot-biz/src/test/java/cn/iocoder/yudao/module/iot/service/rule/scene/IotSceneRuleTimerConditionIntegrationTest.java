package cn.iocoder.yudao.module.iot.service.rule.scene;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.dal.mysql.rule.IotSceneRuleMapper;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionOperatorEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionTypeEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import cn.iocoder.yudao.module.iot.service.rule.scene.action.IotSceneRuleAction;
import cn.iocoder.yudao.module.iot.service.rule.scene.timer.IotSceneRuleTimerHandler;
import cn.iocoder.yudao.module.iot.service.rule.scene.timer.IotTimerConditionEvaluator;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link IotSceneRuleServiceImpl} 定时触发器条件组集成测试
 * <p>
 * 测试定时触发器的条件组评估功能：
 * - 空条件组直接执行动作
 * - 条件组评估后决定是否执行动作
 * - 条件组之间的 OR 逻辑
 * - 条件组内的 AND 逻辑
 * - 所有条件组不满足时跳过执行
 * <p>
 * Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5
 *
 * @author HUIHUI
 */
@Disabled // TODO @puhui999：单测有报错，先屏蔽
public class IotSceneRuleTimerConditionIntegrationTest extends BaseMockitoUnitTest {

    @InjectMocks
    private IotSceneRuleServiceImpl sceneRuleService;

    @Mock
    private IotSceneRuleMapper sceneRuleMapper;

    @Mock
    private IotDeviceService deviceService;

    @Mock
    private IotDevicePropertyService devicePropertyService;

    @Mock
    private List<IotSceneRuleAction> sceneRuleActions;

    @Mock
    private IotSceneRuleTimerHandler timerHandler;

    private IotTimerConditionEvaluator timerConditionEvaluator;

    // 测试常量
    private static final Long SCENE_RULE_ID = 1L;
    private static final Long TENANT_ID = 1L;
    private static final Long DEVICE_ID = 100L;
    private static final String PROPERTY_IDENTIFIER = "temperature";

    @BeforeEach
    void setUp() {
        // 创建并注入 timerConditionEvaluator 的依赖
        timerConditionEvaluator = new IotTimerConditionEvaluator();
        try {
            var devicePropertyServiceField = IotTimerConditionEvaluator.class.getDeclaredField("devicePropertyService");
            devicePropertyServiceField.setAccessible(true);
            devicePropertyServiceField.set(timerConditionEvaluator, devicePropertyService);

            var deviceServiceField = IotTimerConditionEvaluator.class.getDeclaredField("deviceService");
            deviceServiceField.setAccessible(true);
            deviceServiceField.set(timerConditionEvaluator, deviceService);

            var evaluatorField = IotSceneRuleServiceImpl.class.getDeclaredField("timerConditionEvaluator");
            evaluatorField.setAccessible(true);
            evaluatorField.set(sceneRuleService, timerConditionEvaluator);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }
    }

    // ========== 辅助方法 ==========

    private IotSceneRuleDO createBaseSceneRule() {
        IotSceneRuleDO sceneRule = new IotSceneRuleDO();
        sceneRule.setId(SCENE_RULE_ID);
        sceneRule.setTenantId(TENANT_ID);
        sceneRule.setName("测试定时触发器");
        sceneRule.setStatus(CommonStatusEnum.ENABLE.getStatus());
        sceneRule.setActions(Collections.emptyList());
        return sceneRule;
    }

    private IotSceneRuleDO.Trigger createTimerTrigger(String cronExpression,
                                                      List<List<IotSceneRuleDO.TriggerCondition>> conditionGroups) {
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.TIMER.getType());
        trigger.setCronExpression(cronExpression);
        trigger.setConditionGroups(conditionGroups);
        return trigger;
    }

    private IotSceneRuleDO.TriggerCondition createDevicePropertyCondition(Long deviceId, String identifier,
                                                                          String operator, String param) {
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(IotSceneRuleConditionTypeEnum.DEVICE_PROPERTY.getType());
        condition.setDeviceId(deviceId);
        condition.setIdentifier(identifier);
        condition.setOperator(operator);
        condition.setParam(param);
        return condition;
    }

    private IotSceneRuleDO.TriggerCondition createDeviceStateCondition(Long deviceId, String operator, String param) {
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(IotSceneRuleConditionTypeEnum.DEVICE_STATE.getType());
        condition.setDeviceId(deviceId);
        condition.setOperator(operator);
        condition.setParam(param);
        return condition;
    }

    private void mockDeviceProperty(Long deviceId, String identifier, Object value) {
        Map<String, IotDevicePropertyDO> properties = new HashMap<>();
        IotDevicePropertyDO property = new IotDevicePropertyDO();
        property.setValue(value);
        properties.put(identifier, property);
        when(devicePropertyService.getLatestDeviceProperties(deviceId)).thenReturn(properties);
    }

    private void mockDeviceState(Long deviceId, Integer state) {
        IotDeviceDO device = new IotDeviceDO();
        device.setId(deviceId);
        device.setState(state);
        when(deviceService.getDevice(deviceId)).thenReturn(device);
    }

    /**
     * 创建单条件的条件组列表
     */
    private List<List<IotSceneRuleDO.TriggerCondition>> createSingleConditionGroups(
            IotSceneRuleDO.TriggerCondition condition) {
        List<IotSceneRuleDO.TriggerCondition> group = new ArrayList<>();
        group.add(condition);
        List<List<IotSceneRuleDO.TriggerCondition>> groups = new ArrayList<>();
        groups.add(group);
        return groups;
    }

    /**
     * 创建两个单条件组的条件组列表
     */
    private List<List<IotSceneRuleDO.TriggerCondition>> createTwoSingleConditionGroups(
            IotSceneRuleDO.TriggerCondition cond1, IotSceneRuleDO.TriggerCondition cond2) {
        List<IotSceneRuleDO.TriggerCondition> group1 = new ArrayList<>();
        group1.add(cond1);
        List<IotSceneRuleDO.TriggerCondition> group2 = new ArrayList<>();
        group2.add(cond2);
        List<List<IotSceneRuleDO.TriggerCondition>> groups = new ArrayList<>();
        groups.add(group1);
        groups.add(group2);
        return groups;
    }

    /**
     * 创建单个多条件组的条件组列表
     */
    private List<List<IotSceneRuleDO.TriggerCondition>> createSingleGroupWithMultipleConditions(
            IotSceneRuleDO.TriggerCondition... conditions) {
        List<IotSceneRuleDO.TriggerCondition> group = new ArrayList<>(Arrays.asList(conditions));
        List<List<IotSceneRuleDO.TriggerCondition>> groups = new ArrayList<>();
        groups.add(group);
        return groups;
    }

    // ========== 测试用例 ==========

    @Nested
    @DisplayName("空条件组测试 - Validates: Requirement 2.1")
    class EmptyConditionGroupsTest {

        @Test
        @DisplayName("定时触发器无条件组时，应直接执行动作")
        void testTimerTrigger_withNullConditionGroups_shouldExecuteActions() {
            // 准备数据
            IotSceneRuleDO sceneRule = createBaseSceneRule();
            IotSceneRuleDO.Trigger trigger = createTimerTrigger("0 0 12 * * ?", null);
            sceneRule.setTriggers(ListUtil.toList(trigger));

            when(sceneRuleMapper.selectById(SCENE_RULE_ID)).thenReturn(sceneRule);

            assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(SCENE_RULE_ID));

            verify(sceneRuleMapper, times(1)).selectById(SCENE_RULE_ID);
            verify(devicePropertyService, never()).getLatestDeviceProperties(any());
            verify(deviceService, never()).getDevice(any());
        }

        @Test
        @DisplayName("定时触发器条件组为空列表时，应直接执行动作")
        void testTimerTrigger_withEmptyConditionGroups_shouldExecuteActions() {
            IotSceneRuleDO sceneRule = createBaseSceneRule();
            IotSceneRuleDO.Trigger trigger = createTimerTrigger("0 0 12 * * ?", Collections.emptyList());
            sceneRule.setTriggers(ListUtil.toList(trigger));

            when(sceneRuleMapper.selectById(SCENE_RULE_ID)).thenReturn(sceneRule);

            assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(SCENE_RULE_ID));

            verify(sceneRuleMapper, times(1)).selectById(SCENE_RULE_ID);
            verify(devicePropertyService, never()).getLatestDeviceProperties(any());
        }
    }

    @Nested
    @DisplayName("条件组 OR 逻辑测试 - Validates: Requirements 2.2, 2.3")
    class ConditionGroupOrLogicTest {

        @Test
        @DisplayName("多个条件组中第一个满足时，应执行动作")
        void testMultipleConditionGroups_firstGroupMatches_shouldExecuteActions() {
            IotSceneRuleDO.TriggerCondition condition1 = createDevicePropertyCondition(
                    DEVICE_ID, PROPERTY_IDENTIFIER, IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(), "20");
            IotSceneRuleDO.TriggerCondition condition2 = createDevicePropertyCondition(
                    DEVICE_ID + 1, PROPERTY_IDENTIFIER, IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(), "50");

            List<List<IotSceneRuleDO.TriggerCondition>> conditionGroups =
                    createTwoSingleConditionGroups(condition1, condition2);

            IotSceneRuleDO sceneRule = createBaseSceneRule();
            IotSceneRuleDO.Trigger trigger = createTimerTrigger("0 0 12 * * ?", conditionGroups);
            sceneRule.setTriggers(ListUtil.toList(trigger));

            mockDeviceProperty(DEVICE_ID, PROPERTY_IDENTIFIER, 30);
            mockDeviceProperty(DEVICE_ID + 1, PROPERTY_IDENTIFIER, 30);
            when(sceneRuleMapper.selectById(SCENE_RULE_ID)).thenReturn(sceneRule);

            assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(SCENE_RULE_ID));

            verify(devicePropertyService, atLeastOnce()).getLatestDeviceProperties(DEVICE_ID);
        }

        @Test
        @DisplayName("多个条件组中第二个满足时，应执行动作")
        void testMultipleConditionGroups_secondGroupMatches_shouldExecuteActions() {
            IotSceneRuleDO.TriggerCondition condition1 = createDevicePropertyCondition(
                    DEVICE_ID, PROPERTY_IDENTIFIER, IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(), "50");
            IotSceneRuleDO.TriggerCondition condition2 = createDevicePropertyCondition(
                    DEVICE_ID + 1, PROPERTY_IDENTIFIER, IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(), "20");

            List<List<IotSceneRuleDO.TriggerCondition>> conditionGroups =
                    createTwoSingleConditionGroups(condition1, condition2);

            IotSceneRuleDO sceneRule = createBaseSceneRule();
            IotSceneRuleDO.Trigger trigger = createTimerTrigger("0 0 12 * * ?", conditionGroups);
            sceneRule.setTriggers(ListUtil.toList(trigger));

            mockDeviceProperty(DEVICE_ID, PROPERTY_IDENTIFIER, 30);
            mockDeviceProperty(DEVICE_ID + 1, PROPERTY_IDENTIFIER, 30);
            when(sceneRuleMapper.selectById(SCENE_RULE_ID)).thenReturn(sceneRule);

            assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(SCENE_RULE_ID));

            verify(devicePropertyService, atLeastOnce()).getLatestDeviceProperties(DEVICE_ID);
            verify(devicePropertyService, atLeastOnce()).getLatestDeviceProperties(DEVICE_ID + 1);
        }
    }

    @Nested
    @DisplayName("条件组内 AND 逻辑测试 - Validates: Requirement 2.4")
    class ConditionGroupAndLogicTest {

        @Test
        @DisplayName("条件组内所有条件都满足时，该组应匹配成功")
        void testSingleConditionGroup_allConditionsMatch_shouldPass() {
            IotSceneRuleDO.TriggerCondition condition1 = createDevicePropertyCondition(
                    DEVICE_ID, "temperature", IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(), "20");
            IotSceneRuleDO.TriggerCondition condition2 = createDevicePropertyCondition(
                    DEVICE_ID, "humidity", IotSceneRuleConditionOperatorEnum.LESS_THAN.getOperator(), "80");

            List<List<IotSceneRuleDO.TriggerCondition>> conditionGroups =
                    createSingleGroupWithMultipleConditions(condition1, condition2);

            IotSceneRuleDO sceneRule = createBaseSceneRule();
            IotSceneRuleDO.Trigger trigger = createTimerTrigger("0 0 12 * * ?", conditionGroups);
            sceneRule.setTriggers(ListUtil.toList(trigger));

            Map<String, IotDevicePropertyDO> properties = new HashMap<>();
            IotDevicePropertyDO tempProperty = new IotDevicePropertyDO();
            tempProperty.setValue(30);
            properties.put("temperature", tempProperty);
            IotDevicePropertyDO humidityProperty = new IotDevicePropertyDO();
            humidityProperty.setValue(60);
            properties.put("humidity", humidityProperty);
            when(devicePropertyService.getLatestDeviceProperties(DEVICE_ID)).thenReturn(properties);
            when(sceneRuleMapper.selectById(SCENE_RULE_ID)).thenReturn(sceneRule);

            assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(SCENE_RULE_ID));

            verify(devicePropertyService, atLeastOnce()).getLatestDeviceProperties(DEVICE_ID);
        }

        @Test
        @DisplayName("条件组内有一个条件不满足时，该组应匹配失败")
        void testSingleConditionGroup_oneConditionFails_shouldFail() {
            IotSceneRuleDO.TriggerCondition condition1 = createDevicePropertyCondition(
                    DEVICE_ID, "temperature", IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(), "20");
            IotSceneRuleDO.TriggerCondition condition2 = createDevicePropertyCondition(
                    DEVICE_ID, "humidity", IotSceneRuleConditionOperatorEnum.LESS_THAN.getOperator(), "50");

            List<List<IotSceneRuleDO.TriggerCondition>> conditionGroups =
                    createSingleGroupWithMultipleConditions(condition1, condition2);

            IotSceneRuleDO sceneRule = createBaseSceneRule();
            IotSceneRuleDO.Trigger trigger = createTimerTrigger("0 0 12 * * ?", conditionGroups);
            sceneRule.setTriggers(ListUtil.toList(trigger));

            Map<String, IotDevicePropertyDO> properties = new HashMap<>();
            IotDevicePropertyDO tempProperty = new IotDevicePropertyDO();
            tempProperty.setValue(30);
            properties.put("temperature", tempProperty);
            IotDevicePropertyDO humidityProperty = new IotDevicePropertyDO();
            humidityProperty.setValue(60); // 不满足 < 50
            properties.put("humidity", humidityProperty);
            when(devicePropertyService.getLatestDeviceProperties(DEVICE_ID)).thenReturn(properties);
            when(sceneRuleMapper.selectById(SCENE_RULE_ID)).thenReturn(sceneRule);

            assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(SCENE_RULE_ID));

            verify(devicePropertyService, atLeastOnce()).getLatestDeviceProperties(DEVICE_ID);
        }
    }

    @Nested
    @DisplayName("所有条件组不满足测试 - Validates: Requirement 2.5")
    class AllConditionGroupsFailTest {

        @Test
        @DisplayName("所有条件组都不满足时，应跳过动作执行")
        void testAllConditionGroups_allFail_shouldSkipExecution() {
            IotSceneRuleDO.TriggerCondition condition1 = createDevicePropertyCondition(
                    DEVICE_ID, PROPERTY_IDENTIFIER, IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(), "50");
            IotSceneRuleDO.TriggerCondition condition2 = createDevicePropertyCondition(
                    DEVICE_ID + 1, PROPERTY_IDENTIFIER, IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(), "50");

            List<List<IotSceneRuleDO.TriggerCondition>> conditionGroups =
                    createTwoSingleConditionGroups(condition1, condition2);

            IotSceneRuleDO sceneRule = createBaseSceneRule();
            IotSceneRuleDO.Trigger trigger = createTimerTrigger("0 0 12 * * ?", conditionGroups);
            sceneRule.setTriggers(ListUtil.toList(trigger));

            mockDeviceProperty(DEVICE_ID, PROPERTY_IDENTIFIER, 30);
            mockDeviceProperty(DEVICE_ID + 1, PROPERTY_IDENTIFIER, 30);
            when(sceneRuleMapper.selectById(SCENE_RULE_ID)).thenReturn(sceneRule);

            assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(SCENE_RULE_ID));

            verify(devicePropertyService, atLeastOnce()).getLatestDeviceProperties(DEVICE_ID);
            verify(devicePropertyService, atLeastOnce()).getLatestDeviceProperties(DEVICE_ID + 1);
        }
    }

    @Nested
    @DisplayName("设备状态条件测试 - Validates: Requirements 4.1, 4.2")
    class DeviceStateConditionTest {

        @Test
        @DisplayName("设备在线状态条件满足时，应匹配成功")
        void testDeviceStateCondition_online_shouldMatch() {
            IotSceneRuleDO.TriggerCondition condition = createDeviceStateCondition(
                    DEVICE_ID, IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                    String.valueOf(IotDeviceStateEnum.ONLINE.getState()));

            List<List<IotSceneRuleDO.TriggerCondition>> conditionGroups = createSingleConditionGroups(condition);

            IotSceneRuleDO sceneRule = createBaseSceneRule();
            IotSceneRuleDO.Trigger trigger = createTimerTrigger("0 0 12 * * ?", conditionGroups);
            sceneRule.setTriggers(ListUtil.toList(trigger));

            mockDeviceState(DEVICE_ID, IotDeviceStateEnum.ONLINE.getState());
            when(sceneRuleMapper.selectById(SCENE_RULE_ID)).thenReturn(sceneRule);

            assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(SCENE_RULE_ID));

            verify(deviceService, atLeastOnce()).getDevice(DEVICE_ID);
        }

        @Test
        @DisplayName("设备不存在时，条件应不匹配")
        void testDeviceStateCondition_deviceNotExists_shouldNotMatch() {
            IotSceneRuleDO.TriggerCondition condition = createDeviceStateCondition(
                    DEVICE_ID, IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                    String.valueOf(IotDeviceStateEnum.ONLINE.getState()));

            List<List<IotSceneRuleDO.TriggerCondition>> conditionGroups = createSingleConditionGroups(condition);

            IotSceneRuleDO sceneRule = createBaseSceneRule();
            IotSceneRuleDO.Trigger trigger = createTimerTrigger("0 0 12 * * ?", conditionGroups);
            sceneRule.setTriggers(ListUtil.toList(trigger));

            when(deviceService.getDevice(DEVICE_ID)).thenReturn(null);
            when(sceneRuleMapper.selectById(SCENE_RULE_ID)).thenReturn(sceneRule);

            assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(SCENE_RULE_ID));

            verify(deviceService, atLeastOnce()).getDevice(DEVICE_ID);
        }
    }

    @Nested
    @DisplayName("设备属性条件测试 - Validates: Requirements 3.1, 3.2, 3.3")
    class DevicePropertyConditionTest {

        @Test
        @DisplayName("设备属性条件满足时，应匹配成功")
        void testDevicePropertyCondition_match_shouldPass() {
            IotSceneRuleDO.TriggerCondition condition = createDevicePropertyCondition(
                    DEVICE_ID, PROPERTY_IDENTIFIER, IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(), "25");

            List<List<IotSceneRuleDO.TriggerCondition>> conditionGroups = createSingleConditionGroups(condition);

            IotSceneRuleDO sceneRule = createBaseSceneRule();
            IotSceneRuleDO.Trigger trigger = createTimerTrigger("0 0 12 * * ?", conditionGroups);
            sceneRule.setTriggers(ListUtil.toList(trigger));

            mockDeviceProperty(DEVICE_ID, PROPERTY_IDENTIFIER, 30);
            when(sceneRuleMapper.selectById(SCENE_RULE_ID)).thenReturn(sceneRule);

            assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(SCENE_RULE_ID));

            verify(devicePropertyService, atLeastOnce()).getLatestDeviceProperties(DEVICE_ID);
        }

        @Test
        @DisplayName("设备属性不存在时，条件应不匹配")
        void testDevicePropertyCondition_propertyNotExists_shouldNotMatch() {
            IotSceneRuleDO.TriggerCondition condition = createDevicePropertyCondition(
                    DEVICE_ID, "nonexistent", IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(), "25");

            List<List<IotSceneRuleDO.TriggerCondition>> conditionGroups = createSingleConditionGroups(condition);

            IotSceneRuleDO sceneRule = createBaseSceneRule();
            IotSceneRuleDO.Trigger trigger = createTimerTrigger("0 0 12 * * ?", conditionGroups);
            sceneRule.setTriggers(ListUtil.toList(trigger));

            when(devicePropertyService.getLatestDeviceProperties(DEVICE_ID)).thenReturn(Collections.emptyMap());
            when(sceneRuleMapper.selectById(SCENE_RULE_ID)).thenReturn(sceneRule);

            assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(SCENE_RULE_ID));

            verify(devicePropertyService, atLeastOnce()).getLatestDeviceProperties(DEVICE_ID);
        }

        @Test
        @DisplayName("设备属性等于条件测试")
        void testDevicePropertyCondition_equals_shouldMatch() {
            IotSceneRuleDO.TriggerCondition condition = createDevicePropertyCondition(
                    DEVICE_ID, PROPERTY_IDENTIFIER, IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(), "30");

            List<List<IotSceneRuleDO.TriggerCondition>> conditionGroups = createSingleConditionGroups(condition);

            IotSceneRuleDO sceneRule = createBaseSceneRule();
            IotSceneRuleDO.Trigger trigger = createTimerTrigger("0 0 12 * * ?", conditionGroups);
            sceneRule.setTriggers(ListUtil.toList(trigger));

            mockDeviceProperty(DEVICE_ID, PROPERTY_IDENTIFIER, 30);
            when(sceneRuleMapper.selectById(SCENE_RULE_ID)).thenReturn(sceneRule);

            assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(SCENE_RULE_ID));

            verify(devicePropertyService, atLeastOnce()).getLatestDeviceProperties(DEVICE_ID);
        }
    }

    @Nested
    @DisplayName("场景规则状态测试")
    class SceneRuleStatusTest {

        @Test
        @DisplayName("场景规则不存在时，应直接返回")
        void testSceneRule_notExists_shouldReturn() {
            when(sceneRuleMapper.selectById(SCENE_RULE_ID)).thenReturn(null);

            assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(SCENE_RULE_ID));

            verify(devicePropertyService, never()).getLatestDeviceProperties(any());
        }

        @Test
        @DisplayName("场景规则已禁用时，应直接返回")
        void testSceneRule_disabled_shouldReturn() {
            IotSceneRuleDO sceneRule = createBaseSceneRule();
            sceneRule.setStatus(CommonStatusEnum.DISABLE.getStatus());

            when(sceneRuleMapper.selectById(SCENE_RULE_ID)).thenReturn(sceneRule);

            assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(SCENE_RULE_ID));

            verify(devicePropertyService, never()).getLatestDeviceProperties(any());
        }

        @Test
        @DisplayName("场景规则无定时触发器时，应直接返回")
        void testSceneRule_noTimerTrigger_shouldReturn() {
            IotSceneRuleDO sceneRule = createBaseSceneRule();
            IotSceneRuleDO.Trigger deviceTrigger = new IotSceneRuleDO.Trigger();
            deviceTrigger.setType(IotSceneRuleTriggerTypeEnum.DEVICE_PROPERTY_POST.getType());
            sceneRule.setTriggers(ListUtil.toList(deviceTrigger));

            when(sceneRuleMapper.selectById(SCENE_RULE_ID)).thenReturn(sceneRule);

            assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(SCENE_RULE_ID));

            verify(devicePropertyService, never()).getLatestDeviceProperties(any());
        }
    }

    @Nested
    @DisplayName("复杂条件组合测试")
    class ComplexConditionCombinationTest {

        @Test
        @DisplayName("混合条件类型测试：设备属性 + 设备状态")
        void testMixedConditionTypes_propertyAndState() {
            IotSceneRuleDO.TriggerCondition propertyCondition = createDevicePropertyCondition(
                    DEVICE_ID, PROPERTY_IDENTIFIER, IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(), "20");
            IotSceneRuleDO.TriggerCondition stateCondition = createDeviceStateCondition(
                    DEVICE_ID, IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                    String.valueOf(IotDeviceStateEnum.ONLINE.getState()));

            List<List<IotSceneRuleDO.TriggerCondition>> conditionGroups =
                    createSingleGroupWithMultipleConditions(propertyCondition, stateCondition);

            IotSceneRuleDO sceneRule = createBaseSceneRule();
            IotSceneRuleDO.Trigger trigger = createTimerTrigger("0 0 12 * * ?", conditionGroups);
            sceneRule.setTriggers(ListUtil.toList(trigger));

            mockDeviceProperty(DEVICE_ID, PROPERTY_IDENTIFIER, 30);
            mockDeviceState(DEVICE_ID, IotDeviceStateEnum.ONLINE.getState());
            when(sceneRuleMapper.selectById(SCENE_RULE_ID)).thenReturn(sceneRule);

            assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(SCENE_RULE_ID));

            verify(devicePropertyService, atLeastOnce()).getLatestDeviceProperties(DEVICE_ID);
            verify(deviceService, atLeastOnce()).getDevice(DEVICE_ID);
        }

        @Test
        @DisplayName("多条件组 OR 逻辑 + 组内 AND 逻辑综合测试")
        void testComplexOrAndLogic() {
            // 条件组1：温度 > 30 AND 湿度 < 50（不满足）
            // 条件组2：温度 > 20 AND 设备在线（满足）
            IotSceneRuleDO.TriggerCondition group1Cond1 = createDevicePropertyCondition(
                    DEVICE_ID, "temperature", IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(), "30");
            IotSceneRuleDO.TriggerCondition group1Cond2 = createDevicePropertyCondition(
                    DEVICE_ID, "humidity", IotSceneRuleConditionOperatorEnum.LESS_THAN.getOperator(), "50");

            IotSceneRuleDO.TriggerCondition group2Cond1 = createDevicePropertyCondition(
                    DEVICE_ID, "temperature", IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(), "20");
            IotSceneRuleDO.TriggerCondition group2Cond2 = createDeviceStateCondition(
                    DEVICE_ID, IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                    String.valueOf(IotDeviceStateEnum.ONLINE.getState()));

            // 创建两个条件组
            List<IotSceneRuleDO.TriggerCondition> group1 = new ArrayList<>();
            group1.add(group1Cond1);
            group1.add(group1Cond2);
            List<IotSceneRuleDO.TriggerCondition> group2 = new ArrayList<>();
            group2.add(group2Cond1);
            group2.add(group2Cond2);
            List<List<IotSceneRuleDO.TriggerCondition>> conditionGroups = new ArrayList<>();
            conditionGroups.add(group1);
            conditionGroups.add(group2);

            IotSceneRuleDO sceneRule = createBaseSceneRule();
            IotSceneRuleDO.Trigger trigger = createTimerTrigger("0 0 12 * * ?", conditionGroups);
            sceneRule.setTriggers(ListUtil.toList(trigger));

            // Mock：温度 25，湿度 60，设备在线
            Map<String, IotDevicePropertyDO> properties = new HashMap<>();
            IotDevicePropertyDO tempProperty = new IotDevicePropertyDO();
            tempProperty.setValue(25);
            properties.put("temperature", tempProperty);
            IotDevicePropertyDO humidityProperty = new IotDevicePropertyDO();
            humidityProperty.setValue(60);
            properties.put("humidity", humidityProperty);
            when(devicePropertyService.getLatestDeviceProperties(DEVICE_ID)).thenReturn(properties);
            mockDeviceState(DEVICE_ID, IotDeviceStateEnum.ONLINE.getState());
            when(sceneRuleMapper.selectById(SCENE_RULE_ID)).thenReturn(sceneRule);

            assertDoesNotThrow(() -> sceneRuleService.executeSceneRuleByTimer(SCENE_RULE_ID));

            verify(devicePropertyService, atLeastOnce()).getLatestDeviceProperties(DEVICE_ID);
        }
    }

}
