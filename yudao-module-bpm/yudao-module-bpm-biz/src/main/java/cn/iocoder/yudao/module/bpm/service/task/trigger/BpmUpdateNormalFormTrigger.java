package cn.iocoder.yudao.module.bpm.service.task.trigger;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO.TriggerSetting.UpdateNormalFormTriggerSetting;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTriggerTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.SimpleModelUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Component;

import java.util.Map;


/**
 * BPM 更新流程表单触发器
 *
 * @author jason
 */
@Component
@Slf4j
public class BpmUpdateNormalFormTrigger implements BpmTrigger {

    @Resource
    private BpmProcessInstanceService processInstanceService;

    @Override
    public BpmTriggerTypeEnum getType() {
        return BpmTriggerTypeEnum.UPDATE_NORMAL_FORM;
    }

    @Override
    public void execute(String processInstanceId, String param) {
        // 1. 解析更新流程表单配置
        UpdateNormalFormTriggerSetting setting = JsonUtils.parseObject(param, UpdateNormalFormTriggerSetting.class);
        if (setting == null) {
            log.error("[execute][流程({}) 更新流程表单触发器配置为空]", processInstanceId);
            return;
        }
        // 2.获取流程变量
        ProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
        Map<String, Object> processVariables = processInstance.getProcessVariables();
        String expression = SimpleModelUtils.buildConditionExpression(setting.getConditionType(), setting.getConditionExpression(),
                setting.getConditionGroups());

        // 3.满足条件，更新流程表单
        if(BpmnModelUtils.evalConditionExpress(processVariables, expression)) {
            processInstanceService.updateProcessInstanceVariables(processInstanceId, setting.getUpdateFormFields());
        }
    }
}
