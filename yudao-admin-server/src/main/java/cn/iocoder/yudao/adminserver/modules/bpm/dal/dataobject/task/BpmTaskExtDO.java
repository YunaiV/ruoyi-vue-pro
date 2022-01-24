package cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.task;

import cn.iocoder.yudao.adminserver.modules.bpm.enums.task.BpmProcessInstanceResultEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import java.util.Date;

/**
 * Bpm 流程任务的拓展表
 * 主要解决 Activiti {@link Task} 和 {@link HistoricTaskInstance} 不支持拓展字段，所以新建拓展表
 *
 * @author 芋道源码
 */
@TableName(value = "bpm_task_ext", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
public class BpmTaskExtDO extends BaseDO {

    /**
     * 任务的审批人
     *
     * 冗余 {@link Task#getAssignee()}
     */
    private Long assigneeUserId;
    /**
     * 任务的名字
     *
     * 冗余 {@link Task#getName()} 为了筛选
     */
    private String name;
    /**
     * 任务的编号
     *
     * 关联 {@link Task#getId()}
     */
    private String taskId;
//    /**
//     * 任务的标识
//     *
//     * 关联 {@link Task#getTaskDefinitionKey()}
//     */
//    private String definitionKey;
    /**
     * 任务的结果
     *
     * 枚举 {@link BpmProcessInstanceResultEnum}
     */
    private Integer result;
    /**
     * 审批建议
     */
    private String comment;
    /**
     * 任务的结束时间
     *
     * 冗余 {@link HistoricTaskInstance#getEndTime()}
     */
    private Date endTime;

    /**
     * 流程实例的编号
     *
     * 关联 {@link ProcessInstance#getId()}
     */
    private String processInstanceId;
    /**
     * 流程定义的编号
     *
     * 关联 {@link ProcessDefinition#getId()}
     */
    private String processDefinitionId;

}
