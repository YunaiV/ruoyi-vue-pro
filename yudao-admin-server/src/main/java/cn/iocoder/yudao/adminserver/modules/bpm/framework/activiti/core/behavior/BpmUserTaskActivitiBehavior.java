package cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti.core.behavior;

import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;

import java.util.List;

/**
 * 自定义的流程任务的 assignee 负责人的分配
 *
 * @author 芋道源码
 */
public class BpmUserTaskActivitiBehavior extends UserTaskActivityBehavior {

    public BpmUserTaskActivitiBehavior(UserTask userTask) {
        super(userTask);
    }

    @Override
    protected void handleAssignments(TaskEntityManager taskEntityManager,
                                     String assignee, String owner, List<String> candidateUsers, List<String> candidateGroups,
                                     TaskEntity task, ExpressionManager expressionManager, DelegateExecution execution) {
        System.out.println("");
        taskEntityManager.changeTaskAssignee(task, "1");
    }

}
