package cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskApproveTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskAssignStartUserHandlerTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.runtime.ProcessInstance;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.MODEL_DEPLOY_FAIL_TASK_CANDIDATE_NOT_CONFIG;

/**
 * {@link BpmTaskCandidateStrategy} 的调用者，用于调用对应的策略，实现任务的候选人的计算
 *
 * @author 芋道源码
 */
@Slf4j
public class BpmTaskCandidateInvoker {

    private final Map<BpmTaskCandidateStrategyEnum, BpmTaskCandidateStrategy> strategyMap = new HashMap<>();

    private final AdminUserApi adminUserApi;

    public BpmTaskCandidateInvoker(List<BpmTaskCandidateStrategy> strategyList,
                                   AdminUserApi adminUserApi) {
        strategyList.forEach(strategy -> {
            BpmTaskCandidateStrategy oldStrategy = strategyMap.put(strategy.getStrategy(), strategy);
            Assert.isNull(oldStrategy, "策略(%s) 重复", strategy.getStrategy());
        });
        this.adminUserApi = adminUserApi;
    }

    /**
     * 校验流程模型的任务分配规则全部都配置了
     * 目的：如果有规则未配置，会导致流程任务找不到负责人，进而流程无法进行下去！
     *
     * @param bpmnBytes BPMN XML
     */
    public void validateBpmnConfig(byte[] bpmnBytes) {
        BpmnModel bpmnModel = BpmnModelUtils.getBpmnModel(bpmnBytes);
        assert bpmnModel != null;
        List<UserTask> userTaskList = BpmnModelUtils.getBpmnModelElements(bpmnModel, UserTask.class);
        // 遍历所有的 UserTask，校验审批人配置
        userTaskList.forEach(userTask -> {
            // 1.1 非人工审批，无需校验审批人配置
            Integer approveType = BpmnModelUtils.parseApproveType(userTask);
            if (ObjectUtils.equalsAny(approveType,
                    BpmUserTaskApproveTypeEnum.AUTO_APPROVE.getType(),
                    BpmUserTaskApproveTypeEnum.AUTO_REJECT.getType())) {
                return;
            }
            // 1.2 非空校验
            Integer strategy = BpmnModelUtils.parseCandidateStrategy(userTask);
            String param = BpmnModelUtils.parseCandidateParam(userTask);
            if (strategy == null) {
                throw exception(MODEL_DEPLOY_FAIL_TASK_CANDIDATE_NOT_CONFIG, userTask.getName());
            }
            BpmTaskCandidateStrategy candidateStrategy = getCandidateStrategy(strategy);
            if (candidateStrategy.isParamRequired() && StrUtil.isBlank(param)) {
                throw exception(MODEL_DEPLOY_FAIL_TASK_CANDIDATE_NOT_CONFIG, userTask.getName());
            }
            // 2. 具体策略校验
            getCandidateStrategy(strategy).validateParam(param);
        });
    }

    /**
     * 计算任务的候选人
     *
     * @param execution 执行任务
     * @return 用户编号集合
     */
    @DataPermission(enable = false) // 忽略数据权限，避免因为过滤，导致找不到候选人
    public Set<Long> calculateUsersByTask(DelegateExecution execution) {
        // 审批类型非人工审核时，不进行计算候选人。原因是：后续会自动通过、不通过
        FlowElement flowElement = execution.getCurrentFlowElement();
        Integer approveType = BpmnModelUtils.parseApproveType(flowElement);
        if (ObjectUtils.equalsAny(approveType,
                BpmUserTaskApproveTypeEnum.AUTO_APPROVE.getType(),
                BpmUserTaskApproveTypeEnum.AUTO_REJECT.getType())) {
            return new HashSet<>();
        }

        // 1.1 计算任务的候选人
        Integer strategy = BpmnModelUtils.parseCandidateStrategy(flowElement);
        String param = BpmnModelUtils.parseCandidateParam(flowElement);
        Set<Long> userIds = getCandidateStrategy(strategy).calculateUsersByTask(execution, param);
        // 1.2 移除被禁用的用户
        removeDisableUsers(userIds);

        // 2. 候选人为空时，根据“审批人为空”的配置补充
        if (CollUtil.isEmpty(userIds)) {
            userIds = getCandidateStrategy(BpmTaskCandidateStrategyEnum.ASSIGN_EMPTY.getStrategy())
                    .calculateUsersByTask(execution, param);
            // ASSIGN_EMPTY 策略，不需要移除被禁用的用户。原因是，再移除，可能会出现更没审批人了！！！
        }

        // 3. 移除发起人的用户
        ProcessInstance processInstance = SpringUtil.getBean(BpmProcessInstanceService.class)
                .getProcessInstance(execution.getProcessInstanceId());
        Assert.notNull(processInstance, "流程实例({}) 不存在", execution.getProcessInstanceId());
        removeStartUserIfSkip(userIds, flowElement, Long.valueOf(processInstance.getStartUserId()));
        return userIds;
    }

