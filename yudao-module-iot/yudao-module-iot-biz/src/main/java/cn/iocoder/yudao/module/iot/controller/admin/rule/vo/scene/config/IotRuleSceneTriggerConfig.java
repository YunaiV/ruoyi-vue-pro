package cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.config;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotRuleSceneTriggerTypeEnum;
import lombok.Data;

import java.util.List;

// TODO @puhui999：这个要不内嵌到 IoTRuleSceneDO 里面？
/**
 * 触发器配置
 *
 * @author 芋道源码
 */
@Data
public class IotRuleSceneTriggerConfig {

    /**
     * 触发类型
     *
     * 枚举 {@link IotRuleSceneTriggerTypeEnum}
     */
    private Integer type;

    /**
     * 产品标识
     *
     * 关联 {@link IotProductDO#getProductKey()}
     */
    private String productKey;
    /**
     * 设备名称数组
     *
     * 关联 {@link IotDeviceDO#getDeviceName()}
     */
    private List<String> deviceNames;

    /**
     * 触发条件数组
     *
     * 必填：当 {@link #type} 为 {@link IotRuleSceneTriggerTypeEnum#DEVICE} 时
     * 条件与条件之间，是“或”的关系
     */
    private List<IotRuleSceneTriggerCondition> conditions;

    /**
     * CRON 表达式
     *
     * 必填：当 {@link #type} 为 {@link IotRuleSceneTriggerTypeEnum#TIMER} 时
     */
    private String cronExpression;

}