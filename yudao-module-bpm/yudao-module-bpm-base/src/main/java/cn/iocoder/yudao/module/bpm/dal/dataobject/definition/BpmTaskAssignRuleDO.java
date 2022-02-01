package cn.iocoder.yudao.module.bpm.dal.dataobject.definition;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.JsonLongSetTypeHandler;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTaskAssignRuleTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTaskRuleScriptEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.util.Set;

/**
 * Bpm 任务分配的规则表，用于自定义配置每个任务的负责人、候选人的分配规则。
 * 也就是说，废弃 BPMN 原本的 UserTask 设置的 assignee、candidateUsers 等配置，而是通过使用该规则进行计算对应的负责人。
 *
 * 1. 默认情况下，{@link #processDefinitionId} 为 {@link #PROCESS_DEFINITION_ID_NULL} 值，表示贵改则与流程模型关联
 * 2. 在流程模型部署后，会将他的所有规则记录，复制出一份新部署出来的流程定义，通过设置 {@link #processDefinitionId} 为新的流程定义的编号进行关联
 *
 * @author 芋道源码
 */
@TableName(value = "bpm_task_assign_rule", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmTaskAssignRuleDO extends BaseDO {

    /**
     * {@link #processDefinitionId} 空串，用于标识属于流程模型，而不属于流程定义
     */
    public static final String PROCESS_DEFINITION_ID_NULL = "";

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 流程模型编号
     *
     * 关联 Model 的 id 属性
     */
    private String modelId;
    /**
     * 流程定义编号
     *
     * 关联 ProcessDefinition 的 id 属性
     */
    private String processDefinitionId;
    /**
     * 流程任务的定义 Key
     *
     * 关联 Task 的 taskDefinitionKey 属性
     */
    private String taskDefinitionKey;

    /**
     * 规则类型
     *
     * 枚举 {@link BpmTaskAssignRuleTypeEnum}
     */
    @TableField("`type`")
    private Integer type;
    /**
     * 规则值数组，一般关联指定表的编号
     * 根据 type 不同，对应的值是不同的：
     *
     * 1. {@link BpmTaskAssignRuleTypeEnum#ROLE} 时：角色编号
     * 2. {@link BpmTaskAssignRuleTypeEnum#DEPT_MEMBER} 时：部门编号
     * 3. {@link BpmTaskAssignRuleTypeEnum#DEPT_LEADER} 时：部门编号
     * 4. {@link BpmTaskAssignRuleTypeEnum#USER} 时：用户编号
     * 5. {@link BpmTaskAssignRuleTypeEnum#USER_GROUP} 时：用户组编号
     * 6. {@link BpmTaskAssignRuleTypeEnum#SCRIPT} 时：脚本编号，目前通过 {@link BpmTaskRuleScriptEnum#getId()} 标识
     */
    @TableField(typeHandler = JsonLongSetTypeHandler.class)
    private Set<Long> options;

}