    public Set<Long> calculateUsersByActivity(BpmnModel bpmnModel, String activityId,
                                              Long startUserId, String processDefinitionId, Map<String, Object> processVariables) {
        // 审批类型非人工审核时，不进行计算候选人。原因是：后续会自动通过、不通过
        FlowElement flowElement = BpmnModelUtils.getFlowElementById(bpmnModel, activityId);
        Integer approveType = BpmnModelUtils.parseApproveType(flowElement);
        if (ObjectUtils.equalsAny(approveType,
                BpmUserTaskApproveTypeEnum.AUTO_APPROVE.getType(),
                BpmUserTaskApproveTypeEnum.AUTO_REJECT.getType())) {
            return new HashSet<>();
        }

        // 1.1 计算任务的候选人
        Integer strategy = BpmnModelUtils.parseCandidateStrategy(flowElement);
        String param = BpmnModelUtils.parseCandidateParam(flowElement);
        Set<Long> userIds = getCandidateStrategy(strategy).calculateUsersByActivity(bpmnModel, activityId, param,
                startUserId, processDefinitionId, processVariables);
        // 1.2 移除被禁用的用户
        removeDisableUsers(userIds);

        // 2. 候选人为空时，根据“审批人为空”的配置补充
        if (CollUtil.isEmpty(userIds)) {
            userIds = getCandidateStrategy(BpmTaskCandidateStrategyEnum.ASSIGN_EMPTY.getStrategy())
                    .calculateUsersByActivity(bpmnModel, activityId, param, startUserId, processDefinitionId, processVariables);
            // ASSIGN_EMPTY 策略，不需要移除被禁用的用户。原因是，再移除，可能会出现更没审批人了！！！
        }

        // 3. 移除发起人的用户
        removeStartUserIfSkip(userIds, flowElement, startUserId);
        return userIds;
    }

    @VisibleForTesting
    void removeDisableUsers(Set<Long> assigneeUserIds) {
        if (CollUtil.isEmpty(assigneeUserIds)) {
            return;
        }
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(assigneeUserIds);
        assigneeUserIds.removeIf(id -> {
            AdminUserRespDTO user = userMap.get(id);
            return user == null || CommonStatusEnum.isDisable(user.getStatus());
        });
    }

    /**
     * 如果“审批人与发起人相同时”，配置了 SKIP 跳过，则移除发起人
     *
     * 注意：如果只有一个候选人，则不处理，避免无法审批
     *
     * @param assigneeUserIds 当前分配的候选人
     * @param flowElement 当前节点
     * @param startUserId 发起人
     */
    @VisibleForTesting
    void removeStartUserIfSkip(Set<Long> assigneeUserIds, FlowElement flowElement, Long startUserId) {
        if (CollUtil.size(assigneeUserIds) <= 1) {
            return;
        }
        Integer assignStartUserHandlerType = BpmnModelUtils.parseAssignStartUserHandlerType(flowElement);
        if (ObjectUtil.notEqual(assignStartUserHandlerType, BpmUserTaskAssignStartUserHandlerTypeEnum.SKIP.getType())) {
            return;
        }
        assigneeUserIds.remove(startUserId);
    }

    private BpmTaskCandidateStrategy getCandidateStrategy(Integer strategy) {
        BpmTaskCandidateStrategyEnum strategyEnum = BpmTaskCandidateStrategyEnum.valueOf(strategy);
        Assert.notNull(strategyEnum, "策略(%s) 不存在", strategy);
        BpmTaskCandidateStrategy strategyObj = strategyMap.get(strategyEnum);
        Assert.notNull(strategyObj, "策略(%s) 不存在", strategy);
        return strategyObj;
    }

}