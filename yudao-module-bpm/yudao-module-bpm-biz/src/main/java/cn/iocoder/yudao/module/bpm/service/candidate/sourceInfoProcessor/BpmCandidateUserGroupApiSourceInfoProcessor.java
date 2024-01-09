package cn.iocoder.yudao.module.bpm.service.candidate.sourceInfoProcessor;

import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.candidate.vo.BpmTaskCandidateVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmUserGroupDO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTaskAssignRuleTypeEnum;
import cn.iocoder.yudao.module.bpm.service.candidate.BpmCandidateSourceInfo;
import cn.iocoder.yudao.module.bpm.service.candidate.BpmCandidateSourceInfoProcessor;
import cn.iocoder.yudao.module.bpm.service.definition.BpmUserGroupService;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BpmCandidateUserGroupApiSourceInfoProcessor implements BpmCandidateSourceInfoProcessor {
    @Resource
    private BpmUserGroupService api;
    @Resource
    private BpmUserGroupService userGroupService;

    @Override
    public Set<Integer> getSupportedTypes() {
        return SetUtils.asSet(BpmTaskAssignRuleTypeEnum.USER_GROUP.getType());
    }

    @Override
    public void validRuleOptions(Integer type, Set<Long> options) {
        api.validUserGroups(options);
    }

    @Override
    public Set<Long> doProcess(BpmCandidateSourceInfo request, BpmTaskCandidateVO rule) {
        List<BpmUserGroupDO> userGroups = userGroupService.getUserGroupList(rule.getOptions());
        Set<Long> userIds = new HashSet<>();
        userGroups.forEach(group -> userIds.addAll(group.getMemberUserIds()));
        return userIds;
    }

}