package cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.definition;

import cn.iocoder.yudao.adminserver.modules.bpm.enums.definition.BpmTaskRuleScriptEnum;
import cn.iocoder.yudao.adminserver.modules.bpm.enums.definition.BpmTaskRuleTypeEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;

import java.util.List;

/**
 * Bpm 任务规则表
 *
 * @author 芋道源码
 */
@TableName(value = "bpm_task_rule", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmTaskRuleDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 流程模型编号
     *
     * 关联 {@link Model#getId()}
     */
    private String modelId;
    /**
     * 流程定义编号
     *
     * 关联 {@link ProcessDefinition#getId()}
     */
    private String processDefinitionId;
    /**
     * 流程任务的定义 Key
     *
     * 关联 {@link Task#getTaskDefinitionKey()}
     */
    private String taskDefinitionKey;

    /**
     * 规则类型
     *
     * 枚举 {@link BpmTaskRuleTypeEnum}
     */
    private Integer type;
    /**
     * 规则值数组，一般关联指定表的编号
     * 根据 type 不同，对应的值是不同的：
     *
     * 1. {@link BpmTaskRuleTypeEnum#ROLE} 时：角色编号
     * 2. {@link BpmTaskRuleTypeEnum#DEPT} 时：部门编号
     * 3. {@link BpmTaskRuleTypeEnum#DEPT_LEADER} 时：部门编号
     * 4. {@link BpmTaskRuleTypeEnum#USER} 时：用户编号
     * 5. {@link BpmTaskRuleTypeEnum#USER_GROUP} 时：用户组编号
     * 6. {@link BpmTaskRuleTypeEnum#SCRIPT} 时：脚本编号，目前通过 {@link BpmTaskRuleScriptEnum#getId()} 标识
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> values;

}
