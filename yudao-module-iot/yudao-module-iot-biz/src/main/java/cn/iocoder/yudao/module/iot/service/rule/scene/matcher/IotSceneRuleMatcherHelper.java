package cn.iocoder.yudao.module.iot.service.rule.scene.matcher;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.spring.SpringExpressionUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionOperatorEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * IoT 场景规则匹配器工具类
 * <p>
 * 提供通用的条件评估逻辑和工具方法，供触发器和条件匹配器使用
 * <p>
 * 该类包含了匹配器实现中常用的工具方法，如条件评估、参数校验、日志记录等
 *
 * @author HUIHUI
 */
@Slf4j
public final class IotSceneRuleMatcherHelper {

    /**
     * 私有构造函数，防止实例化
     */
    private IotSceneRuleMatcherHelper() {
    }

    /**
     * 评估条件是否匹配
     *
     * @param sourceValue 源值（来自消息）
     * @param operator    操作符
     * @param paramValue  参数值（来自条件配置）
     * @return 是否匹配
     */
    @SuppressWarnings("DataFlowIssue")
    public static boolean evaluateCondition(Object sourceValue, String operator, String paramValue) {
        try {
            // 1. 校验操作符是否合法
            IotSceneRuleConditionOperatorEnum operatorEnum = IotSceneRuleConditionOperatorEnum.operatorOf(operator);
            if (operatorEnum == null) {
                log.warn("[evaluateCondition][存在错误的操作符({})]", operator);
                return false;
            }

            // 2. 构建 Spring 表达式变量
            Map<String, Object> springExpressionVariables = new HashMap<>();
            springExpressionVariables.put(IotSceneRuleConditionOperatorEnum.SPRING_EXPRESSION_SOURCE, sourceValue);

            // 处理参数值
            if (StrUtil.isNotBlank(paramValue)) {
                // 处理多值情况（如 IN、BETWEEN 操作符）
                // TODO @puhui999：使用这个，会不会有问题？例如说：string 恰好有 ， 分隔？
                if (paramValue.contains(",")) {
                    List<String> paramValues = StrUtil.split(paramValue, ',');
                    springExpressionVariables.put(IotSceneRuleConditionOperatorEnum.SPRING_EXPRESSION_VALUE_LIST,
                            convertList(paramValues, NumberUtil::parseDouble));
                } else {
                    // 处理单值情况
                    springExpressionVariables.put(IotSceneRuleConditionOperatorEnum.SPRING_EXPRESSION_VALUE,
                            NumberUtil.parseDouble(paramValue));
                }
            }

            // 3. 计算 Spring 表达式
            return (Boolean) SpringExpressionUtils.parseExpression(operatorEnum.getSpringExpression(), springExpressionVariables);
        } catch (Exception e) {
            log.error("[evaluateCondition][条件评估异常] sourceValue: {}, operator: {}, paramValue: {}",
                    sourceValue, operator, paramValue, e);
            return false;
        }
    }

    // ========== 【触发器】相关工具方法 ==========

    /**
     * 检查基础触发器参数是否有效
     *
     * @param trigger 触发器配置
     * @return 是否有效
     */
    public static boolean isBasicTriggerValid(IotSceneRuleDO.Trigger trigger) {
        return trigger != null && trigger.getType() != null;
    }

    /**
     * 检查触发器操作符和值是否有效
     *
     * @param trigger 触发器配置
     * @return 是否有效
     */
    public static boolean isTriggerOperatorAndValueValid(IotSceneRuleDO.Trigger trigger) {
        return StrUtil.isNotBlank(trigger.getOperator()) && StrUtil.isNotBlank(trigger.getValue());
    }

    /**
     * 记录触发器匹配成功日志
     *
     * @param matcherName 匹配器名称
     * @param message     设备消息
     * @param trigger     触发器配置
     */
    public static void logTriggerMatchSuccess(String matcherName, IotDeviceMessage message, IotSceneRuleDO.Trigger trigger) {
        log.debug("[{}][消息({}) 匹配触发器({}) 成功]", matcherName, message.getRequestId(), trigger.getType());
    }

    /**
     * 记录触发器匹配失败日志
     *
     * @param matcherName 匹配器名称
     * @param message     设备消息
     * @param trigger     触发器配置
     * @param reason      失败原因
     */
    public static void logTriggerMatchFailure(String matcherName, IotDeviceMessage message, IotSceneRuleDO.Trigger trigger, String reason) {
        log.debug("[{}][消息({}) 匹配触发器({}) 失败: {}]", matcherName, message.getRequestId(), trigger.getType(), reason);
    }

    // ========== 【条件】相关工具方法 ==========

    /**
     * 检查基础条件参数是否有效
     *
     * @param condition 触发条件
     * @return 是否有效
     */
    public static boolean isBasicConditionValid(IotSceneRuleDO.TriggerCondition condition) {
        return condition != null && condition.getType() != null;
    }

    /**
     * 检查条件操作符和参数是否有效
     *
     * @param condition 触发条件
     * @return 是否有效
     */
    public static boolean isConditionOperatorAndParamValid(IotSceneRuleDO.TriggerCondition condition) {
        return StrUtil.isNotBlank(condition.getOperator()) && StrUtil.isNotBlank(condition.getParam());
    }

    /**
     * 记录条件匹配成功日志
     *
     * @param matcherName 匹配器名称
     * @param message     设备消息
     * @param condition   触发条件
     */
    public static void logConditionMatchSuccess(String matcherName, IotDeviceMessage message, IotSceneRuleDO.TriggerCondition condition) {
        log.debug("[{}][消息({}) 匹配条件({}) 成功]", matcherName, message.getRequestId(), condition.getType());
    }

    /**
     * 记录条件匹配失败日志
     *
     * @param matcherName 匹配器名称
     * @param message     设备消息
     * @param condition   触发条件
     * @param reason      失败原因
     */
    public static void logConditionMatchFailure(String matcherName, IotDeviceMessage message, IotSceneRuleDO.TriggerCondition condition, String reason) {
        log.debug("[{}][消息({}) 匹配条件({}) 失败: {}]", matcherName, message.getRequestId(), condition.getType(), reason);
    }

    // ========== 【通用】工具方法 ==========

    /**
     * 检查标识符是否匹配
     *
     * @param expectedIdentifier 期望的标识符
     * @param actualIdentifier   实际的标识符
     * @return 是否匹配
     */
    public static boolean isIdentifierMatched(String expectedIdentifier, String actualIdentifier) {
        return StrUtil.isNotBlank(expectedIdentifier) && expectedIdentifier.equals(actualIdentifier);
    }

}
