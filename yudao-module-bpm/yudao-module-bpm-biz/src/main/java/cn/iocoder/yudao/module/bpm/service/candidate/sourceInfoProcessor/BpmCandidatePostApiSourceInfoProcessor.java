package cn.iocoder.yudao.module.bpm.service.candidate.sourceInfoProcessor;

import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.candidate.vo.BpmTaskCandidateRuleVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTaskAssignRuleTypeEnum;
import cn.iocoder.yudao.module.bpm.service.candidate.BpmCandidateSourceInfo;
import cn.iocoder.yudao.module.bpm.service.candidate.BpmCandidateSourceInfoProcessor;
import cn.iocoder.yudao.module.system.api.dept.PostApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.flowable.engine.delegate.DelegateExecution;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

public class BpmCandidatePostApiSourceInfoProcessor implements BpmCandidateSourceInfoProcessor {
    @Resource
    private PostApi api;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public Set<Integer> getSupportedTypes() {
        return SetUtils.asSet(BpmTaskAssignRuleTypeEnum.POST.getType());
    }

    @Override
    public void validRuleOptions(Integer type, Set<Long> options) {
        api.validPostList(options);
    }

    @Override
    public Set<Long> doProcess(BpmCandidateSourceInfo request, BpmTaskCandidateRuleVO rule, DelegateExecution delegateExecution) {
        List<AdminUserRespDTO> users = adminUserApi.getUserListByPostIds(rule.getOptions());
        return convertSet(users, AdminUserRespDTO::getId);
    }
}