package cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.config;

import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotRuleSceneTriggerConditionParameterOperatorEnum;
import lombok.Data;

// TODO @puhui999：这个要不内嵌到 IoTRuleSceneDO 里面？
/**
 * 触发条件参数
 *
 * @author 芋道源码
 */
@Data
public class IotRuleSceneTriggerConditionParameter {

    /**
     * 标识符（属性、事件、服务）
     *
     * 关联 {@link IotThingModelDO#getIdentifier()}
     */
    private String identifier;

    /**
     * 操作符
     *
     * 枚举 {@link IotRuleSceneTriggerConditionParameterOperatorEnum}
     */
    private String operator;

    /**
     * 比较值
     *
     * 如果有多个值，则使用 "," 分隔，类似 "1,2,3"。
     * 例如说，{@link IotRuleSceneTriggerConditionParameterOperatorEnum#IN}、{@link IotRuleSceneTriggerConditionParameterOperatorEnum#BETWEEN}
     */
    private String value;

}