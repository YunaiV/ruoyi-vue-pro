package cn.iocoder.yudao.module.iot.dal.dataobject.rule;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageIdentifierEnum;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageTypeEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotRuleSceneActionTypeEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotRuleSceneTriggerConditionParameterOperatorEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotRuleSceneTriggerTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * IoT 规则场景（场景联动） DO
 *
 * @author 芋道源码
 */
@TableName("iot_rule_scene")
@KeySequence("iot_rule_scene_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotRuleSceneDO extends TenantBaseDO {

    /**
     * 场景编号
     */
    @TableId
    private Long id;
    /**
     * 场景名称
     */
    private String name;
    /**
     * 场景描述
     */
    private String description;
    /**
     * 场景状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

    /**
     * 触发器数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<TriggerConfig> triggers;

    /**
     * 执行器数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ActionConfig> actions;

    /**
     * 触发器配置
     */
    @Data
    public static class TriggerConfig {

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
        private List<TriggerCondition> conditions;

        /**
         * CRON 表达式
         *
         * 必填：当 {@link #type} 为 {@link IotRuleSceneTriggerTypeEnum#TIMER} 时
         */
        private String cronExpression;

    }

    /**
     * 触发条件
     */
    @Data
    public static class TriggerCondition {

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
        private List<TriggerConditionParameter> parameters;

    }

    /**
     * 触发条件参数
     */
    @Data
    public static class TriggerConditionParameter {

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

    /**
     * 执行器配置
     */
    @Data
    public static class ActionConfig {

        /**
         * 执行类型
         *
         * 枚举 {@link IotRuleSceneActionTypeEnum}
         */
        private Integer type;

        /**
         * 设备控制
         *
         * 必填：当 {@link #type} 为 {@link IotRuleSceneActionTypeEnum#DEVICE_CONTROL} 时
         */
        private ActionDeviceControl deviceControl;

        /**
         * 数据桥接编号
         *
         * 必填：当 {@link #type} 为 {@link IotRuleSceneActionTypeEnum#DATA_BRIDGE} 时
         * 关联：{@link IotDataBridgeDO#getId()}
         */
        private Long dataBridgeId;

    }

    /**
     * 执行设备控制
     */
    @Data
    public static class ActionDeviceControl {

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
         * 消息类型
         *
         * 枚举 {@link IotDeviceMessageTypeEnum#PROPERTY}、{@link IotDeviceMessageTypeEnum#SERVICE}
         */
        private String type;
        /**
         * 消息标识符
         *
         * 枚举 {@link IotDeviceMessageIdentifierEnum}
         *
         * 1. 属性设置：对应 {@link IotDeviceMessageIdentifierEnum#PROPERTY_SET}
         * 2. 服务调用：对应 {@link IotDeviceMessageIdentifierEnum#SERVICE_INVOKE}
         */
        private String identifier;

        /**
         * 具体数据
         *
         * 1. 属性设置：在 {@link #type} 是 {@link IotDeviceMessageTypeEnum#PROPERTY} 时，对应 properties
         * 2. 服务调用：在 {@link #type} 是 {@link IotDeviceMessageTypeEnum#SERVICE} 时，对应 params
         */
        private Map<String, Object> data;

    }

}
