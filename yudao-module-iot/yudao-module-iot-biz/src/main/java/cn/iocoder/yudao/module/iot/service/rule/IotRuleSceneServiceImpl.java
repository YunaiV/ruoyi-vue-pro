package cn.iocoder.yudao.module.iot.service.rule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.CharPool;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.common.util.spring.SpringExpressionUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotRuleSceneDO;
import cn.iocoder.yudao.module.iot.dal.mysql.rule.IotRuleSceneMapper;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageIdentifierEnum;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageTypeEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotRuleSceneTriggerConditionParameterOperatorEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotRuleSceneTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;

/**
 * IoT 规则场景 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotRuleSceneServiceImpl implements IotRuleSceneService {

    @Resource
    private IotRuleSceneMapper ruleSceneMapper;

    // TODO 芋艿，缓存待实现
    @Override
    @TenantIgnore // 忽略租户隔离：因为 IotRuleSceneMessageHandler 调用时，一般未传递租户，所以需要忽略
    public List<IotRuleSceneDO> getRuleSceneListByProductKeyAndDeviceNameFromCache(String productKey, String deviceName) {
        if (true) {
            IotRuleSceneDO ruleScene01 = new IotRuleSceneDO();
            ruleScene01.setTriggers(CollUtil.newArrayList());
            IotRuleSceneDO.Trigger trigger01 = new IotRuleSceneDO.Trigger();
            trigger01.setType(IotRuleSceneTriggerTypeEnum.DEVICE.getType());
            trigger01.setConditions(CollUtil.newArrayList());
            IotRuleSceneDO.TriggerCondition condition01 = new IotRuleSceneDO.TriggerCondition();
            condition01.setType(IotDeviceMessageTypeEnum.PROPERTY.getType());
            condition01.setIdentifier(IotDeviceMessageIdentifierEnum.PROPERTY_REPORT.getIdentifier());
            condition01.setParameters(CollUtil.newArrayList());
            IotRuleSceneDO.TriggerConditionParameter parameter011 = new IotRuleSceneDO.TriggerConditionParameter();
            parameter011.setIdentifier("width");
            parameter011.setOperator(IotRuleSceneTriggerConditionParameterOperatorEnum.EQUALS.getOperator());
            parameter011.setValue("1");
            condition01.getParameters().add(parameter011);
            IotRuleSceneDO.TriggerConditionParameter parameter012 = new IotRuleSceneDO.TriggerConditionParameter();
            parameter012.setIdentifier("width");
            parameter012.setOperator(IotRuleSceneTriggerConditionParameterOperatorEnum.NOT_EQUALS.getOperator());
            parameter012.setValue("2");
            condition01.getParameters().add(parameter012);
            IotRuleSceneDO.TriggerConditionParameter parameter013 = new IotRuleSceneDO.TriggerConditionParameter();
            parameter013.setIdentifier("width");
            parameter013.setOperator(IotRuleSceneTriggerConditionParameterOperatorEnum.GREATER_THAN.getOperator());
            parameter013.setValue("0");
            condition01.getParameters().add(parameter013);
            IotRuleSceneDO.TriggerConditionParameter parameter014 = new IotRuleSceneDO.TriggerConditionParameter();
            parameter014.setIdentifier("width");
            parameter014.setOperator(IotRuleSceneTriggerConditionParameterOperatorEnum.GREATER_THAN_OR_EQUALS.getOperator());
            parameter014.setValue("0");
            condition01.getParameters().add(parameter014);
            IotRuleSceneDO.TriggerConditionParameter parameter015 = new IotRuleSceneDO.TriggerConditionParameter();
            parameter015.setIdentifier("width");
            parameter015.setOperator(IotRuleSceneTriggerConditionParameterOperatorEnum.LESS_THAN.getOperator());
            parameter015.setValue("2");
            condition01.getParameters().add(parameter015);
            IotRuleSceneDO.TriggerConditionParameter parameter016 = new IotRuleSceneDO.TriggerConditionParameter();
            parameter016.setIdentifier("width");
            parameter016.setOperator(IotRuleSceneTriggerConditionParameterOperatorEnum.LESS_THAN_OR_EQUALS.getOperator());
            parameter016.setValue("2");
            condition01.getParameters().add(parameter016);
            IotRuleSceneDO.TriggerConditionParameter parameter017 = new IotRuleSceneDO.TriggerConditionParameter();
            parameter017.setIdentifier("width");
            parameter017.setOperator(IotRuleSceneTriggerConditionParameterOperatorEnum.IN.getOperator());
            parameter017.setValue("1,2,3");
            condition01.getParameters().add(parameter017);
            IotRuleSceneDO.TriggerConditionParameter parameter018 = new IotRuleSceneDO.TriggerConditionParameter();
            parameter018.setIdentifier("width");
            parameter018.setOperator(IotRuleSceneTriggerConditionParameterOperatorEnum.NOT_IN.getOperator());
            parameter018.setValue("0,2,3");
            condition01.getParameters().add(parameter018);
            IotRuleSceneDO.TriggerConditionParameter parameter019 = new IotRuleSceneDO.TriggerConditionParameter();
            parameter019.setIdentifier("width");
            parameter019.setOperator(IotRuleSceneTriggerConditionParameterOperatorEnum.BETWEEN.getOperator());
            parameter019.setValue("1,3");
            condition01.getParameters().add(parameter019);
            IotRuleSceneDO.TriggerConditionParameter parameter020 = new IotRuleSceneDO.TriggerConditionParameter();
            parameter020.setIdentifier("width");
            parameter020.setOperator(IotRuleSceneTriggerConditionParameterOperatorEnum.NOT_BETWEEN.getOperator());
            parameter020.setValue("2,3");
            condition01.getParameters().add(parameter020);
            trigger01.getConditions().add(condition01);
            ruleScene01.getTriggers().add(trigger01);

            return ListUtil.toList(ruleScene01);
        }

        List<IotRuleSceneDO> list = ruleSceneMapper.selectList();
        // TODO @芋艿：需要考虑开启状态
        return filterList(list, ruleScene -> {
            for (IotRuleSceneDO.Trigger trigger : ruleScene.getTriggers()) {
                if (ObjUtil.notEqual(trigger.getProductKey(), productKey)) {
                    continue;
                }
                if (CollUtil.isEmpty(trigger.getDeviceNames()) // 无设备名称限制
                        || trigger.getDeviceNames().contains(deviceName)) { // 包含设备名称
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public void executeRuleScene(IotDeviceMessage message) {
        // 1. 获得设备匹配的规则场景
        List<IotRuleSceneDO> ruleScenes = getMatchedRuleSceneList(message);
        if (CollUtil.isEmpty(ruleScenes)) {
            return;
        }
    }

    /**
     * 获得匹配的规则场景列表
     *
     * @param message 设备消息
     * @return 规则场景列表
     */
    @SuppressWarnings("unchecked")
    private List<IotRuleSceneDO> getMatchedRuleSceneList(IotDeviceMessage message) {
        // 1. 匹配设备
        // TODO @芋艿：可能需要 getSelf(); 缓存
        List<IotRuleSceneDO> ruleScenes = getRuleSceneListByProductKeyAndDeviceNameFromCache(
                message.getProductKey(), message.getDeviceName());
        if (CollUtil.isEmpty(ruleScenes)) {
            return ruleScenes;
        }

        // 2. 匹配 trigger 触发器的条件
        return filterList(ruleScenes, ruleScene -> {
            for (IotRuleSceneDO.Trigger trigger : ruleScene.getTriggers()) {
                // 非设备触发，不匹配
                if (ObjUtil.notEqual(trigger.getType(), IotRuleSceneTriggerTypeEnum.DEVICE.getType())) {
                    return false;
                }
                // TODO 芋艿：产品、设备的匹配，要不要这里在做一次？？？貌似和 1. 部分重复了
                // 条件为空，说明没有匹配的条件，因此不匹配
                if (CollUtil.isEmpty(trigger.getConditions())) {
                    return false;
                }
                IotRuleSceneDO.TriggerCondition found = CollUtil.findOne(trigger.getConditions(), condition -> {
                    if (ObjUtil.notEqual(message.getType(), condition.getType())
                            || ObjUtil.notEqual(message.getIdentifier(), condition.getIdentifier())) {
                        return false;
                    }
                    // TODO @芋艿：设备上线，需要测试下。
                    for (IotRuleSceneDO.TriggerConditionParameter parameter : condition.getParameters()) {
                        // 计算是否匹配
                        IotRuleSceneTriggerConditionParameterOperatorEnum operator =
                                IotRuleSceneTriggerConditionParameterOperatorEnum.operatorOf(parameter.getOperator());
                        if (operator == null) {
                            log.error("[getMatchedRuleSceneList][规则场景编号({}) 的触发器({}) 存在错误的操作符({})]",
                                    ruleScene.getId(), trigger, parameter.getOperator());
                            return false;
                        }
                        Object messageValue = ((Map<String, Object>) message.getData()).get(parameter.getIdentifier());
                        if (messageValue == null) {
                            return false;
                        }
                        String springExpression;
                        if (ObjectUtils.equalsAny(operator, IotRuleSceneTriggerConditionParameterOperatorEnum.BETWEEN,
                                IotRuleSceneTriggerConditionParameterOperatorEnum.NOT_BETWEEN)) {
                            String[] parameterValues = StrUtil.splitToArray(parameter.getValue(), CharPool.COMMA);
                            springExpression = String.format(operator.getSpringExpression(), messageValue, parameterValues[0],
                                    messageValue, parameterValues[1]);
                        } else {
                            springExpression = String.format(operator.getSpringExpression(), messageValue, parameter.getValue());
                        }
                        // TODO @芋艿：【需优化】需要考虑 struct、时间等参数的比较
                        try {
                            System.out.println(SpringExpressionUtils.parseExpression(springExpression));
                        } catch (Exception e) {
                            log.error("[getMatchedRuleSceneList][消息({}) 规则场景编号({}) 的触发器({}) 的匹配表达式({}) 计算异常]",
                                    message, ruleScene.getId(), trigger, springExpression, e);
                        }
                    }
                    return true;
                });
                if (found == null) {
                    return false;
                }
                log.info("[getMatchedRuleSceneList][消息({}) 匹配到规则场景编号({}) 的触发器({})]", message, ruleScene.getId(), trigger);
                return true;
            }
            return false;
        });
    }

}
