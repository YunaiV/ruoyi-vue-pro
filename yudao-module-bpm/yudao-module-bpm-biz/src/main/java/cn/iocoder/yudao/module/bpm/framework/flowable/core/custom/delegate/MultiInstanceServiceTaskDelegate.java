package cn.iocoder.yudao.module.bpm.framework.flowable.core.custom.delegate;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.BooleanUtil;
import cn.iocoder.yudao.module.bpm.enums.task.BpmCommentTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import jakarta.annotation.Resource;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * 处理会签 Service Task 代理
 *
 * @author jason
 */
@Component
public class MultiInstanceServiceTaskDelegate implements JavaDelegate {

    @Resource
    private BpmProcessInstanceService processInstanceService;

    @Override
    public void execute(DelegateExecution execution) {
        String attachUserTaskId = BpmnModelUtils.parseExtensionElement(execution.getCurrentFlowElement(),
                BpmnModelConstants.SERVICE_TASK_ATTACH_USER_TASK_ID);
        Assert.notNull(attachUserTaskId, "附属的用户任务 Id 不能为空");
        // 获取会签任务是否被拒绝
        Boolean userTaskRejected = execution.getVariable(String.format("%s_reject", attachUserTaskId), Boolean.class);
        // 如果会签任务被拒绝, 终止流程
        // TODO @jason：【重要】需要测试下，如果基于 createChangeActivityStateBuilder()、changeState 到结束节点，实现审批不通过；
        // 注意：需要考虑 bpmn 的高亮问题；（不过这个，未来可能会废弃掉！）
        if (BooleanUtil.isTrue(userTaskRejected)) {
            processInstanceService.updateProcessInstanceReject(execution.getProcessInstanceId(),
                    BpmCommentTypeEnum.REJECT.formatComment("会签任务拒绝人数满足条件"));
        }
    }

}
