package cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.strategy;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateStrategy;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 连续多级部门的负责人 {@link BpmTaskCandidateStrategy} 实现类
 *
 * @author jason
 */
@Component
public class BpmTaskCandidateDeptLeaderMultiStrategy extends BpmTaskCandidateAbstractDeptLeaderStrategy {

    public BpmTaskCandidateDeptLeaderMultiStrategy(DeptApi deptApi) {
        super(deptApi);
    }

    @Override
    public BpmTaskCandidateStrategyEnum getStrategy() {
        return BpmTaskCandidateStrategyEnum.MULTI_DEPT_LEADER_MULTI;
    }

    @Override
    public void validateParam(String param) {
        // 参数格式: | 分隔 。左边为部门（多个部门用 , 分隔）。 右边为部门层级
        String[] params = param.split("\\|");
        Assert.isTrue(params.length == 2, "参数格式不匹配");
        deptApi.validateDeptList(StrUtils.splitToLong(params[0], ","));
        Assert.isTrue(Integer.parseInt(params[1]) > 0, "部门层级必须大于 0");
    }

    @Override
    public Set<Long> calculateUsers(DelegateExecution execution, String param) {
        // 参数格式: | 分隔 。左边为部门（多个部门用 , 分隔）。 右边为部门层级
        // 参数格式: ,分割。前面的部门Id. 可以为多个。 最后一个为部门层级
        String[] params = param.split("\\|");
        return getMultiLevelDeptLeaderIds(StrUtils.splitToLong(params[0], ","), Integer.valueOf(params[1]));
    }

}
