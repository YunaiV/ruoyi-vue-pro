package cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.script.impl;

import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.script.BpmTaskAssignScript;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static java.util.Collections.emptySet;

/**
 * 分配给发起人的 Leader 审批的 Script 实现类
 * 目前 Leader 的定义是，
 *
 * @author 芋道源码
 */
public abstract class BpmTaskAssignLeaderAbstractScript implements BpmTaskAssignScript {

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;
    @Resource
    @Lazy // 解决循环依赖
    private BpmProcessInstanceService bpmProcessInstanceService;

    protected Set<Long> calculateTaskCandidateUsers(DelegateExecution execution, int level) {
        Assert.isTrue(level > 0, "level 必须大于 0");
        // 获得发起人
        ProcessInstance processInstance = bpmProcessInstanceService.getProcessInstance(execution.getProcessInstanceId());
        Long startUserId = NumberUtils.parseLong(processInstance.getStartUserId());
        // 获得对应 leve 的部门
        DeptRespDTO dept = null;
        for (int i = 0; i < level; i++) {
            // 获得 level 对应的部门
            if (dept == null) {
                dept = getStartUserDept(startUserId);
                if (dept == null) { // 找不到发起人的部门，所以无法使用该规则
                    return emptySet();
                }
            } else {
                DeptRespDTO parentDept = deptApi.getDept(dept.getParentId());
                if (parentDept == null) { // 找不到父级部门，所以只好结束寻找。原因是：例如说，级别比较高的人，所在部门层级比较少
                    break;
                }
                dept = parentDept;
            }
        }
        return dept.getLeaderUserId() != null ? asSet(dept.getLeaderUserId()) : emptySet();
    }

    private DeptRespDTO getStartUserDept(Long startUserId) {
        AdminUserRespDTO startUser = adminUserApi.getUser(startUserId);
        if (startUser.getDeptId() == null) { // 找不到部门，所以无法使用该规则
            return null;
        }
        return deptApi.getDept(startUser.getDeptId());
    }

}
