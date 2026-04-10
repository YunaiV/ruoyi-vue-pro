package cn.iocoder.yudao.module.bpm.service.task.trigger.form;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO.TriggerSetting.FormTriggerSetting;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTriggerTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.SimpleModelUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.module.bpm.service.task.trigger.BpmTrigger;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * BPM 更新流程表单触发器
 *
 * @author jason
 */
@Component
@Slf4j
public class BpmFormUpdateTrigger implements BpmTrigger {

    @Resource
    private BpmProcessInstanceService processInstanceService;

    @Override
    public BpmTriggerTypeEnum getType() {
        return BpmTriggerTypeEnum.FORM_UPDATE;
    }

    @Override
    public void execute(String processInstanceId, String param) {
        // 1. 解析更新流程表单配置
        List<FormTriggerSetting> settings = JsonUtils.parseObject(param, new TypeReference<>() {});
        if (CollUtil.isEmpty(settings)) {
            log.error("[execute][流程({}) 更新流程表单触发器配置为空]", processInstanceId);
            return;
        }

        // 2. 获取流程变量
        Map<String, Object> processVariables = processInstanceService.getProcessInstance(processInstanceId).getProcessVariables();

        // 3. 更新流程变量
        for (FormTriggerSetting setting : settings) {
            if (CollUtil.isEmpty(setting.getUpdateFormFields())) {
                continue;
            }
            // 配置了条件，判断条件是否满足
            boolean isFormUpdateNeeded = true;
            if (setting.getConditionType() != null) {
                String conditionExpression = SimpleModelUtils.buildConditionExpression(
                        setting.getConditionType(), setting.getConditionExpression(), setting.getConditionGroups());
                isFormUpdateNeeded = BpmnModelUtils.evalConditionExpress(processVariables, conditionExpression);
            }
            // 更新流程表单
            if (isFormUpdateNeeded) {
                processInstanceService.updateProcessInstanceVariables(processInstanceId, setting.getUpdateFormFields());
            }
        }
    }
}
