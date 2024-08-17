package cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskApproveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRejectReqVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskApproveTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskAssignEmptyHandlerTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmReasonEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateInvoker;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.impl.el.ExpressionManager;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.util.TaskHelper;
import org.flowable.task.service.TaskService;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Set;

/**
 * 自定义的【单个】流程任务的 assignee 负责人的分配
 * 第一步，基于分配规则，计算出分配任务的【单个】候选人。如果找不到，则直接报业务异常，不继续执行后续的流程；
 * 第二步，随机选择一个候选人，则选择作为 assignee 负责人。
 *
 * @author 芋道源码
 */
@Slf4j
public class BpmUserTaskActivityBehavior extends UserTaskActivityBehavior {

    @Setter
    private BpmTaskCandidateInvoker taskCandidateInvoker;

    public BpmUserTaskActivityBehavior(UserTask userTask) {
        super(userTask);
    }

    @Override
    protected void handleAssignments(TaskService taskService, String assignee, String owner,
        List<String> candidateUsers, List<String> candidateGroups, TaskEntity task, ExpressionManager expressionManager,
        DelegateExecution execution, ProcessEngineConfigurationImpl processEngineConfiguration) {
        // 第一步，获得任务的候选用户
        Long assigneeUserId = calculateTaskCandidateUsers(execution);
        // 第二步，设置作为负责人
        if (assigneeUserId != null) {
            TaskHelper.changeTaskAssignee(task, String.valueOf(assigneeUserId));
            return;
        }

        // 特殊：处理需要自动通过、不通过的情况
        Integer approveType = BpmnModelUtils.parseApproveType(userTask);
        Integer assignEmptyHandlerType = BpmnModelUtils.parseAssignEmptyHandlerType(userTask);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                // 特殊情况一：【人工审核】审批人为空，根据配置是否要自动通过、自动拒绝
                if (ObjectUtil.equal(approveType, BpmUserTaskApproveTypeEnum.USER.getType())) {
                    if (ObjectUtil.equal(assignEmptyHandlerType, BpmUserTaskAssignEmptyHandlerTypeEnum.APPROVE.getType())) {
                        SpringUtil.getBean(BpmTaskService.class).approveTask(null, new BpmTaskApproveReqVO()
                                .setId(task.getId()).setReason(BpmReasonEnum.ASSIGN_EMPTY_APPROVE.getReason()));
                    } else if (ObjectUtil.equal(assignEmptyHandlerType, BpmUserTaskAssignEmptyHandlerTypeEnum.REJECT.getType())) {
                        SpringUtil.getBean(BpmTaskService.class).rejectTask(null, new BpmTaskRejectReqVO()
                                .setId(task.getId()).setReason(BpmReasonEnum.ASSIGN_EMPTY_REJECT.getReason()));
                    }
                // 特殊情况二：【自动审核】审批类型为自动通过、不通过
                } else {
                    if (ObjectUtil.equal(approveType, BpmUserTaskApproveTypeEnum.AUTO_APPROVE.getType())) {
                        SpringUtil.getBean(BpmTaskService.class).approveTask(null, new BpmTaskApproveReqVO()
                                .setId(task.getId()).setReason(BpmReasonEnum.APPROVE_TYPE_AUTO_APPROVE.getReason()));
                    } else if (ObjectUtil.equal(approveType, BpmUserTaskApproveTypeEnum.AUTO_REJECT.getType())) {
                        SpringUtil.getBean(BpmTaskService.class).rejectTask(null, new BpmTaskRejectReqVO()
                                .setId(task.getId()).setReason(BpmReasonEnum.APPROVE_TYPE_AUTO_REJECT.getReason()));
                    }
                }

            }

        });
    }

    private Long calculateTaskCandidateUsers(DelegateExecution execution) {
        // 情况一，如果是多实例的任务，例如说会签、或签等情况，则从 Variable 中获取。
        // 顺序审批可见 BpmSequentialMultiInstanceBehavior，并发审批可见 BpmSequentialMultiInstanceBehavior
        if (super.multiInstanceActivityBehavior != null) {
            return execution.getVariable(super.multiInstanceActivityBehavior.getCollectionElementVariable(), Long.class);
        }

        // 情况二，如果非多实例的任务，则计算任务处理人
        // 第一步，先计算可处理该任务的处理人们
        Set<Long> candidateUserIds = taskCandidateInvoker.calculateUsers(execution);
        if (CollUtil.isEmpty(candidateUserIds)) {
            return null;
        }
        // 第二步，后随机选择一个任务的处理人
        // 疑问：为什么一定要选择一个任务处理人？
        // 解答：项目对 bpm 的任务是责任到人，所以每个任务有且仅有一个处理人。
        //      如果希望一个任务可以同时被多个人处理，可以考虑使用 BpmParallelMultiInstanceBehavior 实现的会签 or 或签。
        int index = RandomUtil.randomInt(candidateUserIds.size());
        return CollUtil.get(candidateUserIds, index);
    }

}
