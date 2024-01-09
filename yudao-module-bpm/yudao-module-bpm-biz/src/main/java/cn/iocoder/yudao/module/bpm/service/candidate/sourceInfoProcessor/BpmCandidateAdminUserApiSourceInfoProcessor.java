package cn.iocoder.yudao.module.bpm.service.candidate.sourceInfoProcessor;

import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.candidate.vo.BpmTaskCandidateRuleVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTaskAssignRuleTypeEnum;
import cn.iocoder.yudao.module.bpm.service.candidate.BpmCandidateSourceInfo;
import cn.iocoder.yudao.module.bpm.service.candidate.BpmCandidateSourceInfoProcessor;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.flowable.engine.delegate.DelegateExecution;

import javax.annotation.Resource;
import java.util.Set;

public class BpmCandidateAdminUserApiSourceInfoProcessor implements BpmCandidateSourceInfoProcessor {
    @Resource
    private AdminUserApi api;

    @Override
    public Set<Integer> getSupportedTypes() {
        return SetUtils.asSet(BpmTaskAssignRuleTypeEnum.USER.getType());
    }

    @Override
    public void validRuleOptions(Integer type, Set<Long> options) {
        api.validateUserList(options);
    }

    @Override
    public Set<Long> doProcess(BpmCandidateSourceInfo request, BpmTaskCandidateRuleVO rule, DelegateExecution delegateExecution) {
        return rule.getOptions();
    }
}