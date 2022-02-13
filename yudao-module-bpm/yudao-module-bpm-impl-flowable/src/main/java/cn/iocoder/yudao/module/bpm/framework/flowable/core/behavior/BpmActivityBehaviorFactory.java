package cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior;

import cn.iocoder.yudao.module.bpm.service.definition.BpmTaskAssignRuleService;
import cn.iocoder.yudao.module.bpm.service.definition.BpmUserGroupService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.flowable.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;

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

    @Override
    public UserTaskActivityBehavior createUserTaskActivityBehavior(UserTask userTask) {
        BpmUserTaskActivityBehavior userTaskActivityBehavior = new BpmUserTaskActivityBehavior(userTask);
        userTaskActivityBehavior.setBpmTaskRuleService(bpmTaskRuleService);
        userTaskActivityBehavior.setPermissionApi(permissionApi);
        userTaskActivityBehavior.setDeptApi(deptApi);
        userTaskActivityBehavior.setUserGroupService(userGroupService);
        userTaskActivityBehavior.setAdminUserApi(adminUserApi);
        //TODO
        //userTaskActivityBehavior.setScripts(scripts);
        return userTaskActivityBehavior;
    }
}
