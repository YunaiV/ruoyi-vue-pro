package cn.iocoder.yudao.module.bpm.service.task.trigger.form;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTriggerTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.SimpleModelUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.module.bpm.service.task.trigger.BpmTrigger;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * BPM 删除流程表单数据触发器
 *
 * @author jason
 */
@Component
@Slf4j
public class BpmFormDeleteTrigger implements BpmTrigger {

    @Resource
    private BpmProcessInstanceService processInstanceService;

    @Override
    public BpmTriggerTypeEnum getType() {
        return BpmTriggerTypeEnum.FORM_DELETE;
    }

    @Override
    public void execute(String processInstanceId, String param) {
        // 1. 解析删除流程表单数据配置
        List<BpmSimpleModelNodeVO.TriggerSetting.FormTriggerSetting> settings = JsonUtils.parseObject(param, new TypeReference<>() {});
        if (CollUtil.isEmpty(settings)) {
            log.error("[execute][流程({}) 删除流程表单数据触发器配置为空]", processInstanceId);
            return;
        }

        // 2. 获取流程变量
        Map<String, Object> processVariables = processInstanceService.getProcessInstance(processInstanceId).getProcessVariables();

        // 3.1 获取需要删除的表单字段
        Set<String> deleteFields = new HashSet<>();
        settings.forEach(setting -> {
            if (CollUtil.isEmpty(setting.getDeleteFields())) {
                return;
            }
            // 配置了条件，判断条件是否满足
            boolean isFieldDeletedNeeded = true;
            if (setting.getConditionType() != null) {
                String conditionExpression = SimpleModelUtils.buildConditionExpression(
                        setting.getConditionType(), setting.getConditionExpression(), setting.getConditionGroups());
                isFieldDeletedNeeded = BpmnModelUtils.evalConditionExpress(processVariables, conditionExpression);
            }
            if (isFieldDeletedNeeded) {
                deleteFields.addAll(setting.getDeleteFields());
            }
        });

        // 3.2 删除流程变量
        if (CollUtil.isNotEmpty(deleteFields)) {
            processInstanceService.removeProcessInstanceVariables(processInstanceId, deleteFields);
        }
    }
}
