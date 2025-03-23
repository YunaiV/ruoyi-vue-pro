package cn.iocoder.yudao.module.iot.dal.dataobject.rule;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.config.IotRuleSceneActionConfig;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.config.IotRuleSceneTriggerConfig;
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
    private List<IotRuleSceneTriggerConfig> triggers;

    /**
     * 执行器数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<IotRuleSceneActionConfig> actions;

}
