package cn.iocoder.yudao.module.bpm.service.candidate.sourceInfoProcessor;

import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.candidate.vo.BpmTaskCandidateRuleVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTaskAssignRuleTypeEnum;
import cn.iocoder.yudao.module.bpm.service.candidate.BpmCandidateSourceInfo;
import cn.iocoder.yudao.module.bpm.service.candidate.BpmCandidateSourceInfoProcessor;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.flowable.engine.delegate.DelegateExecution;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

public class BpmCandidateDeptApiSourceInfoProcessor implements BpmCandidateSourceInfoProcessor {
    @Resource
    private DeptApi api;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public Set<Integer> getSupportedTypes() {
        return SetUtils.asSet(BpmTaskAssignRuleTypeEnum.DEPT_MEMBER.getType(),
                BpmTaskAssignRuleTypeEnum.DEPT_LEADER.getType());
    }

    @Override
    public void validRuleOptions(Integer type, Set<Long> options) {
        api.validateDeptList(options);
    }

    @Override
    public Set<Long> doProcess(BpmCandidateSourceInfo request, BpmTaskCandidateRuleVO rule, DelegateExecution delegateExecution) {
        if (Objects.equals(BpmTaskAssignRuleTypeEnum.DEPT_MEMBER.getType(), rule.getType())) {
            List<AdminUserRespDTO> users = adminUserApi.getUserListByDeptIds(rule.getOptions());
            return convertSet(users, AdminUserRespDTO::getId);
        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.DEPT_LEADER.getType(), rule.getType())) {
            List<DeptRespDTO> depts = api.getDeptList(rule.getOptions());
            return convertSet(depts, DeptRespDTO::getLeaderUserId);
        }
        return Collections.emptySet();
    }
}