package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.trigger;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotBaseConditionMatcherTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotDeviceServiceInvokeTriggerMatcher} 的单元测试
 *
 * @author HUIHUI
 */
public class IotDeviceServiceInvokeTriggerMatcherTest extends IotBaseConditionMatcherTest {

    private IotDeviceServiceInvokeTriggerMatcher matcher;

    @BeforeEach
    public void setUp() {
        matcher = new IotDeviceServiceInvokeTriggerMatcher();
    }

    @Test
    public void testGetSupportedTriggerType_success() {
        // 准备参数
        // 无需准备参数

        // 调用
        IotSceneRuleTriggerTypeEnum result = matcher.getSupportedTriggerType();

        // 断言
        assertEquals(IotSceneRuleTriggerTypeEnum.DEVICE_SERVICE_INVOKE, result);
    }

    @Test
    public void testGetPriority_success() {
        // 准备参数
        // 无需准备参数

        // 调用
        int result = matcher.getPriority();

        // 断言
        assertEquals(40, result);
    }

    @Test
    public void testIsEnabled_success() {
        // 准备参数
        // 无需准备参数

        // 调用
        boolean result = matcher.isEnabled();

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_serviceInvokeSuccess() {
        // 准备参数
        String serviceIdentifier = randomString();
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", serviceIdentifier)
                .put("inputData", MapUtil.builder(new HashMap<String, Object>())
                        .put("mode", randomString())
                        .build())
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(serviceIdentifier);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_configServiceSuccess() {
        // 准备参数
        String serviceIdentifier = randomString();
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", serviceIdentifier)
                .put("inputData", MapUtil.builder(new HashMap<String, Object>())
                        .put("interval", randomInt())
                        .put("enabled", randomBoolean())
                        .put("threshold", randomDouble())
                        .build())
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(serviceIdentifier);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_updateServiceSuccess() {
        // 准备参数
        String serviceIdentifier = randomString();
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", serviceIdentifier)
                .put("inputData", MapUtil.builder(new HashMap<String, Object>())
                        .put("version", randomString())
                        .put("url", randomString())
                        .build())
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(serviceIdentifier);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_serviceIdentifierMismatch() {
        // 准备参数
        String messageIdentifier = randomString();
        String triggerIdentifier = randomString();
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", messageIdentifier)
                .put("inputData", MapUtil.builder(new HashMap<String, Object>())
                        .put("mode", randomString())
                        .build())
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(triggerIdentifier); // 不匹配的服务标识符

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_wrongMessageMethod() {
        // 准备参数
        String serviceIdentifier = randomString();
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", serviceIdentifier)
                .put("inputData", MapUtil.builder(new HashMap<String, Object>())
                        .put("mode", randomString())
                        .build())
                .build();
        IotDeviceMessage message = new IotDeviceMessage();
        message.setDeviceId(randomLongId());
        message.setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod()); // 错误的方法
        message.setParams(serviceParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(serviceIdentifier);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullTriggerIdentifier() {
        // 准备参数
        String serviceIdentifier = randomString();
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", serviceIdentifier)
                .put("inputData", MapUtil.builder(new HashMap<String, Object>())
                        .put("mode", randomString())
                        .build())
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.DEVICE_SERVICE_INVOKE.getType());
        trigger.setIdentifier(null); // 缺少标识符

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullMessageParams() {
        // 准备参数
        String serviceIdentifier = randomString();
        IotDeviceMessage message = new IotDeviceMessage();
        message.setDeviceId(randomLongId());
        message.setMethod(IotDeviceMessageMethodEnum.SERVICE_INVOKE.getMethod());
        message.setParams(null);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(serviceIdentifier);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_invalidMessageParams() {
        // 准备参数
        String serviceIdentifier = randomString();
        IotDeviceMessage message = new IotDeviceMessage();
        message.setDeviceId(randomLongId());
        message.setMethod(IotDeviceMessageMethodEnum.SERVICE_INVOKE.getMethod());
        message.setParams(randomString()); // 不是 Map 类型
        IotSceneRuleDO.Trigger trigger = createValidTrigger(serviceIdentifier);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_missingServiceIdentifierInParams() {
        // 准备参数
        String serviceIdentifier = randomString();
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("inputData", MapUtil.builder(new HashMap<String, Object>())
                        .put("mode", randomString())
                        .build()) // 缺少 identifier 字段
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(serviceIdentifier);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullTrigger() {
        // 准备参数
        String serviceIdentifier = randomString();
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", serviceIdentifier)
                .put("inputData", MapUtil.builder(new HashMap<String, Object>())
                        .put("mode", randomString())
                        .build())
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);

        // 调用
        boolean result = matcher.matches(message, null);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullTriggerType() {
        // 准备参数
        String serviceIdentifier = randomString();
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", serviceIdentifier)
                .put("inputData", MapUtil.builder(new HashMap<String, Object>())
                        .put("mode", randomString())
                        .build())
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(null);
        trigger.setIdentifier(serviceIdentifier);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_emptyInputDataSuccess() {
        // 准备参数
        String serviceIdentifier = randomString();
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", serviceIdentifier)
                .put("inputData", MapUtil.ofEntries()) // 空的输入数据
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(serviceIdentifier);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_noInputDataSuccess() {
        // 准备参数
        String serviceIdentifier = randomString();
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", serviceIdentifier)
                // 没有 inputData 字段
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(serviceIdentifier);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_complexInputDataSuccess() {
        // 准备参数
        String serviceIdentifier = randomString();
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", serviceIdentifier)
                .put("inputData", MapUtil.builder(new HashMap<String, Object>())
                        .put("sensors", new String[]{randomString(), randomString(), randomString()})
                        .put("precision", randomDouble())
                        .put("duration", randomInt())
                        .put("autoSave", randomBoolean())
                        .put("config", MapUtil.builder(new HashMap<String, Object>())
                                .put("mode", randomString())
                                .put("level", randomString())
                                .build())
                        .build())
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(serviceIdentifier);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_caseSensitiveIdentifierMismatch() {
        // 准备参数
        String serviceIdentifier = randomString().toUpperCase(); // 大写
        String triggerIdentifier = serviceIdentifier.toLowerCase(); // 小写
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", serviceIdentifier)
                .put("inputData", MapUtil.builder(new HashMap<String, Object>())
                        .put("mode", randomString())
                        .build())
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(triggerIdentifier);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        // 根据实际实现，这里可能需要调整期望结果
        // 如果实现是大小写敏感的，则应该为 false
        assertFalse(result);
    }

    // ========== 辅助方法 ==========

    /**
     * 创建服务调用消息
     */
    private IotDeviceMessage createServiceInvokeMessage(Map<String, Object> serviceParams) {
        IotDeviceMessage message = new IotDeviceMessage();
        message.setDeviceId(randomLongId());
        message.setMethod(IotDeviceMessageMethodEnum.SERVICE_INVOKE.getMethod());
        message.setParams(serviceParams);
        return message;
    }

    /**
     * 创建有效的触发器
     */
    private IotSceneRuleDO.Trigger createValidTrigger(String identifier) {
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.DEVICE_SERVICE_INVOKE.getType());
        trigger.setIdentifier(identifier);
        return trigger;
    }

}
