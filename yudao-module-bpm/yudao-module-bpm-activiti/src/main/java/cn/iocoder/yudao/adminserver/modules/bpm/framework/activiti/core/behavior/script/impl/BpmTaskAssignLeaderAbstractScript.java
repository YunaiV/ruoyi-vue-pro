package cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti.core.behavior.script.impl;

import cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti.core.behavior.script.BpmTaskAssignScript;

import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.dept.SysDeptDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.coreservice.modules.system.service.dept.SysDeptCoreService;
import cn.iocoder.yudao.coreservice.modules.system.service.user.SysUserCoreService;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
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
    private SysUserCoreService userCoreService;
    @Resource
    private SysDeptCoreService deptCoreService;

    protected Set<Long> calculateTaskCandidateUsers(TaskEntity task, int level) {
        Assert.isTrue(level > 0, "level 必须大于 0");
        // 获得发起人
        Long startUserId = Long.parseLong(task.getProcessInstance().getStartUserId());
        // 获得对应 leve 的部门
        SysDeptDO dept = null;
        for (int i = 0; i < level; i++) {
            // 获得 level 对应的部门
            if (dept == null) {
                dept = getStartUserDept(startUserId);
                if (dept == null) { // 找不到发起人的部门，所以无法使用该规则
                    return emptySet();
                }
            } else {
                SysDeptDO parentDept = deptCoreService.getDept(dept.getParentId());
                if (parentDept == null) { // 找不到父级部门，所以只好结束寻找。原因是：例如说，级别比较高的人，所在部门层级比较少
                    break;
                }
                dept = parentDept;
            }
        }
        return dept.getLeaderUserId() != null ? asSet(dept.getLeaderUserId()) : emptySet();
    }

    private SysDeptDO getStartUserDept(Long startUserId) {
        SysUserDO startUser = userCoreService.getUser(startUserId);
        if (startUser.getDeptId() == null) { // 找不到部门，所以无法使用该规则
            return null;
        }
        return deptCoreService.getDept(startUser.getDeptId());
    }

}
