package cn.iocoder.yudao.module.iot.service.rule.scene.matcher;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IoT 场景规则触发器匹配器测试类
 *
 * @author HUIHUI
 */
public class IotSceneRuleTriggerMatcherTest extends BaseMockitoUnitTest {

    private IotSceneRuleMatcherManager matcherManager;

    @BeforeEach
    void setUp() {
        // 创建所有匹配器实例
        List<IotSceneRuleMatcher> matchers = Arrays.asList(
                new DeviceStateUpdateTriggerMatcher(),
                new DevicePropertyPostTriggerMatcher(),
                new DeviceEventPostTriggerMatcher(),
                new DeviceServiceInvokeTriggerMatcher(),
                new TimerTriggerMatcher()
        );

        // 初始化匹配器管理器
        matcherManager = new IotSceneRuleMatcherManager(matchers);
    }

    @Test
    void testDeviceStateUpdateTriggerMatcher() {
        // 1. 准备测试数据
        IotDeviceMessage message = IotDeviceMessage.builder()
                .requestId("test-001")
                .method("thing.state.update")
                .data(1) // 在线状态
                .build();

        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.DEVICE_STATE_UPDATE.getType());
        trigger.setOperator("=");
        trigger.setValue("1");

        // 2. 执行测试
        boolean matched = matcherManager.isMatched(message, trigger);

        // 3. 验证结果
        assertTrue(matched, "设备状态更新触发器应该匹配");
    }

    @Test
    void testDevicePropertyPostTriggerMatcher() {
        // 1. 准备测试数据
        HashMap<String, Object> params = new HashMap<>();
        IotDeviceMessage message = IotDeviceMessage.builder()
                .requestId("test-002")
                .method("thing.property.post")
                .data(25.5) // 温度值
                .params(params)
                .build();
        // 模拟标识符
        params.put("identifier", "temperature");

        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.DEVICE_PROPERTY_POST.getType());
        trigger.setIdentifier("temperature");
        trigger.setOperator(">");
        trigger.setValue("20");

        // 2. 执行测试
        boolean matched = matcherManager.isMatched(message, trigger);

        // 3. 验证结果
        assertTrue(matched, "设备属性上报触发器应该匹配");
    }

    @Test
    void testDeviceEventPostTriggerMatcher() {
        // 1. 准备测试数据
        HashMap<String, Object> params = new HashMap<>();
        IotDeviceMessage message = IotDeviceMessage.builder()
                .requestId("test-003")
                .method("thing.event.post")
                .data("alarm_data")
                .params(params)
                .build();
        // 模拟标识符
        params.put("identifier", "high_temperature_alarm");

        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.DEVICE_EVENT_POST.getType());
        trigger.setIdentifier("high_temperature_alarm");

        // 2. 执行测试
        boolean matched = matcherManager.isMatched(message, trigger);

        // 3. 验证结果
        assertTrue(matched, "设备事件上报触发器应该匹配");
    }

    @Test
    void testDeviceServiceInvokeTriggerMatcher() {
        // 1. 准备测试数据
        HashMap<String, Object> params = new HashMap<>();
        IotDeviceMessage message = IotDeviceMessage.builder()
                .requestId("test-004")
                .method("thing.service.invoke")
                .msg("alarm_data")
                .params(params)
                .build();
        // 模拟标识符
        params.put("identifier", "restart_device");

        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.DEVICE_SERVICE_INVOKE.getType());
        trigger.setIdentifier("restart_device");

        // 2. 执行测试
        boolean matched = matcherManager.isMatched(message, trigger);

        // 3. 验证结果
        assertTrue(matched, "设备服务调用触发器应该匹配");
    }

    @Test
    void testTimerTriggerMatcher() {
        // 1. 准备测试数据
        IotDeviceMessage message = IotDeviceMessage.builder()
                .requestId("test-005")
                .method("timer.trigger") // 定时触发器不依赖具体消息方法
                .build();

        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.TIMER.getType());
        trigger.setCronExpression("0 0 12 * * ?"); // 每天中午12点

        // 2. 执行测试
        boolean matched = matcherManager.isMatched(message, trigger);

        // 3. 验证结果
        assertTrue(matched, "定时触发器应该匹配");
    }

    @Test
    void testInvalidTriggerType() {
        // 1. 准备测试数据
        IotDeviceMessage message = IotDeviceMessage.builder()
                .requestId("test-006")
                .method("unknown.method")
                .build();

        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(999); // 无效的触发器类型

        // 2. 执行测试
        boolean matched = matcherManager.isMatched(message, trigger);

        // 3. 验证结果
        assertFalse(matched, "无效的触发器类型应该不匹配");
    }

    @Test
    void testMatcherManagerStatistics() {
        // 1. 执行测试
        var statistics = matcherManager.getMatcherStatistics();

        // 2. 验证结果
        assertNotNull(statistics);
        assertEquals(5, statistics.get("totalMatchers"));
        assertEquals(5, statistics.get("enabledMatchers"));
        assertNotNull(statistics.get("supportedTriggerTypes"));
        assertNotNull(statistics.get("matcherDetails"));
    }

    @Test
    void testGetSupportedTriggerTypes() {
        // 1. 执行测试
        var supportedTypes = matcherManager.getSupportedTriggerTypes();

        // 2. 验证结果
        assertNotNull(supportedTypes);
        assertEquals(5, supportedTypes.size());
        assertTrue(supportedTypes.contains(IotSceneRuleTriggerTypeEnum.DEVICE_STATE_UPDATE));
        assertTrue(supportedTypes.contains(IotSceneRuleTriggerTypeEnum.DEVICE_PROPERTY_POST));
        assertTrue(supportedTypes.contains(IotSceneRuleTriggerTypeEnum.DEVICE_EVENT_POST));
        assertTrue(supportedTypes.contains(IotSceneRuleTriggerTypeEnum.DEVICE_SERVICE_INVOKE));
        assertTrue(supportedTypes.contains(IotSceneRuleTriggerTypeEnum.TIMER));
    }

}
