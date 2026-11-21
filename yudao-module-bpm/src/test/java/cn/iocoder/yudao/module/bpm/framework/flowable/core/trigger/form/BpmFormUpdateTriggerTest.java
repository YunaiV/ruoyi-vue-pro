package cn.iocoder.yudao.module.bpm.framework.flowable.core.trigger.form;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO.TriggerSetting.FormTriggerSetting;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModeConditionTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceQueryService;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceUpdateService;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link BpmFormUpdateTrigger} 的单元测试
 *
 * @author 芋道源码
 */
public class BpmFormUpdateTriggerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BpmFormUpdateTrigger formUpdateTrigger;

    @Mock
    private BpmProcessInstanceQueryService processInstanceQueryService;
    @Mock
    private BpmProcessInstanceUpdateService processInstanceUpdateService;

    @Test
    public void testExecute_conditionFilter() {
        // mock 方法
        Map<String, Object> processVariables = Map.of("day", 3);
        ProcessInstance processInstance = mock(ProcessInstance.class);
        when(processInstance.getProcessVariables()).thenReturn(processVariables);
        when(processInstanceQueryService.getProcessInstance("process-instance-1")).thenReturn(processInstance);
        // 准备参数：无条件、条件满足、条件不满足、未配置更新字段
        List<FormTriggerSetting> settings = List.of(
                buildUpdateSetting(null, Map.of("a", 1)),
                buildUpdateSetting("${match}", Map.of("b", 2)),
                buildUpdateSetting("${notMatch}", Map.of("c", 3)),
                buildUpdateSetting("${match}", null));

        try (MockedStatic<BpmnModelUtils> bpmnModelUtilsMockedStatic = mockStatic(BpmnModelUtils.class)) {
            bpmnModelUtilsMockedStatic.when(() -> BpmnModelUtils.evalConditionExpress(processVariables, "${match}"))
                    .thenReturn(true);
            bpmnModelUtilsMockedStatic.when(() -> BpmnModelUtils.evalConditionExpress(processVariables, "${notMatch}"))
                    .thenReturn(false);

            // 调用
            formUpdateTrigger.execute("process-instance-1", JsonUtils.toJsonString(settings));
        }
        // 断言：只更新无条件、条件满足的表单字段
        verify(processInstanceUpdateService).updateProcessInstanceVariables("process-instance-1", Map.of("a", 1));
        verify(processInstanceUpdateService).updateProcessInstanceVariables("process-instance-1", Map.of("b", 2));
        verify(processInstanceUpdateService, times(2)).updateProcessInstanceVariables(anyString(), anyMap());
    }

    @Test
    public void testExecute_settingsEmpty() {
        // 调用
        formUpdateTrigger.execute("process-instance-1", "[]");
        // 断言：配置为空时，不查询、不更新流程变量
        verifyNoInteractions(processInstanceQueryService, processInstanceUpdateService);
    }

    private static FormTriggerSetting buildUpdateSetting(String conditionExpression, Map<String, Object> updateFormFields) {
        FormTriggerSetting setting = new FormTriggerSetting();
        if (conditionExpression != null) {
            setting.setConditionType(BpmSimpleModeConditionTypeEnum.EXPRESSION.getType());
            setting.setConditionExpression(conditionExpression);
        }
        setting.setUpdateFormFields(updateFormFields);
        return setting;
    }

}
