package cn.iocoder.yudao.module.bpm.service.task.listener;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmChildProcessStartUserEmptyTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmChildProcessStartUserTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import jakarta.annotation.Resource;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.impl.el.FixedValue;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * BPM 子流程监听器：设置流程的发起人
 *
 * @author Lesan
 */
@Component
@Slf4j
public class BpmCallActivityListener implements ExecutionListener {

    public static final String DELEGATE_EXPRESSION = "${bpmCallActivityListener}";

    @Setter
    private FixedValue listenerConfig;

    @Resource
    private BpmProcessDefinitionService processDefinitionService;

    @Resource
    private BpmProcessInstanceService processInstanceService;

    @Override
    public void notify(DelegateExecution execution) {
        String expressionText = listenerConfig.getExpressionText();
        Assert.notNull(expressionText, "监听器扩展字段({})不能为空", expressionText);
        BpmSimpleModelNodeVO.ChildProcessSetting.StartUserSetting startUserSetting = JsonUtils.parseObject(
                expressionText, BpmSimpleModelNodeVO.ChildProcessSetting.StartUserSetting.class);
        ProcessInstance processInstance = processInstanceService.getProcessInstance(execution.getRootProcessInstanceId());

        // 1. 当发起人来源为主流程发起人时，并兜底 startUserSetting 为空时
        if (startUserSetting == null
                || startUserSetting.getType().equals(BpmChildProcessStartUserTypeEnum.MAIN_PROCESS_START_USER.getType())) {
            FlowableUtils.setAuthenticatedUserId(Long.parseLong(processInstance.getStartUserId()));
            return;
        }

        // 2. 当发起人来源为表单时
        if (startUserSetting.getType().equals(BpmChildProcessStartUserTypeEnum.FROM_FORM.getType())) {
            String formFieldValue = MapUtil.getStr(processInstance.getProcessVariables(), startUserSetting.getFormField());
            // 2.1 当表单值为空时
            if (StrUtil.isEmpty(formFieldValue)) {
                // 2.1.1 来自主流程发起人
                if (startUserSetting.getEmptyType().equals(BpmChildProcessStartUserEmptyTypeEnum.MAIN_PROCESS_START_USER.getType())) {
                    FlowableUtils.setAuthenticatedUserId(Long.parseLong(processInstance.getStartUserId()));
                    return;
                }
                // 2.1.2 来自子流程管理员
                if (startUserSetting.getEmptyType().equals(BpmChildProcessStartUserEmptyTypeEnum.CHILD_PROCESS_ADMIN.getType())) {
                    BpmProcessDefinitionInfoDO processDefinition = processDefinitionService.getProcessDefinitionInfo(execution.getProcessDefinitionId());
                    List<Long> managerUserIds = processDefinition.getManagerUserIds();
                    FlowableUtils.setAuthenticatedUserId(managerUserIds.get(0));
                    return;
                }
                // 2.1.3 来自主流程管理员
                if (startUserSetting.getEmptyType().equals(BpmChildProcessStartUserEmptyTypeEnum.MAIN_PROCESS_ADMIN.getType())) {
                    BpmProcessDefinitionInfoDO processDefinition = processDefinitionService.getProcessDefinitionInfo(processInstance.getProcessDefinitionId());
                    List<Long> managerUserIds = processDefinition.getManagerUserIds();
                    FlowableUtils.setAuthenticatedUserId(managerUserIds.get(0));
                    return;
                }
            }
            // 2.2 使用表单值，并兜底字符串转 Long 失败时使用主流程发起人
            try {
                FlowableUtils.setAuthenticatedUserId(Long.parseLong(formFieldValue));
            } catch (NumberFormatException ex) {
                try {
                    List<Long> formFieldValues = JsonUtils.parseArray(formFieldValue, Long.class);
                    FlowableUtils.setAuthenticatedUserId(formFieldValues.get(0));
                } catch (Exception e) {
                    log.error("[notify][监听器：{}，子流程监听器设置流程的发起人字符串转 Long 失败，字符串：{}]",
                            DELEGATE_EXPRESSION, formFieldValue);
                    FlowableUtils.setAuthenticatedUserId(Long.parseLong(processInstance.getStartUserId()));
                }
            }
        }
    }

}