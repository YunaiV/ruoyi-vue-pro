package cn.iocoder.yudao.module.bpm.framework.flowable.core.custom.expression;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bpm.enums.definition.BpmApproveMethodEnum.APPROVE_BY_RATIO;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.USER_TASK_APPROVE_METHOD;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.USER_TASK_APPROVE_RATIO;

// TODO @jason：微信已经讨论，简化哈
/**
 * 按拒绝人数计算会签的完成条件的流程表达式实现
 *
 * @author jason
 */
@Component
@Slf4j
public class CompleteByRejectCountExpression {

    /**
     * 会签的完成条件
     */
    public boolean completionCondition(DelegateExecution execution) {
        FlowElement flowElement = execution.getCurrentFlowElement();
        // 实例总数
        Integer nrOfInstances = (Integer) execution.getVariable("nrOfInstances");
        // 完成的实例数
        Integer nrOfCompletedInstances = (Integer) execution.getVariable("nrOfCompletedInstances");
        // 审批方式
        Integer approveMethod = NumberUtils.parseInt(BpmnModelUtils.parseExtensionElement(flowElement, USER_TASK_APPROVE_METHOD));
        Assert.notNull(approveMethod, "审批方式不能空");
        if (!Objects.equals(APPROVE_BY_RATIO.getMethod(), approveMethod)) {
            log.error("[completionCondition] the execution is [{}] 审批方式[{}] 不匹配", execution, approveMethod);
            throw exception(GlobalErrorCodeConstants.ERROR_CONFIGURATION);
        }
        // 获取拒绝人数
        // TODO @jason：CollUtil.filter().size();貌似可以更简洁 @芋艿 CollUtil.filter().size() 使用这个会报错，好坑了.
        Integer rejectCount = CollectionUtils.getSumValue(execution.getExecutions(),
                item -> Objects.equals(BpmTaskStatusEnum.REJECT.getStatus(), item.getVariableLocal(BpmConstants.TASK_VARIABLE_STATUS, Integer.class)) ? 1 : 0,
                Integer::sum, 0);
        // 同意人数： 完成人数 - 拒绝人数
        int agreeCount = nrOfCompletedInstances - rejectCount;
        // 多人会签(按通过比例)
        Integer approveRatio = NumberUtils.parseInt(BpmnModelUtils.parseExtensionElement(flowElement, USER_TASK_APPROVE_RATIO));
        Assert.notNull(approveRatio, "通过比例不能空");
        // 判断通过比例
        double approvePct = approveRatio / (double) 100;
        double realApprovePct = (double) agreeCount / nrOfInstances;
        if (realApprovePct >= approvePct) {
            return true;
        }
        double rejectPct = (100 - approveRatio) / (double) 100;
        double realRejectPct = (double) rejectCount / nrOfInstances;
        // 判断拒绝比例
        if (realRejectPct > rejectPct) {
            execution.setVariable(String.format("%s_reject", flowElement.getId()), Boolean.TRUE);
            return true;
        }
        return false;
    }

}
