package cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.config;

import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageIdentifierEnum;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * 触发条件
 *
 * @author 芋道源码
 */
@Data
public class IotRuleSceneTriggerCondition {

    /**
     * 消息类型
     *
     * 枚举 {@link IotDeviceMessageTypeEnum}
     */
    private String type;
    /**
     * 消息标识符
     *
     * 枚举 {@link IotDeviceMessageIdentifierEnum}
     */
    private String identifier;

    /**
     * 参数数组
     *
     * 参数与参数之间，是“或”的关系
     */
    private List<IotRuleSceneTriggerConditionParameter> parameters;

}