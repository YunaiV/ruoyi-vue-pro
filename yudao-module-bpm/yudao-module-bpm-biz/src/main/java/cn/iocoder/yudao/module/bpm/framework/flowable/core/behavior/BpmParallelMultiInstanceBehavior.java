package cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmTaskAssignRuleDO;
import cn.iocoder.yudao.module.bpm.service.definition.BpmTaskAssignRuleService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.Activity;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.flowable.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.flowable.engine.impl.util.CommandContextUtil;

import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.TASK_CREATE_FAIL_NO_CANDIDATE_USER;

/**
 * @author kemengkai
 * @create 2022-04-21 16:57
 */
@Slf4j
public class BpmParallelMultiInstanceBehavior extends ParallelMultiInstanceBehavior {

    /**
     * EL表达式集合模板
     */
    private final static String EXPRESSION_TEXT_TEMPLATE = "${coll_userList}";

    @Setter
    private BpmTaskAssignRuleService bpmTaskRuleService;

    public BpmParallelMultiInstanceBehavior(Activity activity,
                                            AbstractBpmnActivityBehavior innerActivityBehavior) {
        super(activity, innerActivityBehavior);
    }

    /**
     * 创建并行任务
     *
     * @param multiInstanceRootExecution 并行任务入参
     *
     * @return 返回结果
     */
    @Override
    protected int createInstances(DelegateExecution multiInstanceRootExecution) {
        // 查找任务信息
//        BpmTaskAssignRuleDO taskRule = getTaskRule(multiInstanceRootExecution);
        BpmTaskAssignRuleDO taskRule = null;
        // 获取任务用户
        Set<Long> assigneeUserIds = calculateTaskCandidateUsers(multiInstanceRootExecution, taskRule);
        // 设置任务集合变量
        String expressionText = String.format("%s_userList", taskRule.getTaskDefinitionKey());
        // 设置任务集合变量与任务关系
        multiInstanceRootExecution.setVariable(expressionText, assigneeUserIds);
        // 设置任务集合EL表达式
        this.collectionExpression = CommandContextUtil.getProcessEngineConfiguration().getExpressionManager()
            .createExpression(String.format("${%s}", expressionText));
        // 根据会签，或签类型，设置会签,或签条件
//        if (BpmTaskAssignRuleTypeEnum.USER_SIGN.getType().equals(taskRule.getType())) {
//            // 会签
//            this.completionCondition = "${ nrOfInstances == nrOfCompletedInstances }";
//        } else {
//            // 或签
//            this.completionCondition = "${ nrOfCompletedInstances == 1 }";
//        }
        // 设置取出集合变量
        this.collectionElementVariable = "user";
        return super.createInstances(multiInstanceRootExecution);
    }

    @Override
    protected Object resolveCollection(DelegateExecution execution) {
        Object collection = null;
        if (EXPRESSION_TEXT_TEMPLATE.equals(this.collectionExpression.getExpressionText())) {
            // 查找任务信息
//            BpmTaskAssignRuleDO taskRule = getTaskRule(execution);
            BpmTaskAssignRuleDO taskRule = null;
            // 设置任务集合变量
            String expressionText = String.format("%s_userList", execution.getCurrentActivityId());
            // 获取任务用户
            Set<Long> assigneeUserIds = calculateTaskCandidateUsers(execution, taskRule);
            // 设置任务集合变量与任务关系
            execution.setVariable(expressionText, assigneeUserIds);
            // 设置任务集合EL表达式
            this.collectionExpression = CommandContextUtil.getProcessEngineConfiguration().getExpressionManager()
                .createExpression(String.format("${%s}", expressionText));
        }
        if (this.collectionExpression != null) {
            collection = this.collectionExpression.getValue(execution);
        } else if (this.collectionVariable != null) {
            collection = execution.getVariable(this.collectionVariable);
        } else if (this.collectionString != null) {
            collection = this.collectionString;
        }

        return collection;
    }

    Set<Long> calculateTaskCandidateUsers(DelegateExecution task, BpmTaskAssignRuleDO rule) {
        Set<Long> assigneeUserIds = SetUtils.asSet(1L, 104L);

        // 移除被禁用的用户
        // 如果候选人为空，抛出异常 TODO 芋艿：没候选人的策略选择。1 - 挂起；2 - 直接结束；3 - 强制一个兜底人
        if (CollUtil.isEmpty(assigneeUserIds)) {
            log.error("[calculateTaskCandidateUsers][流程任务({}/{}/{}) 任务规则({}) 找不到候选人]", task.getId(),
                task.getProcessDefinitionId(), task.getCurrentActivityId(), toJsonString(rule));
            throw exception(TASK_CREATE_FAIL_NO_CANDIDATE_USER);
        }
        return assigneeUserIds;
    }

}
