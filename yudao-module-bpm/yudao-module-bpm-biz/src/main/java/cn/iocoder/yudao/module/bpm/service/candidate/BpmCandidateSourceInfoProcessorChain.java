package cn.iocoder.yudao.module.bpm.service.candidate;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.bpm.controller.admin.candidate.vo.BpmTaskCandidateVO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BpmCandidateSourceInfoProcessorChain {

    // 保存处理节点

    private List<BpmCandidateSourceInfoProcessor> processorList;
    @Resource
    private AdminUserApi adminUserApi;

    /**
     * 可添加其他处理器
     *
     * @param processorOp
     * @return
     */
    @Resource
    // 动态扩展处理节点
    public BpmCandidateSourceInfoProcessorChain addProcessor(ObjectProvider<BpmCandidateSourceInfoProcessor> processorOp) {
        List<BpmCandidateSourceInfoProcessor> processor = processorOp.orderedStream().collect(Collectors.toList());
        if (null == processorList) {
            processorList = new ArrayList<>(processor.size());
        }
        processorList.addAll(processor);
        return this;
    }

    // 获取处理器处理
    public Set<Long> process(BpmCandidateSourceInfo sourceInfo) throws Exception {
        // Verify our parameters
        if (sourceInfo == null) {
            throw new IllegalArgumentException();
        }
        for (BpmCandidateSourceInfoProcessor processor : processorList) {
            try {
                for (BpmTaskCandidateVO vo : sourceInfo.getRules()) {
                    processor.validRuleOptions(vo.getType(), vo.getOptions());
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
        Set<Long> saveResult = Collections.emptySet();
        Exception saveException = null;
        for (BpmCandidateSourceInfoProcessor processor : processorList) {
            try {
                saveResult = processor.process(sourceInfo, this);
                if (CollUtil.isNotEmpty(saveResult)) {
                    removeDisableUsers(saveResult);
                    break;
                }
            } catch (Exception e) {
                saveException = e;
                break;
            }
        }
        // Return the exception or result state from the last execute()
        if ((saveException != null)) {
            throw saveException;
        } else {
            return (saveResult);
        }
    }

    public Set<Long> calculateTaskCandidateUsers(DelegateExecution execution, BpmCandidateSourceInfo sourceInfo) {
        sourceInfo.setExecution(execution);
        Set<Long> results = Collections.emptySet();
        try {
            results = process(sourceInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * 移除禁用用户
     * @param assigneeUserIds
     */
    public void removeDisableUsers(Set<Long> assigneeUserIds) {
        if (CollUtil.isEmpty(assigneeUserIds)) {
            return;
        }
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(assigneeUserIds);
        assigneeUserIds.removeIf(id -> {
            AdminUserRespDTO user = userMap.get(id);
            return user == null || !CommonStatusEnum.ENABLE.getStatus().equals(user.getStatus());
        });
    }
}