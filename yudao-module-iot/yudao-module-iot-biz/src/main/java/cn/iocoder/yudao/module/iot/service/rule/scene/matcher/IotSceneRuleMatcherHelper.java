package cn.iocoder.yudao.module.iot.service.rule.scene.matcher;

import cn.hutool.core.text.CharPool;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
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
 * IoT 场景规则匹配器工具类：提供通用的条件评估逻辑和工具方法，供触发器和条件匹配器使用
 *
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
    public static boolean evaluateCondition(Object sourceValue, String operator, String paramValue) {
        try {
            // 1. 校验操作符是否合法
            IotSceneRuleConditionOperatorEnum operatorEnum = IotSceneRuleConditionOperatorEnum.operatorOf(operator);
            if (operatorEnum == null) {
                log.warn("[evaluateCondition][operator({}) 操作符无效]", operator);
                return false;
            }

            // 2. 构建 Spring 表达式变量
            return evaluateConditionWithOperatorEnum(sourceValue, operatorEnum, paramValue);
        } catch (Exception e) {
            log.error("[evaluateCondition][sourceValue({}) operator({}) paramValue({}) 条件评估异常]",
                    sourceValue, operator, paramValue, e);
            return false;
        }
    }

    /**
     * 使用操作符枚举评估条件是否匹配
     *
     * @param sourceValue  源值（来自消息）
     * @param operatorEnum 操作符枚举
     * @param paramValue   参数值（来自条件配置）
     * @return 是否匹配
     */
    @SuppressWarnings("DataFlowIssue")
    public static boolean evaluateConditionWithOperatorEnum(Object sourceValue, IotSceneRuleConditionOperatorEnum operatorEnum, String paramValue) {
        try {
            // 1. 构建 Spring 表达式变量
            Map<String, Object> springExpressionVariables = buildSpringExpressionVariables(sourceValue, operatorEnum, paramValue);

            // 2. 计算 Spring 表达式
            return (Boolean) SpringExpressionUtils.parseExpression(operatorEnum.getSpringExpression(), springExpressionVariables);
        } catch (Exception e) {
            log.error("[evaluateConditionWithOperatorEnum][sourceValue({}) operatorEnum({}) paramValue({}) 条件评估异常]",
                    sourceValue, operatorEnum, paramValue, e);
            return false;
        }
    }

    /**
     * 构建 Spring 表达式变量
     */
    private static Map<String, Object> buildSpringExpressionVariables(Object sourceValue, IotSceneRuleConditionOperatorEnum operatorEnum, String paramValue) {
        Map<String, Object> springExpressionVariables = new HashMap<>();

        // 设置源值
        springExpressionVariables.put(IotSceneRuleConditionOperatorEnum.SPRING_EXPRESSION_SOURCE, StrUtil.toString(sourceValue));

        // 处理参数值
        if (StrUtil.isNotBlank(paramValue)) {
            List<String> parameterValues = StrUtil.splitTrim(paramValue, CharPool.COMMA);

            // 设置原始参数值
            springExpressionVariables.put(IotSceneRuleConditionOperatorEnum.SPRING_EXPRESSION_VALUE, paramValue);
            springExpressionVariables.put(IotSceneRuleConditionOperatorEnum.SPRING_EXPRESSION_VALUE_LIST, parameterValues);

            // 特殊处理：解决数字比较问题
            // Spring 表达式基于 compareTo 方法，对数字的比较存在问题，需要转换为数字类型
            if (isNumericComparisonOperator(operatorEnum) && isNumericComparison(sourceValue, parameterValues)) {
                springExpressionVariables.put(IotSceneRuleConditionOperatorEnum.SPRING_EXPRESSION_SOURCE,
                        NumberUtil.parseDouble(String.valueOf(sourceValue)));
                springExpressionVariables.put(IotSceneRuleConditionOperatorEnum.SPRING_EXPRESSION_VALUE,
                        NumberUtil.parseDouble(paramValue));
                springExpressionVariables.put(IotSceneRuleConditionOperatorEnum.SPRING_EXPRESSION_VALUE_LIST,
                        convertList(parameterValues, NumberUtil::parseDouble));
            }
        }

        return springExpressionVariables;
    }

    /**
     * 判断是否为数字比较操作符
     */
    private static boolean isNumericComparisonOperator(IotSceneRuleConditionOperatorEnum operatorEnum) {
        return ObjectUtils.equalsAny(operatorEnum,
                IotSceneRuleConditionOperatorEnum.BETWEEN,
                IotSceneRuleConditionOperatorEnum.NOT_BETWEEN,
                IotSceneRuleConditionOperatorEnum.GREATER_THAN,
                IotSceneRuleConditionOperatorEnum.GREATER_THAN_OR_EQUALS,
                IotSceneRuleConditionOperatorEnum.LESS_THAN,
                IotSceneRuleConditionOperatorEnum.LESS_THAN_OR_EQUALS);
    }

    /**
     * 判断是否为数字比较场景
     */
    private static boolean isNumericComparison(Object sourceValue, List<String> parameterValues) {
        return NumberUtil.isNumber(String.valueOf(sourceValue)) && NumberUtils.isAllNumber(parameterValues);
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
     * @param message     设备消息
     * @param trigger     触发器配置
     */
    public static void logTriggerMatchSuccess(IotDeviceMessage message, IotSceneRuleDO.Trigger trigger) {
        log.debug("[isMatched][message({}) trigger({}) 匹配触发器成功]", message.getRequestId(), trigger.getType());
    }

    /**
     * 记录触发器匹配失败日志
     *
     * @param message     设备消息
     * @param trigger     触发器配置
     * @param reason      失败原因
     */
    public static void logTriggerMatchFailure(IotDeviceMessage message, IotSceneRuleDO.Trigger trigger, String reason) {
        log.debug("[isMatched][message({}) trigger({}) reason({}) 匹配触发器失败]", message.getRequestId(), trigger.getType(), reason);
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
     * @param message     设备消息
     * @param condition   触发条件
     */
    public static void logConditionMatchSuccess(IotDeviceMessage message, IotSceneRuleDO.TriggerCondition condition) {
        log.debug("[isMatched][message({}) condition({}) 匹配条件成功]", message.getRequestId(), condition.getType());
    }

    /**
     * 记录条件匹配失败日志
     *
     * @param message     设备消息
     * @param condition   触发条件
     * @param reason      失败原因
     */
    public static void logConditionMatchFailure(IotDeviceMessage message, IotSceneRuleDO.TriggerCondition condition, String reason) {
        log.debug("[isMatched][message({}) condition({}) reason({}) 匹配条件失败]", message.getRequestId(), condition.getType(), reason);
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
