package cn.iocoder.yudao.module.iot.dal.dataobject.rule;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.alert.IotAlertConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleActionTypeEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionOperatorEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionTypeEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * IoT 场景联动规则 DO
 *
 * @author 芋道源码
 */
@TableName(value = "iot_scene_rule", autoResultMap = true)
@KeySequence("iot_scene_rule_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotSceneRuleDO extends TenantBaseDO {

    /**
     * 场景联动编号
     */
    @TableId
    private Long id;
    /**
     * 场景联动名称
     */
    private String name;
    /**
     * 场景联动描述
     */
    private String description;
    /**
     * 场景联动状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    /**
     * 场景定义配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Trigger> triggers;

    /**
     * 场景动作配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Action> actions;

    /**
     * 场景定义配置
     */
    @Data
    public static class Trigger {

        // ========== 事件部分 ==========

        /**
         * 场景事件类型
         *
         * 枚举 {@link IotSceneRuleTriggerTypeEnum}
         * 1. {@link IotSceneRuleTriggerTypeEnum#DEVICE_STATE_UPDATE} 时，operator 非空，并且 value 为在线状态
         * 2. {@link IotSceneRuleTriggerTypeEnum#DEVICE_PROPERTY_POST}
         *    {@link IotSceneRuleTriggerTypeEnum#DEVICE_EVENT_POST} 时，identifier、operator 非空，并且 value 为属性值
         * 3. {@link IotSceneRuleTriggerTypeEnum#DEVICE_EVENT_POST}
         *    {@link IotSceneRuleTriggerTypeEnum#DEVICE_SERVICE_INVOKE} 时，identifier 非空，但是 operator、value 为空
         * 4. {@link IotSceneRuleTriggerTypeEnum#TIMER} 时，conditions 非空，并且设备无关（无需 productId、deviceId 字段）
         */
        private Integer type;

        /**
         * 产品编号
         *
         * 关联 {@link IotProductDO#getId()}
         */
        private Long productId;
        /**
         * 设备编号
         *
         * 关联 {@link IotDeviceDO#getId()}
         * 特殊：如果为 {@link IotDeviceDO#DEVICE_ID_ALL} 时，则是全部设备
         */
        private Long deviceId;
        /**
         * 物模型标识符
         *
         * 对应：{@link IotThingModelDO#getIdentifier()}
         */
        private String identifier;
        /**
         * 操作符
         *
         * 枚举 {@link IotSceneRuleConditionOperatorEnum}
         */
        private String operator;
        /**
         * 参数（属性值、在线状态）
         * <p>
         * 如果有多个值，则使用 "," 分隔，类似 "1,2,3"。
         * 例如说，{@link IotSceneRuleConditionOperatorEnum#IN}、{@link IotSceneRuleConditionOperatorEnum#BETWEEN}
         */
        private String value;

        /**
         * CRON 表达式
         */
        private String cronExpression;

        // ========== 条件部分 ==========

        /**
         * 触发条件分组（状态条件分组）的数组
         * <p>
         * 第一层 List：分组与分组之间，是“或”的关系
         * 第二层 List：条件与条件之间，是“且”的关系
         */
        private List<List<TriggerCondition>> conditionGroups;

    }

    /**
     * 触发条件（状态条件）
     */
    @Data
    public static class TriggerCondition {

        /**
         * 触发条件类型
         *
         * 枚举 {@link IotSceneRuleConditionTypeEnum}
         * 1. {@link IotSceneRuleConditionTypeEnum#DEVICE_STATE} 时，operator 非空，并且 value 为在线状态
         * 2. {@link IotSceneRuleConditionTypeEnum#DEVICE_PROPERTY} 时，identifier、operator 非空，并且 value 为属性值
         * 3. {@link IotSceneRuleConditionTypeEnum#CURRENT_TIME} 时，operator 非空（使用 DATE_TIME_ 和 TIME_ 部分），并且 value 非空
         */
        private Integer type;

        /**
         * 产品编号
         *
         * 关联 {@link IotProductDO#getId()}
         */
        private Long productId;
        /**
         * 设备编号
         *
         * 关联 {@link IotDeviceDO#getId()}
         */
        private Long deviceId;
        /**
         * 标识符（属性）
         *
         * 关联 {@link IotThingModelDO#getIdentifier()}
         */
        private String identifier;
        /**
         * 操作符
         *
         * 枚举 {@link IotSceneRuleConditionOperatorEnum}
         */
        private String operator;
        /**
         * 参数
         *
         * 如果有多个值，则使用 "," 分隔，类似 "1,2,3"。
         * 例如说，{@link IotSceneRuleConditionOperatorEnum#IN}、{@link IotSceneRuleConditionOperatorEnum#BETWEEN}
         */
        private String param;

    }

    /**
     * 场景动作配置
     */
    @Data
    public static class Action {

        /**
         * 执行类型
         *
         * 枚举 {@link IotSceneRuleActionTypeEnum}
         * 1. {@link IotSceneRuleActionTypeEnum#DEVICE_PROPERTY_SET} 时，params 非空
         *    {@link IotSceneRuleActionTypeEnum#DEVICE_SERVICE_INVOKE} 时，params 非空
         * 2. {@link IotSceneRuleActionTypeEnum#ALERT_TRIGGER} 时，alertConfigId 为空，因为是 {@link IotAlertConfigDO} 里面关联它
         * 3. {@link IotSceneRuleActionTypeEnum#ALERT_RECOVER} 时，alertConfigId 非空
         */
        private Integer type;

        /**
         * 产品编号
         *
         * 关联 {@link IotProductDO#getId()}
         */
        private Long productId;
        /**
         * 设备编号
         *
         * 关联 {@link IotDeviceDO#getId()}
         */
        private Long deviceId;

        /**
         * 标识符（服务）
         * <p>
         * 关联 {@link IotThingModelDO#getIdentifier()}
         */
        private String identifier;

        /**
         * 请求参数
         *
         * 一般来说，对应 {@link IotDeviceMessage#getParams()} 请求参数
         */
        private String params;

        /**
         * 告警配置编号
         *
         * 关联 {@link IotAlertConfigDO#getId()}
         */
        private Long alertConfigId;

    }

}
