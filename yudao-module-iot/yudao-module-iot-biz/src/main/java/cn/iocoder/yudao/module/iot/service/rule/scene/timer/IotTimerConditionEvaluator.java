package cn.iocoder.yudao.module.iot.service.rule.scene.timer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionOperatorEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import cn.iocoder.yudao.module.iot.service.rule.scene.IotSceneRuleTimeHelper;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotSceneRuleMatcherHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * IoT 定时触发器条件评估器
 * <p>
 * 与设备触发器不同，定时触发器没有设备消息上下文，
 * 需要主动查询设备属性和状态来评估条件。
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class IotTimerConditionEvaluator {

    @Resource
    private IotDevicePropertyService devicePropertyService;

    @Resource
    private IotDeviceService deviceService;

    /**
     * 评估条件
     *
     * @param condition 条件配置
     * @return 是否满足条件
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    public boolean evaluate(IotSceneRuleDO.TriggerCondition condition) {
        // 1.1 基础参数校验
        if (condition == null || condition.getType() == null) {
            log.warn("[evaluate][条件为空或类型为空]");
            return false;
        }
        // 1.2 根据条件类型分发到具体的评估方法
        IotSceneRuleConditionTypeEnum conditionType =
                IotSceneRuleConditionTypeEnum.typeOf(condition.getType());
        if (conditionType == null) {
            log.warn("[evaluate][未知的条件类型: {}]", condition.getType());
            return false;
        }

        // 2. 分发评估
        switch (conditionType) {
            case DEVICE_PROPERTY:
                return evaluateDevicePropertyCondition(condition);
            case DEVICE_STATE:
                return evaluateDeviceStateCondition(condition);
            case CURRENT_TIME:
                return evaluateCurrentTimeCondition(condition);
            default:
                log.warn("[evaluate][未知的条件类型: {}]", conditionType);
                return false;
        }
    }

    /**
     * 评估设备属性条件
     *
     * @param condition 条件配置
     * @return 是否满足条件
     */
    private boolean evaluateDevicePropertyCondition(IotSceneRuleDO.TriggerCondition condition) {
        // 1. 校验必要参数
        if (condition.getDeviceId() == null) {
            log.debug("[evaluateDevicePropertyCondition][设备ID为空]");
            return false;
        }
        if (StrUtil.isBlank(condition.getIdentifier())) {
            log.debug("[evaluateDevicePropertyCondition][属性标识符为空]");
            return false;
        }
        if (!IotSceneRuleMatcherHelper.isConditionOperatorAndParamValid(condition)) {
            log.debug("[evaluateDevicePropertyCondition][操作符或参数无效]");
            return false;
        }

        // 2.1 获取设备最新属性值
        Map<String, IotDevicePropertyDO> properties =
                devicePropertyService.getLatestDeviceProperties(condition.getDeviceId());
        if (CollUtil.isEmpty(properties)) {
            log.debug("[evaluateDevicePropertyCondition][设备({}) 无属性数据]", condition.getDeviceId());
            return false;
        }
        // 2.2 获取指定属性
        IotDevicePropertyDO property = properties.get(condition.getIdentifier());
        if (property == null || property.getValue() == null) {
            log.debug("[evaluateDevicePropertyCondition][设备({}) 属性({}) 不存在或值为空]",
                    condition.getDeviceId(), condition.getIdentifier());
            return false;
        }

        // 3. 使用现有的条件评估逻辑进行比较
        boolean matched = IotSceneRuleMatcherHelper.evaluateCondition(
                property.getValue(), condition.getOperator(), condition.getParam());
        log.debug("[evaluateDevicePropertyCondition][设备({}) 属性({}) 值({}) 操作符({}) 参数({}) 匹配结果: {}]",
                condition.getDeviceId(), condition.getIdentifier(), property.getValue(),
                condition.getOperator(), condition.getParam(), matched);
        return matched;
    }

    /**
     * 评估设备状态条件
     *
     * @param condition 条件配置
     * @return 是否满足条件
     */
    private boolean evaluateDeviceStateCondition(IotSceneRuleDO.TriggerCondition condition) {
        // 1. 校验必要参数
        if (condition.getDeviceId() == null) {
            log.debug("[evaluateDeviceStateCondition][设备ID为空]");
            return false;
        }
        if (!IotSceneRuleMatcherHelper.isConditionOperatorAndParamValid(condition)) {
            log.debug("[evaluateDeviceStateCondition][操作符或参数无效]");
            return false;
        }

        // 2.1 获取设备信息
        IotDeviceDO device = deviceService.getDevice(condition.getDeviceId());
        if (device == null) {
            log.debug("[evaluateDeviceStateCondition][设备({}) 不存在]", condition.getDeviceId());
            return false;
        }
        // 2.2 获取设备状态
        Integer state = device.getState();
        if (state == null) {
            log.debug("[evaluateDeviceStateCondition][设备({}) 状态为空]", condition.getDeviceId());
            return false;
        }

        // 3. 比较状态
        boolean matched = IotSceneRuleMatcherHelper.evaluateCondition(
                state.toString(), condition.getOperator(), condition.getParam());
        log.debug("[evaluateDeviceStateCondition][设备({}) 状态({}) 操作符({}) 参数({}) 匹配结果: {}]",
                condition.getDeviceId(), state, condition.getOperator(), condition.getParam(), matched);
        return matched;
    }

    /**
     * 评估当前时间条件
     *
     * @param condition 条件配置
     * @return 是否满足条件
     */
    private boolean evaluateCurrentTimeCondition(IotSceneRuleDO.TriggerCondition condition) {
        // 1.1 校验必要参数
        if (!IotSceneRuleMatcherHelper.isConditionOperatorAndParamValid(condition)) {
            log.debug("[evaluateCurrentTimeCondition][操作符或参数无效]");
            return false;
        }
        // 1.2 验证操作符是否为支持的时间操作符
        IotSceneRuleConditionOperatorEnum operatorEnum =
                IotSceneRuleConditionOperatorEnum.operatorOf(condition.getOperator());
        if (operatorEnum == null) {
            log.debug("[evaluateCurrentTimeCondition][无效的操作符: {}]", condition.getOperator());
            return false;
        }
        if (IotSceneRuleTimeHelper.isTimeOperator(operatorEnum)) {
            log.debug("[evaluateCurrentTimeCondition][不支持的时间操作符: {}]", condition.getOperator());
            return false;
        }

        // 2. 执行时间匹配
        boolean matched = IotSceneRuleTimeHelper.executeTimeMatching(operatorEnum, condition.getParam());
        log.debug("[evaluateCurrentTimeCondition][操作符({}) 参数({}) 匹配结果: {}]",
                condition.getOperator(), condition.getParam(), matched);
        return matched;
    }

}
