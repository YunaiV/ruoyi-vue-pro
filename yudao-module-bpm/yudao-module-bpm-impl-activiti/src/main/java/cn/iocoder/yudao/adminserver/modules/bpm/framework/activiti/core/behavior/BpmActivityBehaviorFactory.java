package cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti.core.behavior;

import cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti.core.behavior.script.BpmTaskAssignScript;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmTaskAssignRuleService;
import cn.iocoder.yudao.module.bpm.service.definition.BpmUserGroupService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;

import java.util.List;

/**
 * 自定义的 ActivityBehaviorFactory 实现类，目的如下：
 * 1. 自定义 {@link #createUserTaskActivityBehavior(UserTask)}：实现自定义的流程任务的 assignee 负责人的分配
 *
 * @author 芋道源码
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmActivityBehaviorFactory extends DefaultActivityBehaviorFactory {

    @Setter
    private BpmTaskAssignRuleService bpmTaskRuleService;
    @Setter
    private BpmUserGroupService userGroupService;

    @Setter
    private PermissionApi permissionApi;
    @Setter
    private DeptApi deptApi;
    @Setter
    private AdminUserApi adminUserApi;

    @Setter
    private List<BpmTaskAssignScript> scripts;

    @Override
    public UserTaskActivityBehavior createUserTaskActivityBehavior(UserTask userTask) {
        BpmUserTaskActivitiBehavior userTaskActivityBehavior = new BpmUserTaskActivitiBehavior(userTask);
        userTaskActivityBehavior.setBpmTaskRuleService(bpmTaskRuleService);
        userTaskActivityBehavior.setPermissionApi(permissionApi);
        userTaskActivityBehavior.setDeptApi(deptApi);
        userTaskActivityBehavior.setUserGroupService(userGroupService);
        userTaskActivityBehavior.setAdminUserApi(adminUserApi);
        userTaskActivityBehavior.setScripts(scripts);
        return userTaskActivityBehavior;
    }

    // TODO 芋艿：并行任务 ParallelMultiInstanceBehavior

    // TODO 芋艿：并行任务 SequentialMultiInstanceBehavior

}
