package cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.strategy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateStrategy;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 部门的负责人 {@link BpmTaskCandidateStrategy} 抽象类
 *
 * @author jason
 */
public abstract class BpmTaskCandidateAbstractDeptLeaderStrategy implements BpmTaskCandidateStrategy {

    protected DeptApi deptApi;

    public BpmTaskCandidateAbstractDeptLeaderStrategy(DeptApi deptApi) {
        this.deptApi = deptApi;
    }

    /**
     * 获取上级部门的负责人
     *
     * @param assignDept 指定部门
     * @param level      第几级
     * @return 部门负责人 Id
     */
    protected Long getAssignLevelDeptLeaderId(DeptRespDTO assignDept, Integer level) {
        Assert.isTrue(level > 0, "level 必须大于 0");
        if (assignDept == null) {
            return null;
        }
        DeptRespDTO dept = assignDept;
        for (int i = 1; i < level; i++) {
            DeptRespDTO parentDept = deptApi.getDept(dept.getParentId());
            if (parentDept == null) { // 找不到父级部门，到了最高级。返回最高级的部门负责人
                break;
            }
            dept = parentDept;
        }
        return dept.getLeaderUserId();
    }

    /**
     * 获取连续上级部门的负责人, 包含指定部门的负责人
     *
     * @param assignDeptIds 指定部门 Ids
     * @param level         第几级
     * @return 连续部门负责人 Id
     */
    protected Set<Long> getMultiLevelDeptLeaderIds(List<Long> assignDeptIds, Integer level) {
        Assert.isTrue(level > 0, "level 必须大于 0");
        if (CollUtil.isEmpty(assignDeptIds)) {
            return Collections.emptySet();
        }
        Set<Long> deptLeaderIds = new LinkedHashSet<>(); // 保证有序
        DeptRespDTO dept;
        for (Long deptId : assignDeptIds) {
            dept = deptApi.getDept(deptId);
            for (int i = 0; i < level; i++) {
                if (dept.getLeaderUserId() != null) {
                    deptLeaderIds.add(dept.getLeaderUserId());
                }
                DeptRespDTO parentDept = deptApi.getDept(dept.getParentId());
                if (parentDept == null) { // 找不到父级部门. 已经到了最高层级了
                    break;
                }
                dept = parentDept;
            }
        }
        return deptLeaderIds;
    }

}
