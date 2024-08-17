package cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.strategy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateStrategy;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 连续多级部门的负责人 {@link BpmTaskCandidateStrategy} 实现类
 *
 * @author jason
 */
@Component
public class BpmTaskCandidateMultiLevelDeptLeaderStrategy extends BpmTaskCandidateAbstractDeptLeaderStrategy {

    public BpmTaskCandidateMultiLevelDeptLeaderStrategy(DeptApi deptApi) {
        super(deptApi);
    }

    @Override
    public BpmTaskCandidateStrategyEnum getStrategy() {
        return BpmTaskCandidateStrategyEnum.MULTI_LEVEL_DEPT_LEADER;
    }

    @Override
    public void validateParam(String param) {
        // 参数格式: ,分割。 前面一个指定指定部门Id, 后面一个是部门的层级
        List<Long> params = StrUtils.splitToLong(param, ",");
        Assert.isTrue(params.size() == 2, "参数格式不匹配");
        deptApi.validateDeptList(CollUtil.toList(params.get(0)));
        Assert.isTrue(params.get(1) > 0, "部门层级必须大于 0");
    }

    @Override
    public Set<Long> calculateUsers(DelegateExecution execution, String param) {
        // 参数格式: ,分割。 前面一个指定指定部门Id, 后面一个是审批的层级
        List<Long> params = StrUtils.splitToLong(param, ",");
        // TODO @芋艿 是否要支持多个部门。 是不是这种场景，一个部门就可以了
        Long deptId = params.get(0);
        Long level = params.get(1);
        DeptRespDTO dept = deptApi.getDept(deptId);
        return getMultiLevelDeptLeaderIds(dept, level.intValue());
    }

}
