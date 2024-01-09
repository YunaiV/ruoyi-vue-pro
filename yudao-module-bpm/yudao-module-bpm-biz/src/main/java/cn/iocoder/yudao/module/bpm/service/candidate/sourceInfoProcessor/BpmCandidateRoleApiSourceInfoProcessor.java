package cn.iocoder.yudao.module.bpm.service.candidate.sourceInfoProcessor;

import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.candidate.vo.BpmTaskCandidateVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTaskAssignRuleTypeEnum;
import cn.iocoder.yudao.module.bpm.service.candidate.BpmCandidateSourceInfo;
import cn.iocoder.yudao.module.bpm.service.candidate.BpmCandidateSourceInfoProcessor;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.permission.RoleApi;

import javax.annotation.Resource;
import java.util.Set;

public class BpmCandidateRoleApiSourceInfoProcessor implements BpmCandidateSourceInfoProcessor {
    @Resource
    private RoleApi api;

    @Resource
    private PermissionApi permissionApi;

    @Override
    public Set<Integer> getSupportedTypes() {
        return SetUtils.asSet(BpmTaskAssignRuleTypeEnum.ROLE.getType());
    }

    @Override
    public void validRuleOptions(Integer type, Set<Long> options) {
        api.validRoleList(options);
    }

    @Override
    public Set<Long> doProcess(BpmCandidateSourceInfo request, BpmTaskCandidateVO rule) {
        return permissionApi.getUserRoleIdListByRoleIds(rule.getOptions());
    }

}